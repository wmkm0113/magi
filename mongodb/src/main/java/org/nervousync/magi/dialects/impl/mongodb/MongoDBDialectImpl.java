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

import com.mongodb.client.ClientSession;
import org.nervousync.annotations.provider.Provider;
import org.nervousync.brain.annotations.dialect.DataType;
import org.nervousync.brain.annotations.dialect.SchemaDialect;
import org.nervousync.brain.configs.schema.impl.DistributeSchemaConfig;
import org.nervousync.brain.dialects.distribute.DistributeClient;
import org.nervousync.brain.dialects.distribute.DistributeDialect;
import org.nervousync.brain.exceptions.dialects.DialectException;
import org.nervousync.brain.query.param.AbstractParameter;
import org.nervousync.commons.Globals;
import org.nervousync.utils.core.StringUtils;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

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
public final class MongoDBDialectImpl extends DistributeDialect<ClientSession> {

	/**
	 * <h3 class="en-US">Constructor method for MongoDB database dialect implementation class</h3>
	 * <h3 class="zh-CN">MongoDB数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到SchemaDialect注解</span>
	 */
	public MongoDBDialectImpl() throws DialectException {
		super(ClientSession.class);
	}

	@Override
	public DistributeClient<ClientSession> newClient(final DistributeSchemaConfig schemaConfig) throws Exception {
		return new MongoDBClient(this, schemaConfig);
	}

	@Override
	public String nameCase(final String name) {
		return StringUtils.isEmpty(name) ? Globals.DEFAULT_VALUE_STRING : name;
	}

	@Override
	@SuppressWarnings("RedundantThrows")
	protected String parameterValue(final Map<String, String> aliasMap, final AbstractParameter<?> abstractParameter, final List<Object> values) throws SQLException {
		return Globals.DEFAULT_VALUE_STRING;
	}
}
