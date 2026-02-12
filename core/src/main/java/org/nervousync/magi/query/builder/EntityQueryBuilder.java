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
import jakarta.persistence.LockModeType;
import org.intellij.lang.annotations.MagicConstant;
import org.nervousync.brain.defines.ColumnDefine;
import org.nervousync.brain.enumerations.query.FromType;
import org.nervousync.brain.enumerations.query.ItemType;
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.brain.query.builder.*;
import org.nervousync.brain.query.condition.Condition;
import org.nervousync.brain.query.condition.impl.ColumnCondition;
import org.nervousync.brain.query.condition.impl.GroupCondition;
import org.nervousync.brain.query.core.AbstractQuery;
import org.nervousync.brain.query.core.QueryFrom;
import org.nervousync.brain.query.core.QueryItem;
import org.nervousync.brain.query.core.SortedItem;
import org.nervousync.brain.query.from.FromSubQuery;
import org.nervousync.brain.query.from.FromTable;
import org.nervousync.brain.query.item.*;
import org.nervousync.brain.query.join.QueryJoin;
import org.nervousync.brain.query.join.SubQueryJoin;
import org.nervousync.brain.query.join.TableQueryJoin;
import org.nervousync.brain.query.param.AbstractParameter;
import org.nervousync.brain.query.param.impl.*;
import org.nervousync.brain.query.sort.GroupBy;
import org.nervousync.brain.query.sort.OrderBy;
import org.nervousync.brain.query.subqueries.ScalarSubQuery;
import org.nervousync.brain.query.subqueries.TableSubQuery;
import org.nervousync.builder.Builder;
import org.nervousync.builder.ParentBuilder;
import org.nervousync.commons.Globals;
import org.nervousync.enumerations.beans.StringType;
import org.nervousync.enumerations.security.EncodeType;
import org.nervousync.exceptions.builder.BuilderException;
import org.nervousync.magi.entity.EntityFactory;
import org.nervousync.utils.core.BeanUtils;
import org.nervousync.utils.core.StringUtils;
import org.nervousync.utils.logger.LoggerUtils;
import org.nervousync.utils.security.SecurityUtils;

import java.sql.SQLException;
import java.util.*;

/**
 * <h2 class="en-US">Entity class query information builder</h2>
 * <h2 class="zh-CN">实体类查询信息构建器</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
 */
@SuppressWarnings("unused")
public final class EntityQueryBuilder extends ParentBuilder implements Builder<QueryInfo> {

	/**
	 * <span class="en-US">Logger instance</span>
	 * <span class="zh-CN">日志实例</span>
	 */
	private static final LoggerUtils.Logger LOGGER = LoggerUtils.getLogger(EntityQueryBuilder.class);

