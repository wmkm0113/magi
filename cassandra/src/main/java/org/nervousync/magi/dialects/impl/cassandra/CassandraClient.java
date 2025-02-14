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

package org.nervousync.magi.dialects.impl.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.type.codec.ExtraTypeCodecs;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import jakarta.annotation.Nonnull;
import jakarta.persistence.LockModeType;
import org.jetbrains.annotations.NotNull;
import org.nervousync.brain.command.GeneratedCommand;
import org.nervousync.brain.commons.BrainCommons;
import org.nervousync.brain.configs.auth.impl.UserAuthentication;
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
import org.nervousync.brain.enumerations.query.ConditionCode;
import org.nervousync.brain.enumerations.query.ItemType;
import org.nervousync.brain.exceptions.data.RetrieveException;
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.brain.query.condition.Condition;
import org.nervousync.brain.query.condition.impl.ColumnCondition;
import org.nervousync.brain.query.condition.impl.GroupCondition;
import org.nervousync.brain.query.core.AbstractItem;
import org.nervousync.brain.query.data.QueryData;
import org.nervousync.brain.query.item.ColumnItem;
import org.nervousync.brain.query.item.FunctionItem;
import org.nervousync.brain.query.join.JoinInfo;
import org.nervousync.brain.query.join.QueryJoin;
import org.nervousync.brain.query.param.AbstractParameter;
import org.nervousync.brain.query.param.impl.ColumnParameter;
import org.nervousync.brain.query.param.impl.QueryParameter;
import org.nervousync.commons.Globals;
import org.nervousync.enumerations.core.ConnectionCode;
import org.nervousync.magi.entity.EntityFactory;
import org.nervousync.utils.CertificateUtils;
import org.nervousync.utils.DateTimeUtils;
import org.nervousync.utils.LoggerUtils;
import org.nervousync.utils.StringUtils;

import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 *
 */
public final class CassandraClient implements DistributeClient {

	private final LoggerUtils.Logger logger;
	private final CassandraDialectImpl dialect;
	private final CqlSession cqlSession;
	private final List<ServerInfo> serverList;
	private final long lowQueryTimeout;
	private final int cachedLimitSize;
	private final Hashtable<Integer, SimpleStatementBuilder> cachedStatements;
	private final ThreadLocal<BatchStatementBuilder> threadLocal;

	CassandraClient(@Nonnull final CassandraDialectImpl dialect, @Nonnull final DistributeSchemaConfig schemaConfig)
			throws Exception {
		this.dialect = dialect;
		List<InetSocketAddress> serverAddressList = new ArrayList<>();
		List<ServerInfo> serverList = schemaConfig.getServerList();
		serverList.sort(Comparator.comparingInt(ServerInfo::getServerLevel));
		serverList.forEach(serverInfo ->
				serverAddressList.add(new InetSocketAddress(serverInfo.getServerAddress(), serverInfo.getServerPort())));

		ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder =
				DriverConfigLoader.programmaticBuilder()
						.withDuration(DefaultDriverOption.METADATA_SCHEMA_REQUEST_TIMEOUT,
								Duration.ofSeconds(schemaConfig.getValidateTimeout()))
						.withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT,
								Duration.ofSeconds(schemaConfig.getConnectTimeout()))
						.withDuration(DefaultDriverOption.REQUEST_TIMEOUT,
								Duration.ofSeconds(schemaConfig.getRequestTimeout()));
		if (serverList.size() > 1) {
			configLoaderBuilder.withString(DefaultDriverOption.REQUEST_CONSISTENCY, "LOCAL_QUORUM")
					.withString(DefaultDriverOption.REQUEST_SERIAL_CONSISTENCY, "LOCAL_SERIAL");
		} else {
			configLoaderBuilder.withString(DefaultDriverOption.REQUEST_CONSISTENCY, "ONE");
		}

		CqlSessionBuilder sessionBuilder = CqlSession.builder()
				.withConfigLoader(configLoaderBuilder.build())
				.addContactPoints(serverAddressList)
				.withLocalDatacenter(serverList.get(0).getServerName())
				.addTypeCodecs(ExtraTypeCodecs.BLOB_TO_ARRAY, ExtraTypeCodecs.ZONED_TIMESTAMP_PERSISTED,
						ExtraTypeCodecs.listToArrayOf(TypeCodecs.TEXT), ExtraTypeCodecs.optionalOf(TypeCodecs.INET));
		if (schemaConfig.isUseSsl()) {
			SSLContext sslContext = SSLContext.getDefault();
			if (schemaConfig.getTrustStore() != null) {
				TrustStore trustStore = schemaConfig.getTrustStore();
				String password = StringUtils.isEmpty(trustStore.getTrustStorePassword())
						? Globals.DEFAULT_VALUE_STRING
						: trustStore.getTrustStorePassword();
				KeyStore keyStore = CertificateUtils.loadKeyStore(trustStore.getTrustStorePath(), password);
				KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
				keyManagerFactory.init(keyStore, password.toCharArray());
				KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

				TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
				trustManagerFactory.init(keyStore);
				TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

				sslContext = SSLContext.getInstance("TLSv1.3");
				sslContext.init(keyManagers, trustManagers, null);
			}
			sessionBuilder.withSslContext(sslContext);
		}

