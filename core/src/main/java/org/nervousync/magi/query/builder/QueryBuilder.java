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

package org.nervousync.magi.query.builder;

import jakarta.annotation.Nonnull;
import org.nervousync.brain.defines.ColumnDefine;
import org.nervousync.brain.defines.TableDefine;
import org.nervousync.brain.enumerations.query.JoinType;
import org.nervousync.brain.enumerations.query.OrderType;
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.brain.query.condition.Condition;
import org.nervousync.brain.query.data.QueryData;
import org.nervousync.brain.query.join.JoinInfo;
import org.nervousync.brain.query.param.AbstractParameter;
import org.nervousync.builder.Builder;
import org.nervousync.commons.Globals;
import org.nervousync.enumerations.core.ConnectionCode;
import org.nervousync.exceptions.builder.BuilderException;
import org.nervousync.magi.beans.defines.reference.JoinDefine;
import org.nervousync.magi.entity.EntityFactory;
import org.nervousync.utils.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <h2 class="en-US">Entity class query information builder</h2>
 * <h2 class="zh-CN">实体类查询信息构建器</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
 */
public final class QueryBuilder implements Builder<QueryInfo> {

	/**
	 * <span class="en-US">Entity factory instance object</span>
	 * <span class="zh-CN">实体类工厂实例对象</span>
	 */
	private final EntityFactory entityFactory;
	/**
	 * <span class="en-US">Entity class of driven table</span>
	 * <span class="zh-CN">驱动表实体类</span>
	 */
	private final Class<?> entityClass;
	/**
	 * <span class="en-US">Query information builder instance object</span>
	 * <span class="zh-CN">查询信息构建器实例对象</span>
	 */
	private final org.nervousync.brain.query.builder.QueryBuilder queryBuilder;

	/**
	 * <h3 class="en-US">Private constructor method for query plan builder</h3>
	 * <h3 class="zh-CN">查询计划构建器的私有构造方法</h3>
	 *
	 * @param entityClass <span class="en-US">Query driven table entity class</span>
	 *                    <span class="zh-CN">查询驱动表实体类</span>
	 * @throws Exception <span class="en-US">If the driver table is not registered</span>
	 *                   <span class="zh-CN">如果驱动表未注册</span>
	 */
	private QueryBuilder(final Class<?> entityClass, final String aliasName) throws Exception {
		this.entityFactory = EntityFactory.getInstance();
		this.entityClass = entityClass;
		String driverTable = this.entityFactory.tableName(entityClass);
		if (StringUtils.isEmpty(driverTable)) {
			throw new MultilingualSQLException(0x00DB00010012L, entityClass.getName());
		}
		if (!this.entityFactory.registeredTable(driverTable)) {
			throw new MultilingualSQLException(0x00DB00010012L, driverTable);
		}
		this.queryBuilder = org.nervousync.brain.query.builder.QueryBuilder.newBuilder(driverTable, aliasName);
	}

	/**
	 * <h3 class="en-US">Static method used to initialize query plan builder</h3>
	 * <h3 class="zh-CN">静态方法用于初始化查询计划构建器</h3>
	 *
	 * @param entityClass <span class="en-US">Query driven table entity class</span>
	 *                    <span class="zh-CN">查询驱动表实体类</span>
	 * @return <span class="en-US">Query builder instance object</span>
	 * <span class="zh-CN">查询构建器实例对象</span>
	 * @throws Exception <span class="en-US">If the driver table is not registered</span>
	 *                   <span class="zh-CN">如果驱动表未注册</span>
	 */
	public static QueryBuilder newBuilder(final Class<?> entityClass) throws Exception {
		return newBuilder(entityClass, Globals.DEFAULT_VALUE_STRING);
	}

	/**
	 * <h3 class="en-US">Static method used to initialize query plan builder</h3>
	 * <h3 class="zh-CN">静态方法用于初始化查询计划构建器</h3>
	 *
	 * @param entityClass <span class="en-US">Query driven table entity class</span>
	 *                    <span class="zh-CN">查询驱动表实体类</span>
	 * @param aliasName   <span class="en-US">Data table alias name</span>
	 *                    <span class="zh-CN">数据表别名</span>
	 * @return <span class="en-US">Query builder instance object</span>
	 * <span class="zh-CN">查询构建器实例对象</span>
	 * @throws Exception <span class="en-US">If the driver table is not registered</span>
	 *                   <span class="zh-CN">如果驱动表未注册</span>
	 */
	public static QueryBuilder newBuilder(final Class<?> entityClass, final String aliasName) throws Exception {
		return new QueryBuilder(entityClass, aliasName);
	}