	/**
	 * <span class="en-US">Sheet name</span>
	 * <span class="zh-CN">工作表名称</span>
	 */
	private String sheetName;
	/**
	 * <span class="en-US">Data table entity class</span>
	 * <span class="zh-CN">数据表实体类</span>
	 */
	private final Class<?> entityClass;
	/**
	 * <span class="en-US">Query from information list</span>
	 * <span class="zh-CN">查询来源信息列表</span>
	 */
	private final QueryFrom queryFrom;
	/**
	 * <span class="en-US">Related query information list</span>
	 * <span class="zh-CN">关联查询信息列表</span>
	 */
	private final List<QueryJoin> queryJoins;
	/**
	 * <span class="en-US">Query items information list</span>
	 * <span class="zh-CN">查询项目信息列表</span>
	 */
	@Nonnull
	private final List<QueryItem> itemList;
	/**
	 * <span class="en-US">Query condition instance list</span>
	 * <span class="zh-CN">查询条件实例对象列表</span>
	 */
	private final List<Condition> conditionList;
	/**
	 * <span class="en-US">Query order by column list</span>
	 * <span class="zh-CN">查询排序数据列列表</span>
	 */
	private final List<OrderBy> orderByList;
	/**
	 * <span class="en-US">Query group by columns list</span>
	 * <span class="zh-CN">查询分组数据列列表</span>
	 */
	private final List<GroupBy> groupByList;
	/**
	 * <span class="en-US">Group having condition instance list</span>
	 * <span class="zh-CN">分组筛选条件实例对象列表</span>
	 */
	private final List<Condition> havingList;
	/**
	 * <span class="en-US">Query result can cacheable</span>
	 * <span class="zh-CN">查询结果可以缓存</span>
	 */
	private boolean cacheables = Boolean.FALSE;
	/**
	 * <span class="en-US">Current page number</span>
	 * <span class="zh-CN">当前页数</span>
	 */
	private int pageNo = Globals.DEFAULT_VALUE_INT;
	/**
	 * <span class="en-US">Page limit records count</span>
	 * <span class="zh-CN">每页的记录数</span>
	 */
	private int pageLimit = Globals.DEFAULT_VALUE_INT;
	/**
	 * <span class="en-US">Query result using for update record</span>
	 * <span class="zh-CN">查询结果用于更新记录</span>
	 */
	private boolean forUpdate = Boolean.FALSE;
	/**
	 * <span class="en-US">Query record lock option</span>
	 * <span class="zh-CN">查询记录锁定选项</span>
	 */
	private LockModeType lockOption = LockModeType.NONE;

	/**
	 * <h3 class="en-US">Private constructor method for query plan builder</h3>
	 * <h3 class="zh-CN">查询计划构建器的私有构造方法</h3>
	 *
	 * @param entityClass <span class="en-US">Data table entity class</span>
	 *                    <span class="zh-CN">数据表实体类</span>
	 * @param aliasName   <span class="en-US">Alias name</span>
	 *                    <span class="zh-CN">别名</span>
	 */
	private EntityQueryBuilder(final Class<?> entityClass, final String aliasName) {
		this.entityClass = entityClass;
		this.queryFrom = new FromTable();
		((FromTable) this.queryFrom).setTableName(EntityFactory.getInstance().tableName(entityClass));
		if (StringUtils.notBlank(aliasName)) {
			this.queryFrom.setAliasName(aliasName);
		}
		this.queryJoins = new ArrayList<>();
		this.itemList = new ArrayList<>();
		this.conditionList = new ArrayList<>();
		this.orderByList = new ArrayList<>();
		this.groupByList = new ArrayList<>();
		this.havingList = new ArrayList<>();
	}

