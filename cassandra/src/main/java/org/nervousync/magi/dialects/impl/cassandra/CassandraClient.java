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
import org.nervousync.brain.exceptions.data.RetrieveException;
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.brain.query.PartialCollection;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.commons.Globals;
import org.nervousync.enumerations.beans.StringType;
import org.nervousync.utils.cert.CertificateUtils;
import org.nervousync.utils.core.BeanUtils;
import org.nervousync.utils.core.DateTimeUtils;
import org.nervousync.utils.core.StringUtils;
import org.nervousync.utils.logger.LoggerUtils;

import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.util.*;

/**
 * <h2 class="en-US">Cassandra database client implementation class</h2>
 * <h2 class="zh-CN">Cassandra数据库客户端实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 18, 2022 10:08:19 $
 */
public final class CassandraClient implements DistributeClient {

	/**
	 * <span class="en-US">Logger instance</span>
	 * <span class="zh-CN">日志实例</span>
	 */
	private final LoggerUtils.Logger logger = LoggerUtils.getLogger(this.getClass());
	/**
	 * <span class="en-US">Database dialect instance object</span>
	 * <span class="zh-CN">数据库方言实例对象</span>
	 */
	private final CassandraDialectImpl dialect;
	/**
	 * <span class="en-US">Cassandra connection session instance object</span>
	 * <span class="zh-CN">Cassandra 连接实例对象</span>
	 */
	private final CqlSession cqlSession;
	/**
	 * <span class="en-US">Default keyspace name</span>
	 * <span class="zh-CN">默认键空间</span>
	 */
	private final String keyspaceName;
	/**
	 * <span class="en-US">Database server info list</span>
	 * <span class="zh-CN">数据库服务器列表</span>
	 */
	private final List<ServerInfo> serverList;
	/**
	 * <span class="en-US">Low query timeout (Unit: milliseconds)</span>
	 * <span class="zh-CN">慢查询的临界时间（单位：毫秒）</span>
	 */
	private final long lowQueryTimeout;
	/**
	 * <span class="en-US">Maximum size of prepared statement</span>
	 * <span class="zh-CN">查询分析器的最大缓存结果</span>
	 */
	private final int cachedLimitSize;
	/**
	 * <span class="en-US">List of existed keyspace names</span>
	 * <span class="zh-CN">已存在的键空间名称列表</span>
	 */
	private final List<String> existKeySpaces = new ArrayList<>();
	/**
	 * <span class="en-US">Cached prepared statement mapping</span>
	 * <span class="zh-CN">缓存的查询分析器映射表</span>
	 */
	private final Hashtable<Integer, SimpleStatementBuilder> cachedStatements;
	/**
	 * <span class="en-US">Database connection used by the current thread</span>
	 * <span class="zh-CN">当前线程使用的数据库连接</span>
	 */
	private final ThreadLocal<BatchStatementBuilder> threadLocal;

	/**
	 * <h3 class="en-US">Constructor method for Cassandra database client implementation class</h3>
	 * <h3 class="zh-CN">Cassandra数据库客户端实现类的构造方法</h3>
	 *
	 * @param dialect      <span class="en-US">Database dialect instance object</span>
	 *                     <span class="zh-CN">数据库方言实例对象</span>
	 * @param schemaConfig <span class="en-US">Data source configure information</span>
	 *                     <span class="zh-CN">数据源配置信息</span>
	 * @throws Exception <span class="en-US">An error occurs when configure SSL</span>
	 *                   <span class="zh-CN">设置SSL时出错</span>
	 */
	CassandraClient(@Nonnull final CassandraDialectImpl dialect, @Nonnull final DistributeSchemaConfig schemaConfig)
			throws Exception {
		this.dialect = dialect;
		this.keyspaceName = schemaConfig.getDatabaseName();
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
				String password = StringUtils.isEmpty(trustStore.getStorePassword())
						? Globals.DEFAULT_VALUE_STRING
						: trustStore.getStorePassword();
				KeyStore keyStore = CertificateUtils.loadKeyStore(trustStore.getStorePath(), password);
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
		this.cqlSession.getMetadata()
				.getKeyspaces()
				.keySet()
				.forEach(cqlIdentifier -> this.existKeySpaces.add(cqlIdentifier.asInternal()));
	}

