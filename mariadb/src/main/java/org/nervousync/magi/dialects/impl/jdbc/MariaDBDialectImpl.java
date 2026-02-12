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
import org.nervousync.annotations.provider.Provider;
import org.nervousync.brain.annotations.dialect.DataType;
import org.nervousync.brain.annotations.dialect.SchemaDialect;
import org.nervousync.brain.commons.BrainCommons;
import org.nervousync.brain.configs.auth.Authentication;
import org.nervousync.brain.configs.auth.impl.TrustStoreAuthentication;
import org.nervousync.brain.configs.secure.TrustStore;
import org.nervousync.brain.defines.ColumnDefine;
import org.nervousync.brain.dialects.jdbc.JdbcDialect;
import org.nervousync.brain.enumerations.auth.AuthType;
import org.nervousync.brain.enumerations.dialect.DialectType;
import org.nervousync.brain.exceptions.dialects.DialectException;
import org.nervousync.commons.Globals;
import org.nervousync.utils.core.StringUtils;

import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * <h2 class="en-US">MariaDB database dialect implementation class</h2>
 * <h2 class="zh-CN">MariaDB数据库方言实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2022 10:12:19 $
 */
@Provider(name = "MariaDB", titleKey = "MariaDB_Dialect_Title", descriptionKey = "MariaDB_Dialect_Description")
@SchemaDialect(supportJoin = true, sharding = true,
		types = {
				@DataType(code = Types.VARCHAR, type = "varchar({length})"),
				@DataType(code = Types.NVARCHAR, type = "varchar({length})"),
				@DataType(code = Types.INTEGER, type = "integer"),
				@DataType(code = Types.SMALLINT, type = "smallint"),
				@DataType(code = Types.BIGINT, type = "bigint"),
				@DataType(code = Types.TINYINT, type = "tinyint"),
				@DataType(code = Types.FLOAT, type = "float({precision}, {scale})"),
				@DataType(code = Types.DOUBLE, type = "real({precision}, {scale})"),
				@DataType(code = Types.NUMERIC, type = "numeric({precision}, {scale})"),
				@DataType(code = Types.REAL, type = "real({precision}, {scale})"),
				@DataType(code = Types.DECIMAL, type = "decimal({precision}, {scale})"),
				@DataType(code = Types.BIT, type = "bit({length})"),
				@DataType(code = Types.BOOLEAN, type = "boolean"),
				@DataType(code = Types.CHAR, type = "char({length})"),
				@DataType(code = Types.NCHAR, type = "char({length})"),
				@DataType(code = Types.DATE, type = "date"),
				@DataType(code = Types.TIME, type = "time"),
				@DataType(code = Types.TIMESTAMP, type = "timestamp"),
				@DataType(code = Types.BLOB, type = "longblob"),
				@DataType(code = Types.VARBINARY, type = "mediumblob"),
				@DataType(code = Types.LONGVARBINARY, type = "longblob"),
				@DataType(code = Types.CLOB, type = "text"),
				@DataType(code = Types.NCLOB, type = "mediumtext"),
				@DataType(code = Types.LONGNVARCHAR, type = "longtext")
		})
public final class MariaDBDialectImpl extends JdbcDialect {

	/**
	 * <h3 class="en-US">Constructor method for MariaDB database dialect implementation class</h3>
	 * <h3 class="zh-CN">MariaDB数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到SchemaDialect注解</span>
	 */
	public MariaDBDialectImpl() throws DialectException {
		super(DialectType.Relational);
	}

	@Override
	public String alterColumn() {
		return " MODIFY COLUMN ";
	}

	@Override
	public String nameCase(final String name) {
		return name;
	}

	@Override
	protected void trustStoreProperties(@Nonnull final Properties properties, final TrustStore trustStore) {
		Optional.ofNullable(trustStore)
				.ifPresent(store -> {
					properties.put("trustCertificateKeyStoreUrl", store.getStorePath());
					properties.put("trustCertificateKeyStorePassword", store.getStorePassword());
				});
	}

	@Override
	protected void authProperties(@Nonnull final Properties properties, final Authentication authentication) {
		Optional.ofNullable(authentication)
				.filter(auth -> AuthType.CERTIFICATE.equals(auth.getAuthType()))
				.map(auth -> (TrustStoreAuthentication) auth)
				.ifPresent(tsAuthentication -> {
					properties.put("clientCertificateKeyStoreUrl", tsAuthentication.getStorePath());
					properties.put("clientCertificateKeyStorePassword", tsAuthentication.getStorePassword());
				});
	}

	@Override
	protected String limitCommand(@Nonnull final String sqlCmd, final int offset, final int pageLimit,
	                                    @Nonnull final List<Object> values) {
		StringBuilder sqlBuilder = new StringBuilder(sqlCmd).append(" limit ");
		if (offset > 0) {
			sqlBuilder.append("?, ");
			values.add(offset);
		}
		sqlBuilder.append("? ");
		values.add(pageLimit);
		return sqlBuilder.toString();
	}

	@Override
	public String parseDefault(final int jdbcType, final int length, final int precision, final int scale,
	                           final String defaultValue) {
		if (StringUtils.isEmpty(defaultValue) || "NULL".equalsIgnoreCase(defaultValue)) {
			return Globals.DEFAULT_VALUE_STRING;
		}

		switch (jdbcType) {
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
				if (defaultValue.indexOf(".") > 0) {
					return defaultValue.substring(0, defaultValue.indexOf(".")) + ".0";
				}
				break;
		}
		return super.parseDefault(jdbcType, length, precision, scale, defaultValue);
	}

	/**
	 * <h3 class="en-US">Generate SQL commands to modify the default value of data column</h3>
	 * <h3 class="zh-CN">生成修改数据列默认值的SQL命令</h3>
	 *
	 * @param tableName    <span class="en-US">Database table name</span>
	 *                     <span class="zh-CN">数据表名</span>
	 * @param columnDefine <span class="en-US">Data column define information</span>
	 *                     <span class="zh-CN">数据列定义信息</span>
	 * @return <span class="en-US">Generated SQL command</span>
	 * <span class="zh-CN">生成的SQL命令</span>
	 */
	@Override
	protected String modifyColumnDefault(final String tableName, final ColumnDefine columnDefine) {
		StringBuilder sqlCommand = new StringBuilder(ALTER_TABLE)
				.append(this.nameCase(tableName))
				.append(this.alterColumn())
				.append(this.nameCase(columnDefine.getColumnName()))
				.append(BrainCommons.WHITE_SPACE)
				.append(this.columnType(columnDefine));
		if (StringUtils.isEmpty(columnDefine.getDefaultValue())) {
			sqlCommand.append(this.columnRemoveDefault());
		} else {
			sqlCommand.append(this.columnSetDefault(columnDefine.getDefaultValue()));
		}
		return sqlCommand.toString();
	}

	/**
	 * <h3 class="en-US">Commands to set column default value</h3>
	 * <h3 class="zh-CN">设置数据列默认值的命令</h3>
	 *
	 * @param defaultValue <span class="en-US">Default value</span>
	 *                     <span class="zh-CN">默认值</span>
	 * @return <span class="en-US">Command string</span>
	 * <span class="zh-CN">命令字符串</span>
	 */
	@Override
	public String columnSetDefault(String defaultValue) {
		if (StringUtils.isEmpty(defaultValue)) {
			return Globals.DEFAULT_VALUE_STRING;
		}
		return " DEFAULT " + defaultValue;
	}
}