	/**
	 * <h3 class="en-US">Static method used to initialize query plan builder</h3>
	 * <h3 class="zh-CN">静态方法用于初始化查询计划构建器</h3>
	 *
	 * @param entityClass <span class="en-US">Query driven table entity class</span>
	 *                    <span class="zh-CN">查询驱动表实体类</span>
	 * @return <span class="en-US">Query builder instance object</span>
	 * <span class="zh-CN">查询构建器实例对象</span>
	 */
	public static EntityQueryBuilder newBuilder(final Class<?> entityClass) {
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
	 */
	public static EntityQueryBuilder newBuilder(final Class<?> entityClass, final String aliasName) {
		return new EntityQueryBuilder(entityClass, aliasName);
	}

	/**
	 * <h3 class="en-US">Setting the sheet name value</h3>
	 * <h3 class="zh-CN">设置工作表名称</h3>
	 *
	 * @param sheetName <span class="en-US">Sheet name</span>
	 *                  <span class="zh-CN">工作表名称</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public EntityQueryBuilder sheetName(final String sheetName) {
		this.sheetName = sheetName;
		return this;
	}

	/**
	 * <h3 class="en-US">Entity query items information list builder</h3>
	 * <h3 class="zh-CN">实体类查询项目列表构建器</h3>
	 *
	 * @return <span class="en-US">Entity query items information list builder instance object</span>
	 * <span class="zh-CN">实体类查询项目列表构建器实例对象</span>
	 */
	public EntityItemsBuilder<EntityQueryBuilder> items() {
		return new EntityItemsBuilder<>(this, this.itemList);
	}

	/**
	 * <h3 class="en-US">Entity query joins information list builder</h3>
	 * <h3 class="zh-CN">实体类查询关联信息列表构建器</h3>
	 *
	 * @return <span class="en-US">Entity query joins information list builder instance object</span>
	 * <span class="zh-CN">实体类查询关联信息列表构建器实例对象</span>
	 */
	public EntityJoinsBuilder<EntityQueryBuilder> joins() {
		return new EntityJoinsBuilder<>(this, this.queryJoins);
	}

	/**
	 * <h3 class="en-US">Entity query conditions information builder</h3>
	 * <h3 class="zh-CN">实体类查询条件组构建器</h3>
	 *
	 * @return <span class="en-US">Entity query conditions information builder instance object</span>
	 * <span class="zh-CN">实体类查询条件组构建器实例对象</span>
	 */
	public EntityConditionsBuilder<EntityQueryBuilder> where() {
		return new EntityConditionsBuilder<>(this, Boolean.FALSE, this.conditionList);
	}

	/**
	 * <h3 class="en-US">Entity query group by data list builder</h3>
	 * <h3 class="zh-CN">实体类分组数据列构建器</h3>
	 *
	 * @return <span class="en-US">Entity query group by data list builder instance object</span>
	 * <span class="zh-CN">实体类分组数据列构建器实例对象</span>
	 */
	public EntitySortsBuilder.GroupItemsBuilder<EntityQueryBuilder> groups() {
		return new EntitySortsBuilder.GroupItemsBuilder<>(this, this.groupByList);
	}

	/**
	 * <h3 class="en-US">Entity query having conditions information builder</h3>
	 * <h3 class="zh-CN">实体类Having条件组构建器</h3>
	 *
	 * @return <span class="en-US">Entity query having conditions information builder instance object</span>
	 * <span class="zh-CN">实体类Having条件组构建器实例对象</span>
	 */
	public EntityConditionsBuilder<EntityQueryBuilder> having() {
		return new EntityConditionsBuilder<>(this, Boolean.TRUE, this.havingList);
	}

	/**
	 * <h3 class="en-US">Entity query order by data list builder</h3>
	 * <h3 class="zh-CN">实体类排序数据列构建器</h3>
	 *
	 * @return <span class="en-US">Entity query order by data list builder instance object</span>
	 * <span class="zh-CN">实体类排序数据列构建器实例对象</span>
	 */
	public EntitySortsBuilder.OrderItemsBuilder<EntityQueryBuilder> orders() {
		return new EntitySortsBuilder.OrderItemsBuilder<>(this, this.orderByList);
	}

	/**
	 * <h3 class="en-US">Setting for the query result can cacheable</h3>
	 * <h3 class="zh-CN">设置查询结果可以缓存</h3>
	 *
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public EntityQueryBuilder useCache() {
		this.cacheables = Boolean.TRUE;
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
	public EntityQueryBuilder pager(final int pageNo, final int pageLimit) throws SQLException {
		if (pageNo <= Globals.INITIALIZE_INT_VALUE || pageLimit <= Globals.INITIALIZE_INT_VALUE) {
			throw new MultilingualSQLException(0x00DB00010013L, pageNo, pageLimit);
		}
		this.pageNo = pageNo;
		this.pageLimit = pageLimit;
		return this;
	}

	/**
	 * <h3 class="en-US">Setting for the query result will use for update records</h3>
	 * <h3 class="zh-CN">设置查询结果用于更新数据</h3>
	 *
	 * @param lockOption <span class="en-US">Query record lock option</span>
	 *                   <span class="zh-CN">查询记录锁定选项</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public EntityQueryBuilder forUpdate(@MagicConstant(valuesFromClass = LockModeType.class) final LockModeType lockOption) {
		this.forUpdate = Boolean.TRUE;
		this.lockOption = lockOption;
		return this;
	}

	@Override
	public QueryInfo build() throws BuilderException {
		if (this.itemList.isEmpty()) {
			try {
				EntityItemsBuilder<EntityQueryBuilder> itemsBuilder = this.items();
				EntityFactory.TableConfig tableConfig = EntityFactory.getInstance().tableConfig(entityClass);
				for (ColumnDefine columnDefine : tableConfig.queryColumns()) {
					String columnName = columnDefine.getColumnName();
					itemsBuilder =
							itemsBuilder.column(entityClass, columnName).aliasName(tableConfig.fieldName(columnName)).confirm();
				}
				itemsBuilder.confirm();
			} catch (SQLException e) {
				throw new BuilderException(0x00DB00000042L, e);
			}
		}
		QueryInfo queryInfo = new QueryInfo();
		queryInfo.setCacheKey(this.cacheKey());
		queryInfo.setSheetName(this.sheetName);
		queryInfo.setQueryFrom(this.queryFrom);
		queryInfo.setQueryJoins(this.queryJoins);
		queryInfo.setItemList(this.itemList);
		queryInfo.setConditionList(this.conditionList);
		queryInfo.setOrderByList(this.orderByList);
		queryInfo.setGroupByList(this.groupByList);
		queryInfo.setOrderByList(this.orderByList);
		queryInfo.setHavingList(this.havingList);
		queryInfo.setCacheables(this.cacheables);
		queryInfo.setPageNo(this.pageNo);
		queryInfo.setPageLimit(this.pageLimit);
		queryInfo.setForUpdate(this.forUpdate);
		queryInfo.setLockOption(this.lockOption);
		return queryInfo;
	}

	@Override
	public void confirm(final Object object) throws BuilderException {
		if (object instanceof ItemsBuilder.Items) {
			this.itemList.clear();
			this.itemList.addAll(((ItemsBuilder.Items) object).getItemList());
		} else if (object instanceof JoinsBuilder.Joins) {
			this.queryJoins.clear();
			this.queryJoins.addAll(((JoinsBuilder.Joins) object).getJoinList());
		} else if (object instanceof ConditionsBuilder.Conditions) {
			ConditionsBuilder.Conditions conditions = (ConditionsBuilder.Conditions) object;
			if (conditions.isHaving()) {
				this.havingList.clear();
				this.havingList.addAll(conditions.getConditions());
			} else {
				this.conditionList.clear();
				this.conditionList.addAll(conditions.getConditions());
			}
		} else if (object instanceof SortsBuilder.GroupByItems) {
			this.groupByList.clear();
			this.groupByList.addAll(((SortsBuilder.GroupByItems) object).getItemList());
		} else if (object instanceof SortsBuilder.OrderByItems) {
			this.orderByList.clear();
			this.orderByList.addAll(((SortsBuilder.OrderByItems) object).getItemList());
		}
	}

	/**
	 * <h3 class="en-US">Calculate the cache key value of the given query information</h3>
	 * <h3 class="zh-CN">计算查询信息的缓存键值</h3>
	 *
	 * @return <span class="en-US">Cache key value</span>
	 * <span class="zh-CN">缓存键值</span>
	 */
	@Nonnull
	private String cacheKey() {
		if (!this.cacheables || this.forUpdate) {
			//  Return an empty string if query information defines can't cacheables or the query result will use it for update records
			return Globals.DEFAULT_VALUE_STRING;
		}
		TreeMap<String, Object> cacheMap = new TreeMap<>();
		cacheMap.put("items", this.itemsList(this.itemList));
		cacheMap.put("from", this.cacheMap(this.queryFrom));
		cacheMap.put("joins", this.joinsList(this.queryJoins));
		cacheMap.put("where", this.conditionsList(this.conditionList));
		cacheMap.put("having", this.conditionsList(this.havingList));
		cacheMap.put("order", this.orderList(this.orderByList));
		cacheMap.put("group", this.groupList(this.groupByList));
		cacheMap.put("page", this.pageNo);
		cacheMap.put("limit", this.pageLimit);
		String jsonData = BeanUtils.objectToString(cacheMap, StringType.JSON, Boolean.FALSE);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Query_Cache_Debug", jsonData);
		}
		return SecurityUtils.SHA256(jsonData, EncodeType.HEX);
	}

