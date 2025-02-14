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
import org.nervousync.brain.exceptions.dialects.DialectException;

import java.sql.Types;

/**
 * <h2 class="en-US">MySQL 8.0 database dialect implementation class</h2>
 * <h2 class="zh-CN">MySQL 8.0数据库方言实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2022 09:59:33 $
 */
@Provider(name = "MySQL8", titleKey = "MySQL8_Dialect_Title", descriptionKey = "MySQL_Dialect_Description")
@SchemaDialect(supportJoin = true,
		types = {
				@DataType(code = Types.INTEGER, type = "INTEGER"),
				@DataType(code = Types.SMALLINT, type = "SMALLINT"),
				@DataType(code = Types.BIGINT, type = "BIGINT"),
				@DataType(code = Types.TINYINT, type = "TINYINT"),
				@DataType(code = Types.FLOAT, type = "FLOAT({precision}, {scale})"),
				@DataType(code = Types.DOUBLE, type = "REAL({precision}, {scale})"),
				@DataType(code = Types.NUMERIC, type = "NUMERIC({precision}, {scale})"),
				@DataType(code = Types.REAL, type = "REAL({precision}, {scale})"),
				@DataType(code = Types.DECIMAL, type = "DECIMAL({precision}, {scale})"),
				@DataType(code = Types.VARCHAR, type = "VARCHAR({length})"),
				@DataType(code = Types.NVARCHAR, type = "VARCHAR({length})"),
				@DataType(code = Types.BIT, type = "BIT({length})"),
				@DataType(code = Types.BOOLEAN, type = "BOOL"),
				@DataType(code = Types.CHAR, type = "CHAR({length})"),
				@DataType(code = Types.NCHAR, type = "CHAR({length})"),
				@DataType(code = Types.DATE, type = "DATE"),
				@DataType(code = Types.TIME, type = "TIME"),
				@DataType(code = Types.TIMESTAMP, type = "TIMESTAMP"),
				@DataType(code = Types.BLOB, type = "LONGBLOB"),
				@DataType(code = Types.VARBINARY, type = "MEDIUMBLOB"),
				@DataType(code = Types.LONGVARBINARY, type = "LONGBLOB"),
				@DataType(code = Types.CLOB, type = "TEXT"),
				@DataType(code = Types.NCLOB, type = "MEDIUMTEXT"),
				@DataType(code = Types.LONGNVARCHAR, type = "LONGTEXT")
		})
public final class MySQL80DialectImpl extends MySQL50DialectImpl {
	/**
	 * <h3 class="en-US">Constructor method for MySQL 8.0 database dialect implementation class</h3>
	 * <h3 class="zh-CN">MySQL 8.0数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the org. nervousync. brain. annotations. dialect.SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到org. nervousync. brain. annotations. dialect.SchemaDialect注解</span>
	 */
	public MySQL80DialectImpl() throws DialectException {
		super();
	}

}