		Optional.ofNullable(schemaConfig.getAuthentication())
				.filter(authentication -> authentication instanceof UserAuthentication)
				.filter(authentication -> StringUtils.notBlank(((UserAuthentication) authentication).getUserName()))
				.ifPresent(authentication ->
						sessionBuilder.withAuthCredentials(((UserAuthentication) authentication).getUserName(),
								((UserAuthentication) authentication).getPassWord()));
		this.cqlSession = sessionBuilder.build();
		this.serverList = serverList;
		this.lowQueryTimeout = schemaConfig.getLowQueryTimeout();
		this.cachedLimitSize = schemaConfig.getCachedLimitSize();
		this.cachedStatements = new Hashtable<>();
		this.threadLocal = new ThreadLocal<>();
		this.logger = LoggerUtils.getLogger(this.getClass());
	}

	@Override
	public void configRetry(final int retryCount, final long retryPeriod) {
	}

	@Override
	public void initSharding(final String shardingKey) {
		Optional.ofNullable(this.statement(this.dialect.createKeyspace(shardingKey, this.serverList)))
				.ifPresent(this.cqlSession::execute);
	}

	@Override
	public void beginTransactional(final TransactionalConfig transactionalConfig) {
		if (this.threadLocal.get() == null) {
			this.threadLocal.set(BatchStatement.builder(BatchType.LOGGED));
		}
	}

	@Override
	public void rollback() {
		this.threadLocal.get().clearStatements();
	}

	@Override
	public void commit() {
		this.cqlSession.execute(this.threadLocal.get().build());
	}

	@Override
	public void clearTransactional() {
		this.threadLocal.remove();
	}

	@Override
	public void truncateTables() {
		for (String tableName : this.tableNames()) {
			Optional.ofNullable(this.statement(this.dialect.truncateTable(tableName)))
					.ifPresent(this.cqlSession::execute);
		}
	}

	@Override
	public void truncateTable(@NotNull final TableDefine tableDefine) {
		Optional.ofNullable(this.statement(this.dialect.truncateTable(this.identifyCode(tableDefine))))
				.ifPresent(this.cqlSession::execute);
	}

	@Override
	public void dropTables(final DropOption dropOption) {
		for (String tableName : this.tableNames()) {
			Optional.ofNullable(this.statement(this.dialect.dropTable(tableName)))
					.ifPresent(this.cqlSession::execute);
		}
	}

	private String identifyCode(final TableDefine tableDefine) {
		return this.identifyCode(tableDefine.getSchemaName(), tableDefine.getTableName());
	}

	private String identifyCode(final String keyspaceName, final String tableName) {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.identifyCode(CassandraDialectImpl.DEFAULT_KEYSPACE, tableName);
		}
		return keyspaceName + BrainCommons.DEFAULT_NAME_SPLIT + tableName;
	}

	@Override
	public void dropTable(@NotNull final TableDefine tableDefine, @NotNull final DropOption dropOption) throws Exception {
		Optional.ofNullable(this.statement(this.dialect.dropTable(this.identifyCode(tableDefine))))
				.ifPresent(this.cqlSession::execute);
		Thread.sleep(2000L);
	}

	@Override
	public boolean lockRecord(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                          @NotNull final Map<String, Object> filterMap) {
		return Boolean.TRUE;
	}

	@Override
	public Map<String, Object> insert(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                                  @NotNull final Map<String, Object> dataMap) throws Exception {
		this.initTable(DDLType.CREATE, tableDefine, keyspaceName);
		this.executeCQL(this.dialect.insertCommand(keyspaceName, tableDefine, dataMap));
		return Map.of();
	}

	@Override
	public Map<String, Object> retrieve(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                                    final String columns, @NotNull final Map<String, Object> filterMap,
	                                    final boolean forUpdate) throws Exception {
		GeneratedCommand generatedCommand = this.dialect.queryCommand(keyspaceName, tableDefine, columns, filterMap);
		ResultSet resultSet = this.cqlSession.execute(this.statement(generatedCommand));
		Iterator<Row> iterator = resultSet.iterator();
		Map<String, Object> resultMap = null;
		while (iterator.hasNext()) {
			if (resultMap != null) {
				throw new RetrieveException(0x00DB00000028L,
						keyspaceName + BrainCommons.DEFAULT_NAME_SPLIT + tableDefine.getTableName(),
						StringUtils.objectToString(filterMap, StringUtils.StringType.JSON, Boolean.TRUE));
			}
			resultMap = this.rowToMap(tableDefine, iterator.next());
		}
		return resultMap == null ? Map.of() : resultMap;
	}

	private Map<String, Object> rowToMap(@NotNull final TableDefine tableDefine, final Row row) {
		ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
		Map<String, Object> resultMap = new HashMap<>();
		int columnCount = columnDefinitions.size();
		for (int i = 0; i < columnCount; i++) {
			String columnLabel = columnDefinitions.get(i).getName().asInternal();
			ColumnDefine columnDefine = tableDefine.column(columnLabel);
			if (columnDefine == null) {
				continue;
			}
			switch (columnDefine.getJdbcType()) {
				case Types.BLOB:
				case Types.VARBINARY:
				case Types.LONGNVARCHAR:
					ByteBuffer byteBuffer = row.get(i, ByteBuffer.class);
					if (byteBuffer != null) {
						resultMap.put(columnLabel, byteBuffer.array());
					}
					break;
				case Types.DATE:
				case Types.TIME:
				case Types.TIMESTAMP:
					Instant instant = row.getInstant(i);
					if (instant != null) {
						resultMap.put(columnLabel, Date.from(instant));
					}
					break;
				case Types.TINYINT:
					resultMap.put(columnLabel, row.getByte(i));
					break;
				case Types.INTEGER:
					resultMap.put(columnLabel, row.getInt(i));
					break;
				case Types.SMALLINT:
					resultMap.put(columnLabel, Integer.valueOf(row.getInt(i)).shortValue());
					break;
				case Types.DOUBLE:
					resultMap.put(columnLabel, row.getDouble(i));
					break;
				case Types.REAL:
					resultMap.put(columnLabel, Float.valueOf(Double.toString(row.getDouble(i))));
					break;
				case Types.DECIMAL:
					Optional.ofNullable(row.getBigDecimal(i))
							.ifPresent(columnValue -> resultMap.put(columnLabel, columnValue));
					break;
				case Types.VARCHAR:
					Optional.ofNullable(row.get(i, String.class))
							.ifPresent(columnValue -> resultMap.put(columnLabel, columnValue));
					break;
				case Types.BOOLEAN:
					resultMap.put(columnLabel, row.getBoolean(i));
					break;
				default:
					Optional.ofNullable(row.get(i, Object.class))
							.ifPresent(columnValue -> resultMap.put(columnLabel, columnValue));
					break;
			}
		}
		return resultMap;
	}

	@Override
	public int update(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                  @NotNull final Map<String, Object> dataMap, @NotNull final Map<String, Object> filterMap) {
		this.executeCQL(this.dialect.updateCommand(keyspaceName, tableDefine, dataMap, filterMap));
		return Globals.INITIALIZE_INT_VALUE;
	}

	@Override
	public int delete(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                  @NotNull final Map<String, Object> filterMap) {
		this.executeCQL(this.dialect.deleteCommand(keyspaceName, tableDefine, filterMap));
		return Globals.INITIALIZE_INT_VALUE;
	}

	@Override
	public List<Map<String, Object>> query(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                                       @NotNull final QueryInfo queryInfo)
			throws Exception {
		long beginTimestamp = DateTimeUtils.currentUTCTimeMillis();
		Map<String, List<Map<String, Object>>> subQueryResults = new HashMap<>();
		QueryInfo optimizedQuery;
		if (queryInfo.getQueryJoins().isEmpty()) {
			optimizedQuery = queryInfo;
		} else {
			optimizedQuery = this.subQuery(keyspaceName, queryInfo, subQueryResults);
		}
		List<Map<String, Object>> queryResults =
				this.executeQuery(tableDefine, this.dialect.queryCommand(keyspaceName, optimizedQuery),
						queryInfo.getPageNo(), queryInfo.getPageLimit());

		if (this.lowQueryTimeout > 0L) {
			long usedTime = DateTimeUtils.currentUTCTimeMillis() - beginTimestamp;
			if (this.lowQueryTimeout < usedTime) {
				this.logger.warn("", queryInfo.toString());
			}
		}

		return queryResults;
	}

	@Override
	public List<Map<String, Object>> queryForUpdate(@Nonnull final String keyspaceName,
	                                                @NotNull final TableDefine tableDefine,
	                                                final List<Condition> conditionList,
	                                                final LockModeType lockOption) throws Exception {
		return this.executeQuery(tableDefine,
				this.dialect.queryCommand(keyspaceName, tableDefine.getTableName(), conditionList, Boolean.FALSE));
	}

	@Override
	public Long queryTotal(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                       final QueryInfo queryInfo) throws Exception {
		GeneratedCommand generatedCommand;
		QueryInfo optimizedQuery;
		if (queryInfo.getQueryJoins().isEmpty()) {
			generatedCommand =
					this.dialect.queryCommand(keyspaceName, tableDefine.getTableName(), queryInfo.getConditionList(),
							Boolean.TRUE);
		} else {
			Map<String, List<Map<String, Object>>> subQueryResults = new HashMap<>();
			optimizedQuery = this.subQuery(keyspaceName, queryInfo, subQueryResults);
			generatedCommand =
					this.dialect.queryCommand(keyspaceName, tableDefine.getTableName(), optimizedQuery.getConditionList(),
							Boolean.TRUE);
		}
		ResultSet resultSet = this.cqlSession.execute(this.statement(generatedCommand));
		for (Row row : resultSet) {
			ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
			int columnCount = columnDefinitions.size();

			for (int i = 0; i < columnCount; i++) {
				String columnLabel = columnDefinitions.get(i).getName().asInternal();
				if (columnLabel.equalsIgnoreCase("COUNT")) {
					return row.getLong(i);
				}
			}
		}
		return 0L;
	}

	@Override
	public void initTable(@NotNull final DDLType ddlType, @NotNull final TableDefine tableDefine,
	                      final String keyspaceName) throws SQLException {
		if (DDLType.NONE.equals(ddlType)) {
			return;
		}

		String tableName = tableDefine.getTableName();
		List<ColumnDefine> existsColumns = this.existsColumns(keyspaceName, tableName);
		if (existsColumns.isEmpty()) {
			if (DDLType.CREATE.equals(ddlType) || DDLType.CREATE_DROP.equals(ddlType)
					|| DDLType.CREATE_TRUNCATE.equals(ddlType) || DDLType.SYNCHRONIZE.equals(ddlType)) {
				Optional.ofNullable(this.statement(this.dialect.createTable(keyspaceName, tableDefine)))
						.ifPresent(this.cqlSession::execute);
				for (IndexDefine indexDefine : tableDefine.getIndexDefines()) {
					Optional.ofNullable(this.statement(this.dialect.createIndex(keyspaceName, tableName, indexDefine)))
							.ifPresent(this.cqlSession::execute);
				}
			}
		} else {
			if (DDLType.VALIDATE.equals(ddlType)) {
				tableDefine.validate(existsColumns);
			} else if (DDLType.SYNCHRONIZE.equals(ddlType)) {
				List<String> commandList = this.dialect.alterTable(keyspaceName, tableDefine, existsColumns);
				if (!commandList.isEmpty()) {
					BatchStatementBuilder statementBuilder = BatchStatement.builder(BatchType.UNLOGGED);
					for (String command : commandList) {
						statementBuilder.addStatement(SimpleStatement.builder(command).build());
					}
					this.cqlSession.execute(statementBuilder.build());
				}
			}
		}
	}

	@Override
	public void close() {
		this.cqlSession.close();
	}

	/**
	 * <h3 class="en-US">Get a list of all data table names</h3>
	 * <h3 class="zh-CN">获取所有数据表名列表</h3>
	 *
	 * @return <span class="en-US">List of all data table names</span>
	 * <span class="zh-CN">数据表名列表</span>
	 */
	private List<String> tableNames() {
		List<String> tableNames = new ArrayList<>();
		this.cqlSession.getMetadata().getKeyspaces()
				.forEach((keyspaceName, keyspaceMetadata) ->
						keyspaceMetadata.getTables().keySet()
								.forEach(identifier ->
										tableNames.add(keyspaceName.asInternal() + "." + identifier.asInternal())));
		return tableNames;
	}

	/**
	 * <h3 class="en-US">Analyze and execute subqueries</h3>
	 * <h3 class="zh-CN">分析并执行子查询</h3>
	 *
	 * @param keyspaceName    <span class="en-US">Keyspace name</span>
	 *                        <span class="zh-CN">键空间名称</span>
	 * @param queryInfo       <span class="en-US">Query record information</span>
	 *                        <span class="zh-CN">数据检索信息</span>
	 * @param subQueryResults <span class="en-US">Subquery result mapping table</span>
	 *                        <span class="zh-CN">子查询结果映射表</span>
	 * @return <span class="en-US">Final query record information</span>
	 * <span class="zh-CN">最终数据检索信息</span>
	 * @throws SQLException <span class="en-US">An error occurred while execute the subqueries</span>
	 *                      <span class="zh-CN">执行子查询时出现错误</span>
	 */
	private QueryInfo subQuery(@Nonnull final String keyspaceName, @Nonnull final QueryInfo queryInfo,
	                           final Map<String, List<Map<String, Object>>> subQueryResults)
			throws Exception {
		List<String> subQueryNames = new ArrayList<>();
		for (QueryJoin queryJoin : queryInfo.getQueryJoins()) {
			if (!subQueryNames.contains(queryJoin.getJoinTable())) {
				subQueryNames.add(queryJoin.getJoinTable());
			}
		}
		while (!subQueryNames.isEmpty()) {
			Iterator<String> iterator = subQueryNames.iterator();
			while (iterator.hasNext()) {
				String subQueryName = iterator.next();
				QueryInfo subQuery = this.subQuery(keyspaceName, subQueryName, queryInfo.getItemList(),
						queryInfo.getQueryJoins(), queryInfo.getConditionList(), subQueryResults);
				if (subQuery.getQueryJoins().isEmpty()) {
					EntityFactory.TableConfig tableConfig = EntityFactory.getInstance().tableConfig(subQueryName);
					if (tableConfig == null) {
						throw new MultilingualSQLException(0x00DB00010005L);
					}
					subQueryResults.put(this.dialect.nameCase(subQueryName),
							this.executeQuery(tableConfig.getTableDefine(),
									this.dialect.queryCommand(keyspaceName, subQuery)));
					iterator.remove();
				}
			}
		}
		return this.subQuery(keyspaceName, queryInfo.getTableName(), queryInfo.getItemList(), queryInfo.getQueryJoins(),
				queryInfo.getConditionList(), subQueryResults);
	}

	/**
	 * <h3 class="en-US">Analyze and execute subqueries</h3>
	 * <h3 class="zh-CN">分析并执行子查询</h3>
	 *
	 * @param keyspaceName    <span class="en-US">Keyspace name</span>
	 *                        <span class="zh-CN">键空间名称</span>
	 * @param tableName       <span class="en-US">Data table name</span>
	 *                        <span class="zh-CN">数据表名</span>
	 * @param itemList        <span class="en-US">Query item instance list</span>
	 *                        <span class="zh-CN">查询项目实例对象列表</span>
	 * @param joinList        <span class="en-US">Related query information list</span>
	 *                        <span class="zh-CN">关联查询信息列表</span>
	 * @param conditionList   <span class="en-US">Query condition instance list</span>
	 *                        <span class="zh-CN">查询条件实例对象列表</span>
	 * @param subQueryResults <span class="en-US">Subquery result mapping table</span>
	 *                        <span class="zh-CN">子查询结果映射表</span>
	 * @return <span class="en-US">Final query record information</span>
	 * <span class="zh-CN">最终数据检索信息</span>
	 * @throws Exception <span class="en-US">An error occurred while execute the subqueries</span>
	 *                      <span class="zh-CN">执行子查询时出现错误</span>
	 */
	private QueryInfo subQuery(final String keyspaceName, final String tableName, final List<AbstractItem> itemList,
	                           final List<QueryJoin> joinList, final List<Condition> conditionList,
	                           final Map<String, List<Map<String, Object>>> subQueryResults) throws Exception {
		List<AbstractItem> queryItems = new ArrayList<>();
		List<QueryJoin> queryJoinList = new ArrayList<>();
		List<Condition> queryConditions = new ArrayList<>();
		for (QueryJoin queryJoin : joinList) {
			if (queryJoin.getDriverTable().equalsIgnoreCase(tableName)) {
				String referenceName = this.dialect.nameCase(queryJoin.getJoinTable());
				if (subQueryResults.containsKey(referenceName)) {
					List<Map<String, Object>> subQueryResult = subQueryResults.get(referenceName);
					if (!subQueryResult.isEmpty()) {
						if (queryJoin.getJoinInfos().size() == 1) {
							JoinInfo joinColumn = queryJoin.getJoinInfos().get(0);
							queryConditions.add(
									this.subCondition(subQueryResult, queryJoin.getDriverTable(), joinColumn.getJoinKey(),
											this.dialect.nameCase(joinColumn.getReferenceKey())));
						} else if (queryJoin.getJoinInfos().size() > 1) {
							List<Condition> groupConditions = new ArrayList<>();
							for (JoinInfo joinColumn : queryJoin.getJoinInfos()) {
								groupConditions.add(
										this.subCondition(subQueryResult, queryJoin.getDriverTable(), joinColumn.getJoinKey(),
												this.dialect.nameCase(joinColumn.getReferenceKey())));
							}
							queryConditions.add(Condition.group(Globals.DEFAULT_VALUE_INT, ConnectionCode.AND,
									groupConditions.toArray(new Condition[0])));
						} else {
							throw new MultilingualSQLException(0L);
						}
					}
				} else {
					queryJoinList.add(queryJoin);
				}
			} else if (queryJoin.getJoinTable().equalsIgnoreCase(tableName)) {
				for (JoinInfo joinColumn : queryJoin.getJoinInfos()) {
					queryItems.add(AbstractItem.column(tableName, joinColumn.getReferenceKey()));
				}
			}
		}
		for (AbstractItem abstractItem : itemList) {
			if (this.matchItem(tableName, abstractItem)) {
				queryItems.add(abstractItem);
			}
		}
		for (Condition condition : conditionList) {
			Optional.ofNullable(this.subCondition(keyspaceName, tableName, condition, subQueryResults))
					.ifPresent(queryConditions::add);
		}

		QueryInfo subQuery = new QueryInfo();
		subQuery.setTableName(tableName);
		subQuery.setItemList(queryItems);
		subQuery.setQueryJoins(queryJoinList);
		subQuery.setConditionList(queryConditions);
		return subQuery;
	}

	/**
	 * <h3 class="en-US">Check whether the given query item information matches the given data table name</h3>
	 * <h3 class="zh-CN">检查给定的查询项信息是否与给定的数据表名匹配</h3>
	 *
	 * @param tableName    <span class="en-US">Data table name</span>
	 *                     <span class="zh-CN">数据表名</span>
	 * @param abstractItem <span class="en-US">Query item instance object</span>
	 *                     <span class="zh-CN">查询项信息</span>
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 * @throws SQLException <span class="en-US">An error occurred while execute the check</span>
	 *                      <span class="zh-CN">执行检查时出现错误</span>
	 */
	private boolean matchItem(final String tableName, final AbstractItem abstractItem) throws SQLException {
		switch (abstractItem.getItemType()) {
			case COLUMN:
				return abstractItem.unwrap(ColumnItem.class).getTableName().equalsIgnoreCase(tableName);
			case FUNCTION:
				FunctionItem functionItem = abstractItem.unwrap(FunctionItem.class);
				for (AbstractParameter<?> functionParameter : functionItem.getFunctionParams()) {
					if (this.matchParameter(tableName, functionParameter)) {
						return Boolean.TRUE;
					}
				}
				return Boolean.FALSE;
			default:
				throw new MultilingualSQLException(0L);
		}
	}

	/**
	 * <h3 class="en-US">Check whether the given parameter information matches the given data table name</h3>
	 * <h3 class="zh-CN">检查给定的参数信息是否与给定的数据表名匹配</h3>
	 *
	 * @param tableName         <span class="en-US">Data table name</span>
	 *                          <span class="zh-CN">数据表名</span>
	 * @param abstractParameter <span class="en-US">Query parameter instance object</span>
	 *                          <span class="zh-CN">查询参数信息</span>
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 * @throws SQLException <span class="en-US">An error occurred while execute the check</span>
	 *                      <span class="zh-CN">执行检查时出现错误</span>
	 */
	private boolean matchParameter(final String tableName, final AbstractParameter<?> abstractParameter)
			throws SQLException {
		if (ItemType.COLUMN.equals(abstractParameter.getItemType())) {
			return abstractParameter.unwrap(ColumnParameter.class)
					.getItemValue().getTableName().equalsIgnoreCase(tableName);
		}
		return Boolean.FALSE;
	}

	/**
	 * <h3 class="en-US">Convert the given query matching information</h3>
	 * <h3 class="zh-CN">转换给定的查询匹配信息</h3>
	 *
	 * @param keyspaceName    <span class="en-US">Keyspace name</span>
	 *                        <span class="zh-CN">键空间名称</span>
	 * @param tableName       <span class="en-US">Data table name</span>
	 *                        <span class="zh-CN">数据表名</span>
	 * @param condition       <span class="en-US">Original match condition information</span>
	 *                        <span class="zh-CN">原始匹配信息</span>
	 * @param subQueryResults <span class="en-US">Subquery result mapping table</span>
	 *                        <span class="zh-CN">子查询结果映射表</span>
	 * @return <span class="en-US">Converted matching condition information</span>
	 * <span class="zh-CN">转换后的匹配条件信息</span>
	 * @throws Exception <span class="en-US">An error occurred while execute the conversation</span>
	 *                      <span class="zh-CN">执行转换时出现错误</span>
	 */
	private Condition subCondition(final String keyspaceName, final String tableName, final Condition condition,
	                               final Map<String, List<Map<String, Object>>> subQueryResults)
			throws Exception {
		Condition returnCondition = null;
		switch (condition.getConditionType()) {
			case COLUMN:
				ColumnCondition columnCondition = condition.unwrap(ColumnCondition.class);
				if (columnCondition.getTableName().equalsIgnoreCase(tableName)) {
					returnCondition = this.subCondition(keyspaceName, columnCondition, subQueryResults);
				}
				break;
			case GROUP:
				GroupCondition groupCondition = condition.unwrap(GroupCondition.class);
				List<Condition> subConditions = new ArrayList<>();
				for (Condition subCondition : groupCondition.getConditionList()) {
					Optional.ofNullable(this.subCondition(keyspaceName, tableName, subCondition, subQueryResults))
							.ifPresent(subConditions::add);
				}
				if (subConditions.size() == 1) {
					Condition subCondition = subConditions.get(0);
					subCondition.setSortCode(condition.getSortCode());
					subCondition.setConnectionCode(condition.getConnectionCode());
					returnCondition = subCondition;
				} else if (subConditions.size() > 1) {
					returnCondition = Condition.group(condition.getSortCode(), condition.getConnectionCode(),
							subConditions.toArray(new Condition[0]));
				}
				break;
		}
		return returnCondition;
	}

	/**
	 * <h3 class="en-US">Convert the given query matching information</h3>
	 * <h3 class="zh-CN">转换给定的查询匹配信息</h3>
	 *
	 * @param keyspaceName    <span class="en-US">Keyspace name</span>
	 *                        <span class="zh-CN">键空间名称</span>
	 * @param columnCondition <span class="en-US">Data column match condition information</span>
	 *                        <span class="zh-CN">数据列匹配条件</span>
	 * @param subQueryResults <span class="en-US">Subquery result mapping table</span>
	 *                        <span class="zh-CN">子查询结果映射表</span>
	 * @return <span class="en-US">Converted matching condition information</span>
	 * <span class="zh-CN">转换后的匹配条件信息</span>
	 * @throws SQLException <span class="en-US">An error occurred while execute the conversation</span>
	 *                      <span class="zh-CN">执行转换时出现错误</span>
	 */
	private Condition subCondition(final String keyspaceName, final ColumnCondition columnCondition,
	                               final Map<String, List<Map<String, Object>>> subQueryResults) throws Exception {
		final AbstractParameter<?> parameter = columnCondition.getConditionParameter();
		switch (parameter.getItemType()) {
			case COLUMN:
				ColumnItem columnItem = parameter.unwrap(ColumnParameter.class).getItemValue();
				if (!subQueryResults.containsKey(columnItem.getTableName())) {
					throw new MultilingualSQLException(0L);
				}
				return this.subCondition(subQueryResults.get(this.dialect.nameCase(columnItem.getTableName())),
						columnCondition, this.dialect.nameCase(columnItem.getColumnName()));
			case QUERY:
				QueryData queryData = parameter.unwrap(QueryParameter.class).getItemValue();
				GeneratedCommand generatedCommand =
						this.dialect.queryCommand(keyspaceName, queryData.getTableName(), queryData.getQueryItem(),
								queryData.getConditions());
					EntityFactory.TableConfig tableConfig =
							EntityFactory.getInstance().tableConfig(queryData.getTableName());
					if (tableConfig == null) {
						throw new MultilingualSQLException(0x00DB00010005L);
					}
				return this.subCondition(this.executeQuery(tableConfig.getTableDefine(), generatedCommand),
						columnCondition, Globals.DEFAULT_VALUE_STRING);
			default:
				return columnCondition;
		}
	}

	/**
	 * <h3 class="en-US">Convert the given query matching information</h3>
	 * <h3 class="zh-CN">转换给定的查询匹配信息</h3>
	 *
	 * @param subResults <span class="en-US">Sub-query result list</span>
	 *                   <span class="zh-CN">子查询结果集</span>
	 * @param tableName  <span class="en-US">Data table name</span>
	 *                   <span class="zh-CN">数据表名</span>
	 * @param columnName <span class="en-US">Data column name</span>
	 *                   <span class="zh-CN">数据列名</span>
	 * @param matchKey   <span class="en-US">Match data column name</span>
	 *                   <span class="zh-CN">匹配数据列名</span>
	 * @return <span class="en-US">Converted matching condition information</span>
	 * <span class="zh-CN">转换后的匹配条件信息</span>
	 * @throws SQLException <span class="en-US">An error occurred while execute the conversation</span>
	 *                      <span class="zh-CN">执行转换时出现错误</span>
	 */
	private Condition subCondition(final List<Map<String, Object>> subResults, final String tableName,
	                               final String columnName, final String matchKey) throws SQLException {
		if (subResults.size() == 1) {
			Map<String, Object> subResult = subResults.get(0);
			if (StringUtils.isEmpty(columnName)) {
				Iterator<Map.Entry<String, Object>> iterator = subResult.entrySet().iterator();
				if (iterator.hasNext()) {
					return Condition.column(Globals.DEFAULT_VALUE_INT, ConnectionCode.AND,
							ConditionCode.EQUAL, tableName, columnName,
							AbstractParameter.constant(iterator.next().getValue()));
				}
				throw new MultilingualSQLException(0L);
			} else {
				if (subResult.containsKey(matchKey)) {
					return Condition.column(Globals.DEFAULT_VALUE_INT, ConnectionCode.AND,
							ConditionCode.EQUAL, tableName, columnName,
							AbstractParameter.constant(subResult.get(this.dialect.nameCase(matchKey))));
				}
			}
			throw new MultilingualSQLException(0L);
		} else if (subResults.size() > 1) {
			return Condition.column(Globals.DEFAULT_VALUE_INT, ConnectionCode.AND,
					ConditionCode.IN, tableName, columnName, this.arrayParameter(subResults, matchKey));
		} else {
			throw new MultilingualSQLException(0L);
		}
	}

	/**
	 * <h3 class="en-US">Convert the given query matching information</h3>
	 * <h3 class="zh-CN">转换给定的查询匹配信息</h3>
	 *
	 * @param subResults      <span class="en-US">Sub-query result list</span>
	 *                        <span class="zh-CN">子查询结果集</span>
	 * @param columnCondition <span class="en-US">Data column match condition information</span>
	 *                        <span class="zh-CN">数据列匹配条件</span>
	 * @param columnName      <span class="en-US">Data column name</span>
	 *                        <span class="zh-CN">数据列名</span>
	 * @return <span class="en-US">Converted matching condition information</span>
	 * <span class="zh-CN">转换后的匹配条件信息</span>
	 * @throws SQLException <span class="en-US">An error occurred while execute the conversation</span>
	 *                      <span class="zh-CN">执行转换时出现错误</span>
	 */
	private Condition subCondition(final List<Map<String, Object>> subResults,
	                               final ColumnCondition columnCondition, final String columnName)
			throws SQLException {
		if (subResults.size() == 1) {
			Map<String, Object> subResult = subResults.get(0);
			if (StringUtils.isEmpty(columnName)) {
				Iterator<Map.Entry<String, Object>> iterator = subResult.entrySet().iterator();
				if (iterator.hasNext()) {
					return Condition.column(columnCondition.getSortCode(), columnCondition.getConnectionCode(),
							columnCondition.getConditionCode(), columnCondition.getTableName(),
							columnCondition.getColumnName(), AbstractParameter.constant(iterator.next().getValue()));
				}
				throw new MultilingualSQLException(0L);
			} else {
				if (subResult.containsKey(columnName)) {
					return Condition.column(columnCondition.getSortCode(), columnCondition.getConnectionCode(),
							columnCondition.getConditionCode(), columnCondition.getTableName(),
							columnCondition.getColumnName(), AbstractParameter.constant(subResult.get(columnName)));
				}
			}
			throw new MultilingualSQLException(0L);
		} else if (subResults.size() > 1) {
			ConditionCode conditionCode = columnCondition.getConditionCode();
			if (!ConditionCode.EQUAL.equals(conditionCode)
					&& !ConditionCode.NOT_EQUAL.equals(conditionCode)
					&& !ConditionCode.IN.equals(conditionCode)
					&& !ConditionCode.NOT_IN.equals(conditionCode)) {
				throw new MultilingualSQLException(0L);
			}
			switch (conditionCode) {
				case EQUAL:
					conditionCode = ConditionCode.IN;
					break;
				case NOT_EQUAL:
					conditionCode = ConditionCode.NOT_IN;
					break;
			}
			return Condition.column(columnCondition.getSortCode(), columnCondition.getConnectionCode(),
					conditionCode, columnCondition.getTableName(),
					columnCondition.getColumnName(), this.arrayParameter(subResults, columnName));
		} else {
			throw new MultilingualSQLException(0L);
		}
	}

	/**
	 * <h3 class="en-US">Generate the array parameter instance object</h3>
	 * <h3 class="zh-CN">生成数组匹配参数</h3>
	 *
	 * @param subResults <span class="en-US">Sub-query result list</span>
	 *                   <span class="zh-CN">子查询结果集</span>
	 * @param columnName <span class="en-US">Data column name</span>
	 *                   <span class="zh-CN">数据列名</span>
	 * @return <span class="en-US">Generated parameter instance object</span>
	 * <span class="zh-CN">生成的参数实例对象</span>
	 * @throws SQLException <span class="en-US">Match results not found</span>
	 *                      <span class="zh-CN">匹配结果未找到</span>
	 */
	private AbstractParameter<?> arrayParameter(final List<Map<String, Object>> subResults, final String columnName)
			throws SQLException {
		List<Object> resultValues = new ArrayList<>();
		for (Map<String, Object> subResult : subResults) {
			Optional.ofNullable(subResult.get(columnName)).ifPresent(resultValues::add);
		}
		if (resultValues.isEmpty()) {
			throw new MultilingualSQLException(0L);
		}
		return AbstractParameter.arrays(resultValues.toArray());
	}

	/**
	 * <h3 class="en-US">Execute query command</h3>
	 * <h3 class="zh-CN">执行查询命令</h3>
	 *
	 * @param generatedCommand <span class="en-US">Generated CQL command</span>
	 *                         <span class="zh-CN">生成的CQL命令</span>
	 * @return <span class="en-US">Query result record list</span>
	 * <span class="zh-CN">查询结果数据列表</span>
	 */
	private List<Map<String, Object>> executeQuery(@NotNull final TableDefine tableDefine,
	                                               final GeneratedCommand generatedCommand) {
		return this.executeQuery(tableDefine, generatedCommand, Globals.DEFAULT_VALUE_INT, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * <h3 class="en-US">Execute query command</h3>
	 * <h3 class="zh-CN">执行查询命令</h3>
	 *
	 * @param generatedCommand <span class="en-US">Generated CQL command</span>
	 *                         <span class="zh-CN">生成的CQL命令</span>
	 * @return <span class="en-US">Query result record list</span>
	 * <span class="zh-CN">查询结果数据列表</span>
	 */
	private List<Map<String, Object>> executeQuery(@NotNull final TableDefine tableDefine,
	                                               final GeneratedCommand generatedCommand,
	                                               final int pageNo, final int pageLimit) {
		ResultSet resultSet = this.cqlSession.execute(this.statement(generatedCommand));
		Iterator<Row> iterator = resultSet.iterator();
		List<Map<String, Object>> resultList = new ArrayList<>();
		int offset = Globals.DEFAULT_VALUE_INT;
		if (pageLimit > Globals.INITIALIZE_INT_VALUE) {
			offset = Integer.max(Globals.INITIALIZE_INT_VALUE, (pageNo - 1) * pageLimit);
		}
		int index = Globals.INITIALIZE_INT_VALUE;
		while (iterator.hasNext()) {
			if (offset <= index) {
				resultList.add(this.rowToMap(tableDefine, iterator.next()));
			}
			if (pageLimit > Globals.INITIALIZE_INT_VALUE && resultList.size() == pageLimit) {
				return resultList;
			}
			index++;
		}
		return resultList;
	}

	/**
	 * <h3 class="en-US">Execute CQL command</h3>
	 * <h3 class="zh-CN">执行CQL命令</h3>
	 *
	 * @param generatedCommand <span class="en-US">Generated CQL command</span>
	 *                         <span class="zh-CN">生成的CQL命令</span>
	 */
	private void executeCQL(final GeneratedCommand generatedCommand) {
		Optional.ofNullable(this.statement(generatedCommand))
				.ifPresent(statement -> {
					BatchStatementBuilder statementBuilder = this.threadLocal.get();
					if (statementBuilder == null) {
						this.cqlSession.execute(statement);
					} else {
						statementBuilder.addStatement(statement);
					}
				});
	}

	/**
	 * <h3 class="en-US">Get structural information of a given data table</h3>
	 * <h3 class="zh-CN">获取给定数据表的结构信息</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 * @param tableName    <span class="en-US">Data table name</span>
	 *                     <span class="zh-CN">数据表名</span>
	 * @return <span class="en-US">Exists data column define information list</span>
	 * <span class="zh-CN">已存在数据列定义信息列表</span>
	 */
	private List<ColumnDefine> existsColumns(final String keyspaceName, final String tableName) {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.existsColumns(CassandraDialectImpl.DEFAULT_KEYSPACE, tableName);
		}
		return this.cqlSession.getMetadata()
				.getKeyspaces()
				.entrySet()
				.stream()
				.filter(entry -> entry.getKey().asInternal().equalsIgnoreCase(keyspaceName))
				.findFirst()
				.flatMap(entry ->
						entry.getValue()
								.getTables()
								.entrySet()
								.stream()
								.filter(tableEntry -> tableEntry.getKey().asInternal().equalsIgnoreCase(tableName))
								.findFirst())
				.map(entry -> this.parseColumns(entry.getValue()))
				.orElse(Collections.emptyList());
	}

	/**
	 * <h3 class="en-US">Parse the given data table structure information</h3>
	 * <h3 class="zh-CN">解析给定的数据表结构信息</h3>
	 *
	 * @param tableMetadata <span class="en-US">Data table metadata information</span>
	 *                      <span class="zh-CN">数据表元信息</span>
	 * @return <span class="en-US">Exists data column define information list</span>
	 * <span class="zh-CN">已存在数据列定义信息列表</span>
	 */
	private List<ColumnDefine> parseColumns(@Nonnull final TableMetadata tableMetadata) {
		List<ColumnDefine> columnDefineList = new ArrayList<>();
		tableMetadata.getColumns().forEach(((cqlIdentifier, columnMetadata) -> {
			ColumnDefine columnDefine = new ColumnDefine();
			columnDefine.setColumnName(cqlIdentifier.asInternal());
			columnDefine.setJdbcType(this.dialect.parseJdbcType(columnMetadata.getType().toString()));
			columnDefineList.add(columnDefine);
		}));
		return columnDefineList;
	}

	/**
	 * <h3 class="en-US">Parses the given CQL command and generates a query instance object</h3>
	 * <h3 class="zh-CN">解析给定的CQL命令并生成查询实例对象</h3>
	 *
	 * @param generatedCommand <span class="en-US">Generated CQL command</span>
	 *                         <span class="zh-CN">生成的CQL命令</span>
	 * @return <span class="en-US">Query statement instance object</span>
	 * <span class="zh-CN">查询实例对象</span>
	 */
	private SimpleStatement statement(final GeneratedCommand generatedCommand) {
		if (generatedCommand == null) {
			return null;
		}
		Integer identifyCode = generatedCommand.getCommand().hashCode();
		SimpleStatementBuilder statementBuilder =
				this.cachedStatements.getOrDefault(generatedCommand.getCommand().hashCode(),
						SimpleStatement.builder(generatedCommand.getCommand()));
		if (this.cachedLimitSize > Globals.INITIALIZE_INT_VALUE && !this.cachedStatements.containsKey(identifyCode)) {
			this.cachedStatements.put(identifyCode, statementBuilder);
		}
		statementBuilder.clearPositionalValues();
		Optional.ofNullable(generatedCommand.getParameters())
				.ifPresent(parameters -> parameters.forEach(statementBuilder::addPositionalValue));
		return statementBuilder.build();
	}
}