	/**
	 * <h3 class="en-US">Convert the query item information list into the list of data mapping table</h3>
	 * <h3 class="zh-CN">转换查询项信息列表为数据映射表列表</h3>
	 *
	 * @param itemList <span class="en-US">Query item information list</span>
	 *                 <span class="zh-CN">查询项信息列表</span>
	 * @return <span class="en-US">List of data mapping table</span>
	 * <span class="zh-CN">数据映射表列表</span>
	 */
	@Nonnull
	private List<TreeMap<String, Object>> itemsList(@Nonnull final List<QueryItem> itemList) {
		itemList.sort(SortedItem.desc());
		List<TreeMap<String, Object>> parameterList = new ArrayList<>();
		itemList.stream().filter(Objects::nonNull).forEach(queryItem -> parameterList.add(cacheMap(queryItem)));
		return parameterList;
	}

	/**
	 * <h3 class="en-US">Convert query item information into a data mapping table</h3>
	 * <h3 class="zh-CN">转换查询项信息为数据映射表</h3>
	 *
	 * @param queryItem <span class="en-US">Query item information</span>
	 *                  <span class="zh-CN">查询项信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 */
	@Nonnull
	private TreeMap<String, Object> cacheMap(@Nonnull final QueryItem queryItem) {
		TreeMap<String, Object> cacheMap = new TreeMap<>();
		switch (queryItem.getItemType()) {
			case CALCULATE:
				cacheMap.put("type", ItemType.CALCULATE);
				cacheMap.put("calculate", ((CalculateItem) queryItem).getCalculateCode());
				cacheMap.put("items", this.itemsList(((CalculateItem) queryItem).getCalculateItems()));
				break;
			case COLUMN:
				cacheMap.put("type", ItemType.COLUMN);
				cacheMap.put("table", ((ColumnItem) queryItem).getTableName());
				cacheMap.put("column", ((ColumnItem) queryItem).getColumnName());
				break;
			case CONSTANT:
				cacheMap.put("type", ItemType.CONSTANT);
				cacheMap.put("value", ((ConstantItem) queryItem).getConstantValue());
				break;
			case FUNCTION:
				cacheMap.put("type", ItemType.FUNCTION);
				cacheMap.put("function", ((FunctionItem) queryItem).getFunctionName());
				cacheMap.put("parameters", this.parametersList(((FunctionItem) queryItem).getFunctionParams()));
				break;
			case QUERY:
				cacheMap.put("type", ItemType.QUERY);
				cacheMap.put("query", this.cacheMap(((SubQueryItem) queryItem).getQueryData()));
				break;
		}
		return cacheMap;
	}

