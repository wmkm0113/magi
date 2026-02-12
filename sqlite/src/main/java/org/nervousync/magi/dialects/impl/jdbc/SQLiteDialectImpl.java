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
import org.nervousync.commons.Globals;
import org.nervousync.utils.core.ObjectUtils;
import org.nervousync.utils.core.StringUtils;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import java.util.Properties;

/**
 * <h2 class="en-US">SQLite database dialect implementation class</h2>
 * <h2 class="zh-CN">SQLite数据库方言实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2022 13:08:12 $
 */
@Provider(name = "SQLite", titleKey = "SQLite_Dialect_Title", descriptionKey = "SQLite_Dialect_Description")
@SchemaDialect(supportJoin = true,
		types = {
		@DataType(code = Types.VARCHAR, type = "VARCHAR({length})"),
		@DataType(code = Types.NVARCHAR, type = "NVARCHAR({length})"),
		@DataType(code = Types.INTEGER, type = "INTEGER"),
		@DataType(code = Types.SMALLINT, type = "SMALLINT"),
		@DataType(code = Types.BIGINT, type = "BIGINT"),
		@DataType(code = Types.TINYINT, type = "INTEGER"),
		@DataType(code = Types.FLOAT, type = "FLOAT({precision})"),
		@DataType(code = Types.DOUBLE, type = "FLOAT(52)"),
		@DataType(code = Types.NUMERIC, type = "NUMERIC({precision}, {scale})"),
		@DataType(code = Types.REAL, type = "FLOAT(22)"),
		@DataType(code = Types.DECIMAL, type = "DECIMAL({precision}, {scale})"),
		@DataType(code = Types.BIT, type = "INTEGER"),
		@DataType(code = Types.BOOLEAN, type = "INTEGER"),
		@DataType(code = Types.CHAR, type = "CHARACTER({length})"),
		@DataType(code = Types.NCHAR, type = "NCHAR({length})"),
		@DataType(code = Types.DATE, type = "DATE"),
		@DataType(code = Types.TIME, type = "DATETIME"),
		@DataType(code = Types.TIMESTAMP, type = "DATETIME"),
		@DataType(code = Types.BLOB, type = "BLOB"),
		@DataType(code = Types.VARBINARY, type = "BLOB"),
		@DataType(code = Types.LONGVARBINARY, type = "BLOB"),
		@DataType(code = Types.CLOB, type = "CLOB"),
		@DataType(code = Types.NCLOB, type = "CLOB"),
		@DataType(code = Types.LONGNVARCHAR, type = "CLOB")
		})
public final class SQLiteDialectImpl extends JdbcDialect {

	/**
	 * <h3 class="en-US">Constructor method for SQLite database dialect implementation class</h3>
	 * <h3 class="zh-CN">SQLite数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到SchemaDialect注解</span>
	 */
	public SQLiteDialectImpl() throws DialectException {
		super(DialectType.Relational);
	}

	@Override
	public String alterColumn() {
		return Globals.DEFAULT_VALUE_STRING;
	}

	@Override
	protected String currentDate() {
		return " CURRENT_DATE ";
	}

	@Override
	protected String currentTime() {
		return " CURRENT_TIME ";
	}

	@Override
	protected String currentTimestamp() {
		return " CURRENT_TIMESTAMP ";
	}

	@Override
	public String truncateTable(@Nonnull String tableName) {
		return COMMAND_DELETE + this.nameCase(tableName);
	}

	@Override
	protected void trustStoreProperties(@Nonnull final Properties properties, final TrustStore trustStore) {
	}

	@Override
	protected void authProperties(@Nonnull final Properties properties, final Authentication authentication) {
	}

	@Override
	protected String lockWhereClause(final String whereClause, final boolean forUpdate, final LockModeType lockOption) {
		StringBuilder stringBuilder = new StringBuilder(WHERE_COMMAND).append(BrainCommons.CONSTANT_CLAUSE_TRUE);
		if (StringUtils.notBlank(whereClause)) {
			stringBuilder.append(ConnectionCode.AND)
					.append(BrainCommons.BRACKETS_BEGIN)
					.append(whereClause)
					.append(BrainCommons.BRACKETS_END);
		}
		return stringBuilder.toString();
	}

	@Override
	public byte[] readBlob(final ResultSet resultSet, final int columnIndex) {
		try {
			return resultSet.getBytes(columnIndex);
		} catch (Exception e) {
			this.logger.warn("Read_Lob_Error", "BLOB");
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Stack_Message_Error", e);
			}
		}
		return new byte[0];
	}

	@Override
	public String parseDefault(final int jdbcType, final int length, final int precision, final int scale,
	                           final String defaultValue) {
		if (StringUtils.isEmpty(defaultValue)) {
			return Globals.DEFAULT_VALUE_STRING;
		}
		if (jdbcType == Types.INTEGER
				&& ("false".equalsIgnoreCase(defaultValue) || "true".equalsIgnoreCase(defaultValue))) {
			return defaultValue;
		}
		return super.parseDefault(jdbcType, length, precision, scale, defaultValue);
	}

	@Override
	protected String limitCommand(@Nonnull final String sqlCmd, final int offset, final int pageLimit,
	                              @Nonnull final List<Object> values) {
		StringBuilder sqlBuilder = new StringBuilder(sqlCmd).append(" limit ?");
		values.add(pageLimit);
		if (offset > 0) {
			sqlBuilder.append(" offset ?");
			values.add(offset);
		}
		return sqlBuilder.toString();
	}

	@Override
	public String nameCase(final String name) {
		return name.toUpperCase();
	}

	@Override
	protected boolean modifiedDefaultValue(final ColumnDefine existColumn, final ColumnDefine columnDefine) {
		if (columnDefine.getJdbcType() == Types.BOOLEAN) {
			if (existColumn.getJdbcType() == Types.INTEGER) {
				return !ObjectUtils.nullSafeEquals(columnDefine.getDefaultValue(), existColumn.getDefaultValue());
			}
			return Boolean.TRUE;
		}
		return !ObjectUtils.nullSafeEquals(existColumn.getDefaultValue(), columnDefine.getDefaultValue());
	}
}
