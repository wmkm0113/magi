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

import jakarta.persistence.LockModeType;
import org.nervousync.annotations.provider.Provider;
import org.nervousync.brain.annotations.dialect.DataType;
import org.nervousync.brain.annotations.dialect.SchemaDialect;
import org.nervousync.brain.commons.BrainCommons;
import org.nervousync.brain.dialects.jdbc.JdbcDialect;
import org.nervousync.brain.enumerations.dialect.DialectType;
import org.nervousync.brain.exceptions.dialects.DialectException;
import org.nervousync.commons.Globals;
import org.nervousync.utils.StringUtils;

import java.sql.Types;

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
	 * @throws DialectException <span class="en-US">If the implementation class does not find the org. nervousync. brain. annotations. dialect.SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到org. nervousync. brain. annotations. dialect.SchemaDialect注解</span>
	 */
	public SQLServer2000DialectImpl() throws DialectException {
		super(DialectType.Relational);
	}

	@Override
	public String nameCase(final String name) {
		return name;
	}

	@Override
	protected String lockWhereClause(final String whereClause, final LockModeType lockOption) {
		if (StringUtils.isEmpty(whereClause)) {
			return Globals.DEFAULT_VALUE_STRING;
		}
		switch (lockOption) {
			case WRITE:
			case PESSIMISTIC_WRITE:
				return " with(UPDLOCK) " + WHERE_COMMAND + BrainCommons.DEFAULT_WHERE_CLAUSE + whereClause;
			case READ:
			case PESSIMISTIC_READ:
				return " with(HOLDLOCK) " + WHERE_COMMAND + BrainCommons.DEFAULT_WHERE_CLAUSE + whereClause;
			default:
				return WHERE_COMMAND + BrainCommons.DEFAULT_WHERE_CLAUSE + whereClause;
		}
	}

	@Override
	public String nextVal(final String sequenceName) {
		return Globals.DEFAULT_VALUE_STRING;
	}

	@Override
	public String defaultValue(final int jdbcType, final int length, final int precision, final int scale,
	                           final Object object) {
		if (object == null) {
			return Globals.DEFAULT_VALUE_STRING;
		}
		if (jdbcType == Types.BOOLEAN) {
			return ((Boolean) object) ? "'1'" : "'0'";
		}
		return super.defaultValue(jdbcType, length, precision, scale, object);
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
}