	/**
	 * <h3 class="en-US">Convert the query from information into a data mapping table</h3>
	 * <h3 class="zh-CN">转换查询来源信息为数据映射表</h3>
	 *
	 * @param queryFrom <span class="en-US">Query from information</span>
	 *                  <span class="zh-CN">查询来源信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 */
	@Nonnull
	private TreeMap<String, Object> cacheMap(@Nonnull final QueryFrom queryFrom) {
		TreeMap<String, Object> cacheMap = new TreeMap<>();
		switch (queryFrom.getFromType()) {
			case Table:
				cacheMap.put("type", FromType.Table);
				cacheMap.put("table", ((FromTable) queryFrom).getTableName());
				break;
			case SubQuery:
				cacheMap.put("type", FromType.SubQuery);
				cacheMap.put("subQuery", cacheMap(((FromSubQuery) queryFrom).getQueryData()));
				break;
		}
		return cacheMap;
	}

	/**
	 * <h3 class="en-US">Convert the query parameter information list into the list of data mapping table</h3>
	 * <h3 class="zh-CN">转换查询参数信息列表为数据映射表列表</h3>
	 *
	 * @param abstractParameters <span class="en-US">Query parameter information list</span>
	 *                           <span class="zh-CN">查询参数信息列表</span>
	 * @return <span class="en-US">List of data mapping table</span>
	 * <span class="zh-CN">数据映射表列表</span>
	 */
	@Nonnull
	private List<TreeMap<String, Object>> parametersList(@Nonnull final List<AbstractParameter<?>> abstractParameters) {
		abstractParameters.sort(SortedItem.desc());
		List<TreeMap<String, Object>> parameterList = new ArrayList<>();
		abstractParameters.forEach(parameter -> parameterList.add(this.cacheMap(parameter)));
		return parameterList;
	}

