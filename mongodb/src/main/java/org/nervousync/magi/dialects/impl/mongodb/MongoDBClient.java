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
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Decimal128;
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
import org.nervousync.brain.dialects.Dialect;
import org.nervousync.brain.dialects.distribute.DistributeClient;
import org.nervousync.brain.enumerations.ddl.DDLType;
import org.nervousync.brain.enumerations.ddl.DropOption;
import org.nervousync.brain.enumerations.query.ItemType;
import org.nervousync.brain.exceptions.data.RetrieveException;
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.brain.manager.TableManager;
import org.nervousync.brain.query.PartialCollection;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.brain.query.condition.Condition;
import org.nervousync.brain.query.condition.impl.ColumnCondition;
import org.nervousync.brain.query.condition.impl.GroupCondition;
import org.nervousync.brain.query.core.QueryFrom;
import org.nervousync.brain.query.core.QueryItem;
import org.nervousync.brain.query.data.RangesData;
import org.nervousync.brain.query.from.FromTable;
import org.nervousync.brain.query.item.ColumnItem;
import org.nervousync.brain.query.join.JoinInfo;
import org.nervousync.brain.query.join.QueryJoin;
import org.nervousync.brain.query.join.TableQueryJoin;
import org.nervousync.brain.query.param.AbstractParameter;
import org.nervousync.brain.query.param.impl.ArraysParameter;
import org.nervousync.brain.query.param.impl.ConstantParameter;
import org.nervousync.brain.query.param.impl.RangesParameter;
import org.nervousync.commons.Globals;
import org.nervousync.magi.entity.EntityFactory;
import org.nervousync.utils.cert.CertificateUtils;
import org.nervousync.utils.core.ObjectUtils;
import org.nervousync.utils.core.StringUtils;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <h2 class="en-US">MongoDB database client implementation class</h2>
 * <h2 class="zh-CN">MongoDB数据库客户端实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 18, 2022 15:22:27 $
 */
public final class MongoDBClient implements DistributeClient {

	/**
	 * <span class="en-US">Default database name</span>
	 * <span class="zh-CN">默认数据库名</span>
	 */
	private final String databaseName;
	/**
	 * <span class="en-US">Database dialect instance object</span>
	 * <span class="zh-CN">数据库方言实例对象</span>
	 */
	private final Dialect dialect;
	/**
	 * <span class="en-US">MongoDB client instance object</span>
	 * <span class="zh-CN">MongoDB客户端实例对象</span>
	 */
	private final MongoClient mongoClient;
	/**
	 * <span class="en-US">Database connection used by the current thread</span>
	 * <span class="zh-CN">当前线程使用的数据库连接</span>
	 */
	private final ThreadLocal<ClientSession> threadLocal;

	/**
	 * <h3 class="en-US">Constructor method for MongoDB database client implementation class</h3>
	 * <h3 class="zh-CN">MongoDB数据库客户端实现类的构造方法</h3>
	 *
	 * @param dialect      <span class="en-US">Database dialect instance object</span>
	 *                     <span class="zh-CN">数据库方言实例对象</span>
	 * @param schemaConfig <span class="en-US">Data source configure information</span>
	 *                     <span class="zh-CN">数据源配置信息</span>
	 * @throws Exception <span class="en-US">An error occurs when configure SSL</span>
	 *                   <span class="zh-CN">设置SSL时出错</span>
	 */
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

