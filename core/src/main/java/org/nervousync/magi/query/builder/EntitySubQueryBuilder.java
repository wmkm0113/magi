package org.nervousync.magi.query.builder;

import jakarta.annotation.Nonnull;
import org.intellij.lang.annotations.MagicConstant;
import org.nervousync.brain.enumerations.query.CalculateCode;
import org.nervousync.brain.query.builder.*;
import org.nervousync.brain.query.condition.Condition;
import org.nervousync.brain.query.core.AbstractQuery;
import org.nervousync.brain.query.core.QueryFrom;
import org.nervousync.brain.query.core.QueryItem;
import org.nervousync.brain.query.from.FromSubQuery;
import org.nervousync.brain.query.from.FromTable;
import org.nervousync.brain.query.join.QueryJoin;
import org.nervousync.brain.query.sort.GroupBy;
import org.nervousync.brain.query.subqueries.ScalarSubQuery;
import org.nervousync.brain.query.subqueries.TableSubQuery;
import org.nervousync.builder.AbstractBuilder;
import org.nervousync.builder.ParentBuilder;
import org.nervousync.commons.Globals;
import org.nervousync.exceptions.builder.BuilderException;
import org.nervousync.magi.entity.EntityFactory;
import org.nervousync.utils.core.StringUtils;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2 class="en-US">Entity Sub-query information list builder</h2>
 * <h2 class="zh-CN">实体类子查询信息构建器</h2>
 *
 * @param <P> <span class="en-US">Parent builder generic type class</span>
 *            <span class="zh-CN">父构建器泛型类</span>
 * @param <T> <span class="en-US">Sub-query information generic type class</span>
 *            <span class="zh-CN">子查询信息泛型类</span>
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
 */