	/**
	 * <h3 class="en-US">Add data table join information</h3>
	 * <h3 class="zh-CN">添加数据表关联信息</h3>
	 *
	 * @param joinType       <span class="en-US">Join type</span>
	 *                       <span class="zh-CN">关联类型</span>
	 * @param entityClass    <span class="en-US">Driven table entity class</span>
	 *                       <span class="zh-CN">驱动表实体类</span>
	 * @param referenceClass <span class="en-US">Join table entity class</span>
	 *                       <span class="zh-CN">关联数据表实体类</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder joinTable(final JoinType joinType, final Class<?> entityClass, final Class<?> referenceClass)
			throws SQLException {
		return this.joinTable(joinType, entityClass, referenceClass, Globals.DEFAULT_VALUE_STRING);
	}

	/**
	 * <h3 class="en-US">Add data table join information</h3>
	 * <h3 class="zh-CN">添加数据表关联信息</h3>
	 *
	 * @param joinType       <span class="en-US">Join type</span>
	 *                       <span class="zh-CN">关联类型</span>
	 * @param entityClass    <span class="en-US">Driven table entity class</span>
	 *                       <span class="zh-CN">驱动表实体类</span>
	 * @param referenceClass <span class="en-US">Join table entity class</span>
	 *                       <span class="zh-CN">关联数据表实体类</span>
	 * @param aliasName      <span class="en-US">Join table alias name</span>
	 *                       <span class="zh-CN">关联数据表别名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder joinTable(final JoinType joinType, final Class<?> entityClass,
	                              final Class<?> referenceClass, final String aliasName) throws SQLException {
		String drivenTable = this.entityFactory.tableName(entityClass);
		String joinTable = this.entityFactory.tableName(referenceClass);
		if (StringUtils.isEmpty(drivenTable)) {
			throw new MultilingualSQLException(0x00DB00010012L, entityClass.getName());
		}
		if (StringUtils.isEmpty(joinTable)) {
			throw new MultilingualSQLException(0x00DB00010012L, referenceClass.getName());
		}

		List<JoinInfo> joinInfos = new ArrayList<>();
		for (JoinDefine joinDefine : this.entityFactory.joinColumns(entityClass, referenceClass)) {
			joinInfos.add(JoinInfo.newInstance(
					this.entityFactory.columnName(entityClass, joinDefine.getCurrentField()),
					this.entityFactory.columnName(referenceClass, joinDefine.getReferenceField())));
		}
		this.queryBuilder.joinTable(joinType, drivenTable, joinTable, joinInfos);
		return this;
	}

	/**
	 * <h3 class="en-US">Add query data column</h3>
	 * <h3 class="zh-CN">添加查询数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder queryColumn(final Class<?> entityClass, final String identifyCode) throws SQLException {
		return this.queryColumn(entityClass, identifyCode, Boolean.FALSE);
	}

	/**
	 * <h3 class="en-US">Add query data column</h3>
	 * <h3 class="zh-CN">添加查询数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param aliasName    <span class="en-US">Item alias name</span>
	 *                     <span class="zh-CN">查询项别名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder queryColumn(final Class<?> entityClass, final String identifyCode, final String aliasName)
			throws SQLException {
		return this.queryColumn(entityClass, identifyCode, aliasName, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * <h3 class="en-US">Add query data column</h3>
	 * <h3 class="zh-CN">添加查询数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param aliasName    <span class="en-US">Item alias name</span>
	 *                     <span class="zh-CN">查询项别名</span>
	 * @param sortCode     <span class="en-US">Sort code</span>
	 *                     <span class="zh-CN">排序代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder queryColumn(final Class<?> entityClass, final String identifyCode,
	                                final String aliasName, final int sortCode) throws SQLException {
		return this.queryColumn(entityClass, identifyCode, Boolean.FALSE, aliasName, sortCode);
	}

	/**
	 * <h3 class="en-US">Add query data column</h3>
	 * <h3 class="zh-CN">添加查询数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param distinct     <span class="en-US">Column distinct</span>
	 *                     <span class="zh-CN">数据列去重</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder queryColumn(final Class<?> entityClass, final String identifyCode, final boolean distinct)
			throws SQLException {
		return this.queryColumn(entityClass, identifyCode, distinct, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * <h3 class="en-US">Add query data column</h3>
	 * <h3 class="zh-CN">添加查询数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param distinct     <span class="en-US">Column distinct</span>
	 *                     <span class="zh-CN">数据列去重</span>
	 * @param sortCode     <span class="en-US">Sort code</span>
	 *                     <span class="zh-CN">排序代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder queryColumn(final Class<?> entityClass, final String identifyCode,
	                                final boolean distinct, final int sortCode) throws SQLException {
		return this.queryColumn(entityClass, identifyCode, distinct, Globals.DEFAULT_VALUE_STRING, sortCode);
	}

	/**
	 * <h3 class="en-US">Add query data column</h3>
	 * <h3 class="zh-CN">添加查询数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param distinct     <span class="en-US">Column distinct</span>
	 *                     <span class="zh-CN">数据列去重</span>
	 * @param aliasName    <span class="en-US">Item alias name</span>
	 *                     <span class="zh-CN">查询项别名</span>
	 * @param sortCode     <span class="en-US">Sort code</span>
	 *                     <span class="zh-CN">排序代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder queryColumn(final Class<?> entityClass, final String identifyCode, final boolean distinct,
	                                final String aliasName, final int sortCode) throws SQLException {
		this.queryBuilder.queryColumn(this.entityFactory.tableName(entityClass),
				this.entityFactory.columnName(entityClass, identifyCode),
				distinct, aliasName, sortCode);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue   <span class="en-US">Match value</span>
	 *                     <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final Class<?> entityClass, final String identifyCode, final Object matchValue)
			throws SQLException {
		return this.greater(ConnectionCode.AND, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity <span class="en-US">Target data table entity class</span>
	 *                     <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode   <span class="en-US">Target data column identification name</span>
	 *                     <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.greater(ConnectionCode.AND, entityClass, identifyCode,
				targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                            final AbstractParameter<?>... functionParams) throws SQLException {
		return this.greater(ConnectionCode.AND, entityClass, identifyCode,
				sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery     <span class="en-US">Sub-query instance object</span>
	 *                     <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.greater(ConnectionCode.AND, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity <span class="en-US">Target data table entity class</span>
	 *                     <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode   <span class="en-US">Target data column identification name</span>
	 *                     <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final Class<?> entityClass, final String identifyCode,
	                                 final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.greaterEqual(ConnectionCode.AND, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                                 final AbstractParameter<?>... functionParams) throws SQLException {
		return this.greaterEqual(ConnectionCode.AND, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery     <span class="en-US">Sub-query instance object</span>
	 *                     <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.greaterEqual(ConnectionCode.AND, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue   <span class="en-US">Match value</span>
	 *                     <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final Class<?> entityClass, final String identifyCode, final Object matchValue)
			throws SQLException {
		return this.greaterEqual(ConnectionCode.AND, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity <span class="en-US">Target data table entity class</span>
	 *                     <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode   <span class="en-US">Target data column identification name</span>
	 *                     <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final Class<?> entityClass, final String identifyCode,
	                         final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.less(ConnectionCode.AND, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                         final AbstractParameter<?>... functionParams) throws SQLException {
		return this.less(ConnectionCode.AND, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery     <span class="en-US">Sub-query instance object</span>
	 *                     <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.less(ConnectionCode.AND, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue   <span class="en-US">Match value</span>
	 *                     <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final Class<?> entityClass, final String identifyCode, final Object matchValue)
			throws SQLException {
		return this.less(ConnectionCode.AND, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity <span class="en-US">Target data table entity class</span>
	 *                     <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode   <span class="en-US">Target data column identification name</span>
	 *                     <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final Class<?> entityClass, final String identifyCode,
	                              final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.lessEqual(ConnectionCode.AND, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                              final AbstractParameter<?>... functionParams) throws SQLException {
		return this.lessEqual(ConnectionCode.AND, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery     <span class="en-US">Sub-query instance object</span>
	 *                     <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.lessEqual(ConnectionCode.AND, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue   <span class="en-US">Match value</span>
	 *                     <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final Class<?> entityClass, final String identifyCode, final Object matchValue)
			throws SQLException {
		return this.lessEqual(ConnectionCode.AND, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity <span class="en-US">Target data table entity class</span>
	 *                     <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode   <span class="en-US">Target data column identification name</span>
	 *                     <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.equalTo(ConnectionCode.AND, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                            final AbstractParameter<?>... functionParams) throws SQLException {
		return this.equalTo(ConnectionCode.AND, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery     <span class="en-US">Sub-query instance object</span>
	 *                     <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.equalTo(ConnectionCode.AND, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue   <span class="en-US">Match value</span>
	 *                     <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final Class<?> entityClass, final String identifyCode, final Object matchValue)
			throws SQLException {
		return this.equalTo(ConnectionCode.AND, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity <span class="en-US">Target data table entity class</span>
	 *                     <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode   <span class="en-US">Target data column identification name</span>
	 *                     <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final Class<?> entityClass, final String identifyCode,
	                             final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.notEqual(ConnectionCode.AND, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                             final AbstractParameter<?>... functionParams) throws SQLException {
		return this.notEqual(ConnectionCode.AND, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery     <span class="en-US">Sub-query instance object</span>
	 *                     <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.notEqual(ConnectionCode.AND, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue   <span class="en-US">Match value</span>
	 *                     <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final Class<?> entityClass, final String identifyCode, final Object matchValue)
			throws SQLException {
		return this.notEqual(ConnectionCode.AND, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition between certain two values</h3>
	 * <h3 class="zh-CN">添加介于某两个值之间的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue   <span class="en-US">Begin value</span>
	 *                     <span class="zh-CN">起始值</span>
	 * @param endValue     <span class="en-US">End value</span>
	 *                     <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder inRanges(final Class<?> entityClass, final String identifyCode,
	                             final Object beginValue, final Object endValue) throws SQLException {
		return this.inRanges(ConnectionCode.AND, entityClass, identifyCode, beginValue, endValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition not between certain two values</h3>
	 * <h3 class="zh-CN">添加不介于某两个值之间的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue   <span class="en-US">Begin value</span>
	 *                     <span class="zh-CN">起始值</span>
	 * @param endValue     <span class="en-US">End value</span>
	 *                     <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notInRanges(final Class<?> entityClass, final String identifyCode,
	                                final Object beginValue, final Object endValue) throws SQLException {
		return this.notInRanges(ConnectionCode.AND, entityClass, identifyCode, beginValue, endValue);
	}

	/**
	 * <h3 class="en-US">Add query conditions for fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加模糊匹配值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule    <span class="en-US">match rule string</span>
	 *                     <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder like(final Class<?> entityClass, final String identifyCode, final String matchRule)
			throws SQLException {
		return this.like(ConnectionCode.AND, entityClass, identifyCode, matchRule);
	}

	/**
	 * <h3 class="en-US">Add query conditions for not fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加非模糊匹配值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule    <span class="en-US">match rule string</span>
	 *                     <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notLike(final Class<?> entityClass, final String identifyCode, final String matchRule)
			throws SQLException {
		return this.notLike(ConnectionCode.AND, entityClass, identifyCode, matchRule);
	}

	/**
	 * <h3 class="en-US">Add query condition with null value</h3>
	 * <h3 class="zh-CN">添加空值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder matchNull(final Class<?> entityClass, final String identifyCode) throws SQLException {
		return this.matchNull(ConnectionCode.AND, entityClass, identifyCode);
	}

	/**
	 * <h3 class="en-US">Add query condition with not null value</h3>
	 * <h3 class="zh-CN">添加非空值的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notNull(final Class<?> entityClass, final String identifyCode) throws SQLException {
		return this.notNull(ConnectionCode.AND, entityClass, identifyCode);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery     <span class="en-US">Sub-query instance object</span>
	 *                     <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.in(ConnectionCode.AND, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues  <span class="en-US">Condition data array</span>
	 *                     <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final Class<?> entityClass, final String identifyCode, final Object... matchValues)
			throws SQLException {
		return this.in(ConnectionCode.AND, entityClass, identifyCode, matchValues);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery     <span class="en-US">Sub-query instance object</span>
	 *                     <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.notIn(ConnectionCode.AND, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues  <span class="en-US">Condition data array</span>
	 *                     <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final Class<?> entityClass, final String identifyCode, final Object... matchValues)
			throws SQLException {
		return this.notIn(ConnectionCode.AND, entityClass, identifyCode, matchValues);
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param subQuery     <span class="en-US">Sub-query instance object</span>
	 *                     <span class="zh-CN">子查询实例对象</span>
	 * @param functionName <span class="en-US">Function name of sub-query</span>
	 *                     <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder exists(final Class<?> entityClass, final QueryData subQuery, final String functionName) {
		return this.exists(ConnectionCode.AND, entityClass, subQuery, functionName);
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param subQuery     <span class="en-US">Sub-query instance object</span>
	 *                     <span class="zh-CN">子查询实例对象</span>
	 * @param functionName <span class="en-US">Function name of sub-query</span>
	 *                     <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder notExists(final Class<?> entityClass, final QueryData subQuery, final String functionName) {
		return this.notExists(ConnectionCode.AND, entityClass, subQuery, functionName);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final ConnectionCode connectionCode, final Class<?> entityClass,
	                            final String identifyCode, final Object matchValue) throws SQLException {
		return this.greater(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.greater(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode,
				targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final ConnectionCode connectionCode, final Class<?> entityClass,
	                            final String identifyCode, final String sqlFunction,
	                            final AbstractParameter<?>... functionParams) throws SQLException {
		return this.greater(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode,
				sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final ConnectionCode connectionCode, final Class<?> entityClass,
	                            final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.greater(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final ConnectionCode connectionCode,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.greaterEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final ConnectionCode connectionCode,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final String sqlFunction, final AbstractParameter<?>... functionParams) throws SQLException {
		return this.greaterEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final ConnectionCode connectionCode, final Class<?> entityClass,
	                                 final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.greaterEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final ConnectionCode connectionCode, final Class<?> entityClass,
	                                 final String identifyCode, final Object matchValue) throws SQLException {
		return this.greaterEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final ConnectionCode connectionCode,
	                         final Class<?> entityClass, final String identifyCode,
	                         final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.less(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final ConnectionCode connectionCode,
	                         final Class<?> entityClass, final String identifyCode,
	                         final String sqlFunction, final AbstractParameter<?>... functionParams) throws SQLException {
		return this.less(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final ConnectionCode connectionCode, final Class<?> entityClass,
	                         final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.less(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final ConnectionCode connectionCode, final Class<?> entityClass,
	                         final String identifyCode, final Object matchValue) throws SQLException {
		return this.less(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final ConnectionCode connectionCode,
	                              final Class<?> entityClass, final String identifyCode,
	                              final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.lessEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final ConnectionCode connectionCode, final Class<?> entityClass,
	                              final String identifyCode, final String sqlFunction,
	                              final AbstractParameter<?>... functionParams) throws SQLException {
		return this.lessEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final ConnectionCode connectionCode, final Class<?> entityClass,
	                              final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.lessEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final ConnectionCode connectionCode, final Class<?> entityClass,
	                              final String identifyCode, final Object matchValue) throws SQLException {
		return this.lessEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.equalTo(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final ConnectionCode connectionCode, final Class<?> entityClass,
	                            final String identifyCode, final String sqlFunction,
	                            final AbstractParameter<?>... functionParams) throws SQLException {
		return this.equalTo(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final ConnectionCode connectionCode, final Class<?> entityClass,
	                            final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.equalTo(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Object matchValue) throws SQLException {
		return this.equalTo(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final ConnectionCode connectionCode,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.notEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final ConnectionCode connectionCode, final Class<?> entityClass,
	                             final String identifyCode, final String sqlFunction,
	                             final AbstractParameter<?>... functionParams) throws SQLException {
		return this.notEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final ConnectionCode connectionCode, final Class<?> entityClass,
	                             final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.notEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final ConnectionCode connectionCode,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Object matchValue) throws SQLException {
		return this.notEqual(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition between certain two values</h3>
	 * <h3 class="zh-CN">添加介于某两个值之间的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue     <span class="en-US">Begin value</span>
	 *                       <span class="zh-CN">起始值</span>
	 * @param endValue       <span class="en-US">End value</span>
	 *                       <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder inRanges(final ConnectionCode connectionCode,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Object beginValue, final Object endValue) throws SQLException {
		return this.inRanges(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, beginValue, endValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition not between certain two values</h3>
	 * <h3 class="zh-CN">添加不介于某两个值之间的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue     <span class="en-US">Begin value</span>
	 *                       <span class="zh-CN">起始值</span>
	 * @param endValue       <span class="en-US">End value</span>
	 *                       <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notInRanges(final ConnectionCode connectionCode,
	                                final Class<?> entityClass, final String identifyCode,
	                                final Object beginValue, final Object endValue) throws SQLException {
		return this.notInRanges(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, beginValue, endValue);
	}

	/**
	 * <h3 class="en-US">Add query conditions for fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加模糊匹配值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule      <span class="en-US">match rule string</span>
	 *                       <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder like(final ConnectionCode connectionCode, final Class<?> entityClass,
	                         final String identifyCode, final String matchRule) throws SQLException {
		return this.like(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, matchRule);
	}

	/**
	 * <h3 class="en-US">Add query conditions for not fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加非模糊匹配值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule      <span class="en-US">match rule string</span>
	 *                       <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notLike(final ConnectionCode connectionCode, final Class<?> entityClass,
	                            final String identifyCode, final String matchRule) throws SQLException {
		return this.notLike(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, matchRule);
	}

	/**
	 * <h3 class="en-US">Add query condition with null value</h3>
	 * <h3 class="zh-CN">添加空值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder matchNull(final ConnectionCode connectionCode, final Class<?> entityClass,
	                              final String identifyCode) throws SQLException {
		return this.matchNull(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode);
	}

	/**
	 * <h3 class="en-US">Add query condition with not null value</h3>
	 * <h3 class="zh-CN">添加非空值的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notNull(final ConnectionCode connectionCode, final Class<?> entityClass,
	                            final String identifyCode) throws SQLException {
		return this.notNull(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final ConnectionCode connectionCode, final Class<?> entityClass,
	                       final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.in(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues    <span class="en-US">Condition data array</span>
	 *                       <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final ConnectionCode connectionCode, final Class<?> entityClass,
	                       final String identifyCode, final Object... matchValues) throws SQLException {
		return this.in(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, matchValues);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final ConnectionCode connectionCode, final Class<?> entityClass,
	                          final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.notIn(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues    <span class="en-US">Condition data array</span>
	 *                       <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final ConnectionCode connectionCode, final Class<?> entityClass,
	                          final String identifyCode, final Object... matchValues) throws SQLException {
		return this.notIn(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, identifyCode, matchValues);
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @param functionName   <span class="en-US">Function name of sub-query</span>
	 *                       <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder exists(final ConnectionCode connectionCode, final Class<?> entityClass,
	                           final QueryData subQuery, final String functionName) {
		return this.exists(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, subQuery, functionName);
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @param functionName   <span class="en-US">Function name of sub-query</span>
	 *                       <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder notExists(final ConnectionCode connectionCode, final Class<?> entityClass,
	                              final QueryData subQuery, final String functionName) {
		return this.notExists(Globals.DEFAULT_VALUE_INT, connectionCode, entityClass, subQuery, functionName);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Object matchValue) throws SQLException {
		return this.greater(ConnectionCode.AND, havingCondition, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.greater(ConnectionCode.AND, havingCondition, entityClass, identifyCode,
				targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                            final AbstractParameter<?>... functionParams) throws SQLException {
		return this.greater(ConnectionCode.AND, havingCondition, entityClass, identifyCode,
				sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.greater(ConnectionCode.AND, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final boolean havingCondition,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.greaterEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final boolean havingCondition,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final String sqlFunction, final AbstractParameter<?>... functionParams) throws SQLException {
		return this.greaterEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final boolean havingCondition,
	                                 final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.greaterEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final boolean havingCondition,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final Object matchValue) throws SQLException {
		return this.greaterEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode,
	                         final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.less(ConnectionCode.AND, havingCondition, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode,
	                         final String sqlFunction, final AbstractParameter<?>... functionParams) throws SQLException {
		return this.less(ConnectionCode.AND, havingCondition, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.less(ConnectionCode.AND, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode,
	                         final Object matchValue) throws SQLException {
		return this.less(ConnectionCode.AND, havingCondition, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode,
	                              final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.lessEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                              final AbstractParameter<?>... functionParams) throws SQLException {
		return this.lessEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.lessEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode,
	                              final Object matchValue) throws SQLException {
		return this.lessEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.equalTo(ConnectionCode.AND, havingCondition, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final boolean havingCondition, final Class<?> entityClass, final String identifyCode,
	                            final String sqlFunction, final AbstractParameter<?>... functionParams)
			throws SQLException {
		return this.equalTo(ConnectionCode.AND, havingCondition, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.equalTo(ConnectionCode.AND, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final boolean havingCondition, final Class<?> entityClass, final String identifyCode,
	                            final Object matchValue) throws SQLException {
		return this.equalTo(ConnectionCode.AND, havingCondition, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.notEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                             final AbstractParameter<?>... functionParams) throws SQLException {
		return this.notEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.notEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Object matchValue) throws SQLException {
		return this.notEqual(ConnectionCode.AND, havingCondition, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition between certain two values</h3>
	 * <h3 class="zh-CN">添加介于某两个值之间的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue      <span class="en-US">Begin value</span>
	 *                        <span class="zh-CN">起始值</span>
	 * @param endValue        <span class="en-US">End value</span>
	 *                        <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder inRanges(final boolean havingCondition, final Class<?> entityClass, final String identifyCode,
	                             final Object beginValue, final Object endValue) throws SQLException {
		return this.inRanges(ConnectionCode.AND, havingCondition, entityClass, identifyCode, beginValue, endValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition not between certain two values</h3>
	 * <h3 class="zh-CN">添加不介于某两个值之间的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue      <span class="en-US">Begin value</span>
	 *                        <span class="zh-CN">起始值</span>
	 * @param endValue        <span class="en-US">End value</span>
	 *                        <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notInRanges(final boolean havingCondition, final Class<?> entityClass, final String identifyCode,
	                                final Object beginValue, final Object endValue) throws SQLException {
		return this.notInRanges(ConnectionCode.AND, havingCondition, entityClass, identifyCode, beginValue, endValue);
	}

	/**
	 * <h3 class="en-US">Add query conditions for fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加模糊匹配值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule       <span class="en-US">match rule string</span>
	 *                        <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder like(final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode, final String matchRule)
			throws SQLException {
		return this.like(ConnectionCode.AND, havingCondition, entityClass, identifyCode, matchRule);
	}

	/**
	 * <h3 class="en-US">Add query conditions for not fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加非模糊匹配值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule       <span class="en-US">match rule string</span>
	 *                        <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notLike(final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final String matchRule)
			throws SQLException {
		return this.notLike(ConnectionCode.AND, havingCondition, entityClass, identifyCode, matchRule);
	}

	/**
	 * <h3 class="en-US">Add query condition with null value</h3>
	 * <h3 class="zh-CN">添加空值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder matchNull(final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode) throws SQLException {
		return this.matchNull(ConnectionCode.AND, havingCondition, entityClass, identifyCode);
	}

	/**
	 * <h3 class="en-US">Add query condition with not null value</h3>
	 * <h3 class="zh-CN">添加非空值的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notNull(final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode) throws SQLException {
		return this.notNull(ConnectionCode.AND, havingCondition, entityClass, identifyCode);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final boolean havingCondition,
	                       final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.in(ConnectionCode.AND, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues     <span class="en-US">Condition data array</span>
	 *                        <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final boolean havingCondition,
	                       final Class<?> entityClass, final String identifyCode, final Object... matchValues)
			throws SQLException {
		return this.in(ConnectionCode.AND, havingCondition, entityClass, identifyCode, matchValues);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final boolean havingCondition,
	                          final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.notIn(ConnectionCode.AND, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues     <span class="en-US">Condition data array</span>
	 *                        <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final boolean havingCondition,
	                          final Class<?> entityClass, final String identifyCode, final Object... matchValues)
			throws SQLException {
		return this.notIn(ConnectionCode.AND, havingCondition, entityClass, identifyCode, matchValues);
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @param functionName    <span class="en-US">Function name of sub-query</span>
	 *                        <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder exists(final boolean havingCondition,
	                           final Class<?> entityClass, final QueryData subQuery, final String functionName) {
		return this.exists(ConnectionCode.AND, havingCondition, entityClass, subQuery, functionName);
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @param functionName    <span class="en-US">Function name of sub-query</span>
	 *                        <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder notExists(final boolean havingCondition,
	                              final Class<?> entityClass, final QueryData subQuery, final String functionName) {
		return this.notExists(ConnectionCode.AND, havingCondition, entityClass, subQuery, functionName);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final Object matchValue)
			throws SQLException {
		return this.greater(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.greater(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode,
				targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                            final AbstractParameter<?>... functionParams) throws SQLException {
		return this.greater(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode,
				sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.greater(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.greaterEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition,
				entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final String sqlFunction, final AbstractParameter<?>... functionParams)
			throws SQLException {
		return this.greaterEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition,
				entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                                 final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.greaterEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition,
				entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final Object matchValue) throws SQLException {
		return this.greaterEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition,
				entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final ConnectionCode connectionCode, final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode,
	                         final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.less(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition,
				entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final ConnectionCode connectionCode, final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode,
	                         final String sqlFunction, final AbstractParameter<?>... functionParams) throws SQLException {
		return this.less(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final ConnectionCode connectionCode, final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.less(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final ConnectionCode connectionCode, final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode,
	                         final Object matchValue) throws SQLException {
		return this.less(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode,
	                              final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.lessEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                              final AbstractParameter<?>... functionParams) throws SQLException {
		return this.lessEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.lessEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode,
	                              final Object matchValue) throws SQLException {
		return this.lessEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition,
				entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.equalTo(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                            final AbstractParameter<?>... functionParams) throws SQLException {
		return this.equalTo(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.equalTo(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Object matchValue) throws SQLException {
		return this.equalTo(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition,
				entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.notEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                             final AbstractParameter<?>... functionParams) throws SQLException {
		return this.notEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.notEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final ConnectionCode connectionCode, final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Object matchValue) throws SQLException {
		return this.notEqual(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition,
				entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition between certain two values</h3>
	 * <h3 class="zh-CN">添加介于某两个值之间的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue      <span class="en-US">Begin value</span>
	 *                        <span class="zh-CN">起始值</span>
	 * @param endValue        <span class="en-US">End value</span>
	 *                        <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder inRanges(final ConnectionCode connectionCode, final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Object beginValue, final Object endValue) throws SQLException {
		return this.inRanges(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition,
				entityClass, identifyCode, beginValue, endValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition not between certain two values</h3>
	 * <h3 class="zh-CN">添加不介于某两个值之间的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue      <span class="en-US">Begin value</span>
	 *                        <span class="zh-CN">起始值</span>
	 * @param endValue        <span class="en-US">End value</span>
	 *                        <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notInRanges(final ConnectionCode connectionCode, final boolean havingCondition,
	                                final Class<?> entityClass, final String identifyCode,
	                                final Object beginValue, final Object endValue) throws SQLException {
		return this.notInRanges(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition,
				entityClass, identifyCode, beginValue, endValue);
	}

	/**
	 * <h3 class="en-US">Add query conditions for fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加模糊匹配值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule       <span class="en-US">match rule string</span>
	 *                        <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder like(final ConnectionCode connectionCode, final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode, final String matchRule)
			throws SQLException {
		return this.like(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, matchRule);
	}

	/**
	 * <h3 class="en-US">Add query conditions for not fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加非模糊匹配值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule       <span class="en-US">match rule string</span>
	 *                        <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notLike(final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final String matchRule)
			throws SQLException {
		return this.notLike(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, matchRule);
	}

	/**
	 * <h3 class="en-US">Add query condition with null value</h3>
	 * <h3 class="zh-CN">添加空值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder matchNull(final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode) throws SQLException {
		return this.matchNull(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode);
	}

	/**
	 * <h3 class="en-US">Add query condition with not null value</h3>
	 * <h3 class="zh-CN">添加非空值的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notNull(final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode) throws SQLException {
		return this.notNull(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final ConnectionCode connectionCode, final boolean havingCondition,
	                       final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.in(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues     <span class="en-US">Condition data array</span>
	 *                        <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final ConnectionCode connectionCode, final boolean havingCondition,
	                       final Class<?> entityClass, final String identifyCode, final Object... matchValues)
			throws SQLException {
		return this.in(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, matchValues);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final ConnectionCode connectionCode, final boolean havingCondition,
	                          final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.notIn(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues     <span class="en-US">Condition data array</span>
	 *                        <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final ConnectionCode connectionCode, final boolean havingCondition,
	                          final Class<?> entityClass, final String identifyCode, final Object... matchValues)
			throws SQLException {
		return this.notIn(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, identifyCode, matchValues);
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @param functionName    <span class="en-US">Function name of sub-query</span>
	 *                        <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder exists(final ConnectionCode connectionCode, final boolean havingCondition,
	                           final Class<?> entityClass, final QueryData subQuery, final String functionName) {
		return this.exists(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, subQuery, functionName);
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @param functionName    <span class="en-US">Function name of sub-query</span>
	 *                        <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder notExists(final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final QueryData subQuery, final String functionName) {
		return this.notExists(Globals.DEFAULT_VALUE_INT, connectionCode, havingCondition, entityClass, subQuery, functionName);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final int sortCode, final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Object matchValue) throws SQLException {
		return this.greater(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final int sortCode, final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.greater(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final int sortCode, final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode,
	                            final String sqlFunction, final AbstractParameter<?>... functionParams)
			throws SQLException {
		return this.greater(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final int sortCode, final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.greater(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final int sortCode, final ConnectionCode connectionCode,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.greaterEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final int sortCode, final ConnectionCode connectionCode,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final String sqlFunction, final AbstractParameter<?>... functionParams)
			throws SQLException {
		return this.greaterEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final int sortCode, final ConnectionCode connectionCode,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final QueryData subQuery) throws SQLException {
		return this.greaterEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final int sortCode, final ConnectionCode connectionCode,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final Object matchValue) throws SQLException {
		return this.greaterEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final int sortCode, final ConnectionCode connectionCode,
	                         final Class<?> entityClass, final String identifyCode,
	                         final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.less(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final int sortCode, final ConnectionCode connectionCode,
	                         final Class<?> entityClass, final String identifyCode,
	                         final String sqlFunction, final AbstractParameter<?>... functionParams) throws SQLException {
		return this.less(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final int sortCode, final ConnectionCode connectionCode,
	                         final Class<?> entityClass, final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.less(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final int sortCode, final ConnectionCode connectionCode,
	                         final Class<?> entityClass, final String identifyCode,
	                         final Object matchValue) throws SQLException {
		return this.less(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final int sortCode, final ConnectionCode connectionCode,
	                              final Class<?> entityClass, final String identifyCode,
	                              final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.lessEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final int sortCode, final ConnectionCode connectionCode,
	                              final Class<?> entityClass, final String identifyCode,
	                              final String sqlFunction, final AbstractParameter<?>... functionParams)
			throws SQLException {
		return this.lessEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final int sortCode, final ConnectionCode connectionCode,
	                              final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		return this.lessEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final int sortCode, final ConnectionCode connectionCode,
	                              final Class<?> entityClass, final String identifyCode,
	                              final Object matchValue) throws SQLException {
		return this.lessEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final int sortCode, final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.equalTo(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final int sortCode, final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode,
	                            final String sqlFunction, final AbstractParameter<?>... functionParams) throws SQLException {
		return this.equalTo(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final int sortCode, final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.equalTo(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final int sortCode, final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Object matchValue) throws SQLException {
		return this.equalTo(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity   <span class="en-US">Target data table entity class</span>
	 *                       <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode     <span class="en-US">Target data column identification name</span>
	 *                       <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final int sortCode, final ConnectionCode connectionCode,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Class<?> targetEntity, final String targetCode) throws SQLException {
		return this.notEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				targetEntity, targetCode);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction    <span class="en-US">Function name</span>
	 *                       <span class="zh-CN">函数名称</span>
	 * @param functionParams <span class="en-US">Function parameter values</span>
	 *                       <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final int sortCode, final ConnectionCode connectionCode,
	                             final Class<?> entityClass, final String identifyCode,
	                             final String sqlFunction, final AbstractParameter<?>... functionParams) throws SQLException {
		return this.notEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				sqlFunction, functionParams);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final int sortCode, final ConnectionCode connectionCode,
	                             final Class<?> entityClass, final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.notEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue     <span class="en-US">Match value</span>
	 *                       <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final int sortCode, final ConnectionCode connectionCode,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Object matchValue) throws SQLException {
		return this.notEqual(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, matchValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition between certain two values</h3>
	 * <h3 class="zh-CN">添加介于某两个值之间的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue     <span class="en-US">Begin value</span>
	 *                       <span class="zh-CN">起始值</span>
	 * @param endValue       <span class="en-US">End value</span>
	 *                       <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder inRanges(final int sortCode, final ConnectionCode connectionCode,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Object beginValue, final Object endValue) throws SQLException {
		return this.inRanges(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				beginValue, endValue);
	}

	/**
	 * <h3 class="en-US">Add a query condition not between certain two values</h3>
	 * <h3 class="zh-CN">添加不介于某两个值之间的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue     <span class="en-US">Begin value</span>
	 *                       <span class="zh-CN">起始值</span>
	 * @param endValue       <span class="en-US">End value</span>
	 *                       <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notInRanges(final int sortCode, final ConnectionCode connectionCode,
	                                final Class<?> entityClass, final String identifyCode,
	                                final Object beginValue, final Object endValue) throws SQLException {
		return this.notInRanges(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode,
				beginValue, endValue);
	}

	/**
	 * <h3 class="en-US">Add query conditions for fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加模糊匹配值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule      <span class="en-US">match rule string</span>
	 *                       <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder like(final int sortCode, final ConnectionCode connectionCode,
	                         final Class<?> entityClass, final String identifyCode, final String matchRule) throws SQLException {
		return this.like(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, matchRule);
	}

	/**
	 * <h3 class="en-US">Add query conditions for not fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加非模糊匹配值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule      <span class="en-US">match rule string</span>
	 *                       <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notLike(final int sortCode, final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode, final String matchRule) throws SQLException {
		return this.notLike(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, matchRule);
	}

	/**
	 * <h3 class="en-US">Add query condition with null value</h3>
	 * <h3 class="zh-CN">添加空值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder matchNull(final int sortCode, final ConnectionCode connectionCode,
	                              final Class<?> entityClass, final String identifyCode) throws SQLException {
		return this.matchNull(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode);
	}

	/**
	 * <h3 class="en-US">Add query condition with not null value</h3>
	 * <h3 class="zh-CN">添加非空值的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notNull(final int sortCode, final ConnectionCode connectionCode,
	                            final Class<?> entityClass, final String identifyCode) throws SQLException {
		return this.notNull(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final int sortCode, final ConnectionCode connectionCode, final Class<?> entityClass,
	                       final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.in(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues    <span class="en-US">Condition data array</span>
	 *                       <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final int sortCode, final ConnectionCode connectionCode,
	                       final Class<?> entityClass, final String identifyCode, final Object... matchValues) throws SQLException {
		return this.in(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, matchValues);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final int sortCode, final ConnectionCode connectionCode, final Class<?> entityClass,
	                          final String identifyCode, final QueryData subQuery) throws SQLException {
		return this.notIn(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, subQuery);
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode   <span class="en-US">Data column identification code</span>
	 *                       <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues    <span class="en-US">Condition data array</span>
	 *                       <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final int sortCode, final ConnectionCode connectionCode,
	                          final Class<?> entityClass, final String identifyCode, final Object... matchValues) throws SQLException {
		return this.notIn(sortCode, connectionCode, Boolean.FALSE, entityClass, identifyCode, matchValues);
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @param functionName   <span class="en-US">Function name of sub-query</span>
	 *                       <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder exists(final int sortCode, final ConnectionCode connectionCode, final Class<?> entityClass,
	                           final QueryData subQuery, final String functionName) {
		return this.exists(sortCode, connectionCode, Boolean.FALSE, entityClass, subQuery, functionName);
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Entity class where the data column is located</span>
	 *                       <span class="zh-CN">数据列所在实体类</span>
	 * @param subQuery       <span class="en-US">Sub-query instance object</span>
	 *                       <span class="zh-CN">子查询实例对象</span>
	 * @param functionName   <span class="en-US">Function name of sub-query</span>
	 *                       <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder notExists(final int sortCode, final ConnectionCode connectionCode, final Class<?> entityClass,
	                              final QueryData subQuery, final String functionName) {
		return this.notExists(sortCode, connectionCode, Boolean.FALSE, entityClass, subQuery, functionName);
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final Object matchValue)
			throws SQLException {
		this.queryBuilder.greater(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				matchValue);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		this.queryBuilder.greater(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				this.entityFactory.tableName(targetEntity), this.entityFactory.columnName(targetEntity, targetCode));
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode,
	                            final String sqlFunction, final AbstractParameter<?>... functionParams)
			throws SQLException {
		this.queryBuilder.greater(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				sqlFunction, functionParams);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greater(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		this.queryBuilder.greater(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				subQuery);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final Class<?> targetEntity, final String targetCode) throws SQLException {
		this.queryBuilder.greaterEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				this.entityFactory.tableName(targetEntity), this.entityFactory.columnName(targetEntity, targetCode));
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final int sortCode, final ConnectionCode connectionCode,
	                                 final boolean havingCondition, final Class<?> entityClass,
	                                 final String identifyCode, final String sqlFunction,
	                                 final AbstractParameter<?>... functionParams) throws SQLException {
		this.queryBuilder.greaterEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				sqlFunction, functionParams);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final int sortCode, final ConnectionCode connectionCode,
	                                 final boolean havingCondition, final Class<?> entityClass,
	                                 final String identifyCode, final QueryData subQuery) throws SQLException {
		this.queryBuilder.greaterEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				subQuery);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition greater than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder greaterEqual(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                                 final Class<?> entityClass, final String identifyCode,
	                                 final Object matchValue) throws SQLException {
		this.queryBuilder.greaterEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				matchValue);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode,
	                         final Class<?> targetEntity, final String targetCode) throws SQLException {
		this.queryBuilder.less(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				this.entityFactory.tableName(targetEntity), this.entityFactory.columnName(targetEntity, targetCode));
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                         final AbstractParameter<?>... functionParams) throws SQLException {
		this.queryBuilder.less(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				sqlFunction, functionParams);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		this.queryBuilder.less(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				subQuery);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition less than a certain value</h3>
	 * <h3 class="zh-CN">添加大于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder less(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode, final Object matchValue)
			throws SQLException {
		this.queryBuilder.less(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				matchValue);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode,
	                              final Class<?> targetEntity, final String targetCode) throws SQLException {
		this.queryBuilder.lessEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				this.entityFactory.tableName(targetEntity), this.entityFactory.columnName(targetEntity, targetCode));
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode,
	                              final String sqlFunction, final AbstractParameter<?>... functionParams)
			throws SQLException {
		this.queryBuilder.lessEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				sqlFunction, functionParams);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		this.queryBuilder.lessEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				subQuery);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition less than or equal to a certain value</h3>
	 * <h3 class="zh-CN">添加大于等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder lessEqual(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode,
	                              final Object matchValue) throws SQLException {
		this.queryBuilder.lessEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				matchValue);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode,
	                            final Class<?> targetEntity, final String targetCode) throws SQLException {
		this.queryBuilder.equalTo(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				this.entityFactory.tableName(targetEntity), this.entityFactory.columnName(targetEntity, targetCode));
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final String sqlFunction,
	                            final AbstractParameter<?>... functionParams) throws SQLException {
		this.queryBuilder.equalTo(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				sqlFunction, functionParams);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		this.queryBuilder.equalTo(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				subQuery);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition equal to a certain value</h3>
	 * <h3 class="zh-CN">添加等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder equalTo(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final Object matchValue)
			throws SQLException {
		this.queryBuilder.equalTo(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				matchValue);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param targetEntity    <span class="en-US">Target data table entity class</span>
	 *                        <span class="zh-CN">目标数据表实体类</span>
	 * @param targetCode      <span class="en-US">Target data column identification name</span>
	 *                        <span class="zh-CN">目标数据列识别名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Class<?> targetEntity, final String targetCode) throws SQLException {
		this.queryBuilder.notEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				this.entityFactory.tableName(targetEntity), this.entityFactory.columnName(targetEntity, targetCode));
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param sqlFunction     <span class="en-US">Function name</span>
	 *                        <span class="zh-CN">函数名称</span>
	 * @param functionParams  <span class="en-US">Function parameter values</span>
	 *                        <span class="zh-CN">函数参数值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode,
	                             final String sqlFunction, final AbstractParameter<?>... functionParams)
			throws SQLException {
		this.queryBuilder.notEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				sqlFunction, functionParams);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		this.queryBuilder.notEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				subQuery);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition not equal to a certain value</h3>
	 * <h3 class="zh-CN">添加不等于某值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValue      <span class="en-US">Match value</span>
	 *                        <span class="zh-CN">匹配值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notEqual(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Object matchValue) throws SQLException {
		this.queryBuilder.notEqual(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				matchValue);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition between certain two values</h3>
	 * <h3 class="zh-CN">添加介于某两个值之间的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue      <span class="en-US">Begin value</span>
	 *                        <span class="zh-CN">起始值</span>
	 * @param endValue        <span class="en-US">End value</span>
	 *                        <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder inRanges(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                             final Class<?> entityClass, final String identifyCode,
	                             final Object beginValue, final Object endValue) throws SQLException {
		this.queryBuilder.inRanges(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				beginValue, endValue);
		return this;
	}

	/**
	 * <h3 class="en-US">Add a query condition not between certain two values</h3>
	 * <h3 class="zh-CN">添加不介于某两个值之间的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param beginValue      <span class="en-US">Begin value</span>
	 *                        <span class="zh-CN">起始值</span>
	 * @param endValue        <span class="en-US">End value</span>
	 *                        <span class="zh-CN">终止值</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notInRanges(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                                final Class<?> entityClass, final String identifyCode,
	                                final Object beginValue, final Object endValue) throws SQLException {
		this.queryBuilder.notInRanges(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				beginValue, endValue);
		return this;
	}

	/**
	 * <h3 class="en-US">Add query conditions for fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加模糊匹配值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule       <span class="en-US">match rule string</span>
	 *                        <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder like(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                         final Class<?> entityClass, final String identifyCode, final String matchRule)
			throws SQLException {
		this.queryBuilder.like(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				matchRule);
		return this;
	}

	/**
	 * <h3 class="en-US">Add query conditions for not fuzzy matching values</h3>
	 * <h3 class="zh-CN">添加非模糊匹配值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchRule       <span class="en-US">match rule string</span>
	 *                        <span class="zh-CN">匹配规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notLike(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode, final String matchRule)
			throws SQLException {
		this.queryBuilder.notLike(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode),
				matchRule);
		return this;
	}

	/**
	 * <h3 class="en-US">Add query condition with null value</h3>
	 * <h3 class="zh-CN">添加空值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder matchNull(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final String identifyCode) throws SQLException {
		this.queryBuilder.matchNull(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode));
		return this;
	}

	/**
	 * <h3 class="en-US">Add query condition with not null value</h3>
	 * <h3 class="zh-CN">添加非空值的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notNull(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                            final Class<?> entityClass, final String identifyCode) throws SQLException {
		this.queryBuilder.notNull(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), this.entityFactory.columnName(entityClass, identifyCode));
		return this;
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                       final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		this.queryBuilder.in(sortCode, connectionCode, havingCondition, this.entityFactory.tableName(entityClass),
				this.entityFactory.columnName(entityClass, identifyCode), subQuery);
		return this;
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is contained in the given data</h3>
	 * <h3 class="zh-CN">添加值包含在给定数据中的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues     <span class="en-US">Condition data array</span>
	 *                        <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder in(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                       final Class<?> entityClass, final String identifyCode, final Object... matchValues)
			throws SQLException {
		this.queryBuilder.in(sortCode, connectionCode, havingCondition, this.entityFactory.tableName(entityClass),
				this.entityFactory.columnName(entityClass, identifyCode), matchValues);
		return this;
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                          final Class<?> entityClass, final String identifyCode, final QueryData subQuery)
			throws SQLException {
		this.queryBuilder.notIn(sortCode, connectionCode, havingCondition, this.entityFactory.tableName(entityClass),
				this.entityFactory.columnName(entityClass, identifyCode), subQuery);
		return this;
	}

	/**
	 * <h3 class="en-US">Adds a query condition where the value is not contained in the given data</h3>
	 * <h3 class="zh-CN">添加值非包含在给定数据中的查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode    <span class="en-US">Data column identification code</span>
	 *                        <span class="zh-CN">数据列识别代码</span>
	 * @param matchValues     <span class="en-US">Condition data array</span>
	 *                        <span class="zh-CN">匹配值数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder notIn(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                          final Class<?> entityClass, final String identifyCode, final Object... matchValues)
			throws SQLException {
		this.queryBuilder.notIn(sortCode, connectionCode, havingCondition, this.entityFactory.tableName(entityClass),
				this.entityFactory.columnName(entityClass, identifyCode), matchValues);
		return this;
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @param functionName    <span class="en-US">Function name of sub-query</span>
	 *                        <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder exists(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                           final Class<?> entityClass, final QueryData subQuery, final String functionName) {
		this.queryBuilder.exists(sortCode, connectionCode, havingCondition, this.entityFactory.tableName(entityClass),
				subQuery, functionName);
		return this;
	}

	/**
	 * <h3 class="en-US">Adds a sub-query condition</h3>
	 * <h3 class="zh-CN">添加子查询条件</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param entityClass     <span class="en-US">Entity class where the data column is located</span>
	 *                        <span class="zh-CN">数据列所在实体类</span>
	 * @param subQuery        <span class="en-US">Sub-query instance object</span>
	 *                        <span class="zh-CN">子查询实例对象</span>
	 * @param functionName    <span class="en-US">Function name of sub-query</span>
	 *                        <span class="zh-CN">子查询函数名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder notExists(final int sortCode, final ConnectionCode connectionCode, final boolean havingCondition,
	                              final Class<?> entityClass, final QueryData subQuery, final String functionName) {
		this.queryBuilder.notExists(sortCode, connectionCode, havingCondition,
				this.entityFactory.tableName(entityClass), subQuery, functionName);
		return this;
	}

	/**
	 * <h3 class="en-US">Add data group condition</h3>
	 * <h3 class="zh-CN">添加数据筛选条件组</h3>
	 *
	 * @param sortCode       <span class="en-US">Sort code</span>
	 *                       <span class="zh-CN">排序代码</span>
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param conditions     <span class="en-US">Condition information array</span>
	 *                       <span class="zh-CN">条件信息数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder group(final int sortCode, final ConnectionCode connectionCode,
	                          @Nonnull final Condition... conditions) {
		return this.group(sortCode, connectionCode, Boolean.FALSE, conditions);
	}

	/**
	 * <h3 class="en-US">Add data group condition</h3>
	 * <h3 class="zh-CN">添加数据筛选条件组</h3>
	 *
	 * @param sortCode        <span class="en-US">Sort code</span>
	 *                        <span class="zh-CN">排序代码</span>
	 * @param connectionCode  <span class="en-US">Query connection code</span>
	 *                        <span class="zh-CN">查询条件连接代码</span>
	 * @param havingCondition <span class="en-US">Condition information is having condition</span>
	 *                        <span class="zh-CN">Having字句的条件信息</span>
	 * @param conditions      <span class="en-US">Condition information array</span>
	 *                        <span class="zh-CN">条件信息数组</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder group(final int sortCode, final ConnectionCode connectionCode,
	                          final boolean havingCondition, @Nonnull final Condition... conditions) {
		this.queryBuilder.group(sortCode, connectionCode, havingCondition, conditions);
		return this;
	}

	/**
	 * <h3 class="en-US">Add order by data column</h3>
	 * <h3 class="zh-CN">添加排序数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder addOrderBy(final Class<?> entityClass, final String identifyCode)
			throws SQLException {
		return this.addOrderBy(entityClass, identifyCode, OrderType.DESC);
	}

	/**
	 * <h3 class="en-US">Add order by data column</h3>
	 * <h3 class="zh-CN">添加排序数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param orderType    <span class="en-US">Query order type</span>
	 *                     <span class="zh-CN">查询结果集排序类型</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder addOrderBy(final Class<?> entityClass, final String identifyCode, final OrderType orderType)
			throws SQLException {
		return this.addOrderBy(entityClass, identifyCode, orderType, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * <h3 class="en-US">Add order by data column</h3>
	 * <h3 class="zh-CN">添加排序数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param orderType    <span class="en-US">Query order type</span>
	 *                     <span class="zh-CN">查询结果集排序类型</span>
	 * @param sortCode     <span class="en-US">Sort code</span>
	 *                     <span class="zh-CN">排序代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder addOrderBy(final Class<?> entityClass, final String identifyCode,
	                               final OrderType orderType, final int sortCode) throws SQLException {
		this.queryBuilder.addOrderBy(this.entityFactory.tableName(entityClass),
				this.entityFactory.columnName(entityClass, identifyCode), orderType, sortCode);
		return this;
	}

	/**
	 * <h3 class="en-US">Add group by data column</h3>
	 * <h3 class="zh-CN">添加分组数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder addGroupBy(final Class<?> entityClass, final String identifyCode) throws SQLException {
		return this.addGroupBy(entityClass, identifyCode, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * <h3 class="en-US">Add group by data column</h3>
	 * <h3 class="zh-CN">添加分组数据列</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class where the data column is located</span>
	 *                     <span class="zh-CN">数据列所在实体类</span>
	 * @param identifyCode <span class="en-US">Data column identification code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @param sortCode     <span class="en-US">Sort code</span>
	 *                     <span class="zh-CN">排序代码</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public QueryBuilder addGroupBy(final Class<?> entityClass, final String identifyCode, final int sortCode)
			throws SQLException {
		this.queryBuilder.addGroupBy(this.entityFactory.tableName(entityClass),
				this.entityFactory.columnName(entityClass, identifyCode), sortCode);
		return this;
	}

	/**
	 * <h3 class="en-US">Setting for query result can cacheable</h3>
	 * <h3 class="zh-CN">设置查询结果可以缓存</h3>
	 *
	 * @param cacheables <span class="en-US">Query result can cacheable</span>
	 *                   <span class="zh-CN">查询结果可以缓存</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public QueryBuilder useCache(final boolean cacheables) {
		this.queryBuilder.useCache(cacheables);
		return this;
	}

	/**
	 * <h3 class="en-US">Setting for pager information</h3>
	 * <h3 class="zh-CN">设置分页信息</h3>
	 *
	 * @param pageNo    <span class="en-US">Current page number</span>
	 *                  <span class="zh-CN">当前页数</span>
	 * @param pageLimit <span class="en-US">Page limit records count</span>
	 *                  <span class="zh-CN">每页的记录数</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the configuration information is invalid</span>
	 *                      <span class="zh-CN">如果配置信息错误</span>
	 */
	public QueryBuilder configPager(final int pageNo, final int pageLimit) throws SQLException {
		this.queryBuilder.configPager(pageNo, pageLimit);
		return this;
	}

	@Override
	public QueryInfo confirm() {
		if (this.queryBuilder.itemListIsEmpty()) {
			String tableName = this.entityFactory.tableName(this.entityClass);
			Optional.ofNullable(this.entityFactory.tableConfig(this.entityClass))
					.map(EntityFactory.TableConfig::getTableDefine)
					.map(TableDefine::getColumnDefines)
					.ifPresent(columnDefines ->
							columnDefines.stream()
									.filter(columnDefine -> !columnDefine.isLazyLoad())
									.forEach(columnDefine -> {
										try {
											this.queryBuilder.queryColumn(tableName, columnDefine.getColumnName());
										} catch (SQLException ignore) {
										}
									}));
		}
		return this.queryBuilder.confirm();
	}
}