	/**
	 * <h3 class="en-US">Convert parameter information into data mapping table</h3>
	 * <h3 class="zh-CN">转换参数信息为数据映射表</h3>
	 *
	 * @param parameter <span class="en-US">Parameter information</span>
	 *                  <span class="zh-CN">参数信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 */
	@Nonnull
	private TreeMap<String, Object> cacheMap(final AbstractParameter<?> parameter) {
		TreeMap<String, Object> cacheMap = new TreeMap<>();
		if (parameter != null) {
			switch (parameter.getItemType()) {
				case ARRAY:
					cacheMap.put("type", ItemType.ARRAY);
					cacheMap.put("arrays", ((ArraysParameter) parameter).getItemValue().getArrayObject());
					break;
				case CALCULATE:
					cacheMap.put("type", ItemType.CALCULATE);
					cacheMap.put("calculate", ((CalculateParameter) parameter).getItemValue().getCalculateCode());
					cacheMap.put("items", this.itemsList(((CalculateParameter) parameter).getItemValue().getCalculateItems()));
					break;
				case COLUMN:
					cacheMap.put("type", ItemType.COLUMN);
					cacheMap.put("table", ((ColumnParameter) parameter).getItemValue().getTableName());
					cacheMap.put("column", ((ColumnParameter) parameter).getItemValue().getColumnName());
					break;
				case CONSTANT:
					cacheMap.put("type", ItemType.CONSTANT);
					cacheMap.put("value", ((ConstantParameter) parameter).getItemValue());
					break;
				case FUNCTION:
					cacheMap.put("type", ItemType.FUNCTION);
					FunctionItem functionItem = ((FunctionParameter) parameter).getItemValue();
					cacheMap.put("function", functionItem.getFunctionName());
					cacheMap.put("parameters", this.parametersList(functionItem.getFunctionParams()));
					break;
				case QUERY:
					cacheMap.put("type", ItemType.QUERY);
					cacheMap.put("function", ((QueryParameter) parameter).getFunctionName());
					cacheMap.put("query", this.cacheMap(((QueryParameter) parameter).getItemValue()));
					break;
				case RANGE:
					cacheMap.put("type", ItemType.RANGE.toString());
					cacheMap.put("begin", ((RangesParameter) parameter).getItemValue().getBeginValue());
					cacheMap.put("end", ((RangesParameter) parameter).getItemValue().getEndValue());
					break;
			}
		}
		return cacheMap;
	}

