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

package org.nervousync.magi.dialects.impl.jdbc;

import jakarta.annotation.Nonnull;
import jakarta.persistence.LockModeType;
import org.nervousync.annotations.provider.Provider;
import org.nervousync.brain.annotations.dialect.DataType;
import org.nervousync.brain.annotations.dialect.SchemaDialect;
import org.nervousync.brain.commons.BrainCommons;
import org.nervousync.brain.configs.auth.Authentication;
import org.nervousync.brain.configs.secure.TrustStore;
import org.nervousync.brain.defines.ColumnDefine;
import org.nervousync.brain.dialects.jdbc.JdbcDialect;
import org.nervousync.brain.enumerations.dialect.DialectType;
import org.nervousync.brain.enumerations.query.ConnectionCode;
import org.nervousync.brain.exceptions.dialects.DialectException;
import org.nervousync.brain.query.sort.OrderBy;
import org.nervousync.commons.Globals;
import org.nervousync.utils.core.StringUtils;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <h2 class="en-US">MS SQLServer 2000 database dialect implementation class</h2>
 * <h2 class="zh-CN">微软SQLServer 2000数据库方言实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2022 15:42:06 $
 */
@Provider(name = "MSSQL2K", titleKey = "MSSQL2K_Dialect_Title", descriptionKey = "MSSQL_Dialect_Description")
@SchemaDialect(supportJoin = true,
		types = {
				@DataType(code = Types.VARCHAR, type = "varchar({length})"),
				@DataType(code = Types.NVARCHAR, type = "nvarchar({length})"),
				@DataType(code = Types.INTEGER, type = "integer"),
				@DataType(code = Types.SMALLINT, type = "smallint"),
				@DataType(code = Types.BIGINT, type = "bigint"),
				@DataType(code = Types.TINYINT, type = "tinyint"),
				@DataType(code = Types.FLOAT, type = "float(53)"),
				@DataType(code = Types.DOUBLE, type = "float(24)"),
				@DataType(code = Types.NUMERIC, type = "decimal({precision}, {scale})"),
				@DataType(code = Types.REAL, type = "float(53)"),
				@DataType(code = Types.DECIMAL, type = "decimal({precision}, {scale})"),
				@DataType(code = Types.BIT, type = "bit({length})"),
				@DataType(code = Types.BOOLEAN, type = "bit"),
				@DataType(code = Types.CHAR, type = "char({length})"),
				@DataType(code = Types.NCHAR, type = "nchar({length})"),
				@DataType(code = Types.DATE, type = "date"),
				@DataType(code = Types.TIME, type = "time"),
				@DataType(code = Types.TIMESTAMP, type = "datetime"),
				@DataType(code = Types.BLOB, type = "image"),
				@DataType(code = Types.VARBINARY, type = "image"),
				@DataType(code = Types.LONGVARBINARY, type = "image"),
				@DataType(code = Types.CLOB, type = "text"),
				@DataType(code = Types.NCLOB, type = "ntext"),
				@DataType(code = Types.LONGVARCHAR, type = "text"),
				@DataType(code = Types.LONGNVARCHAR, type = "ntext")
		})
public class SQLServer2000DialectImpl extends JdbcDialect {

	/**
	 * <h3 class="en-US">Constructor method for SQLServer 2000 database dialect implementation class</h3>
	 * <h3 class="zh-CN">SQLServer 2000数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到SchemaDialect注解</span>
	 */
	public SQLServer2000DialectImpl() throws DialectException {
		super(DialectType.Relational);
	}

	@Override
	protected String currentDate() {
		return " GETDATE() ";
	}

	@Override
	protected String currentTime() {
		return " CONVERT(TIME, GETDATE()) ";
	}

	@Override
	protected String currentTimestamp() {
		return " CURRENT_TIMESTAMP ";
	}

	@Override
	protected void trustStoreProperties(@Nonnull final Properties properties, final TrustStore trustStore) {
	}

	@Override
	protected void authProperties(@Nonnull final Properties properties, final Authentication authentication) {
	}

	@Override
	public String nameCase(final String name) {
		return name;
	}

