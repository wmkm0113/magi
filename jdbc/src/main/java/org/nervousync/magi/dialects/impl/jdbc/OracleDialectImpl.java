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

import org.nervousync.annotations.provider.Provider;
import org.nervousync.brain.annotations.dialect.DataType;
import org.nervousync.brain.annotations.dialect.SchemaDialect;
import org.nervousync.brain.commons.BrainCommons;
import org.nervousync.brain.configs.auth.Authentication;
import org.nervousync.brain.configs.auth.impl.TrustStoreAuthentication;
import org.nervousync.brain.configs.secure.TrustStore;
import org.nervousync.brain.dialects.jdbc.JdbcDialect;
import org.nervousync.brain.enumerations.auth.AuthType;
import org.nervousync.brain.enumerations.dialect.DialectType;
import org.nervousync.brain.exceptions.dialects.DialectException;
import org.nervousync.commons.Globals;

import java.sql.Types;
import java.util.Optional;
import java.util.Properties;

/**
 * <h2 class="en-US">Oracle database dialect implementation class</h2>
 * <h2 class="zh-CN">甲骨文数据库方言实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2022 10:37:22 $
 */
@Provider(name = "Oracle", titleKey = "Oracle_Dialect_Title", descriptionKey = "Oracle_Dialect_Description")
@SchemaDialect(supportJoin = true,
		types = {
				@DataType(code = Types.VARCHAR, type = "VARCHAR2({length})"),
				@DataType(code = Types.NVARCHAR, type = "VARCHAR2({length})"),
				@DataType(code = Types.INTEGER, type = "NUMBER(10, 0)"),
				@DataType(code = Types.SMALLINT, type = "NUMBER(5, 0)"),
				@DataType(code = Types.BIGINT, type = "NUMBER(18, 0)"),
				@DataType(code = Types.TINYINT, type = "NUMBER(3, 0)"),
				@DataType(code = Types.FLOAT, type = "FLOAT"),
				@DataType(code = Types.DOUBLE, type = "NUMBER"),
				@DataType(code = Types.NUMERIC, type = "NUMBER({precision}, {scale})"),
				@DataType(code = Types.REAL, type = "FLOAT"),
				@DataType(code = Types.DECIMAL, type = "NUMBER({precision}, {scale})"),
				@DataType(code = Types.BIT, type = "NUMBER({length})"),
				@DataType(code = Types.BOOLEAN, type = "NUMBER(1, 0)"),
				@DataType(code = Types.CHAR, type = "CHAR({length})"),
				@DataType(code = Types.NCHAR, type = "CHAR({length})"),
				@DataType(code = Types.DATE, type = "DATE"),
				@DataType(code = Types.TIME, type = "TIMESTAMP"),
				@DataType(code = Types.TIMESTAMP, type = "TIMESTAMP"),
				@DataType(code = Types.BLOB, type = "BLOB"),
				@DataType(code = Types.VARBINARY, type = "BLOB"),
				@DataType(code = Types.LONGVARBINARY, type = "BLOB"),
				@DataType(code = Types.CLOB, type = "CLOB"),
				@DataType(code = Types.NCLOB, type = "CLOB"),
				@DataType(code = Types.LONGNVARCHAR, type = "CLOB")
		})
public final class OracleDialectImpl extends JdbcDialect {

	/**
	 * <h3 class="en-US">Constructor method for Oracle database dialect implementation class</h3>
	 * <h3 class="zh-CN">Oracle数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the org. nervousync. brain. annotations. dialect.SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到org. nervousync. brain. annotations. dialect.SchemaDialect注解</span>
	 */
	public OracleDialectImpl() throws DialectException {
		super(DialectType.Relational);
	}

	@Override
	public String alterColumn() {
		return " MODIFY ";
	}

	@Override
	public String aliasCommand() {
		return BrainCommons.WHITE_SPACE;
	}

	@Override
	public Properties properties(final TrustStore trustStore, final Authentication authentication) {
		Properties properties = super.properties(trustStore, authentication);
		Optional.ofNullable(trustStore)
				.ifPresent(store -> {
					properties.put("javax.net.ssl.trustStore", store.getTrustStorePath());
					properties.put("javax.net.ssl.trustStorePassword", store.getTrustStorePassword());
				});
		Optional.ofNullable(authentication)
				.filter(auth -> AuthType.CERTIFICATE.equals(auth.getAuthType()))
				.map(auth -> (TrustStoreAuthentication) auth)
				.ifPresent(tsAuthentication -> {
					properties.put("javax.net.ssl.keyStore", tsAuthentication.getTrustStorePath());
					properties.put("javax.net.ssl.keyStorePassword", tsAuthentication.getTrustStorePassword());
				});
		return properties;
	}

	@Override
	public String nextVal(String sequenceName) {
		return this.nameCase(sequenceName) + ".NEXTVAL";
	}

	@Override
	public String nameCase(final String name) {
		return name.toUpperCase();
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
	protected boolean modifiedType(final String existType, final String defineType) {
		if ("NUMBER".equalsIgnoreCase(defineType)) {
			return !"NUMBER(0, 0)".equalsIgnoreCase(existType);
		}
		return super.modifiedType(existType, defineType);
	}
}