	/**
	 * <h3 class="en-US">Convert sub-query information into data mapping table</h3>
	 * <h3 class="zh-CN">转换子查询信息为数据映射表</h3>
	 *
	 * @param queryData <span class="en-US">Sub-query information</span>
	 *                  <span class="zh-CN">子查询信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 */
	@Nonnull
	private TreeMap<String, Object> cacheMap(@Nonnull final AbstractQuery queryData) {
		TreeMap<String, Object> cacheMap = new TreeMap<>();
		List<QueryItem> itemList = new ArrayList<>();
		switch (queryData.getQueryType()) {
			case SCALAR:
				itemList.add(((ScalarSubQuery) queryData).getQueryItem());
				break;
			case TABLE:
				itemList.addAll(((TableSubQuery) queryData).getItemList());
				break;
		}
		cacheMap.put("items", this.itemsList(itemList));
		cacheMap.put("from", this.cacheMap(queryData.getQueryFrom()));
		cacheMap.put("joins", this.joinsList(queryData.getQueryJoins()));
		cacheMap.put("where", this.conditionsList(queryData.getConditionList()));
		cacheMap.put("having", this.conditionsList(queryData.getHavingList()));
		cacheMap.put("group", this.groupList(queryData.getGroupByList()));
		return cacheMap;
	}

	/**
	 * <h3 class="en-US">Convert the query join information list into the list of data mapping table</h3>
	 * <h3 class="zh-CN">转换查询关联信息列表为数据映射表列表</h3>
	 *
	 * @param queryJoinList <span class="en-US">Query join information list</span>
	 *                      <span class="zh-CN">查询关联信息列表</span>
	 * @return <span class="en-US">List of data mapping table</span>
	 * <span class="zh-CN">数据映射表列表</span>
	 */
	@Nonnull
	private List<TreeMap<String, Object>> joinsList(@Nonnull final List<QueryJoin> queryJoinList) {
		if (queryJoinList.isEmpty()) {
			return Collections.emptyList();
		}
		List<TreeMap<String, Object>> joinList = new ArrayList<>();
		queryJoinList.forEach(queryJoin -> joinList.add(this.cacheMap(queryJoin)));
		return joinList;
	}

	/**
	 * <h3 class="en-US">Convert query join information into a data mapping table</h3>
	 * <h3 class="zh-CN">转换查询关联信息为数据映射表</h3>
	 *
	 * @param queryJoin <span class="en-US">Query join information</span>
	 *                  <span class="zh-CN">查询关联信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 */
	@Nonnull
	private TreeMap<String, Object> cacheMap(@Nonnull final QueryJoin queryJoin) {
		TreeMap<String, Object> cacheMap = new TreeMap<>();
		if (queryJoin instanceof TableQueryJoin) {
			cacheMap.put("table", ((TableQueryJoin) queryJoin).getJoinTable());
		} else if (queryJoin instanceof SubQueryJoin) {
			cacheMap.put("subQuery", cacheMap(((SubQueryJoin) queryJoin).getSubQuery()));
		}
		cacheMap.put("type", queryJoin.getJoinType());

		List<TreeMap<String, Object>> joinColumns = new ArrayList<>();
		queryJoin.getJoinInfos().forEach(joinInfo -> {
			TreeMap<String, Object> joinMap = new TreeMap<>();
			joinMap.put("connection", joinInfo.getConnectionCode());
			joinMap.put("condition", joinInfo.getConditionCode());
			joinMap.put("leftKey", joinInfo.getLeftKey());
			joinMap.put("rightKey", joinInfo.getRightKey());
			joinColumns.add(joinMap);
		});
		cacheMap.put("joinColumns", joinColumns);

		return cacheMap;
	}

	/**
	 * <h3 class="en-US">Convert the query condition information list into the list of data mapping table</h3>
	 * <h3 class="zh-CN">转换查询匹配信息列表为数据映射表列表</h3>
	 *
	 * @param conditionList <span class="en-US">Query condition information list</span>
	 *                      <span class="zh-CN">查询匹配信息列表</span>
	 * @return <span class="en-US">List of data mapping table</span>
	 * <span class="zh-CN">数据映射表列表</span>
	 */
	@Nonnull
	private List<TreeMap<String, Object>> conditionsList(@Nonnull final List<Condition> conditionList) {
		if (conditionList.isEmpty()) {
			return Collections.emptyList();
		}
		conditionList.sort(SortedItem.desc());
		List<TreeMap<String, Object>> conditions = new ArrayList<>();
		conditionList.forEach(condition -> conditions.add(this.cacheMap(condition)));
		return conditions;
	}