	@Override
	protected String lockWhereClause(final String whereClause, final boolean forUpdate, final LockModeType lockOption) {
		if (StringUtils.isEmpty(whereClause)) {
			return Globals.DEFAULT_VALUE_STRING;
		}
		StringBuilder stringBuilder = new StringBuilder();
		if (forUpdate) {
			switch (lockOption) {
				case WRITE:
				case PESSIMISTIC_WRITE:
					stringBuilder.append(" with(UPDLOCK) ");
					break;
				case READ:
				case PESSIMISTIC_READ:
					stringBuilder.append(" with(HOLDLOCK) ");
					break;
			}
		}
		stringBuilder.append(WHERE_COMMAND)
				.append(BrainCommons.CONSTANT_CLAUSE_TRUE)
				.append(BrainCommons.WHITE_SPACE)
				.append(ConnectionCode.AND)
				.append(BrainCommons.WHITE_SPACE)
				.append(BrainCommons.BRACKETS_BEGIN)
				.append(whereClause)
				.append(BrainCommons.BRACKETS_END);
		return stringBuilder.toString();
	}

	@Override
	public String nextVal(final String sequenceName) {
		return Globals.DEFAULT_VALUE_STRING;
	}

	@Override
	public String defaultValue(final ColumnDefine columnDefine, final Object object) {
		if (object == null) {
			return Globals.DEFAULT_VALUE_STRING;
		}
		if (columnDefine.getJdbcType() == Types.BOOLEAN) {
			return ((Boolean) object) ? "'1'" : "'0'";
		}
		return super.defaultValue(columnDefine, object);
	}

	@Override
	protected String limitCommand(@Nonnull final String sqlCmd, final int offset, final int pageLimit,
	                              @Nonnull final List<Object> values) {
		StringBuilder sqlBuilder = new StringBuilder(sqlCmd);
		if (offset > 0) {
			sqlBuilder.append(" OFFSET ? ROWS");
			values.add(offset);
		}
		sqlBuilder.append(" FETCH NEXT ? ROWS ONLY");
		values.add(pageLimit);
		return sqlBuilder.toString();
	}

	@Override
	public String parseDefault(final int jdbcType, final int length, final int precision, final int scale,
	                           final String defaultValue) {
		if (StringUtils.isEmpty(defaultValue)) {
			return Globals.DEFAULT_VALUE_STRING;
		}
		if (defaultValue.startsWith("(")) {
			String parseValue = defaultValue;
			while (parseValue.startsWith("(")) {
				parseValue = parseValue.substring(1);
			}
			while (parseValue.endsWith(")")) {
				parseValue = parseValue.substring(0, parseValue.length() - 1);
			}
			return parseValue;
		}
		return super.parseDefault(jdbcType, length, precision, scale, defaultValue);
	}

	@Override
	protected final String modifyColumnDefault(final String tableName, final ColumnDefine columnDefine) {
		if (StringUtils.notBlank(columnDefine.getDefaultValue())) {
			return ALTER_TABLE + this.nameCase(tableName)
					+ " ADD DEFAULT " + columnDefine.getDefaultValue()
					+ " FOR " + this.nameCase(columnDefine.getColumnName());
		}
		return Globals.DEFAULT_VALUE_STRING;
	}
	/**
	 * <h3 class="en-US">Generate order by commands</h3>
	 * <h3 class="zh-CN">生成排序命令</h3>
	 *
	 * @param aliasMap    <span class="en-US">Data table alias mapping table</span>
	 *                    <span class="zh-CN">数据表别名映射表</span>
	 * @param orderByList <span class="en-US">Sort data column definition list</span>
	 *                    <span class="zh-CN">排序数据列定义列表</span>
	 * @return <span class="en-US">Generated SQL command</span>
	 * <span class="zh-CN">生成的SQL命令</span>
	 */
	@Override
	protected String orderBy(final Map<String, String> aliasMap, final List<OrderBy> orderByList) {
		String sqlCmd = super.orderBy(aliasMap, orderByList);
		if (StringUtils.isEmpty(sqlCmd)) {
			return BrainCommons.BRACKETS_BEGIN + "SELECT NULL" + BrainCommons.BRACKETS_END;
		}
		return sqlCmd;
	}
}
