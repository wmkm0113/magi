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

import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.nervousync.annotations.provider.Provider;
import org.nervousync.brain.annotations.dialect.DataType;
import org.nervousync.brain.annotations.dialect.SchemaDialect;
import org.nervousync.brain.command.GeneratedCommand;
import org.nervousync.brain.commons.BrainCommons;
import org.nervousync.brain.configs.auth.Authentication;
import org.nervousync.brain.configs.schema.impl.DistributeSchemaConfig;
import org.nervousync.brain.configs.secure.TrustStore;
import org.nervousync.brain.configs.server.ServerInfo;
import org.nervousync.brain.defines.ColumnDefine;
import org.nervousync.brain.defines.IndexDefine;
import org.nervousync.brain.defines.TableDefine;
import org.nervousync.brain.dialects.distribute.DistributeClient;
import org.nervousync.brain.dialects.distribute.DistributeDialect;
import org.nervousync.brain.exceptions.dialects.DialectException;
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.brain.query.condition.Condition;
import org.nervousync.brain.query.condition.impl.ColumnCondition;
import org.nervousync.brain.query.condition.impl.GroupCondition;
import org.nervousync.brain.query.core.AbstractItem;
import org.nervousync.brain.query.core.SortedItem;
import org.nervousync.brain.query.data.ArrayData;
import org.nervousync.brain.query.data.RangesData;
import org.nervousync.brain.query.filter.OrderBy;
import org.nervousync.brain.query.item.ColumnItem;
import org.nervousync.brain.query.item.FunctionItem;
import org.nervousync.brain.query.param.AbstractParameter;
import org.nervousync.brain.query.param.impl.*;
import org.nervousync.commons.Globals;
import org.nervousync.enumerations.core.ConnectionCode;
import org.nervousync.utils.ObjectUtils;
import org.nervousync.utils.StringUtils;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * <h2 class="en-US">Cassandra database dialect implementation class</h2>
 * <h2 class="zh-CN">Cassandra数据库方言实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 18, 2022 09:05:28 $
 */
@Provider(name = "Cassandra", titleKey = "Cassandra_Dialect_Title", descriptionKey = "Cassandra_Dialect_Description")
@SchemaDialect(supportJoin = false, types = {
		@DataType(code = Types.VARCHAR, type = "text"),
		@DataType(code = Types.NVARCHAR, type = "text"),
		@DataType(code = Types.INTEGER, type = "int"),
		@DataType(code = Types.SMALLINT, type = "int"),
		@DataType(code = Types.BIGINT, type = "bigint"),
		@DataType(code = Types.TINYINT, type = "tinyint"),
		@DataType(code = Types.FLOAT, type = "float"),
		@DataType(code = Types.DOUBLE, type = "double"),
		@DataType(code = Types.NUMERIC, type = "decimal"),
		@DataType(code = Types.REAL, type = "double"),
		@DataType(code = Types.DECIMAL, type = "decimal"),
		@DataType(code = Types.BIT, type = "int"),
		@DataType(code = Types.BOOLEAN, type = "boolean"),
		@DataType(code = Types.CHAR, type = "ascii"),
		@DataType(code = Types.NCHAR, type = "ascii"),
		@DataType(code = Types.DATE, type = "timestamp"),
		@DataType(code = Types.TIME, type = "timestamp"),
		@DataType(code = Types.TIMESTAMP, type = "timestamp"),
		@DataType(code = Types.BLOB, type = "blob"),
		@DataType(code = Types.VARBINARY, type = "blob"),
		@DataType(code = Types.LONGVARBINARY, type = "blob"),
		@DataType(code = Types.CLOB, type = "text"),
		@DataType(code = Types.NCLOB, type = "text"),
		@DataType(code = Types.LONGNVARCHAR, type = "text")
})
public final class CassandraDialectImpl extends DistributeDialect {

	/**
	 * <span class="en-US">Default keyspace</span>
	 * <span class="zh-CN">默认的键空间</span>
	 */
	static final String DEFAULT_KEYSPACE = "nervousync";

	/**
	 * <h3 class="en-US">Constructor method for Cassandra database dialect implementation class</h3>
	 * <h3 class="zh-CN">Cassandra数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the org. nervousync. brain. annotations. dialect.SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到org. nervousync. brain. annotations. dialect.SchemaDialect注解</span>
	 */
	public CassandraDialectImpl() throws DialectException {
		super();
	}

	@Override
	public DistributeClient newClient(final DistributeSchemaConfig schemaConfig) throws Exception {
		return new CassandraClient(this, schemaConfig);
	}

	@Override
	public String defaultValue(final int jdbcType, final int length, final int precision,
	                           final int scale, final Object object) {
		//  Cassandra not support default value for column
		return Globals.DEFAULT_VALUE_STRING;
	}

	@Override
	public String nameCase(final String name) {
		return StringUtils.isEmpty(name) ? Globals.DEFAULT_VALUE_STRING : name.toLowerCase();
	}

	@Override
	public Properties properties(final TrustStore trustStore, final Authentication authentication) {
		return null;
	}