		this.databaseName = schemaConfig.getDatabaseName();
		this.dialect = dialect;
		this.mongoClient = MongoClients.create(settingsBuilder.build());
		this.threadLocal = new ThreadLocal<>();
	}

	@Override
	public void configRetry(final int retryCount, final long retryPeriod) {
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
	public void truncateTable(@Nonnull final TableDefine tableDefine) {
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
	public void dropTable(@Nonnull final TableDefine tableDefine, @Nonnull final DropOption dropOption) {
		for (String databaseName : this.mongoClient.listDatabaseNames()) {
			MongoDatabase mongoDatabase = this.mongoClient.getDatabase(databaseName);
			mongoDatabase.getCollection(this.dialect.nameCase(tableDefine.getTableName())).drop();
		}
	}

	@Override
	public boolean lockRecord(@Nonnull final TableDefine tableDefine, @Nonnull final Map<String, Object> filterMap) {
		//  Do nothing
		return Boolean.TRUE;
	}

	@Override
	public Map<String, Object> insert(@Nonnull final TableDefine tableDefine,
	                                  @Nonnull final Map<String, Object> dataMap) {
		Document document = new Document(dataMap);
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(this.databaseName);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(tableDefine.getTableName());
		ClientSession clientSession = this.threadLocal.get();
		if (clientSession == null) {
			mongoCollection.insertOne(document);
		} else {
			mongoCollection.insertOne(clientSession, document);
		}
		return dataMap;
	}

	@Override
	public Map<String, Object> retrieve(@Nonnull final TableDefine tableDefine, final String columns,
	                                    @Nonnull final Map<String, Object> filterMap, final boolean forUpdate) {
		Document findColumns = new Document();
		findColumns.put("_id", 0);
		Map<String, Integer> jdbcTypeMap = new HashMap<>();
		if ("*".equalsIgnoreCase(columns) || forUpdate || StringUtils.isEmpty(columns)) {
			tableDefine.getColumnDefines()
					.forEach(columnDefine -> {
						findColumns.append(this.dialect.nameCase(columnDefine.getColumnName()), 1);
						jdbcTypeMap.put(columnDefine.getColumnName(), columnDefine.getJdbcType());
					});
		} else {
			Arrays.asList(StringUtils.tokenizeToStringArray(columns, ","))
					.forEach(columnName ->
							Optional.ofNullable(tableDefine.column(columnName))
									.ifPresent(columnDefine -> {
										findColumns.append(this.dialect.nameCase(columnDefine.getColumnName()), 1);
										jdbcTypeMap.put(columnDefine.getColumnName(), columnDefine.getJdbcType());
									}));
		}
		Document filterDocument = new Document(filterMap);
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(this.databaseName);
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
				.map(result -> this.convertResult(result, jdbcTypeMap))
				.orElse(Map.of());
	}

	@Override
	public int update(@Nonnull final TableDefine tableDefine,
	                  @Nonnull final Map<String, Object> dataMap, @Nonnull final Map<String, Object> filterMap) {
		Document updateDocument = new Document("$set", dataMap);
		Document filterDocument = new Document(filterMap);
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(this.databaseName);
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
	public int delete(@Nonnull final TableDefine tableDefine, @Nonnull final Map<String, Object> filterMap) {
		Document filterDocument = new Document(filterMap);
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(this.databaseName);
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
	public PartialCollection query(@Nonnull final QueryInfo queryInfo) throws Exception {
		String mainDocument = this.tableName(queryInfo.getQueryFrom());
		if (StringUtils.isEmpty(mainDocument)) {
			throw new MultilingualSQLException(0x00DB00010020L);
		}
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(this.databaseName);
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(mainDocument);
		ClientSession clientSession = this.threadLocal.get();
		Map<String, Integer> jdbcTypeMap = new HashMap<>();
		List<Map<String, Object>> resultList = new ArrayList<>();
		if (queryInfo.getQueryJoins().isEmpty()) {
			Document queryDocument = this.conditionsToDocument(queryInfo.getConditionList(), Map.of());
			if (queryDocument == null) {
				throw new MultilingualSQLException(0x00DB000A0001L);
			}
			Document projectionDocument = new Document();
			TableManager tableManager = TableManager.getInstance();
			for (QueryItem abstractItem : queryInfo.getItemList()) {
				if (ItemType.COLUMN.equals(abstractItem.getItemType())) {
					ColumnItem columnItem = (ColumnItem) abstractItem;
					String columnName = this.dialect.nameCase(columnItem.getColumnName());
					projectionDocument.put(columnName, 1);
					jdbcTypeMap.put(columnName, tableManager.jdbcType(columnItem.getTableName(), columnName));
				}
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
			findIterable.forEach(document -> resultList.add(this.convertResult(document, jdbcTypeMap)));
		} else {
			List<Document> queryDocuments = this.parseQuery(queryInfo, jdbcTypeMap, Boolean.FALSE);
			if (queryDocuments.isEmpty()) {
				throw new MultilingualSQLException(0x00DB000A0001L);
			}
			if (clientSession == null) {
				mongoCollection.aggregate(queryDocuments)
						.forEach(document -> resultList.add(this.convertResult(document, jdbcTypeMap)));
			} else {
				mongoCollection.aggregate(clientSession, queryDocuments)
						.forEach(document -> resultList.add(this.convertResult(document, jdbcTypeMap)));
			}
		}
		return new PartialCollection(resultList, this.queryTotal(queryInfo));
	}

	private Map<String, Object> convertResult(final Document document, @Nonnull final Map<String, Integer> jdbcTypeMap) {
		Map<String, Object> resultMap = new HashMap<>();
		for (Map.Entry<String, Object> entry : document.entrySet()) {
			Object value = entry.getValue();
			switch (jdbcTypeMap.get(entry.getKey())) {
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
				case Types.DATE:
					value = new java.sql.Date(((Date) value).getTime());
					break;
				case Types.TIME:
					value = new java.sql.Time(((Date) value).getTime());
					break;
				case Types.TIMESTAMP:
					value = new java.sql.Timestamp(((Date) value).getTime());
					break;
			}
			resultMap.put(entry.getKey(), value);
		}
		return resultMap;
	}

	@Override
	public Long queryTotal(final QueryInfo queryInfo) throws Exception {
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(this.databaseName);
		String mainDocument = this.tableName(queryInfo.getQueryFrom());
		if (StringUtils.isEmpty(mainDocument)) {
			throw new MultilingualSQLException(0x00DB00010020L);
		}
		MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(mainDocument);
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
			List<Document> queryDocuments = this.parseQuery(queryInfo, null, Boolean.TRUE);
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
					return cursor.next().getInteger("COUNT").longValue();
				}
			}
		}
		return (long) Globals.INITIALIZE_INT_VALUE;
	}

	private SSLContext sslContext(final TrustStoreAuthentication trustStoreAuthentication,
	                              final TrustStore trustStore) throws Exception {
		if (trustStoreAuthentication == null || StringUtils.isEmpty(trustStoreAuthentication.getCertificateName())) {
			return null;
		}
		TrustManager[] trustManagers = null;
		if (trustStore != null) {
			trustManagers = this.trustManagers(trustStore.getStorePath(), trustStore.getStorePassword());
		}
		SSLContext sslContext = SSLContext.getDefault();
		sslContext.init(
				this.keyManagers(trustStoreAuthentication.getStorePath(), trustStoreAuthentication.getStorePassword()),
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
		sslContext.init(this.keyManagers(trustStore.getStorePath(), trustStore.getStorePassword()),
				this.trustManagers(trustStore.getStorePath(), trustStore.getStorePassword()),
				new SecureRandom());
		return sslContext;
	}

	private void registerAliasName(@Nonnull final String tableName, final String aliasName,
	                               @Nonnull final Map<String, String> aliasMap)
			throws SQLException {
		if (EntityFactory.getInstance().registeredTable(tableName)) {
			aliasMap.put(tableName.toLowerCase(), StringUtils.isEmpty(aliasName) ? tableName.toLowerCase() : aliasName);
		} else {
			throw new MultilingualSQLException(0x00DB00010020L);
		}
	}

	private String tableName(@Nonnull final QueryFrom queryFrom) {
		if (queryFrom instanceof FromTable) {
			return ((FromTable) queryFrom).getTableName();
		}
		return Globals.DEFAULT_VALUE_STRING;
	}

	private String tableName(@Nonnull final QueryJoin queryJoin) {
		if (queryJoin instanceof TableQueryJoin) {
			return ((TableQueryJoin) queryJoin).getJoinTable();
		}
		return Globals.DEFAULT_VALUE_STRING;
	}

	private List<Document> parseQuery(@Nonnull final QueryInfo queryInfo, final Map<String, Integer> jdbcTypeMap,
	                                  final boolean totalCount) throws SQLException {
		if (queryInfo.getQueryJoins().isEmpty()) {
			return Collections.emptyList();
		}
		Map<String, String> aliasMap = new HashMap<>();
		String mainDocument = this.tableName(queryInfo.getQueryFrom());
		Set<String> drivenDocuments = new HashSet<>();
		for (QueryJoin queryJoin : queryInfo.getQueryJoins()) {
			if (queryJoin instanceof TableQueryJoin) {
				this.registerAliasName(this.tableName(queryJoin), queryJoin.getAliasName(), aliasMap);
				drivenDocuments.add(queryJoin.getDrivenIdentify().toLowerCase());
			}
		}
		List<Document> queryDocuments = new ArrayList<>();
		for (QueryJoin queryJoin : queryInfo.getQueryJoins()) {
			if (queryJoin instanceof TableQueryJoin) {
				Optional.ofNullable(this.joinDocument(mainDocument, (TableQueryJoin) queryJoin, aliasMap))
						.ifPresent(lookupDocument -> {
							queryDocuments.add(new Document("$lookup", lookupDocument));
							String aliasName = aliasMap.get(queryJoin.getDrivenIdentify().toLowerCase());
							if (drivenDocuments.contains(queryJoin.getDrivenIdentify())) {
								queryDocuments.add(new Document("$unwind", "$" + aliasName));
							}
						});
			}
		}
		Optional.ofNullable(this.conditionsToDocument(queryInfo.getConditionList(), aliasMap))
				.ifPresent(conditionDocument -> queryDocuments.add(new Document("$match", conditionDocument)));
		if (!queryInfo.getItemList().isEmpty()) {
			Document itemDocument = new Document();
			itemDocument.put("_id", 0);
			TableManager tableManager = TableManager.getInstance();
			for (QueryItem queryItem : queryInfo.getItemList()) {
				if (ItemType.COLUMN.equals(queryItem.getItemType())) {
					ColumnItem columnItem = queryItem.unwrap(ColumnItem.class);
					String columnLabel = aliasMap.getOrDefault(columnItem.getTableName().toLowerCase(), Globals.DEFAULT_VALUE_STRING);
					if (StringUtils.notBlank(columnLabel)) {
						columnLabel += BrainCommons.DEFAULT_NAME_SPLIT;
					}
					columnLabel += columnItem.getColumnName();
					String aliasName = columnItem.getAliasName();
					if (StringUtils.isEmpty(aliasName)) {
						aliasName = columnLabel;
					}
					itemDocument.put(aliasName, "$" + this.dialect.nameCase(columnLabel));
					if (jdbcTypeMap != null) {
						jdbcTypeMap.put(aliasName,
								tableManager.jdbcType(columnItem.getTableName(), columnItem.getColumnName()));
					}
				}
			}
			queryDocuments.add(new Document("$project", itemDocument));
		}
		if (totalCount) {
			queryDocuments.add(new Document("$count", "COUNT"));
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

	private Document joinDocument(final String mainDocument, final TableQueryJoin queryJoin,
	                              final Map<String, String> aliasMap) throws SQLException {
		List<JoinInfo> joinColumns = queryJoin.getJoinInfos();
		String aliasName = aliasMap.get(queryJoin.getJoinTable().toLowerCase());
		if (joinColumns.size() == 1) {
			JoinInfo joinInfo = joinColumns.get(0);
			if (StringUtils.isEmpty(joinInfo.getLeftKey()) || StringUtils.isEmpty(joinInfo.getRightKey())) {
				return null;
			}
			String localField = ObjectUtils.nullSafeEquals(queryJoin.getDrivenIdentify(), mainDocument)
					? Globals.DEFAULT_VALUE_STRING
					: aliasMap.get(queryJoin.getDrivenIdentify().toLowerCase());
			if (StringUtils.notBlank(localField)) {
				localField += BrainCommons.DEFAULT_NAME_SPLIT;
			}
			localField += EntityFactory.getInstance().columnName(queryJoin.getDrivenIdentify(), joinInfo.getLeftKey());
			Document document = new Document();
			document.put("from", this.dialect.nameCase(queryJoin.getJoinTable()));
			document.put("localField", this.dialect.nameCase(localField));
			document.put("foreignField", this.dialect.nameCase(joinInfo.getRightKey()));
			document.put("as", this.dialect.nameCase(aliasName));
			return document;
		} else {
			List<Document> columnsDocument = new ArrayList<>();
			for (JoinInfo joinInfo : joinColumns) {
				if (StringUtils.notBlank(joinInfo.getLeftKey()) && StringUtils.notBlank(joinInfo.getRightKey())) {
					String localField = ObjectUtils.nullSafeEquals(queryJoin.getDrivenIdentify(), mainDocument)
							? Globals.DEFAULT_VALUE_STRING
							: aliasMap.get(queryJoin.getDrivenIdentify().toLowerCase());
					if (StringUtils.notBlank(localField)) {
						localField += BrainCommons.DEFAULT_NAME_SPLIT;
					}
					localField += EntityFactory.getInstance().columnName(queryJoin.getDrivenIdentify(), joinInfo.getLeftKey());
					String foreignField = aliasMap.get(queryJoin.getJoinTable().toLowerCase());
					if (StringUtils.notBlank(foreignField)) {
						foreignField += BrainCommons.DEFAULT_NAME_SPLIT;
					}
					foreignField += joinInfo.getRightKey();
					columnsDocument.add(new Document("$eq",
							new Document(this.dialect.nameCase(localField), this.dialect.nameCase(foreignField))));
				}
			}
			if (columnsDocument.isEmpty()) {
				return null;
			}
			Document document = new Document();
			document.put("from", this.dialect.nameCase(queryJoin.getJoinTable()));
			document.put("pipeline",
					new Document("$match", new Document("$expr", new Document("$and", columnsDocument))));
			document.put("as", this.dialect.nameCase(aliasName));
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
	public void initTable(@Nonnull final DDLType ddlType, @Nonnull final TableDefine tableDefine) {
		MongoDatabase mongoDatabase = this.mongoClient.getDatabase(this.databaseName);
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