	@Override
	public void configRetry(final int retryCount, final long retryPeriod) {
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
	public void truncateTable(@Nonnull final TableDefine tableDefine) {
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

	@Override
	public void dropTable(@Nonnull final TableDefine tableDefine, @Nonnull final DropOption dropOption) throws Exception {
		Optional.ofNullable(this.statement(this.dialect.dropTable(this.identifyCode(tableDefine))))
				.ifPresent(this.cqlSession::execute);
		Thread.sleep(2000L);
	}

	@Override
	public boolean lockRecord(@Nonnull final TableDefine tableDefine, @Nonnull final Map<String, Object> filterMap) {
		return Boolean.TRUE;
	}

	@Override
	public Map<String, Object> insert(@Nonnull final TableDefine tableDefine, @Nonnull final Map<String, Object> dataMap) {
		this.initKeyspace(tableDefine.getCatalog());
		this.execute(this.dialect.insertCommand(keyspaceName, tableDefine, dataMap));
		return Map.of();
	}

	@Override
	public Map<String, Object> retrieve(@Nonnull final TableDefine tableDefine,
	                                    final String columns, @Nonnull final Map<String, Object> filterMap,
	                                    final boolean forUpdate) {
		this.initKeyspace(tableDefine.getCatalog());
		GeneratedCommand generatedCommand = this.dialect.queryCommand(keyspaceName, tableDefine, columns, filterMap);
		ResultSet resultSet = this.cqlSession.execute(this.statement(generatedCommand));
		Iterator<Row> iterator = resultSet.iterator();
		Map<String, Object> resultMap = null;
		while (iterator.hasNext()) {
			if (resultMap != null) {
				throw new RetrieveException(0x00DB00000028L,
						keyspaceName + BrainCommons.DEFAULT_NAME_SPLIT + tableDefine.getTableName(),
						BeanUtils.objectToString(filterMap, StringType.JSON, Boolean.TRUE));
			}
			resultMap = this.rowToMap(generatedCommand.getJdbcTypeMap(), generatedCommand.getKeyMap(), iterator.next());
		}
		return resultMap == null ? Map.of() : resultMap;
	}

	@Override
	public int update(@Nonnull final TableDefine tableDefine, @Nonnull final Map<String, Object> dataMap,
	                  @Nonnull final Map<String, Object> filterMap) {
		this.initKeyspace(tableDefine.getCatalog());
		this.execute(this.dialect.updateCommand(keyspaceName, tableDefine, dataMap, filterMap));
		return 1;
	}

	@Override
	public int delete(@Nonnull final TableDefine tableDefine, @Nonnull final Map<String, Object> filterMap) {
		this.initKeyspace(tableDefine.getCatalog());
		this.execute(this.dialect.deleteCommand(keyspaceName, tableDefine, filterMap));
		return 1;
	}

	@Override
	public PartialCollection query(@Nonnull final QueryInfo queryInfo) throws Exception {
		long beginTimestamp = DateTimeUtils.currentUTCTimeMillis();
		if (!queryInfo.getQueryJoins().isEmpty()) {
			throw new MultilingualSQLException(0x00DB00CA0001L);
		}
		this.initKeyspace(this.keyspaceName);
		List<Map<String, Object>> queryResults =
				this.executeQuery(this.dialect.queryCommand(this.keyspaceName, queryInfo),
						queryInfo.getPageNo(), queryInfo.getPageLimit());

		if (this.lowQueryTimeout > 0L) {
			long usedTime = DateTimeUtils.currentUTCTimeMillis() - beginTimestamp;
			if (this.lowQueryTimeout < usedTime) {
				this.logger.warn("Low_Query_Warning", queryInfo.toString(), this.lowQueryTimeout, usedTime);
			}
		}

		return new PartialCollection(queryResults, this.queryTotal(queryInfo));
	}

	@Override
	public Long queryTotal(final QueryInfo queryInfo) throws Exception {
		if (!queryInfo.getQueryJoins().isEmpty()) {
			throw new MultilingualSQLException(0x00DB00CA0001L);
		}
		this.initKeyspace(this.keyspaceName);
		GeneratedCommand generatedCommand = this.dialect.queryTotalCommand(this.keyspaceName, queryInfo);
		ResultSet resultSet = this.cqlSession.execute(this.statement(generatedCommand));
		for (Row row : resultSet) {
			ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
			int columnCount = columnDefinitions.size();

			for (int i = 0; i < columnCount; i++) {
				String columnLabel = columnDefinitions.get(i).getName().asInternal();
				if ("COUNT".equalsIgnoreCase(columnLabel)) {
					return row.getLong(i);
				}
			}
		}
		return 0L;
	}

	@Override
	public void initTable(@Nonnull final DDLType ddlType, @Nonnull final TableDefine tableDefine) throws SQLException {
		if (DDLType.NONE.equals(ddlType)) {
			return;
		}
		String keyspace = StringUtils.isEmpty(tableDefine.getCatalog()) ? this.keyspaceName : tableDefine.getCatalog();
		this.initKeyspace(keyspace);

		String tableName = tableDefine.getTableName();
		List<ColumnDefine> existsColumns = this.existsColumns(keyspace, tableName);
		if (existsColumns.isEmpty()) {
			if (DDLType.CREATE.equals(ddlType) || DDLType.CREATE_DROP.equals(ddlType)
					|| DDLType.CREATE_TRUNCATE.equals(ddlType) || DDLType.SYNCHRONIZE.equals(ddlType)) {
				Optional.ofNullable(this.statement(this.dialect.createTable(keyspace, tableDefine)))
						.ifPresent(this.cqlSession::execute);
				for (IndexDefine indexDefine : tableDefine.getIndexDefines()) {
					Optional.ofNullable(this.statement(this.dialect.createIndex(keyspace, tableName, indexDefine)))
							.ifPresent(this.cqlSession::execute);
				}
			}
		} else {
			if (DDLType.VALIDATE.equals(ddlType)) {
				tableDefine.validate(existsColumns);
			} else if (DDLType.SYNCHRONIZE.equals(ddlType)) {
				List<String> commandList = this.dialect.alterTable(keyspace, tableDefine, existsColumns);
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
	 * <h3 class="en-US">Initialize keyspace</h3>
	 * <h3 class="zh-CN">初始化键空间</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 */
	private void initKeyspace(final String keyspaceName) {
		if (StringUtils.notBlank(keyspaceName) && !this.existKeySpaces.contains(keyspaceName)) {
			Optional.ofNullable(this.statement(this.dialect.createKeyspace(keyspaceName, this.serverList)))
					.ifPresent(this.cqlSession::execute);
		}
	}

	/**
	 * <h3 class="en-US">Generate data table identify code</h3>
	 * <h3 class="zh-CN">生成数据表识别代码</h3>
	 *
	 * @param tableDefine <span class="en-US">Table defines information</span>
	 *                    <span class="zh-CN">数据表定义信息</span>
	 * @return <span class="en-US">Identify code</span>
	 * <span class="zh-CN">识别代码</span>
	 */
	private String identifyCode(final TableDefine tableDefine) {
		String keyspaceName = StringUtils.isEmpty(tableDefine.getCatalog()) ? this.keyspaceName : tableDefine.getCatalog();
		return this.identifyCode(keyspaceName, tableDefine.getTableName());
	}

	/**
	 * <h3 class="en-US">Generate data table identify code</h3>
	 * <h3 class="zh-CN">生成数据表识别代码</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 * @param tableName    <span class="en-US">Data table name</span>
	 *                     <span class="zh-CN">数据表名</span>
	 * @return <span class="en-US">Identify code</span>
	 * <span class="zh-CN">识别代码</span>
	 */
	private String identifyCode(final String keyspaceName, final String tableName) {
		return keyspaceName + BrainCommons.DEFAULT_NAME_SPLIT + tableName;
	}

	/**
	 * <h3 class="en-US">Convert data records into data mapping tables</h3>
	 * <h3 class="zh-CN">转换数据记录为数据映射表</h3>
	 *
	 * @param jdbcTypeMap <span class="en-US">Data column label and types mapping table</span>
	 *                    <span class="zh-CN">数据列类型映射表</span>
	 * @param row         <span class="en-US">Data record</span>
	 *                    <span class="zh-CN">数据记录</span>
	 * @return <span class="en-US">Data mapping tables</span>
	 * <span class="zh-CN">数据映射表</span>
	 */
	private Map<String, Object> rowToMap(@Nonnull final Map<String, Integer> jdbcTypeMap,
	                                     @Nonnull final Map<String, String> keyMap, final Row row) {
		ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
		Map<String, Object> resultMap = new HashMap<>();
		int columnCount = columnDefinitions.size();
		for (int i = 0; i < columnCount; i++) {
			String columnLabel = columnDefinitions.get(i).getName().asInternal();
			String aliasName = keyMap.getOrDefault(columnLabel, columnLabel);
			Integer jdbcType = jdbcTypeMap.get(columnLabel);
			if (jdbcType == null) {
				continue;
			}
			switch (jdbcType) {
				case Types.BLOB:
				case Types.VARBINARY:
				case Types.LONGNVARCHAR:
					ByteBuffer byteBuffer = row.get(i, ByteBuffer.class);
					if (byteBuffer != null) {
						resultMap.put(aliasName, byteBuffer.array());
					}
					break;
				case Types.DATE:
					Optional.ofNullable(row.getInstant(i))
							.map(instant -> new java.sql.Date(instant.toEpochMilli()))
							.ifPresent(value -> resultMap.put(aliasName, value));
					break;
				case Types.TIME:
					Optional.ofNullable(row.getInstant(i))
							.map(instant -> new java.sql.Time(instant.toEpochMilli()))
							.ifPresent(value -> resultMap.put(aliasName, value));
					break;
				case Types.TIMESTAMP:
					Optional.ofNullable(row.getInstant(i))
							.map(instant -> new java.sql.Timestamp(instant.toEpochMilli()))
							.ifPresent(value -> resultMap.put(aliasName, value));
					break;
				case Types.TINYINT:
					resultMap.put(aliasName, row.getByte(i));
					break;
				case Types.INTEGER:
					resultMap.put(aliasName, row.getInt(i));
					break;
				case Types.SMALLINT:
					resultMap.put(aliasName, Integer.valueOf(row.getInt(i)).shortValue());
					break;
				case Types.DOUBLE:
					resultMap.put(aliasName, row.getDouble(i));
					break;
				case Types.REAL:
					resultMap.put(aliasName, Float.valueOf(Double.toString(row.getDouble(i))));
					break;
				case Types.DECIMAL:
					Optional.ofNullable(row.getBigDecimal(i))
							.ifPresent(columnValue -> resultMap.put(aliasName, columnValue));
					break;
				case Types.VARCHAR:
					Optional.ofNullable(row.get(i, String.class))
							.ifPresent(columnValue -> resultMap.put(aliasName, columnValue));
					break;
				case Types.BOOLEAN:
					resultMap.put(aliasName, row.getBoolean(i));
					break;
				default:
					Optional.ofNullable(row.get(i, Object.class))
							.ifPresent(columnValue -> resultMap.put(aliasName, columnValue));
					break;
			}
		}
		return resultMap;
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
	 * <h3 class="en-US">Execute query command</h3>
	 * <h3 class="zh-CN">执行查询命令</h3>
	 *
	 * @param generatedCommand <span class="en-US">Generated CQL command</span>
	 *                         <span class="zh-CN">生成的CQL命令</span>
	 * @return <span class="en-US">Query result record list</span>
	 * <span class="zh-CN">查询结果数据列表</span>
	 */
	private List<Map<String, Object>> executeQuery(final GeneratedCommand generatedCommand,
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
			Row row = iterator.next();
			if (offset <= index) {
				resultList.add(this.rowToMap(generatedCommand.getJdbcTypeMap(), generatedCommand.getKeyMap(), row));
			}
			if (pageLimit > Globals.INITIALIZE_INT_VALUE && resultList.size() == pageLimit) {
				break;
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
	private void execute(final GeneratedCommand generatedCommand) {
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