	/**
	 * <h3 class="en-US">Parse data type into JDBC type code</h3>
	 * <h3 class="zh-CN">解析数据类型为JDBC类型代码</h3>
	 *
	 * @param typeName <span class="en-US">Data type string</span>
	 *                 <span class="zh-CN">数据类型字符串</span>
	 * @return <span class="en-US">JDBC type code</span>
	 * <span class="zh-CN">JDBC类型代码</span>
	 */
	int parseJdbcType(final String typeName) {
		if (StringUtils.isEmpty(typeName)) {
			return Types.OTHER;
		}
		switch (typeName.toLowerCase()) {
			case "text":
				return Types.VARCHAR;
			case "int":
				return Types.INTEGER;
			case "bigint":
				return Types.BIGINT;
			case "tinyint":
				return Types.TINYINT;
			case "float":
				return Types.FLOAT;
			case "double":
				return Types.DOUBLE;
			case "decimal":
				return Types.DECIMAL;
			case "boolean":
				return Types.BOOLEAN;
			case "ascii":
				return Types.CHAR;
			case "timestamp":
				return Types.TIMESTAMP;
			case "blob":
				return Types.BLOB;
			default:
				return Types.OTHER;
		}
	}

	/**
	 * <h3 class="en-US">Generate create keyspace commands</h3>
	 * <h3 class="zh-CN">生成创建键空间命令</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 * @param serverList   <span class="en-US">Database server information list</span>
	 *                     <span class="zh-CN">服务器信息列表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	GeneratedCommand createKeyspace(final String keyspaceName, final List<ServerInfo> serverList) {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.createKeyspace(DEFAULT_KEYSPACE, serverList);
		}
		StringBuilder commandBuilder = new StringBuilder("CREATE KEYSPACE IF NOT EXISTS ")
				.append(this.nameCase(keyspaceName)).append(" WITH REPLICATION = {");
		if (serverList.size() == 1) {
			commandBuilder.append("'class': 'SimpleStrategy', 'replication_factor': 1");
		} else if (serverList.size() > 1) {
			commandBuilder.append("'class': 'NetworkTopologyStrategy'");
			for (ServerInfo serverInfo : serverList) {
				commandBuilder.append(", '")
						.append(serverInfo.getServerName()).append("': ")
						.append(serverInfo.getServerLevel());
			}
		}
		commandBuilder.append("}");
		return new GeneratedCommand(commandBuilder.toString(), Collections.emptyList());
	}

	/**
	 * <h3 class="en-US">Generate data index commands</h3>
	 * <h3 class="zh-CN">生成创建数据索引命令</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 * @param tableName    <span class="en-US">Data table name</span>
	 *                     <span class="zh-CN">数据表名称</span>
	 * @param indexDefine  <span class="en-US">Index define</span>
	 *                     <span class="zh-CN">索引定义</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	GeneratedCommand createIndex(@Nonnull final String keyspaceName, @Nonnull final String tableName,
	                             @Nonnull final IndexDefine indexDefine) {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.createIndex(DEFAULT_KEYSPACE, tableName, indexDefine);
		}
		if (StringUtils.isEmpty(indexDefine.getIndexName())) {
			return null;
		}
		StringBuilder commandBuilder = new StringBuilder("CREATE INDEX IF NOT EXISTS ")
				.append(indexDefine.getIndexName())
				.append(" ON ")
				.append(this.nameCase(keyspaceName))
				.append(BrainCommons.DEFAULT_NAME_SPLIT)
				.append(this.nameCase(tableName))
				.append(BrainCommons.BRACKETS_BEGIN);

		String split = Globals.DEFAULT_VALUE_STRING;
		for (String columnName : indexDefine.getColumnList()) {
			commandBuilder.append(split).append(this.nameCase(columnName));
			split = BrainCommons.DEFAULT_SPLIT_CHARACTER;
		}
		commandBuilder.append(BrainCommons.BRACKETS_END);
		return new GeneratedCommand(commandBuilder.toString(), Collections.emptyList());
	}

	/**
	 * <h3 class="en-US">Generate create data table commands</h3>
	 * <h3 class="zh-CN">生成创建数据表命令</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 * @param tableDefine  <span class="en-US">Data table define</span>
	 *                     <span class="zh-CN">数据表定义</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	GeneratedCommand createTable(final String keyspaceName, final TableDefine tableDefine) {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.createTable(DEFAULT_KEYSPACE, tableDefine);
		}
		StringBuilder columns = new StringBuilder();
		StringBuilder primaryKey = new StringBuilder();
		for (ColumnDefine columnDefine : tableDefine.getColumnDefines()) {
			columns.append(BrainCommons.DEFAULT_SPLIT_CHARACTER)
					.append(this.nameCase(columnDefine.getColumnName()))
					.append(BrainCommons.WHITE_SPACE)
					.append(this.columnType(columnDefine.getJdbcType(), columnDefine.getLength(),
							columnDefine.getPrecision(), columnDefine.getScale()));
			if (columnDefine.isPrimaryKey()) {
				primaryKey.append(BrainCommons.DEFAULT_SPLIT_CHARACTER).append(this.nameCase(columnDefine.getColumnName()));
			}
		}

		StringBuilder commandBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
				.append(keyspaceName)
				.append(BrainCommons.DEFAULT_NAME_SPLIT)
				.append(this.nameCase(tableDefine.getTableName()))
				.append(BrainCommons.BRACKETS_BEGIN);
		if (columns.length() > 0) {
			commandBuilder.append(columns.substring(BrainCommons.DEFAULT_SPLIT_CHARACTER.length()));
		}
		if (primaryKey.length() > 0) {
			commandBuilder.append(", PRIMARY KEY ")
					.append(BrainCommons.BRACKETS_BEGIN)
					.append(primaryKey.substring(BrainCommons.DEFAULT_SPLIT_CHARACTER.length()))
					.append(BrainCommons.BRACKETS_END);
		}
		commandBuilder.append(BrainCommons.BRACKETS_END);
		return new GeneratedCommand(commandBuilder.toString(), Collections.emptyList());
	}

	/**
	 * <h3 class="en-US">Generate alter data table struct commands</h3>
	 * <h3 class="zh-CN">生成数据表结构更新命令</h3>
	 *
	 * @param keyspaceName  <span class="en-US">Keyspace name</span>
	 *                      <span class="zh-CN">键空间名称</span>
	 * @param tableDefine   <span class="en-US">Data table define</span>
	 *                      <span class="zh-CN">数据表定义</span>
	 * @param existsColumns <span class="en-US">Existing data column information</span>
	 *                      <span class="zh-CN">已存在的数据列信息</span>
	 * @return <span class="en-US">Generated CQL command list</span>
	 * <span class="zh-CN">生成的CQL命令列表</span>
	 */
	List<String> alterTable(final String keyspaceName, final TableDefine tableDefine, List<ColumnDefine> existsColumns) {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.alterTable(DEFAULT_KEYSPACE, tableDefine, existsColumns);
		}
		String tableName = this.nameCase(tableDefine.getTableName());
		List<ColumnDefine> matchedColumns = new ArrayList<>();
		List<String> commandList = new ArrayList<>();
		for (ColumnDefine columnDefine : tableDefine.getColumnDefines()) {
			ColumnDefine existColumn =
					existsColumns.stream()
							.filter(columnInfo ->
									ObjectUtils.nullSafeEquals(this.nameCase(columnDefine.getColumnName()),
											this.nameCase(columnInfo.getColumnName())))
							.findFirst()
							.orElse(null);
			if (existColumn == null) {
				//  Find column name histories
				List<String> nameHistories = new ArrayList<>();
				columnDefine.getNameHistories().forEach(nameHistory -> nameHistories.add(this.nameCase(nameHistory)));
				if (!nameHistories.isEmpty()) {
					existColumn = existsColumns.stream()
							.filter(columnInfo -> nameHistories.contains(this.nameCase(columnInfo.getColumnName())))
							.findFirst()
							.map(columnInfo -> {
								commandList.add("ALTER TABLE " + keyspaceName + BrainCommons.DEFAULT_NAME_SPLIT
										+ tableName + " RENAME " + this.nameCase(columnInfo.getColumnName())
										+ " TO " + this.nameCase(columnDefine.getColumnName()));
								return columnInfo;
							})
							.orElse(null);
				}
			}
			if (existColumn == null) {
				commandList.add("ALTER TABLE " + keyspaceName + "." + tableName + " ADD "
						+ this.nameCase(columnDefine.getColumnName()) + BrainCommons.WHITE_SPACE
						+ this.columnType(columnDefine.getJdbcType(), columnDefine.getLength(),
						columnDefine.getPrecision(), columnDefine.getScale()));
			} else {
				String existType = this.columnType(existColumn.getJdbcType(), existColumn.getLength(),
						existColumn.getPrecision(), existColumn.getScale());
				String defineType = this.columnType(columnDefine.getJdbcType(), columnDefine.getLength(),
						columnDefine.getPrecision(), columnDefine.getScale());
				if (!ObjectUtils.nullSafeEquals(existType, defineType)) {
					commandList.add("ALTER TABLE " + keyspaceName + BrainCommons.DEFAULT_NAME_SPLIT + tableName
							+ " ALTER " + this.nameCase(columnDefine.getColumnName()) + " TYPE "
							+ this.columnType(columnDefine.getJdbcType(), columnDefine.getLength(),
							columnDefine.getPrecision(), columnDefine.getScale()));
				}
				matchedColumns.add(existColumn);
			}
		}