@SuppressWarnings("unused")
public abstract class EntitySubQueryBuilder<P extends ParentBuilder, T extends AbstractQuery>
		extends AbstractBuilder<P, T> {

	/**
	 * <span class="en-US">Query item instance list</span>
	 * <span class="zh-CN">查询项目实例对象列表</span>
	 */
	protected final List<QueryItem> itemList;
	/**
	 * <span class="en-US">Query from information</span>
	 * <span class="zh-CN">查询来源信息</span>
	 */
	protected QueryFrom queryFrom;
	/**
	 * <span class="en-US">Related query information list</span>
	 * <span class="zh-CN">关联查询信息列表</span>
	 */
	protected final List<QueryJoin> queryJoins;
	/**
	 * <span class="en-US">Query condition instance list</span>
	 * <span class="zh-CN">查询条件实例对象列表</span>
	 */
	protected final List<Condition> conditionList;
	/**
	 * <span class="en-US">Identify key</span>
	 * <span class="zh-CN">分组识别代码列表</span>
	 */
	protected final List<GroupBy> groupByList;
	/**
	 * <span class="en-US">Group having condition instance list</span>
	 * <span class="zh-CN">分组筛选条件实例对象列表</span>
	 */
	protected final List<Condition> havingList;

	/**
	 * <h3 class="en-US">Protected constructor for AbstractBuilder</h3>
	 * <h3 class="zh-CN">AbstractBuilder的构造函数</h3>
	 *
	 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
	 *                      <span class="zh-CN">父构建器实例对象</span>
	 */
	EntitySubQueryBuilder(final P parentBuilder) {
		super(parentBuilder);
		this.itemList = new ArrayList<>();
		this.queryJoins = new ArrayList<>();
		this.conditionList = new ArrayList<>();
		this.groupByList = new ArrayList<>();
		this.havingList = new ArrayList<>();
	}

	@Override
	public void confirm(final Object object) {
		if (object instanceof TableSubQuery) {
			if (this.queryFrom instanceof FromSubQuery) {
				((FromSubQuery) this.queryFrom).setQueryData((TableSubQuery) object);
			}
		} else if (object instanceof ConditionsBuilder.Conditions) {
			ConditionsBuilder.Conditions conditions = (ConditionsBuilder.Conditions) object;
			if (conditions.isHaving()) {
				this.havingList.clear();
				this.havingList.addAll(conditions.getConditions());
			} else {
				this.conditionList.clear();
				this.conditionList.addAll(conditions.getConditions());
			}
		} else if (object instanceof ItemsBuilder.Items) {
			this.itemList.clear();
			this.itemList.addAll(((ItemsBuilder.Items) object).getItemList());
		} else if (object instanceof QueryFrom) {
			this.queryFrom = (QueryFrom) object;
		} else if (object instanceof JoinsBuilder.Joins) {
			this.queryJoins.clear();
			this.queryJoins.addAll(((JoinsBuilder.Joins) object).getJoinList());
		} else if (object instanceof SortsBuilder.GroupByItems) {
			this.groupByList.clear();
			this.groupByList.addAll(((SortsBuilder.GroupByItems) object).getItemList());
		}
	}

	/**
	 * <h2 class="en-US">Entity scalar sub-query information list builder</h2>
	 * <h2 class="zh-CN">实体类标量子查询信息构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder generic type class</span>
	 *            <span class="zh-CN">父构建器泛型类</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public static final class EntityScalarSubQueryBuilder<P extends ParentBuilder>
			extends EntitySubQueryBuilder<P, ScalarSubQuery> {

		/**
		 * <span class="en-US">Query item information</span>
		 * <span class="zh-CN">查询项信息</span>
		 */
		private QueryItem queryItem;

		/**
		 * <h3 class="en-US">Constructor method for the sub-query information list builder</h3>
		 * <h3 class="zh-CN">子查询信息构建器的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 */
		EntityScalarSubQueryBuilder(final P parentBuilder) {
			super(parentBuilder);
		}

		/**
		 * <h3 class="en-US">Calculate item information builder</h3>
		 * <h3 class="zh-CN">计算查询项构建器</h3>
		 *
		 * @param calculateCode <span class="en-US">Enumeration value of calculate code</span>
		 *                      <span class="zh-CN">计算代码的枚举值</span>
		 * @param jdbcType      <span class="en-US">Jdbc type code</span>
		 *                      <span class="zh-CN">JDBC类型代码</span>
		 * @return <span class="en-US">Calculate item information builder instance object</span>
		 * <span class="zh-CN">计算查询项构建器实例对象</span>
		 */
		public EntityItemsBuilder.CalculateItemBuilder<EntityScalarSubQueryBuilder<P>>
		calculate(@Nonnull final CalculateCode calculateCode, @MagicConstant(valuesFromClass = Types.class) final int jdbcType) {
			return new EntityItemsBuilder.CalculateItemBuilder<>(this, calculateCode, jdbcType);
		}

		/**
		 * <h3 class="en-US">Data column item information builder</h3>
		 * <h3 class="zh-CN">数据列查询项构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column item information builder instance object</span>
		 * <span class="zh-CN">数据列查询项构建器实例对象</span>
		 * @throws SQLException <span class="en-US">The data table is not registered</span>
		 *                      <span class="zh-CN">数据表未注册</span>
		 */
		public EntityItemsBuilder.ColumnItemBuilder<EntityScalarSubQueryBuilder<P>> column(@Nonnull final Class<?> entityClass,
		                                                                                   @Nonnull final String identifyName)
				throws SQLException {
			return new EntityItemsBuilder.ColumnItemBuilder<>(this, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Constant value item information builder</h3>
		 * <h3 class="zh-CN">常量值查询项构建器</h3>
		 *
		 * @param constantValue <span class="en-US">Constant value</span>
		 *                      <span class="zh-CN">常量值</span>
		 * @param aliasName     <span class="en-US">Alias name</span>
		 *                      <span class="zh-CN">别名</span>
		 * @param jdbcType      <span class="en-US">Jdbc type code</span>
		 *                      <span class="zh-CN">JDBC类型代码</span>
		 * @return <span class="en-US">Constant value item information builder instance object</span>
		 * <span class="zh-CN">常量值查询项构建器实例对象</span>
		 */
		public ItemsBuilder.ConstantItemBuilder<EntityScalarSubQueryBuilder<P>>
		constant(@Nonnull final Object constantValue, @Nonnull final String aliasName,
		         @MagicConstant(valuesFromClass = Types.class) final int jdbcType) {
			return new ItemsBuilder.ConstantItemBuilder<>(this, constantValue, aliasName, jdbcType);
		}

		/**
		 * <h3 class="en-US">Function query item information builder</h3>
		 * <h3 class="zh-CN">函数查询项构建器</h3>
		 *
		 * @param functionName <span class="en-US">Function name</span>
		 *                     <span class="zh-CN">函数名</span>
		 * @param jdbcType     <span class="en-US">Jdbc type code</span>
		 *                     <span class="zh-CN">JDBC类型代码</span>
		 * @return <span class="en-US">Function query item information builder instance object</span>
		 * <span class="zh-CN">函数查询项构建器实例对象</span>
		 */
		public EntityItemsBuilder.FunctionItemBuilder<EntityScalarSubQueryBuilder<P>>
		function(@Nonnull final String functionName, @MagicConstant(valuesFromClass = Types.class) final int jdbcType) {
			return new EntityItemsBuilder.FunctionItemBuilder<>(this, functionName, jdbcType);
		}

		/**
		 * <h3 class="en-US">Scalar sub-query item information builder</h3>
		 * <h3 class="zh-CN">标量子查询项构建器</h3>
		 *
		 * @return <span class="en-US">Function query item information builder instance object</span>
		 * <span class="zh-CN">函数查询项构建器实例对象</span>
		 */
		public EntityItemsBuilder.SubQueryItemBuilder<EntityScalarSubQueryBuilder<P>> subQuery() {
			return new EntityItemsBuilder.SubQueryItemBuilder<>(this);
		}

		/**
		 * <h3 class="en-US">Query from information list builder</h3>
		 * <h3 class="zh-CN">查询来源信息列表构建器</h3>
		 *
		 * @param entityClass <span class="en-US">Data table entity class</span>
		 *                    <span class="zh-CN">数据表实体类</span>
		 * @return <span class="en-US">Query from information list builder instance object</span>
		 * <span class="zh-CN">查询来源信息列表构建器实例对象</span>
		 */
		public EntityScalarSubQueryBuilder<P> fromEntity(@Nonnull final Class<?> entityClass) {
			return this.fromEntity(entityClass, Globals.DEFAULT_VALUE_STRING);
		}

		/**
		 * <h3 class="en-US">Query from information list builder</h3>
		 * <h3 class="zh-CN">查询来源信息列表构建器</h3>
		 *
		 * @param entityClass <span class="en-US">Data table entity class</span>
		 *                    <span class="zh-CN">数据表实体类</span>
		 * @param aliasName   <span class="en-US">Alias name</span>
		 *                    <span class="zh-CN">别名</span>
		 * @return <span class="en-US">Query from information list builder instance object</span>
		 * <span class="zh-CN">查询来源信息列表构建器实例对象</span>
		 */
		public EntityScalarSubQueryBuilder<P> fromEntity(@Nonnull final Class<?> entityClass, final String aliasName) {
			this.queryFrom = new FromTable();
			((FromTable) this.queryFrom).setTableName(EntityFactory.getInstance().tableName(entityClass));
			if (StringUtils.notBlank(aliasName)) {
				this.queryFrom.setAliasName(aliasName);
			}
			return this;
		}

		/**
		 * <h3 class="en-US">Query from information list builder</h3>
		 * <h3 class="zh-CN">查询来源信息列表构建器</h3>
		 *
		 * @param aliasName <span class="en-US">Alias name</span>
		 *                  <span class="zh-CN">别名</span>
		 * @return <span class="en-US">Query from information list builder instance object</span>
		 * <span class="zh-CN">查询来源信息列表构建器实例对象</span>
		 */
		public EntityTableSubQueryBuilder<EntityScalarSubQueryBuilder<P>>
		fromSubQuery(@Nonnull final String aliasName) {
			this.queryFrom = new FromSubQuery();
			this.queryFrom.setAliasName(aliasName);
			return new EntityTableSubQueryBuilder<>(this);
		}

		/**
		 * <h3 class="en-US">Query joins information lists builder</h3>
		 * <h3 class="zh-CN">查询关联信息列表构建器构建器</h3>
		 *
		 * @return <span class="en-US">Query joins information lists builder instance object</span>
		 * <span class="zh-CN">查询关联信息列表构建器构建器实例对象</span>
		 */
		public EntityJoinsBuilder<EntityScalarSubQueryBuilder<P>> joins() {
			return new EntityJoinsBuilder<>(this, this.queryJoins);
		}

		/**
		 * <h3 class="en-US">Query conditions information builder</h3>
		 * <h3 class="zh-CN">查询条件组构建器</h3>
		 *
		 * @return <span class="en-US">Query conditions information builder instance object</span>
		 * <span class="zh-CN">查询条件组构建器实例对象</span>
		 */
		public EntityConditionsBuilder<EntityScalarSubQueryBuilder<P>> where() {
			return new EntityConditionsBuilder<>(this, Boolean.FALSE, this.conditionList);
		}

		/**
		 * <h3 class="en-US">Group by data list builder</h3>
		 * <h3 class="zh-CN">分组数据列构建器</h3>
		 *
		 * @return <span class="en-US">Group by data list builder instance object</span>
		 * <span class="zh-CN">分组数据列构建器实例对象</span>
		 */
		public EntitySortsBuilder.GroupItemsBuilder<EntityScalarSubQueryBuilder<P>> groups() {
			return new EntitySortsBuilder.GroupItemsBuilder<>(this, this.groupByList);
		}

		/**
		 * <h3 class="en-US">Having conditions information builder</h3>
		 * <h3 class="zh-CN">Having条件组构建器</h3>
		 *
		 * @return <span class="en-US">Having conditions information builder instance object</span>
		 * <span class="zh-CN">Having条件组构建器实例对象</span>
		 */
		public EntityConditionsBuilder<EntityScalarSubQueryBuilder<P>> having() {
			return new EntityConditionsBuilder<>(this, Boolean.TRUE, this.havingList);
		}

		@Override
		public void confirm(final Object object) {
			if (object instanceof QueryItem) {
				this.queryItem = (QueryItem) object;
			} else {
				super.confirm(object);
			}
		}

		@Override
		public ScalarSubQuery build() throws BuilderException {
			ScalarSubQuery subQuery = new ScalarSubQuery();
			subQuery.setQueryFrom(this.queryFrom);
			subQuery.setQueryItem(this.queryItem);
			subQuery.setQueryJoins(this.queryJoins);
			subQuery.setConditionList(this.conditionList);
			subQuery.setGroupByList(this.groupByList);
			subQuery.setHavingList(this.havingList);
			return subQuery;
		}
	}

	/**
	 * <h2 class="en-US">Table sub-query information list builder</h2>
	 * <h2 class="zh-CN">表子查询信息构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder generic type class</span>
	 *            <span class="zh-CN">父构建器泛型类</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public static final class EntityTableSubQueryBuilder<P extends ParentBuilder>
			extends EntitySubQueryBuilder<P, TableSubQuery> {

		/**
		 * <h3 class="en-US">Constructor method for the sub-query information list builder</h3>
		 * <h3 class="zh-CN">子查询信息构建器的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 */
		EntityTableSubQueryBuilder(final P parentBuilder) {
			super(parentBuilder);
		}

		/**
		 * <h3 class="en-US">Query items information list builder</h3>
		 * <h3 class="zh-CN">查询项目列表构建器</h3>
		 *
		 * @return <span class="en-US">Query items information list builder instance object</span>
		 * <span class="zh-CN">查询项目列表构建器实例对象</span>
		 */
		public EntityItemsBuilder<EntityTableSubQueryBuilder<P>> items() {
			return new EntityItemsBuilder<>(this, this.itemList);
		}

		/**
		 * <h3 class="en-US">Query from table information builder</h3>
		 * <h3 class="zh-CN">查询来源数据表信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public EntityTableSubQueryBuilder<P> fromTable(@Nonnull final Class<?> entityClass) {
			return this.fromTable(entityClass, Globals.DEFAULT_VALUE_STRING);
		}

		/**
		 * <h3 class="en-US">Query from table information builder</h3>
		 * <h3 class="zh-CN">查询来源数据表信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param aliasName <span class="en-US">Alias name</span>
		 *                  <span class="zh-CN">别名</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public EntityTableSubQueryBuilder<P> fromTable(@Nonnull final Class<?> entityClass, final String aliasName) {
			this.queryFrom = new FromTable();
			((FromTable) this.queryFrom).setTableName(EntityFactory.getInstance().tableName(entityClass));
			if (StringUtils.notBlank(aliasName)) {
				this.queryFrom.setAliasName(aliasName);
			}
			return this;
		}

		/**
		 * <h3 class="en-US">Query from table sub-query information builder</h3>
		 * <h3 class="zh-CN">查询来源子查询信息构建器</h3>
		 *
		 * @param aliasName <span class="en-US">Alias name</span>
		 *                  <span class="zh-CN">别名</span>
		 * @return <span class="en-US">Query from information list builder instance object</span>
		 * <span class="zh-CN">查询来源信息列表构建器实例对象</span>
		 */
		public EntityTableSubQueryBuilder<EntityTableSubQueryBuilder<P>>
		fromSubQuery(@Nonnull final String aliasName) {
			this.queryFrom = new FromSubQuery();
			this.queryFrom.setAliasName(aliasName);
			return new EntityTableSubQueryBuilder<>(this);
		}

		/**
		 * <h3 class="en-US">Query joins information lists builder</h3>
		 * <h3 class="zh-CN">查询关联信息列表构建器构建器</h3>
		 *
		 * @return <span class="en-US">Query joins information lists builder instance object</span>
		 * <span class="zh-CN">查询关联信息列表构建器构建器实例对象</span>
		 */
		public EntityJoinsBuilder<EntityTableSubQueryBuilder<P>> joins() {
			return new EntityJoinsBuilder<>(this, this.queryJoins);
		}

		/**
		 * <h3 class="en-US">Query conditions information builder</h3>
		 * <h3 class="zh-CN">查询条件组构建器</h3>
		 *
		 * @return <span class="en-US">Query conditions information builder instance object</span>
		 * <span class="zh-CN">查询条件组构建器实例对象</span>
		 */
		public EntityConditionsBuilder<EntityTableSubQueryBuilder<P>> where() {
			return new EntityConditionsBuilder<>(this, Boolean.FALSE, this.conditionList);
		}

		/**
		 * <h3 class="en-US">Group by data list builder</h3>
		 * <h3 class="zh-CN">分组数据列构建器</h3>
		 *
		 * @return <span class="en-US">Group by data list builder instance object</span>
		 * <span class="zh-CN">分组数据列构建器实例对象</span>
		 */
		public SortsBuilder.GroupItemsBuilder<EntityTableSubQueryBuilder<P>> groups() {
			return new SortsBuilder.GroupItemsBuilder<>(this, this.groupByList);
		}

		/**
		 * <h3 class="en-US">Having conditions information builder</h3>
		 * <h3 class="zh-CN">Having条件组构建器</h3>
		 *
		 * @return <span class="en-US">Having conditions information builder instance object</span>
		 * <span class="zh-CN">Having条件组构建器实例对象</span>
		 */
		public ConditionsBuilder<EntityTableSubQueryBuilder<P>> having() {
			return new ConditionsBuilder<>(this, Boolean.TRUE, this.havingList);
		}

		@Override
		public TableSubQuery build() throws BuilderException {
			TableSubQuery subQuery = new TableSubQuery();
			subQuery.setQueryFrom(this.queryFrom);
			subQuery.setItemList(this.itemList);
			subQuery.setQueryJoins(this.queryJoins);
			subQuery.setConditionList(this.conditionList);
			subQuery.setGroupByList(this.groupByList);
			subQuery.setHavingList(this.havingList);
			return subQuery;
		}
	}
}
