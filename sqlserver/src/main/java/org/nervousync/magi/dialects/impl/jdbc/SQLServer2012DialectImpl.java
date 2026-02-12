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
import org.nervousync.brain.enumerations.auth.AuthType;
import org.nervousync.brain.exceptions.dialects.DialectException;
import org.nervousync.commons.Globals;

import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * <h2 class="en-US">MS SQLServer 2012 database dialect implementation class</h2>
 * <h2 class="zh-CN">微软SQLServer 2012数据库方言实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2022 16:37:19 $
 */
@Provider(name = "MSSQL2K12", titleKey = "MSSQL2K12_Dialect_Title", descriptionKey = "MSSQL_Dialect_Description")
@SchemaDialect(supportJoin = true, sharding = true,
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
public class SQLServer2012DialectImpl extends SQLServer2000DialectImpl {

	/**
	 * <h3 class="en-US">Constructor method for SQLServer 2012 database dialect implementation class</h3>
	 * <h3 class="zh-CN">SQLServer 2012数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到SchemaDialect注解</span>
	 */
	public SQLServer2012DialectImpl() throws DialectException {
	}

	@Override
	public String nextVal(final String sequenceName) {
		return "SELECT NEXT VALUE FOR " + this.nameCase(sequenceName);
	}

	@Override
	protected String limitCommand(@Nonnull final String sqlCmd, final int offset, final int pageLimit,
	                              @Nonnull final List<Object> values) {
		values.add((offset > 0) ? offset : Globals.INITIALIZE_INT_VALUE);
		values.add(pageLimit);
		return sqlCmd + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
	}

	@Override
	protected void trustStoreProperties(@Nonnull final Properties properties, final TrustStore trustStore) {
		Optional.ofNullable(trustStore)
				.ifPresent(store -> {
					properties.put("trustStore", store.getStorePath());
					properties.put("trustStorePassword", store.getStorePassword());
				});
	}

	@Override
	protected void authProperties(@Nonnull final Properties properties, final Authentication authentication) {
		Optional.ofNullable(authentication)
				.filter(auth -> AuthType.CERTIFICATE.equals(auth.getAuthType()))
				.map(auth -> (TrustStoreAuthentication) auth)
				.ifPresent(tsAuthentication -> {
					properties.put("clientKey", tsAuthentication.getStorePath());
					properties.put("clientKeyPassword", tsAuthentication.getStorePassword());
				});
	}
}