		existsColumns.stream()
				.filter(columnInfo -> !matchedColumns.contains(columnInfo))
				.forEach(columnInfo ->
						commandList.add("ALTER TABLE " + keyspaceName + "." + tableName + " DROP "
								+ this.nameCase(columnInfo.getColumnName())));
		return commandList;
	}

	/**
	 * <h3 class="en-US">Generate data insert commands</h3>
	 * <h3 class="zh-CN">生成数据插入命令</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 * @param tableDefine  <span class="en-US">Data table define</span>
	 *                     <span class="zh-CN">数据表定义</span>
	 * @param dataMap      <span class="en-US">Insert data mapping</span>
	 *                     <span class="zh-CN">写入数据映射表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	GeneratedCommand insertCommand(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                               @NotNull final Map<String, Object> dataMap) {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.insertCommand(DEFAULT_KEYSPACE, tableDefine, dataMap);
		}
		StringBuilder commandBuilder = new StringBuilder("INSERT INTO ")
				.append(this.nameCase(keyspaceName))
				.append(BrainCommons.DEFAULT_NAME_SPLIT)
				.append(this.nameCase(tableDefine.getTableName()));

		List<Object> parameters = new ArrayList<>();
		StringBuilder columnBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();
		for (ColumnDefine columnDefine : tableDefine.getColumnDefines()) {
			if (dataMap.containsKey(columnDefine.getColumnName())) {
				columnBuilder.append(BrainCommons.DEFAULT_SPLIT_CHARACTER)
						.append(this.nameCase(columnDefine.getColumnName()));
				valueBuilder.append(BrainCommons.DEFAULT_SPLIT_CHARACTER).append(BrainCommons.DEFAULT_PLACE_HOLDER);
				parameters.add(this.convertValue(columnDefine.getJdbcType(), dataMap.get(columnDefine.getColumnName())));
			}
		}

		if (columnBuilder.length() == 0) {
			return null;
		}
		commandBuilder.append(BrainCommons.BRACKETS_BEGIN)
				.append(columnBuilder.substring(BrainCommons.DEFAULT_SPLIT_CHARACTER.length()))
				.append(BrainCommons.BRACKETS_END)
				.append(" VALUES ")
				.append(BrainCommons.BRACKETS_BEGIN)
				.append(valueBuilder.substring(BrainCommons.DEFAULT_SPLIT_CHARACTER.length()))
				.append(BrainCommons.BRACKETS_END);
		return new GeneratedCommand(commandBuilder.toString(), parameters);
	}

	/**
	 * <h3 class="en-US">Checks whether the given data column already exists</h3>
	 * <h3 class="zh-CN">检查给定的数据列是否已存在</h3>
	 *
	 * @param columnName <span class="en-US">Data column name</span>
	 *                   <span class="zh-CN">数据列名称</span>
	 * @param jdbcType   <span class="en-US">JDBC data type code</span>
	 *                   <span class="zh-CN">JDBC类型代码</span>
	 * @param dataMap    <span class="en-US">Update data mapping</span>
	 *                   <span class="zh-CN">更新数据映射表</span>
	 * @param parameters <span class="en-US">Parameter value list</span>
	 *                   <span class="zh-CN">参数值列表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	private String matchCommand(final String columnName, final int jdbcType,
	                            final Map<String, Object> dataMap, final List<Object> parameters) {
		if (dataMap.containsKey(columnName)) {
			parameters.add(this.convertValue(jdbcType, dataMap.get(columnName)));
			return this.nameCase(columnName) + BrainCommons.OPERATOR_EQUAL + BrainCommons.DEFAULT_PLACE_HOLDER;
		}
		return Globals.DEFAULT_VALUE_STRING;
	}

	/**
	 * <h3 class="en-US">Generate data update commands</h3>
	 * <h3 class="zh-CN">生成数据更新命令</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 * @param tableDefine  <span class="en-US">Data table define</span>
	 *                     <span class="zh-CN">数据表定义</span>
	 * @param dataMap      <span class="en-US">Update data mapping</span>
	 *                     <span class="zh-CN">更新数据映射表</span>
	 * @param filterMap    <span class="en-US">Update filter mapping</span>
	 *                     <span class="zh-CN">更新条件映射表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	GeneratedCommand updateCommand(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                               @NotNull final Map<String, Object> dataMap, @NotNull final Map<String, Object> filterMap) {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.updateCommand(DEFAULT_KEYSPACE, tableDefine, dataMap, filterMap);
		}
		StringBuilder commandBuilder = new StringBuilder("UPDATE ")
				.append(this.nameCase(keyspaceName))
				.append(BrainCommons.DEFAULT_NAME_SPLIT)
				.append(this.nameCase(tableDefine.getTableName()));

		List<Object> parameters = new ArrayList<>();
		StringBuilder columnBuilder = new StringBuilder();
		for (ColumnDefine columnDefine : tableDefine.getColumnDefines()) {
			String setCommand = this.matchCommand(columnDefine.getColumnName(),
					columnDefine.getJdbcType(), dataMap, parameters);
			if (StringUtils.notBlank(setCommand)) {
				columnBuilder.append(BrainCommons.DEFAULT_SPLIT_CHARACTER).append(setCommand);
			}
		}
		if (columnBuilder.length() == 0) {
			return null;
		}
		commandBuilder.append(" SET ")
				.append(columnBuilder.substring(BrainCommons.DEFAULT_SPLIT_CHARACTER.length()));

		StringBuilder filterBuilder = new StringBuilder();
		for (ColumnDefine columnDefine : tableDefine.getColumnDefines()) {
			String whereCommand = this.matchCommand(columnDefine.getColumnName(),
					columnDefine.getJdbcType(), filterMap, parameters);
			if (StringUtils.notBlank(whereCommand)) {
				filterBuilder.append(ConnectionCode.AND).append(BrainCommons.WHITE_SPACE).append(whereCommand);
			}
		}
		if (filterBuilder.length() > 0) {
			commandBuilder.append(" WHERE ")
					.append(filterBuilder.substring(ConnectionCode.AND.toString().length()));
		}
		return new GeneratedCommand(commandBuilder.toString(), parameters);
	}

	/**
	 * <h3 class="en-US">Generate data delete commands</h3>
	 * <h3 class="zh-CN">生成数据删除命令</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 * @param tableDefine  <span class="en-US">Data table define</span>
	 *                     <span class="zh-CN">数据表定义</span>
	 * @param filterMap    <span class="en-US">Update filter mapping</span>
	 *                     <span class="zh-CN">更新条件映射表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	GeneratedCommand deleteCommand(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                               @NotNull final Map<String, Object> filterMap) {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.deleteCommand(DEFAULT_KEYSPACE, tableDefine, filterMap);
		}
		StringBuilder commandBuilder = new StringBuilder("DELETE FROM ")
				.append(this.nameCase(keyspaceName))
				.append(BrainCommons.DEFAULT_NAME_SPLIT)
				.append(this.nameCase(tableDefine.getTableName()));

		List<Object> parameters = new ArrayList<>();
		StringBuilder filterBuilder = new StringBuilder();
		for (ColumnDefine columnDefine : tableDefine.getColumnDefines()) {
			String columnName = columnDefine.getColumnName();
			if (filterMap.containsKey(columnName) && columnDefine.isPrimaryKey()) {
				filterBuilder.append(ConnectionCode.AND)
						.append(BrainCommons.WHITE_SPACE)
						.append(this.nameCase(columnName))
						.append(BrainCommons.OPERATOR_EQUAL)
						.append(BrainCommons.DEFAULT_PLACE_HOLDER);
				parameters.add(this.convertValue(columnDefine.getJdbcType(), filterMap.get(columnName)));
			}
		}

		if (filterBuilder.length() == 0) {
			return null;
		}
		commandBuilder.append(" WHERE ")
				.append(filterBuilder.substring(ConnectionCode.AND.toString().length()))
				.append(" IF EXISTS");
		return new GeneratedCommand(commandBuilder.toString(), parameters);

	}

	/**
	 * <h3 class="en-US">Generate data query commands</h3>
	 * <h3 class="zh-CN">生成数据查询命令</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 * @param tableDefine  <span class="en-US">Data table define</span>
	 *                     <span class="zh-CN">数据表定义</span>
	 * @param columns      <span class="en-US">Query column names</span>
	 *                     <span class="zh-CN">查询数据列名</span>
	 * @param filterMap    <span class="en-US">Update filter mapping</span>
	 *                     <span class="zh-CN">更新条件映射表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	GeneratedCommand queryCommand(@Nonnull final String keyspaceName, @NotNull final TableDefine tableDefine,
	                              final String columns, @NotNull final Map<String, Object> filterMap) {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.queryCommand(DEFAULT_KEYSPACE, tableDefine, columns, filterMap);
		}
		StringBuilder commandBuilder = new StringBuilder("SELECT ");
		if (StringUtils.isEmpty(columns)) {
			StringBuilder columnBuilder = new StringBuilder();
			for (ColumnDefine columnDefine : tableDefine.getColumnDefines()) {
				if (!columnDefine.isLazyLoad()) {
					columnBuilder.append(BrainCommons.DEFAULT_SPLIT_CHARACTER)
							.append(this.nameCase(columnDefine.getColumnName()));
				}
			}
			if (columnBuilder.length() > 0) {
				commandBuilder.append(columnBuilder.substring(BrainCommons.DEFAULT_SPLIT_CHARACTER.length()));
			}
		} else {
			commandBuilder.append(columns);
		}

		commandBuilder.append(" FROM ")
				.append(this.nameCase(keyspaceName))
				.append(BrainCommons.DEFAULT_NAME_SPLIT)
				.append(this.nameCase(tableDefine.getTableName()));

		List<Object> parameters = new ArrayList<>();
		StringBuilder filterBuilder = new StringBuilder();
		for (ColumnDefine columnDefine : tableDefine.getColumnDefines()) {
			String columnName = columnDefine.getColumnName();
			if (filterMap.containsKey(columnName)) {
				filterBuilder.append(ConnectionCode.AND)
						.append(BrainCommons.WHITE_SPACE)
						.append(this.nameCase(columnName))
						.append(BrainCommons.OPERATOR_EQUAL)
						.append(BrainCommons.DEFAULT_PLACE_HOLDER);
				parameters.add(this.convertValue(columnDefine.getJdbcType(), filterMap.get(columnName)));
			}
		}

		if (filterBuilder.length() > 0) {
			commandBuilder.append(" WHERE ").append(filterBuilder.substring(ConnectionCode.AND.toString().length()));
		}
		return new GeneratedCommand(commandBuilder.toString(), parameters);
	}

	/**
	 * <h3 class="en-US">Generate data query commands</h3>
	 * <h3 class="zh-CN">生成数据查询命令</h3>
	 *
	 * @param keyspaceName <span class="en-US">Keyspace name</span>
	 *                     <span class="zh-CN">键空间名称</span>
	 * @param queryInfo    <span class="en-US">Query record information</span>
	 *                     <span class="zh-CN">数据检索信息</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 * @throws SQLException <span class="en-US">An error occurred while generating the CQL command</span>
	 *                      <span class="zh-CN">生成CQL命令时出现错误</span>
	 */
	GeneratedCommand queryCommand(@Nonnull final String keyspaceName, @Nonnull final QueryInfo queryInfo)
			throws SQLException {
		return this.queryCommand(keyspaceName, queryInfo.getTableName(), queryInfo.getItemList(),
				queryInfo.getConditionList(), queryInfo.getOrderByList(),
				queryInfo.getPageNo(), queryInfo.getPageLimit());
	}

	/**
	 * <h3 class="en-US">Generate data query commands</h3>
	 * <h3 class="zh-CN">生成数据查询命令</h3>
	 *
	 * @param keyspaceName  <span class="en-US">Keyspace name</span>
	 *                      <span class="zh-CN">键空间名称</span>
	 * @param tableName     <span class="en-US">Data table name</span>
	 *                      <span class="zh-CN">数据表名称</span>
	 * @param queryItem     <span class="en-US">Query item instance object</span>
	 *                      <span class="zh-CN">查询项实例对象</span>
	 * @param conditionList <span class="en-US">Query matching condition list</span>
	 *                      <span class="zh-CN">查询匹配条件列表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 * @throws SQLException <span class="en-US">An error occurred while generating the CQL command</span>
	 *                      <span class="zh-CN">生成CQL命令时出现错误</span>
	 */
	GeneratedCommand queryCommand(@Nonnull final String keyspaceName, @Nonnull final String tableName,
	                              final AbstractItem queryItem, final List<Condition> conditionList)
			throws SQLException {
		return this.queryCommand(keyspaceName, tableName, List.of(queryItem), conditionList);
	}

	/**
	 * <h3 class="en-US">Generate data query commands</h3>
	 * <h3 class="zh-CN">生成数据查询命令</h3>
	 *
	 * @param keyspaceName  <span class="en-US">Keyspace name</span>
	 *                      <span class="zh-CN">键空间名称</span>
	 * @param tableName     <span class="en-US">Data table name</span>
	 *                      <span class="zh-CN">数据表名称</span>
	 * @param queryItems    <span class="en-US">Query item instance object list</span>
	 *                      <span class="zh-CN">查询项实例对象列表</span>
	 * @param conditionList <span class="en-US">Query matching condition list</span>
	 *                      <span class="zh-CN">查询匹配条件列表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 * @throws SQLException <span class="en-US">An error occurred while generating the CQL command</span>
	 *                      <span class="zh-CN">生成CQL命令时出现错误</span>
	 */
	GeneratedCommand queryCommand(@Nonnull final String keyspaceName, @Nonnull final String tableName,
	                              final List<AbstractItem> queryItems, final List<Condition> conditionList)
			throws SQLException {
		return this.queryCommand(keyspaceName, tableName, queryItems, conditionList, Collections.emptyList(),
				Globals.DEFAULT_VALUE_INT, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * <h3 class="en-US">Generate data query commands</h3>
	 * <h3 class="zh-CN">生成数据查询命令</h3>
	 *
	 * @param keyspaceName  <span class="en-US">Keyspace name</span>
	 *                      <span class="zh-CN">键空间名称</span>
	 * @param tableName     <span class="en-US">Data table name</span>
	 *                      <span class="zh-CN">数据表名称</span>
	 * @param queryItems    <span class="en-US">Query item instance object list</span>
	 *                      <span class="zh-CN">查询项实例对象列表</span>
	 * @param conditionList <span class="en-US">Query matching condition list</span>
	 *                      <span class="zh-CN">查询匹配条件列表</span>
	 * @param orderByList   <span class="en-US">Query order by column list</span>
	 *                      <span class="zh-CN">查询排序数据列列表</span>
	 * @param pageNo        <span class="en-US">Current page number</span>
	 *                      <span class="zh-CN">当前页数</span>
	 * @param pageLimit     <span class="en-US">Page limit records count</span>
	 *                      <span class="zh-CN">每页的记录数</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 * @throws SQLException <span class="en-US">An error occurred while generating the CQL command</span>
	 *                      <span class="zh-CN">生成CQL命令时出现错误</span>
	 */
	GeneratedCommand queryCommand(@Nonnull final String keyspaceName, @Nonnull final String tableName,
	                              final List<AbstractItem> queryItems, final List<Condition> conditionList,
	                              final List<OrderBy> orderByList, final int pageNo, final int pageLimit)
			throws SQLException {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.queryCommand(DEFAULT_KEYSPACE, tableName, queryItems, conditionList, orderByList,
					pageNo, pageLimit);
		}
		List<Object> values = new ArrayList<>();
		StringBuilder itemBuilder = new StringBuilder();
		for (AbstractItem abstractItem : queryItems) {
			Optional.of(this.queryItem(abstractItem, values))
					.filter(StringUtils::notBlank)
					.ifPresent(item -> itemBuilder.append(BrainCommons.DEFAULT_SPLIT_CHARACTER).append(item));
		}

		if (itemBuilder.length() == 0) {
			throw new MultilingualSQLException(0x00DB00000011L);
		}
		StringBuilder commandBuilder = new StringBuilder("SELECT ")
				.append(itemBuilder.substring(BrainCommons.DEFAULT_SPLIT_CHARACTER.length()))
				.append(" FROM ")
				.append(this.nameCase(keyspaceName))
				.append(BrainCommons.DEFAULT_NAME_SPLIT)
				.append(this.nameCase(tableName));

		String whereClause = this.whereClause(tableName, conditionList, values);
		if (StringUtils.isEmpty(whereClause)) {
			if (this.logger.isDebugEnabled()) {
				this.logger.warn("Query_Condition_Empty");
			}
		} else {
			commandBuilder.append(" WHERE ").append(whereClause);
		}

		String orderBy = this.orderBy(orderByList);
		if (StringUtils.notBlank(orderBy)) {
			commandBuilder.append(" ORDER BY ").append(orderBy);
		}

		int limitValue = this.limitValue(pageNo, pageLimit);
		if (limitValue > Globals.INITIALIZE_INT_VALUE) {
			commandBuilder.append(" LIMIT ").append(limitValue);
		}
		commandBuilder.append(" ALLOW FILTERING");

		return new GeneratedCommand(commandBuilder.toString(), values);
	}

	/**
	 * <h3 class="en-US">Generate data query commands</h3>
	 * <h3 class="zh-CN">生成数据查询命令</h3>
	 *
	 * @param keyspaceName  <span class="en-US">Keyspace name</span>
	 *                      <span class="zh-CN">键空间名称</span>
	 * @param tableName     <span class="en-US">Data table name</span>
	 *                      <span class="zh-CN">数据表名称</span>
	 * @param conditionList <span class="en-US">Query matching condition list</span>
	 *                      <span class="zh-CN">查询匹配条件列表</span>
	 * @param totalCount    <span class="en-US">Command to query the total number of records</span>
	 *                      <span class="zh-CN">用于查询总记录数的命令</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 * @throws SQLException <span class="en-US">An error occurred while generating the CQL command</span>
	 *                      <span class="zh-CN">生成CQL命令时出现错误</span>
	 */
	GeneratedCommand queryCommand(@Nonnull final String keyspaceName, @Nonnull final String tableName,
	                              final List<Condition> conditionList, final boolean totalCount) throws SQLException {
		if (StringUtils.isEmpty(keyspaceName)) {
			return this.queryCommand(DEFAULT_KEYSPACE, tableName, conditionList, totalCount);
		}
		StringBuilder commandBuilder = new StringBuilder("SELECT ")
				.append(totalCount ? " COUNT(1) AS COUNT " : " * ")
				.append(" FROM ")
				.append(this.nameCase(keyspaceName))
				.append(BrainCommons.DEFAULT_NAME_SPLIT)
				.append(this.nameCase(tableName));

		List<Object> values = new ArrayList<>();
		String whereClause = this.whereClause(tableName, conditionList, values);
		if (StringUtils.isEmpty(whereClause)) {
			if (this.logger.isDebugEnabled()) {
				this.logger.warn("Query_Condition_Empty");
			}
		} else {
			commandBuilder.append(" WHERE ").append(whereClause);
		}
		commandBuilder.append(" ALLOW FILTERING");

		return new GeneratedCommand(commandBuilder.toString(), values);
	}

	/**
	 * <h3 class="en-US">Get the number of paging end records</h3>
	 * <h3 class="zh-CN">获取分页结束记录数</h3>
	 *
	 * @param pageNo    <span class="en-US">Current page number</span>
	 *                  <span class="zh-CN">当前页数</span>
	 * @param pageLimit <span class="en-US">Page limit records count</span>
	 *                  <span class="zh-CN">每页的记录数</span>
	 * @return <span class="en-US">Number of records at the end of paging</span>
	 * <span class="zh-CN">分页结束记录数</span>
	 */
	private int limitValue(final int pageNo, final int pageLimit) {
		if (pageNo > Globals.INITIALIZE_INT_VALUE) {
			return (pageLimit > Globals.INITIALIZE_INT_VALUE)
					? (pageNo + 1) * pageLimit
					: (pageNo + 1) * BrainCommons.DEFAULT_PAGE_LIMIT;
		}
		return Globals.DEFAULT_VALUE_INT;
	}

	/**
	 * <h3 class="en-US">CQL command to generate query items</h3>
	 * <h3 class="zh-CN">生成查询项的CQL命令</h3>
	 *
	 * @param abstractItem <span class="en-US">Query item instance object</span>
	 *                     <span class="zh-CN">查询项实例对象</span>
	 * @param values       <span class="en-US">Parameter value list</span>
	 *                     <span class="zh-CN">参数值列表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 * @throws SQLException <span class="en-US">An error occurred while generating the CQL command</span>
	 *                      <span class="zh-CN">生成CQL命令时出现错误</span>
	 */
	String queryItem(final AbstractItem abstractItem, final List<Object> values) throws SQLException {
		StringBuilder stringBuilder = new StringBuilder();
		switch (abstractItem.getItemType()) {
			case FUNCTION:
				FunctionItem functionItem = abstractItem.unwrap(FunctionItem.class);
				for (AbstractParameter<?> functionParameter : functionItem.getFunctionParams()) {
					String parameter = this.parameterValue(Map.of(), functionParameter, values);
					if (StringUtils.notBlank(parameter)) {
						stringBuilder.append(BrainCommons.DEFAULT_SPLIT_CHARACTER);
						stringBuilder.append(parameter);
					}
				}
				stringBuilder.append(BrainCommons.BRACKETS_END);
				stringBuilder.insert(Globals.INITIALIZE_INT_VALUE, BrainCommons.BRACKETS_BEGIN);
				stringBuilder.insert(Globals.INITIALIZE_INT_VALUE, functionItem.getFunctionName());
				break;
			case COLUMN:
				ColumnItem columnItem = abstractItem.unwrap(ColumnItem.class);
				stringBuilder.append(this.nameCase(columnItem.getColumnName()));
				if (StringUtils.notBlank(abstractItem.getAliasName())) {
					stringBuilder.append(" AS ")
							.append(BrainCommons.WHITE_SPACE)
							.append(abstractItem.getAliasName())
							.append(BrainCommons.WHITE_SPACE);
				}
				break;
			default:
				throw new MultilingualSQLException(0x00DB00000016L, abstractItem.getItemType());
		}
		return stringBuilder.toString();
	}

	@Override
	protected String parameterValue(final Map<String, String> aliasMap, final AbstractParameter<?> abstractParameter,
	                              final List<Object> values) throws SQLException {
		StringBuilder sqlBuilder = new StringBuilder();
		switch (abstractParameter.getItemType()) {
			case COLUMN:
				ColumnItem columnItem = abstractParameter.unwrap(ColumnParameter.class).getItemValue();
				sqlBuilder.append(this.queryItem(columnItem, values));
				break;
			case ARRAY:
				ArrayData arrayData = abstractParameter.unwrap(ArraysParameter.class).getItemValue();
				if (arrayData.getArrayObject().length == 0) {
					throw new MultilingualSQLException(0x00DB00000017L);
				}
				if (arrayData.getArrayObject().length == 1) {
					throw new MultilingualSQLException(0x00DB00000018L);
				}
				for (Object object : arrayData.getArrayObject()) {
					if (sqlBuilder.length() > 0) {
						sqlBuilder.append(BrainCommons.DEFAULT_SPLIT_CHARACTER);
					}
					sqlBuilder.append(BrainCommons.DEFAULT_PLACE_HOLDER);
					values.add(object);
				}
				sqlBuilder.insert(Globals.INITIALIZE_INT_VALUE, BrainCommons.BRACKETS_BEGIN);
				sqlBuilder.append(BrainCommons.BRACKETS_END);
				break;
			case RANGE:
				RangesData rangesData = abstractParameter.unwrap(RangesParameter.class).getItemValue();
				if (rangesData == null) {
					throw new MultilingualSQLException(0x00DB00000019L);
				}
				values.add(rangesData.getBeginValue());
				values.add(rangesData.getEndValue());
				break;
			case CONSTANT:
				ConstantParameter constantParameter = abstractParameter.unwrap(ConstantParameter.class);
				sqlBuilder.append(BrainCommons.DEFAULT_PLACE_HOLDER);
				values.add(constantParameter.getItemValue());
				break;
			case FUNCTION:
				FunctionItem functionItem = abstractParameter.unwrap(FunctionParameter.class).getItemValue();
				sqlBuilder.append(this.queryItem(functionItem, values));
				break;
		}
		return sqlBuilder.toString();
	}

	/**
	 * <h3 class="en-US">Generate where commands to data filter</h3>
	 * <h3 class="zh-CN">生成数据的WHERE命令</h3>
	 *
	 * @param conditionList <span class="en-US">Query matching condition list</span>
	 *                      <span class="zh-CN">查询匹配条件列表</span>
	 * @param values        <span class="en-US">Parameter value list</span>
	 *                      <span class="zh-CN">参数值列表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 * @throws SQLException <span class="en-US">An error occurred while generating the CQL command</span>
	 *                      <span class="zh-CN">生成CQL命令时出现错误</span>
	 */
	private String whereClause(final String tableName, final List<Condition> conditionList, final List<Object> values)
			throws SQLException {
		StringBuilder sqlBuilder = new StringBuilder();
		conditionList.sort(SortedItem.desc());
		for (Condition condition : conditionList) {
			if (sqlBuilder.length() > Globals.INITIALIZE_INT_VALUE) {
				sqlBuilder.append(BrainCommons.WHITE_SPACE)
						.append(condition.getConnectionCode().toString())
						.append(BrainCommons.WHITE_SPACE);
			}
			switch (condition.getConditionType()) {
				case COLUMN:
					ColumnCondition columnCondition = condition.unwrap(ColumnCondition.class);
					if (columnCondition.getTableName().equalsIgnoreCase(tableName)) {
						sqlBuilder.append(this.columnCondition(Map.of(), columnCondition, values));
					}
					break;
				case GROUP:
					GroupCondition groupCondition = condition.unwrap(GroupCondition.class);
					String groupWhereClause = this.whereClause(tableName, groupCondition.getConditionList(), values);
					if (StringUtils.notBlank(groupWhereClause)) {
						sqlBuilder.append(BrainCommons.BRACKETS_BEGIN)
								.append(groupWhereClause)
								.append(BrainCommons.BRACKETS_END);
					}
					break;
				default:
					throw new MultilingualSQLException(0x00DB00000014L, condition.getConditionType());
			}
		}
		return sqlBuilder.toString();
	}

	/**
	 * <h3 class="en-US">Generate order by commands</h3>
	 * <h3 class="zh-CN">生成排序命令</h3>
	 *
	 * @param orderByList <span class="en-US">Sort data column definition list</span>
	 *                    <span class="zh-CN">排序数据列定义列表</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	private String orderBy(final List<OrderBy> orderByList) {
		if (orderByList == null) {
			return Globals.DEFAULT_VALUE_STRING;
		}
		StringBuilder sqlBuilder = new StringBuilder();
		if (!orderByList.isEmpty()) {
			for (OrderBy orderBy : orderByList) {
				sqlBuilder.append(BrainCommons.DEFAULT_SPLIT_CHARACTER)
						.append(this.nameCase(orderBy.getColumnName()))
						.append(BrainCommons.WHITE_SPACE)
						.append(orderBy.getOrderType().toString());
			}
			sqlBuilder.append(BrainCommons.BRACKETS_END);
			sqlBuilder.insert(Globals.INITIALIZE_INT_VALUE, BrainCommons.BRACKETS_BEGIN);
		}
		return sqlBuilder.length() > 0
				? sqlBuilder.substring(BrainCommons.DEFAULT_SPLIT_CHARACTER.length())
				: Globals.DEFAULT_VALUE_STRING;
	}

	/**
	 * <h3 class="en-US">Generate truncate table commands</h3>
	 * <h3 class="zh-CN">生成清空数据表命令</h3>
	 *
	 * @param tableName <span class="en-US">Data table name</span>
	 *                  <span class="zh-CN">数据表名称</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	GeneratedCommand truncateTable(final String tableName) {
		return new GeneratedCommand("TRUNCATE TABLE " + tableName, Collections.emptyList());
	}

	/**
	 * <h3 class="en-US">Generate drop table commands</h3>
	 * <h3 class="zh-CN">生成删除数据表命令</h3>
	 *
	 * @param tableName <span class="en-US">Data table name</span>
	 *                  <span class="zh-CN">数据表名称</span>
	 * @return <span class="en-US">Generated CQL command</span>
	 * <span class="zh-CN">生成的CQL命令</span>
	 */
	GeneratedCommand dropTable(final String tableName) {
		return new GeneratedCommand("DROP TABLE IF EXISTS " + tableName, Collections.emptyList());
	}

	/**
	 * <h3 class="en-US">Convert data type</h3>
	 * <h3 class="zh-CN">转换数据类型</h3>
	 *
	 * @param jdbcType <span class="en-US">JDBC data type code</span>
	 *                 <span class="zh-CN">JDBC类型代码</span>
	 * @param object   <span class="en-US">Original data</span>
	 *                 <span class="zh-CN">原始数据</span>
	 * @return <span class="en-US">Converted data</span>
	 * <span class="zh-CN">转换后的数据</span>
	 */
	private Object convertValue(final int jdbcType, final Object object) {
		switch (jdbcType) {
			case Types.BLOB:
			case Types.VARBINARY:
			case Types.LONGNVARCHAR:
				return ByteBuffer.wrap((byte[]) object);
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				return ((Date) object).toInstant();
			case Types.SMALLINT:
				return ((Short) object).intValue();
			case Types.REAL:
				return ((Float) object).doubleValue();
			default:
				return object;
		}
	}
}