	/**
	 * <h3 class="en-US">Convert query condition information into a data mapping table</h3>
	 * <h3 class="zh-CN">转换查询匹配信息为数据映射表</h3>
	 *
	 * @param condition <span class="en-US">Query condition information</span>
	 *                  <span class="zh-CN">查询匹配信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 */
	@Nonnull
	private TreeMap<String, Object> cacheMap(@Nonnull final Condition condition) {
		TreeMap<String, Object> cacheMap = new TreeMap<>();
		cacheMap.put("connection", condition.getConnectionCode());
		cacheMap.put("type", condition.getConditionType());
		switch (condition.getConditionType()) {
			case COLUMN:
				cacheMap.put("condition", ((ColumnCondition) condition).getConditionCode());
				cacheMap.put("table", ((ColumnCondition) condition).getTableName());
				cacheMap.put("column", ((ColumnCondition) condition).getColumnName());
				cacheMap.put("function", ((ColumnCondition) condition).getFunctionName());
				cacheMap.put("parameter", this.cacheMap(((ColumnCondition) condition).getConditionParameter()));
				break;
			case GROUP:
				cacheMap.put("conditions", this.conditionsList(((GroupCondition) condition).getConditionList()));
				break;
		}
		return cacheMap;
	}

	/**
	 * <h3 class="en-US">Convert the query order by list into the list of data mapping table</h3>
	 * <h3 class="zh-CN">转换查询排序信息列表为数据映射表列表</h3>
	 *
	 * @param orderByList <span class="en-US">Query order by list</span>
	 *                    <span class="zh-CN">查询排序信息列表</span>
	 * @return <span class="en-US">List of data mapping table</span>
	 * <span class="zh-CN">数据映射表列表</span>
	 */
	@Nonnull
	private List<TreeMap<String, Object>> orderList(@Nonnull final List<OrderBy> orderByList) {
		if (orderByList.isEmpty()) {
			return Collections.emptyList();
		}
		orderByList.sort(SortedItem.desc());
		List<TreeMap<String, Object>> orderList = new ArrayList<>();
		orderByList.forEach(orderBy -> {
			TreeMap<String, Object> cacheMap = new TreeMap<>();
			cacheMap.put("table", orderBy.getTableName());
			cacheMap.put("column", orderBy.getColumnName());
			cacheMap.put("type", orderBy.getOrderType());
			orderList.add(cacheMap);
		});
		return orderList;
	}

	/**
	 * <h3 class="en-US">Convert the query group by list into the list of data mapping table</h3>
	 * <h3 class="zh-CN">转换查询分组信息列表为数据映射表列表</h3>
	 *
	 * @param groupByList <span class="en-US">Query group by list</span>
	 *                    <span class="zh-CN">查询分组信息列表</span>
	 * @return <span class="en-US">List of data mapping table</span>
	 * <span class="zh-CN">数据映射表列表</span>
	 */
	@Nonnull
	private List<TreeMap<String, String>> groupList(@Nonnull final List<GroupBy> groupByList) {
		if (groupByList.isEmpty()) {
			return Collections.emptyList();
		}
		groupByList.sort(SortedItem.desc());
		List<TreeMap<String, String>> groupList = new ArrayList<>();
		groupByList.forEach(groupBy -> {
			TreeMap<String, String> cacheMap = new TreeMap<>();
			cacheMap.put("table", groupBy.getTableName());
			cacheMap.put("column", groupBy.getColumnName());
			groupList.add(cacheMap);
		});
		return groupList;
	}
}
