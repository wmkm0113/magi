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
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.utils.core.StringUtils;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * <h2 class="en-US">Apache Derby database dialect implementation class</h2>
 * <h2 class="zh-CN">Apache Derby数据库方言实现类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2022 09:05:28 $
 */
@Provider(name = "Derby", titleKey = "Derby_Dialect_Title", descriptionKey = "Derby_Dialect_Description")
@SchemaDialect(supportJoin = true,
		types = {
				@DataType(code = Types.VARCHAR, type = "varchar({length})"),
				@DataType(code = Types.NVARCHAR, type = "varchar({length})"),
				@DataType(code = Types.INTEGER, type = "integer"),
				@DataType(code = Types.SMALLINT, type = "smallint"),
				@DataType(code = Types.BIGINT, type = "bigint"),
				@DataType(code = Types.TINYINT, type = "integer"),
				@DataType(code = Types.FLOAT, type = "float({precision})"),
				@DataType(code = Types.DOUBLE, type = "float(52)"),
				@DataType(code = Types.NUMERIC, type = "numeric({precision}, {scale})"),
				@DataType(code = Types.REAL, type = "float(22)"),
				@DataType(code = Types.DECIMAL, type = "decimal({precision}, {scale})"),
				@DataType(code = Types.BIT, type = "bit({length})"),
				@DataType(code = Types.BOOLEAN, type = "boolean"),
				@DataType(code = Types.CHAR, type = "char({length})"),
				@DataType(code = Types.NCHAR, type = "char({length})"),
				@DataType(code = Types.DATE, type = "date"),
				@DataType(code = Types.TIME, type = "time"),
				@DataType(code = Types.TIMESTAMP, type = "timestamp"),
				@DataType(code = Types.BLOB, type = "blob"),
				@DataType(code = Types.VARBINARY, type = "blob"),
				@DataType(code = Types.LONGVARBINARY, type = "blob"),
				@DataType(code = Types.CLOB, type = "clob"),
				@DataType(code = Types.NCLOB, type = "clob"),
				@DataType(code = Types.LONGNVARCHAR, type = "clob")
		})
public final class DerbyDialectImpl extends JdbcDialect {

	/**
	 * <h3 class="en-US">Constructor method for Derby database dialect implementation class</h3>
	 * <h3 class="zh-CN">Derby数据库方言实现类的构造方法</h3>
	 *
	 * @throws DialectException <span class="en-US">If the implementation class does not find the SchemaDialect annotation</span>
	 *                          <span class="zh-CN">如果实现类未找到 SchemaDialect 注解</span>
	 */
	public DerbyDialectImpl() throws DialectException {
		super(DialectType.Relational);
	}

	@Override
	protected void trustStoreProperties(@Nonnull final Properties properties, final TrustStore trustStore) {
	}

	@Override
	protected void authProperties(@Nonnull final Properties properties, final Authentication authentication) {
	}

	@Override
	public String alterType() {
		return " SET DATA TYPE ";
	}

	@Override
	public String addColumn() {
		return " ADD COLUMN ";
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
	protected String limitCommand(@Nonnull final String sqlCmd, final int offset, final int pageLimit, @Nonnull final List<Object> values) {
		StringBuilder stringBuilder = new StringBuilder(sqlCmd);

		if (offset > 0) {
			stringBuilder.append(" OFFSET ? ROWS");
			values.add(offset);
		}
		stringBuilder.append(" FETCH NEXT ? ROWS only");
		values.add(pageLimit);

		return stringBuilder.toString();
	}

	@Override
	public String nameCase(final String name) {
		return name.toUpperCase();
	}

	@Override
	protected String lockWhereClause(final String whereClause, final boolean forUpdate, final LockModeType lockOption) {
		StringBuilder sqlBuilder = new StringBuilder(WHERE_COMMAND).append(BrainCommons.CONSTANT_CLAUSE_TRUE);
		if (StringUtils.isEmpty(whereClause)) {
			if (this.logger.isDebugEnabled()) {
				this.logger.warn("Query_Condition_Empty");
			}
		} else {
			sqlBuilder.append(BrainCommons.WHITE_SPACE)
					.append(ConnectionCode.AND)
					.append(BrainCommons.WHITE_SPACE)
					.append(BrainCommons.BRACKETS_BEGIN)
					.append(whereClause)
					.append(BrainCommons.BRACKETS_END);
		}
		if (forUpdate) {
			switch (lockOption) {
				case WRITE:
				case PESSIMISTIC_WRITE:
					sqlBuilder.append(" FOR UPDATE ");
					break;
			}
		}
		return sqlBuilder.toString();
	}

	@Override
	public List<String> modifyColumnType(final String tableName, final int currentType, final ColumnDefine columnDefine)
			throws SQLException {
		List<String> sqlCmdList = new ArrayList<>();
		if (columnDefine.getJdbcType() == Types.VARCHAR || columnDefine.getJdbcType() == Types.NVARCHAR) {
			String sqlCmd = ALTER_TABLE + this.nameCase(tableName)
					+ this.alterColumn() + this.nameCase(columnDefine.getColumnName()) + BrainCommons.WHITE_SPACE
					+ this.alterType() + BrainCommons.WHITE_SPACE + this.columnType(columnDefine);
			sqlCmdList.add(sqlCmd);
		} else {
			if (currentType == columnDefine.getJdbcType()) {
				//  Add new column define to table
				String tmpName = this.nameCase(columnDefine.getColumnName() + "New");
				String columnName = this.nameCase(columnDefine.getColumnName());
				sqlCmdList.add(ALTER_TABLE + this.nameCase(tableName) + this.addColumn()
						+ tmpName + BrainCommons.WHITE_SPACE + this.columnType(columnDefine));
				//  Copy data to new column
				sqlCmdList.add(COMMAND_UPDATE + this.nameCase(tableName) + COMMAND_SET + tmpName + " = " + columnName);
				//  Remove old column
				sqlCmdList.add(ALTER_TABLE + this.nameCase(tableName) + this.dropColumn() + columnName);
				//  Rename column
				sqlCmdList.add(this.renameColumn(tableName, tmpName, columnName));
			} else {
				throw new MultilingualSQLException(0x00DB00020001L, currentType, columnDefine.getJdbcType());
			}
		}
		return sqlCmdList;
	}
}
