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

package org.nervousync.magi.dialects.impl.mongodb;

import org.nervousync.annotations.provider.Provider;
import org.nervousync.brain.annotations.dialect.DataType;
import org.nervousync.brain.annotations.dialect.SchemaDialect;
import org.nervousync.brain.configs.auth.Authentication;
import org.nervousync.brain.configs.schema.impl.DistributeSchemaConfig;
import org.nervousync.brain.configs.secure.TrustStore;
import org.nervousync.brain.dialects.distribute.DistributeClient;
import org.nervousync.brain.dialects.distribute.DistributeDialect;
import org.nervousync.brain.exceptions.dialects.DialectException;
import org.nervousync.commons.Globals;

import java.sql.Types;
import java.util.Properties;

/**
 * <h2 class="en-US">MongoDB database dialect implementation class</h2>
 * <h2 class="zh-CN">MongoDB数据库方言实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 18, 2022 09:05:28 $
 */
@Provider(name = "MongoDB", titleKey = "MongoDB_Dialect_Title", descriptionKey = "MongoDB_Dialect_Description")
@SchemaDialect(supportJoin = true, types = {
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
public final class MongoDBDialectImpl extends DistributeDialect {

	/**
	 * <h3 class="en-US">Constructor method for MongoDB database dialect implementation class</h3>
	 * <h3 class="zh-CN">MongoDB数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the org. nervousync. brain. annotations. dialect.SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到org. nervousync. brain. annotations. dialect.SchemaDialect注解</span>
	 */
	public MongoDBDialectImpl() throws DialectException {
	}

	@Override
	public DistributeClient newClient(final DistributeSchemaConfig schemaConfig) throws Exception {
		return new MongoDBClient(this, schemaConfig);
	}

	@Override
	public String defaultValue(final int jdbcType, final int length, final int precision,
	                           final int scale, final Object object) {
		return Globals.DEFAULT_VALUE_STRING;
	}

	@Override
	public String nameCase(final String name) {
		return name;
	}

	@Override
	public Properties properties(final TrustStore trustStore, final Authentication authentication) {
		return null;
	}
}
