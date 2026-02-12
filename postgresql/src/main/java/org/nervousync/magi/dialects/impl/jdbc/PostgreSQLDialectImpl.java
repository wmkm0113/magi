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
import org.nervousync.brain.configs.auth.Authentication;
import org.nervousync.brain.configs.auth.impl.TrustStoreAuthentication;
import org.nervousync.brain.configs.secure.TrustStore;
import org.nervousync.brain.defines.ColumnDefine;
import org.nervousync.brain.dialects.jdbc.JdbcDialect;
import org.nervousync.brain.enumerations.auth.AuthType;
import org.nervousync.brain.enumerations.dialect.DialectType;
import org.nervousync.brain.exceptions.dialects.DialectException;
import org.nervousync.commons.Globals;
import org.nervousync.utils.core.ObjectUtils;
import org.nervousync.utils.core.StringUtils;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * <h2 class="en-US">PostgreSQL database dialect implementation class</h2>
 * <h2 class="zh-CN">PostgreSQL数据库方言实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2022 12:19:46 $
 */
@Provider(name = "PgSQL", titleKey = "PgSQL_Dialect_Title", descriptionKey = "PgSQL_Dialect_Description")
@SchemaDialect(supportJoin = true, sharding = true,
		types = {
				@DataType(code = Types.VARCHAR, type = "character varying({length})"),
				@DataType(code = Types.NVARCHAR, type = "character varying({length})"),
				@DataType(code = Types.INTEGER, type = "integer"),
				@DataType(code = Types.SMALLINT, type = "smallint"),
				@DataType(code = Types.BIGINT, type = "bigint"),
				@DataType(code = Types.TINYINT, type = "smallint"),
				@DataType(code = Types.FLOAT, type = "float"),
				@DataType(code = Types.DOUBLE, type = "double precision"),
				@DataType(code = Types.NUMERIC, type = "numeric({precision}, {scale})"),
				@DataType(code = Types.REAL, type = "real"),
				@DataType(code = Types.DECIMAL, type = "numeric({precision}, {scale})"),
				@DataType(code = Types.BIT, type = "bit"),
				@DataType(code = Types.BOOLEAN, type = "boolean"),
				@DataType(code = Types.CHAR, type = "char({length})"),
				@DataType(code = Types.NCHAR, type = "varchar({length})"),
				@DataType(code = Types.DATE, type = "date"),
				@DataType(code = Types.TIME, type = "time"),
				@DataType(code = Types.TIMESTAMP, type = "timestamp"),
				@DataType(code = Types.BINARY, type = "bytea"),
				@DataType(code = Types.BLOB, type = "bytea"),
				@DataType(code = Types.VARBINARY, type = "bytea"),
				@DataType(code = Types.LONGVARBINARY, type = "bytea"),
				@DataType(code = Types.CLOB, type = "text"),
				@DataType(code = Types.NCLOB, type = "text"),
				@DataType(code = Types.LONGNVARCHAR, type = "text")
		})
public final class PostgreSQLDialectImpl extends JdbcDialect {

	/**
	 * <h3 class="en-US">Constructor method for PostgreSQL database dialect implementation class</h3>
	 * <h3 class="zh-CN">PostgreSQL数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到SchemaDialect注解</span>
	 */
	public PostgreSQLDialectImpl() throws DialectException {
		super(DialectType.Relational);
	}

	@Override
	public String alterType() {
		return " TYPE ";
	}

	@Override
	public String nextVal(final String sequenceName) {
		return "nextval(' " + this.nameCase(sequenceName) + "')";
	}

	@Override
	public String currentDate() {
		return " now() ";
	}

	@Override
	public String currentTime() {
		return " now() ";
	}

	@Override
	public String currentTimestamp() {
		return "now() ";
	}

	@Override
	public String nameCase(final String name) {
		return name.toLowerCase();
	}

	@Override
	public byte[] readBlob(ResultSet resultSet, int columnIndex) {
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
	protected void trustStoreProperties(@Nonnull final Properties properties, final TrustStore trustStore) {
		Optional.ofNullable(trustStore)
				.ifPresent(store -> {
					properties.put("ssl", "true");
					properties.put("javax.net.ssl.trustStore", store.getStorePath());
					properties.put("javax.net.ssl.trustStorePassword", store.getStorePassword());
					properties.put("sslmode", "verify-ca");
				});
	}

	@Override
	protected void authProperties(@Nonnull final Properties properties, final Authentication authentication) {
		Optional.ofNullable(authentication)
				.filter(auth -> AuthType.CERTIFICATE.equals(auth.getAuthType()))
				.map(auth -> (TrustStoreAuthentication) auth)
				.ifPresent(tsAuthentication -> {
					properties.put("sslkey", tsAuthentication.getStorePath());
					properties.put("sslpassword", tsAuthentication.getStorePassword());
					properties.put("sslmode", "verify-full");
				});
	}

	@Override
	protected String limitCommand(@Nonnull final String sqlCmd, final int offset, final int pageLimit,
	                              @Nonnull final List<Object> values) {
		StringBuilder sqlBuilder = new StringBuilder(sqlCmd);
		if (offset > 0) {
			sqlBuilder.append(" offset ?");
			values.add(offset);
		}
		sqlBuilder.append(" limit ?");
		values.add(pageLimit);
		return sqlBuilder.toString();
	}

	@Override
	public String parseDefault(final int jdbcType, final int length, final int precision, final int scale,
	                           final String defaultValue) {
		if (StringUtils.isEmpty(defaultValue)) {
			return Globals.DEFAULT_VALUE_STRING;
		}
		if (defaultValue.contains("::")) {
			String parseValue = defaultValue.substring(0, defaultValue.indexOf("::"));
			return parseValue.substring(1, parseValue.length() - 1);
		}
		return super.parseDefault(jdbcType, length, precision, scale, defaultValue);
	}

	@Override
	protected boolean modifiedType(final String existType, final String defineType) {
		if ("boolean".equalsIgnoreCase(defineType)) {
			return !"bit".equalsIgnoreCase(existType);
		}
		return super.modifiedType(existType, defineType);
	}

	@Override
	protected boolean modifiedDefaultValue(final ColumnDefine existColumn, final ColumnDefine columnDefine) {
		if (existColumn.getJdbcType() == Types.BIT && columnDefine.getJdbcType() == Types.BOOLEAN) {
			return !ObjectUtils.nullSafeEquals(Boolean.valueOf(existColumn.getDefaultValue()),
					Boolean.valueOf(columnDefine.getDefaultValue()));
		}
		return super.modifiedDefaultValue(existColumn, columnDefine);
	}
}
