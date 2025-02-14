/*
 * Licensed to the Nervousync Studio (NSYC) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nervousync.magi.dialects.impl.mongodb;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterType;
import jakarta.annotation.Nonnull;
import jakarta.persistence.LockModeType;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Decimal128;
import org.jetbrains.annotations.NotNull;
import org.nervousync.brain.commons.BrainCommons;
import org.nervousync.brain.configs.auth.Authentication;
import org.nervousync.brain.configs.auth.impl.TokenAuthentication;
import org.nervousync.brain.configs.auth.impl.TrustStoreAuthentication;
import org.nervousync.brain.configs.auth.impl.UserAuthentication;
import org.nervousync.brain.configs.auth.impl.X509Authentication;
import org.nervousync.brain.configs.schema.impl.DistributeSchemaConfig;
import org.nervousync.brain.configs.secure.TrustStore;
import org.nervousync.brain.configs.server.ServerInfo;
import org.nervousync.brain.configs.transactional.TransactionalConfig;
import org.nervousync.brain.defines.ColumnDefine;
import org.nervousync.brain.defines.IndexDefine;
import org.nervousync.brain.defines.TableDefine;
import org.nervousync.brain.dialects.distribute.DistributeClient;
import org.nervousync.brain.enumerations.ddl.DDLType;
import org.nervousync.brain.enumerations.ddl.DropOption;
import org.nervousync.brain.enumerations.query.ItemType;
import org.nervousync.brain.exceptions.data.RetrieveException;
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.brain.query.condition.Condition;
import org.nervousync.brain.query.condition.impl.ColumnCondition;
import org.nervousync.brain.query.condition.impl.GroupCondition;
import org.nervousync.brain.query.core.AbstractItem;
import org.nervousync.brain.query.data.RangesData;
import org.nervousync.brain.query.item.ColumnItem;
import org.nervousync.brain.query.join.JoinInfo;
import org.nervousync.brain.query.join.QueryJoin;
import org.nervousync.brain.query.param.AbstractParameter;
import org.nervousync.brain.query.param.impl.ArraysParameter;
import org.nervousync.brain.query.param.impl.ConstantParameter;
import org.nervousync.brain.query.param.impl.RangesParameter;
import org.nervousync.commons.Globals;
import org.nervousync.utils.CertificateUtils;
import org.nervousync.utils.StringUtils;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class MongoDBClient implements DistributeClient {

	private final String defaultName;
	private final MongoDBDialectImpl dialect;
	private final MongoClient mongoClient;
	private final ThreadLocal<ClientSession> threadLocal;

	MongoDBClient(@Nonnull final MongoDBDialectImpl dialect, @Nonnull final DistributeSchemaConfig schemaConfig)
			throws Exception {
		MongoClientSettings.Builder settingsBuilder =
				MongoClientSettings.builder().writeConcern(WriteConcern.ACKNOWLEDGED)
						.retryWrites(Boolean.FALSE);
		if (schemaConfig.isPooled()) {
			settingsBuilder.applyToConnectionPoolSettings(builder ->
					builder.maxSize(schemaConfig.getMaxConnections())
							.minSize(schemaConfig.getMinConnections())
							.maxWaitTime(schemaConfig.getConnectTimeout(), TimeUnit.SECONDS));
		}

		List<ServerInfo> serverList = schemaConfig.getServerList();
		serverList.sort(Comparator.comparingInt(ServerInfo::getServerLevel));
		List<ServerAddress> hostList = new ArrayList<>();
		serverList.forEach(serverinfo ->
				hostList.add(new ServerAddress(serverinfo.getServerAddress(), serverinfo.getServerPort())));
		if (serverList.size() == 1) {
			settingsBuilder.applyToClusterSettings(builder ->
					builder.hosts(hostList).mode(ClusterConnectionMode.SINGLE));
		} else if (serverList.size() > 1) {
			settingsBuilder.applyToClusterSettings(builder ->
							builder.hosts(hostList)
									.mode(ClusterConnectionMode.MULTIPLE)
									.requiredClusterType(ClusterType.REPLICA_SET)
									.requiredReplicaSetName(schemaConfig.getDatabaseName()))
					.readPreference(ReadPreference.secondaryPreferred());
		}

		Authentication authentication = schemaConfig.getAuthentication();
		boolean secureContext = Boolean.TRUE;
		if (authentication != null) {
			switch (authentication.getAuthType()) {
				case BASIC:
					UserAuthentication userAuthentication = (UserAuthentication) authentication;
					if (StringUtils.notBlank(userAuthentication.getUserName())) {
						String password = StringUtils.isEmpty(userAuthentication.getPassWord())
								? Globals.DEFAULT_VALUE_STRING
								: userAuthentication.getPassWord();
						settingsBuilder.credential(MongoCredential.createCredential(userAuthentication.getUserName(),
								schemaConfig.getDatabaseName(), password.toCharArray()));
					}
					break;
				case CERTIFICATE:
					X509Authentication x509Authentication = (X509Authentication) authentication;
					Optional.ofNullable(CertificateUtils.x509(StringUtils.base64Decode(x509Authentication.getCertData())))
							.ifPresent(x509Certificate ->
									settingsBuilder
											.credential(MongoCredential.createMongoX509Credential()
													.withMechanismProperty(MongoCredential.MONGODB_X509_MECHANISM,
															x509Certificate)));
					break;
				case TOKEN:
					TokenAuthentication tokenAuthentication = (TokenAuthentication) authentication;
					if (StringUtils.notBlank(tokenAuthentication.getKeyId())
							&& StringUtils.notBlank(tokenAuthentication.getSecretKey())
							&& StringUtils.notBlank(tokenAuthentication.getSessionToken())) {
						settingsBuilder.credential(
								MongoCredential.createAwsCredential(null, null)
										.withMechanismProperty(MongoCredential.AWS_CREDENTIAL_PROVIDER_KEY,
												new AwsCredential(tokenAuthentication.getKeyId(),
														tokenAuthentication.getSecretKey(),
														tokenAuthentication.getSessionToken())));
					}
					break;
				case TRUST_STORE:
					TrustStoreAuthentication trustStoreAuthentication = (TrustStoreAuthentication) authentication;
					Optional.ofNullable(this.sslContext(trustStoreAuthentication, schemaConfig.getTrustStore()))
							.ifPresent(sslContext ->
									settingsBuilder
											.applyToSslSettings(builder ->
													builder.enabled(Boolean.TRUE).context(sslContext))
											.credential(MongoCredential.createMongoX509Credential(
													trustStoreAuthentication.getCertificateName())));
					secureContext = Boolean.FALSE;
					break;
			}
		}
		if (schemaConfig.isUseSsl()) {
			settingsBuilder.applyToSslSettings(builder -> builder.enabled(Boolean.TRUE));
		}
		if (secureContext) {
			Optional.ofNullable(this.sslContext(schemaConfig.getTrustStore()))
					.ifPresent(sslContext ->
							settingsBuilder.applyToSslSettings(builder ->
									builder.invalidHostNameAllowed(Boolean.FALSE).context(sslContext)));
		}

		this.defaultName = schemaConfig.getDatabaseName();
		this.dialect = dialect;
		this.mongoClient = MongoClients.create(settingsBuilder.build());
		this.threadLocal = new ThreadLocal<>();
	}

	private SSLContext sslContext(final TrustStoreAuthentication trustStoreAuthentication,
	                              final TrustStore trustStore) throws Exception {
		if (trustStoreAuthentication == null || StringUtils.isEmpty(trustStoreAuthentication.getCertificateName())) {
			return null;
		}
		TrustManager[] trustManagers = null;
		if (trustStore != null) {
			trustManagers = this.trustManagers(trustStore.getTrustStorePath(), trustStore.getTrustStorePassword());
		}
		SSLContext sslContext = SSLContext.getDefault();
		sslContext.init(
				this.keyManagers(trustStoreAuthentication.getTrustStorePath(),
						trustStoreAuthentication.getTrustStorePassword()),
				trustManagers, new SecureRandom());
		return sslContext;
	}

	private KeyManager[] keyManagers(final String storePath, final String storePassword) throws Exception {
		String password = StringUtils.isEmpty(storePassword) ? Globals.DEFAULT_VALUE_STRING : storePassword;
		KeyStore keyStore = CertificateUtils.loadKeyStore(storePath, password);
		KeyManagerFactory keyManagerFactory =
				KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, password.toCharArray());
		return keyManagerFactory.getKeyManagers();
	}

	private TrustManager[] trustManagers(final String storePath, final String storePassword) throws Exception {
		String password = StringUtils.isEmpty(storePassword) ? Globals.DEFAULT_VALUE_STRING : storePassword;
		KeyStore keyStore = CertificateUtils.loadKeyStore(storePath, password);
		TrustManagerFactory trustManagerFactory =
				TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		return trustManagerFactory.getTrustManagers();
	}

	private SSLContext sslContext(final TrustStore trustStore) throws Exception {
		if (trustStore == null) {
			return null;
		}
		SSLContext sslContext = SSLContext.getDefault();
		sslContext.init(this.keyManagers(trustStore.getTrustStorePath(), trustStore.getTrustStorePassword()),
				this.trustManagers(trustStore.getTrustStorePath(), trustStore.getTrustStorePassword()),
				new SecureRandom());
		return sslContext;
	}

	@Override
	public void configRetry(final int retryCount, final long retryPeriod) {
	}

	@Override
	public void initSharding(final String shardingKey) {
		this.mongoClient.getDatabase(StringUtils.isEmpty(shardingKey) ? this.defaultName : shardingKey);
	}

	@Override
	public void beginTransactional(final TransactionalConfig transactionalConfig) {
		if (this.threadLocal.get() != null || transactionalConfig == null
				|| transactionalConfig.getIsolation() == Connection.TRANSACTION_NONE) {
			return;
		}

		TransactionOptions.Builder txOptionBuilder =
				TransactionOptions.builder().maxCommitTime((long) transactionalConfig.getTimeout(), TimeUnit.SECONDS)
						.readPreference(ReadPreference.primary()).writeConcern(WriteConcern.MAJORITY);
		switch (transactionalConfig.getIsolation()) {
			case Connection.TRANSACTION_READ_UNCOMMITTED:
				txOptionBuilder.readConcern(ReadConcern.LOCAL);
				break;
			case Connection.TRANSACTION_READ_COMMITTED:
				txOptionBuilder.readConcern(ReadConcern.MAJORITY);
				break;
			case Connection.TRANSACTION_REPEATABLE_READ:
				txOptionBuilder.readConcern(ReadConcern.SNAPSHOT);
				break;
			case Connection.TRANSACTION_SERIALIZABLE:
				txOptionBuilder.readConcern(ReadConcern.LINEARIZABLE);
				break;
		}

		ClientSession clientSession = this.mongoClient.startSession();
		clientSession.startTransaction(txOptionBuilder.build());
		this.threadLocal.set(clientSession);
	}

	@Override
	public void rollback() {
		Optional.ofNullable(this.threadLocal.get()).ifPresent(ClientSession::abortTransaction);
	}

	@Override
	public void commit() {
		Optional.ofNullable(this.threadLocal.get()).ifPresent(ClientSession::commitTransaction);
	}

	@Override
	public void clearTransactional() {
		this.threadLocal.remove();
	}

	@Override
	public void truncateTables() {
		this.dropTables(DropOption.NONE);
	}

	@Override
	public void truncateTable(@NotNull final TableDefine tableDefine) {
		this.dropTable(tableDefine, DropOption.NONE);
	}

	@Override
	public void dropTables(final DropOption dropOption) {
		for (String databaseName : this.mongoClient.listDatabaseNames()) {
			MongoDatabase mongoDatabase = this.mongoClient.getDatabase(databaseName);
			for (String collectionName : mongoDatabase.listCollectionNames()) {
				mongoDatabase.getCollection(collectionName).drop();
			}
		}
	}

	@Override
	public void dropTable(@NotNull final TableDefine tableDefine, @NotNull final DropOption dropOption) {
		for (String databaseName : this.mongoClient.listDatabaseNames()) {
			MongoDatabase mongoDatabase = this.mongoClient.getDatabase(databaseName);
			mongoDatabase.getCollection(this.dialect.nameCase(tableDefine.getTableName())).drop();
		}
	}

	@Override
	public boolean lockRecord(final String shardingDatabase, @NotNull final TableDefine tableDefine,
	                          @NotNull final Map<String, Object> filterMap) {
		//  Do nothing
		return Boolean.TRUE;
	}

	@Override
	public Map<String, Object> insert(final String shardingDatabase, @NotNull final TableDefine tableDefine,
	                                  @NotNull final Map<String, Object> dataMap) {
		if (StringUtils.isEmpty(shardingDatabase)) {
			return this.insert(this.defaultName, tableDefine, dataMap);
		}
		this.initTable(DDLType.CREATE, tableDefine, shardingDatabase);
		Document document = new Document(dataMap);
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(shardingDatabase);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(tableDefine.getTableName());
		ClientSession clientSession = this.threadLocal.get();
		if (clientSession == null) {
			mongoCollection.insertOne(document);
		} else {
			mongoCollection.insertOne(clientSession, document);
		}
		return Map.of();
	}

	@Override
	public Map<String, Object> retrieve(final String shardingDatabase, @NotNull final TableDefine tableDefine,
	                                    final String columns, @NotNull final Map<String, Object> filterMap,
	                                    final boolean forUpdate) throws Exception {
		if (StringUtils.isEmpty(shardingDatabase)) {
			return this.retrieve(this.defaultName, tableDefine, columns, filterMap, forUpdate);
		}
		Document findColumns = new Document();
		if ("*".equalsIgnoreCase(columns) || forUpdate) {
			tableDefine.getColumnDefines()
					.forEach(columnDefine ->
							findColumns.append(this.dialect.nameCase(columnDefine.getColumnName()), 1));
		} else {
			if (StringUtils.isEmpty(columns)) {
				tableDefine.getColumnDefines()
						.stream()
						.filter(columnDefine -> !columnDefine.isLazyLoad())
						.forEach(columnDefine ->
								findColumns.append(this.dialect.nameCase(columnDefine.getColumnName()), 1));
			} else {
				Arrays.asList(StringUtils.tokenizeToStringArray(columns, ","))
						.forEach(columnName -> findColumns.append(this.dialect.nameCase(columnName), 1));
			}
		}
		Document filterDocument = new Document(filterMap);
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(shardingDatabase);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(tableDefine.getTableName());
		if (mongoCollection.countDocuments(filterDocument) > 1L) {
			throw new RetrieveException(0x00DB00000028L);
		}
		ClientSession clientSession = this.threadLocal.get();
		Document document;
		if (clientSession == null) {
			document = mongoCollection.find(filterDocument).projection(findColumns).first();
		} else {
			document = mongoCollection.find(clientSession, filterDocument).projection(findColumns).first();
		}
		return Optional.ofNullable(document)
				.map(result -> this.convertResult(result, tableDefine))
				.orElse(Map.of());
	}

	@Override
	public int update(final String shardingDatabase, @NotNull final TableDefine tableDefine,
	                  @NotNull final Map<String, Object> dataMap, @NotNull final Map<String, Object> filterMap) {
		if (StringUtils.isEmpty(shardingDatabase)) {
			return this.update(this.defaultName, tableDefine, dataMap, filterMap);
		}
		Document updateDocument = new Document("$set", dataMap);
		Document filterDocument = new Document(filterMap);
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(shardingDatabase);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(tableDefine.getTableName());
		ClientSession clientSession = this.threadLocal.get();
		long modifiedCount;
		if (clientSession == null) {
			modifiedCount = mongoCollection.updateMany(filterDocument, updateDocument).getModifiedCount();
		} else {
			modifiedCount =
					mongoCollection.updateMany(clientSession, filterDocument, updateDocument).getModifiedCount();
		}
		return Long.valueOf(modifiedCount).intValue();
	}

	@Override
	public int delete(final String shardingDatabase, @NotNull final TableDefine tableDefine,
	                  @NotNull final Map<String, Object> filterMap) {
		if (StringUtils.isEmpty(shardingDatabase)) {
			return this.delete(this.defaultName, tableDefine, filterMap);
		}
		Document filterDocument = new Document(filterMap);
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(shardingDatabase);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(tableDefine.getTableName());
		ClientSession clientSession = this.threadLocal.get();
		long deletedCount;
		if (clientSession == null) {
			deletedCount = mongoCollection.deleteMany(filterDocument).getDeletedCount();
		} else {
			deletedCount = mongoCollection.deleteMany(clientSession, filterDocument).getDeletedCount();
		}
		return Long.valueOf(deletedCount).intValue();
	}

	@Override
	public List<Map<String, Object>> query(final String shardingDatabase, @NotNull final TableDefine tableDefine,
	                                       @NotNull final QueryInfo queryInfo) throws Exception {
		if (StringUtils.isEmpty(shardingDatabase)) {
			return this.query(this.defaultName, tableDefine, queryInfo);
		}
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(shardingDatabase);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(queryInfo.getTableName());
		ClientSession clientSession = this.threadLocal.get();
		List<Map<String, Object>> resultList = new ArrayList<>();
		if (queryInfo.getQueryJoins().isEmpty()) {
			Document queryDocument = this.conditionsToDocument(queryInfo.getConditionList(), Map.of());
			if (queryDocument == null) {
				throw new MultilingualSQLException(0x00DB000A0001L);
			}
			Document projectionDocument = new Document();
			if (queryInfo.getItemList().isEmpty()) {
				tableDefine.getColumnDefines()
						.stream()
						.filter(columnDefine -> !columnDefine.isLazyLoad())
						.forEach(columnDefine ->
								projectionDocument.append(this.dialect.nameCase(columnDefine.getColumnName()), 1));
			} else {
				queryInfo.getItemList()
						.stream()
						.filter(abstractItem -> ItemType.COLUMN.equals(abstractItem.getItemType()))
						.forEach(abstractItem -> {
							String columnName = this.dialect.nameCase(((ColumnItem) abstractItem).getColumnName());
							projectionDocument.put(columnName, 1);
						});
			}
			Document sortDocument = new Document();
			queryInfo.getOrderByList().forEach(orderBy -> {
				switch (orderBy.getOrderType()) {
					case ASC:
						sortDocument.put(this.dialect.nameCase(orderBy.getColumnName()), 1);
						break;
					case DESC:
						sortDocument.put(this.dialect.nameCase(orderBy.getColumnName()), -1);
						break;
				}
			});
			FindIterable<Document> findIterable;
			if (clientSession == null) {
				findIterable = mongoCollection.find(queryDocument).projection(projectionDocument).sort(sortDocument);
			} else {
				findIterable = mongoCollection.find(clientSession, queryDocument)
						.projection(projectionDocument).sort(sortDocument);
			}
			if (queryInfo.getPageLimit() > Globals.INITIALIZE_INT_VALUE) {
				int offset = Integer.max(Globals.INITIALIZE_INT_VALUE,
						(queryInfo.getPageNo() - 1) * queryInfo.getPageLimit());
				findIterable.skip(offset).limit(queryInfo.getPageLimit());
			}
			findIterable.forEach(document -> resultList.add(this.convertResult(document, tableDefine)));
		} else {
			List<Document> queryDocuments = this.parseQuery(tableDefine, queryInfo, Boolean.FALSE);
			if (queryDocuments.isEmpty()) {
				throw new MultilingualSQLException(0x00DB000A0001L);
			}
			if (clientSession == null) {
				mongoCollection.aggregate(queryDocuments)
						.forEach(document -> resultList.add(this.convertResult(document, tableDefine)));
			} else {
				mongoCollection.aggregate(clientSession, queryDocuments)
						.forEach(document -> resultList.add(this.convertResult(document, tableDefine)));
			}
		}
		return resultList;
	}

	private Map<String, Object> convertResult(final Document document, final TableDefine tableDefine) {
		Map<String, Object> resultMap = new HashMap<>();
		for (Map.Entry<String, Object> entry : document.entrySet()) {
			ColumnDefine columnDefine = tableDefine.column(entry.getKey());
			if (columnDefine == null) {
				continue;
			}

			Object value = entry.getValue();
			switch (columnDefine.getJdbcType()) {
				case Types.BLOB:
					value = ((Binary) value).getData();
					break;
				case Types.REAL:
					value = ((Double) value).floatValue();
					break;
				case Types.SMALLINT:
					value = ((Integer) value).shortValue();
					break;
				case Types.TINYINT:
					value = ((Integer) value).byteValue();
					break;
				case Types.DECIMAL:
					value = ((Decimal128) value).bigDecimalValue();
					break;
			}
			resultMap.put(entry.getKey(), value);
		}
		return resultMap;
	}

	@Override
	public List<Map<String, Object>> queryForUpdate(final String shardingDatabase,
	                                                @NotNull final TableDefine tableDefine,
	                                                final List<Condition> conditionList, final LockModeType lockOption)
			throws Exception {
		if (StringUtils.isEmpty(shardingDatabase)) {
			return this.queryForUpdate(this.defaultName, tableDefine, conditionList, lockOption);
		}
		Document queryDocument = this.conditionsToDocument(conditionList, Map.of());
		if (queryDocument == null) {
			throw new MultilingualSQLException(0x00DB000A0001L);
		}
		List<Map<String, Object>> resultList = new ArrayList<>();
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(shardingDatabase);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(tableDefine.getTableName());
		ClientSession clientSession = this.threadLocal.get();
		if (clientSession == null) {
			mongoCollection.find(queryDocument).forEach(resultList::add);
		} else {
			mongoCollection.find(clientSession, queryDocument).forEach(resultList::add);
		}
		return resultList;
	}

	@Override
	public Long queryTotal(final String shardingDatabase, @NotNull final TableDefine tableDefine,
	                       final QueryInfo queryInfo) throws Exception {
		if (StringUtils.isEmpty(shardingDatabase)) {
			return this.queryTotal(this.defaultName, tableDefine, queryInfo);
		}
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(shardingDatabase);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(queryInfo.getTableName());
		ClientSession clientSession = this.threadLocal.get();
		if (queryInfo.getQueryJoins().isEmpty()) {
			Document queryDocument = this.conditionsToDocument(queryInfo.getConditionList(), Map.of());
			if (queryDocument == null) {
				throw new MultilingualSQLException(0x00DB000A0001L);
			}
			if (clientSession == null) {
				return mongoCollection.countDocuments(queryDocument);
			} else {
				return mongoCollection.countDocuments(clientSession, queryDocument);
			}
		} else {
			List<Document> queryDocuments = this.parseQuery(tableDefine, queryInfo, Boolean.TRUE);
			if (queryDocuments.isEmpty()) {
				throw new MultilingualSQLException(0x00DB000A0001L);
			}
			AggregateIterable<Document> aggregateIterable;
			if (clientSession == null) {
				aggregateIterable = mongoCollection.aggregate(queryDocuments);
			} else {
				aggregateIterable = mongoCollection.aggregate(clientSession, queryDocuments);
			}
			try (MongoCursor<Document> cursor = aggregateIterable.iterator()) {
				if (cursor.hasNext()) {
					return cursor.next().getInteger("totalCount").longValue();
				}
			}
		}
		return (long) Globals.INITIALIZE_INT_VALUE;
	}

	private List<Document> parseQuery(final TableDefine tableDefine, @Nonnull final QueryInfo queryInfo,
	                                  final boolean totalCount)
			throws SQLException {
		if (queryInfo.getQueryJoins() == null || queryInfo.getQueryJoins().isEmpty()) {
			return Collections.emptyList();
		}
		Document itemDocument = new Document();
		if (!totalCount) {
			if (queryInfo.getItemList().isEmpty()) {
				tableDefine.getColumnDefines()
						.stream()
						.filter(columnDefine -> !columnDefine.isLazyLoad())
						.forEach(columnDefine ->
								itemDocument.put(this.dialect.nameCase(columnDefine.getColumnName()), 1));
			} else {
				for (AbstractItem abstractItem : queryInfo.getItemList()) {
					if (ItemType.COLUMN.equals(abstractItem.getItemType())) {
						itemDocument.put(this.dialect.nameCase(abstractItem.unwrap(ColumnItem.class).getColumnName()),
								1);
					}
				}
			}
		}
		List<Document> queryDocuments = new ArrayList<>();
		if (!itemDocument.isEmpty()) {
			queryDocuments.add(new Document("$project", itemDocument));
		}
		Map<String, String> aliasMap = new HashMap<>();
		queryInfo.getQueryJoins()
				.stream()
				.filter(queryJoin -> queryJoin.getDriverTable().equalsIgnoreCase(queryInfo.getTableName()))
				.forEach(queryJoin -> {
					if (!aliasMap.containsKey(queryJoin.getJoinTable())) {
						String aliasName = "T_" + aliasMap.size();
						if (StringUtils.notBlank(queryJoin.getAliasName())) {
							aliasName = queryJoin.getAliasName();
						}
						aliasMap.put(queryJoin.getJoinTable().toLowerCase(), aliasName);
					}
				});
		for (QueryJoin queryJoin : queryInfo.getQueryJoins()) {
			if (queryJoin.getDriverTable().equalsIgnoreCase(queryInfo.getTableName())) {
				Optional.ofNullable(this.joinDocument(queryJoin, aliasMap))
						.ifPresent(joinDocument -> queryDocuments.add(new Document("$lookup", joinDocument)));
			}
		}
		Optional.ofNullable(this.conditionsToDocument(queryInfo.getConditionList(), aliasMap))
				.ifPresent(conditionDocument -> queryDocuments.add(new Document("$match", conditionDocument)));
		if (totalCount) {
			queryDocuments.add(new Document("$count", "totalCount"));
		} else {
			if (queryInfo.getPageLimit() > Globals.INITIALIZE_INT_VALUE) {
				int skipCount = Globals.INITIALIZE_INT_VALUE;
				if (queryInfo.getPageNo() > 1) {
					skipCount = (queryInfo.getPageNo() - 1) * queryInfo.getPageLimit();
				}
				queryDocuments.add(new Document("$skip", skipCount));
				queryDocuments.add(new Document("$limit", queryInfo.getPageLimit()));
			}
		}
		return queryDocuments;
	}

	private Document joinDocument(@Nonnull final QueryJoin queryJoin, final Map<String, String> aliasMap) {
		List<JoinInfo> joinColumns = queryJoin.getJoinInfos();
		if (joinColumns.size() == 1) {
			JoinInfo joinInfo = joinColumns.get(0);
			if (StringUtils.isEmpty(joinInfo.getJoinKey()) || StringUtils.isEmpty(joinInfo.getReferenceKey())) {
				return null;
			}
			Document document = new Document();
			document.put("from", this.dialect.nameCase(queryJoin.getJoinTable()));
			document.put("localField", this.dialect.nameCase(joinInfo.getJoinKey()));
			document.put("foreignField", this.dialect.nameCase(joinInfo.getReferenceKey()));
			document.put("as", this.dialect.nameCase(aliasMap.get(queryJoin.getJoinTable().toLowerCase())));
			return document;
		} else {
			List<Document> columnsDocument = new ArrayList<>();
			for (JoinInfo joinInfo : joinColumns) {
				if (StringUtils.notBlank(joinInfo.getJoinKey()) && StringUtils.notBlank(joinInfo.getReferenceKey())) {
					String referenceKey = aliasMap.get(queryJoin.getJoinTable().toLowerCase());
					if (StringUtils.notBlank(referenceKey)) {
						referenceKey += BrainCommons.DEFAULT_NAME_SPLIT;
					}
					referenceKey += joinInfo.getReferenceKey();
					columnsDocument.add(new Document("$eq",
							new Document(this.dialect.nameCase(joinInfo.getJoinKey()),
									this.dialect.nameCase(referenceKey))));
				}
			}
			if (columnsDocument.isEmpty()) {
				return null;
			}
			Document document = new Document();
			document.put("from", this.dialect.nameCase(queryJoin.getJoinTable()));
			document.put("pipeline",
					new Document("$match", new Document("$expr", new Document("$and", columnsDocument))));
			if (StringUtils.notBlank(queryJoin.getAliasName())) {
				document.put("as", this.dialect.nameCase(queryJoin.getAliasName()));
			}
			return document;
		}
	}

	private Document conditionsToDocument(@Nonnull final List<Condition> conditionList,
	                                      final Map<String, String> aliasMap) throws SQLException {
		if (conditionList.isEmpty()) {
			return null;
		}
		List<Document> andDocuments = new ArrayList<>();
		List<Document> orDocuments = new ArrayList<>();
		for (Condition subCondition : conditionList) {
			Optional.ofNullable(this.conditionToDocument(subCondition, aliasMap))
					.ifPresent(subDocument -> {
						switch (subCondition.getConnectionCode()) {
							case AND:
								andDocuments.add(subDocument);
								break;
							case OR:
								orDocuments.add(subDocument);
								break;
						}
					});
		}
		if (!orDocuments.isEmpty()) {
			andDocuments.add(new Document("$or", orDocuments));
		}
		return new Document("$and", andDocuments);
	}

	private Object[] parseParameters(final AbstractParameter<?> abstractParameter) throws SQLException {
		if (abstractParameter == null) {
			return new Object[0];
		}
		switch (abstractParameter.getItemType()) {
			case ARRAY:
				return abstractParameter.unwrap(ArraysParameter.class).getItemValue().getArrayObject();
			case RANGE:
				RangesData rangesData = abstractParameter.unwrap(RangesParameter.class).getItemValue();
				return new Object[]{rangesData.getBeginValue(), rangesData.getEndValue()};
			case CONSTANT:
				return new Object[]{abstractParameter.unwrap(ConstantParameter.class).getItemValue()};
			default:
				throw new MultilingualSQLException(0x00DB000A0002L);
		}
	}

	private Document conditionToDocument(@Nonnull final Condition condition, final Map<String, String> aliasMap)
			throws SQLException {
		Document document = null;
		switch (condition.getConditionType()) {
			case COLUMN:
				ColumnCondition columnCondition = condition.unwrap(ColumnCondition.class);
				String matchName =
						aliasMap.getOrDefault(columnCondition.getTableName().toLowerCase(),
								Globals.DEFAULT_VALUE_STRING);
				if (StringUtils.notBlank(matchName)) {
					matchName += BrainCommons.DEFAULT_NAME_SPLIT;
				}
				matchName += columnCondition.getColumnName();
				Object[] matchValues = this.parseParameters(columnCondition.getConditionParameter());
				switch (columnCondition.getConditionCode()) {
					case IN:
						document = new Document(this.dialect.nameCase(matchName),
								new BasicDBObject("$in", matchValues));
						break;
					case NOT_IN:
						document = new Document(this.dialect.nameCase(matchName),
								new BasicDBObject("$nin", matchValues));
						break;
					case LESS:
						document = new Document(this.dialect.nameCase(matchName),
								new BasicDBObject("$lt", matchValues[0]));
						break;
					case LESS_EQUAL:
						document = new Document(this.dialect.nameCase(matchName),
								new BasicDBObject("$lte", matchValues[0]));
						break;
					case EQUAL:
						if (matchValues.length == 1) {
							document = new Document(this.dialect.nameCase(matchName),
									new BasicDBObject("$eq", matchValues[0]));
						} else {
							document = new Document(this.dialect.nameCase(matchName),
									new BasicDBObject("$in", matchValues));
						}
						break;
					case NOT_EQUAL:
						if (matchValues.length == 1) {
							document = new Document(this.dialect.nameCase(matchName),
									new BasicDBObject("$ne", matchValues[0]));
						} else {
							document = new Document(this.dialect.nameCase(matchName),
									new BasicDBObject("$nin", matchValues));
						}
						break;
					case GREATER:
						document = new Document(this.dialect.nameCase(matchName),
								new BasicDBObject("$gt", matchValues[0]));
						break;
					case GREATER_EQUAL:
						document = new Document(this.dialect.nameCase(matchName),
								new BasicDBObject("$gte", matchValues[0]));
						break;
					case BETWEEN_AND:
						document = new Document(this.dialect.nameCase(matchName),
								Arrays.asList(new BasicDBObject("$gte", matchValues[0]),
										new BasicDBObject("$lt", matchValues[1])));
						break;
					case NOT_BETWEEN_AND:
						document = new Document(this.dialect.nameCase(matchName),
								Arrays.asList(new BasicDBObject("$lt", matchValues[0]),
										new BasicDBObject("$gte", matchValues[1])));
						break;
					case LIKE:
						document = new Document(this.dialect.nameCase(matchName),
								new BasicDBObject("$regex", matchValues[0]));
						break;
					case NOT_LIKE:
						document = new Document(this.dialect.nameCase(matchName),
								new BasicDBObject("$not", new BasicDBObject("$regex", matchValues[0])));
						break;
					case IS_NULL:
						document = new Document(this.dialect.nameCase(matchName), null);
						break;
					case NOT_NULL:
						document = new Document(this.dialect.nameCase(matchName),
								new BasicDBObject("$exists", Boolean.TRUE));
						break;
				}
				break;
			case GROUP:
				document = this.conditionsToDocument(condition.unwrap(GroupCondition.class).getConditionList(), aliasMap);
				break;
		}
		return document;
	}

	@Override
	public void initTable(@NotNull final DDLType ddlType, @NotNull final TableDefine tableDefine,
	                      final String shardingDatabase) {
		if (StringUtils.isEmpty(shardingDatabase)) {
			this.initTable(ddlType, tableDefine, this.defaultName);
			return;
		}
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(shardingDatabase);
		if (mongoDatabase.listCollectionNames().into(new ArrayList<>()).contains(tableDefine.getTableName())) {
			return;
		}

		mongoDatabase.createCollection(tableDefine.getTableName());
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(tableDefine.getTableName());
		IndexOptions indexOptions = new IndexOptions();
		indexOptions.unique(Boolean.TRUE).background(Boolean.TRUE);
		BasicDBObject indexKeys = new BasicDBObject();
		tableDefine.getColumnDefines()
				.stream()
				.filter(ColumnDefine::isPrimaryKey)
				.forEach(columnDefine ->
						indexKeys.append(this.dialect.nameCase(columnDefine.getColumnName()), 1));
		mongoCollection.createIndex(indexKeys, indexOptions);

		for (IndexDefine indexDefine : tableDefine.getIndexDefines()) {
			IndexOptions searchOptions = new IndexOptions().name(indexDefine.getIndexName()).background(Boolean.TRUE);
			BasicDBObject searchKeys = new BasicDBObject();
			indexDefine.getColumnList().forEach(columnName ->
					searchKeys.append(this.dialect.nameCase(columnName), 1));
			mongoCollection.createIndex(searchKeys, searchOptions);
		}
	}

	@Override
	public void close() {
		this.mongoClient.close();
	}
}
