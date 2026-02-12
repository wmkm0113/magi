package org.nervousync.magi.query.builder;

import jakarta.annotation.Nonnull;
import org.intellij.lang.annotations.MagicConstant;
import org.nervousync.brain.enumerations.query.CalculateCode;
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.brain.query.builder.ItemsBuilder;
import org.nervousync.brain.query.core.QueryItem;
import org.nervousync.brain.query.item.CalculateItem;
import org.nervousync.brain.query.item.ColumnItem;
import org.nervousync.brain.query.item.FunctionItem;
import org.nervousync.brain.query.item.SubQueryItem;
import org.nervousync.brain.query.subqueries.ScalarSubQuery;
import org.nervousync.builder.AbstractBuilder;
import org.nervousync.builder.ParentBuilder;
import org.nervousync.exceptions.builder.BuilderException;
import org.nervousync.magi.entity.EntityFactory;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2 class="en-US">Query items information list builder</h2>
 * <h2 class="zh-CN">查询项目信息列表构建器</h2>
 *
 * @param <P> <span class="en-US">Parent builder generic type class</span>
 *            <span class="zh-CN">父构建器泛型类</span>
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
 */
@SuppressWarnings("unused")
public final class EntityItemsBuilder<P extends ParentBuilder> extends AbstractBuilder<P, ItemsBuilder.Items> {

	/**
	 * <span class="en-US">Query item instance list</span>
	 * <span class="zh-CN">查询项目实例对象列表</span>
	 */
	@Nonnull
	private final List<QueryItem> itemList = new ArrayList<>();

	/**
	 * <h3 class="en-US">Constructor method for the query items information list builder</h3>
	 * <h3 class="zh-CN">查询项目信息列表构建器的构造函数</h3>
	 *
	 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
	 *                      <span class="zh-CN">父构建器实例对象</span>
	 * @param itemList      <span class="en-US">Query item instance list</span>
	 *                      <span class="zh-CN">查询项目实例对象列表</span>
	 */
	public EntityItemsBuilder(final P parentBuilder, final List<QueryItem> itemList) {
		super(parentBuilder);
		if (itemList != null) {
			this.itemList.addAll(itemList);
		}
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
	public CalculateItemBuilder<EntityItemsBuilder<P>> calculate(@Nonnull final CalculateCode calculateCode,
	                                                             @MagicConstant(valuesFromClass = Types.class) final int jdbcType) {
		return new CalculateItemBuilder<>(this, calculateCode, jdbcType);
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
	 * @throws SQLException <span class="en-US">If the configuration information is invalid</span>
	 *                      <span class="zh-CN">如果配置信息错误</span>
	 */
	public ColumnItemBuilder<EntityItemsBuilder<P>> column(@Nonnull final Class<?> entityClass,
	                                                       @Nonnull final String identifyName) throws SQLException {
		return new ColumnItemBuilder<>(this, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Constant value item information builder</h3>
	 * <h3 class="zh-CN">常量值查询项构建器</h3>
	 *
	 * @param constantValue <span class="en-US">Constant value</span>
	 *                      <span class="zh-CN">常量值</span>
	 * @param aliasName     <span class="en-US">Alias name</span>
	 *                      <span class="zh-CN">别名</span>
	 * @return <span class="en-US">Constant value item information builder instance object</span>
	 * <span class="zh-CN">常量值查询项构建器实例对象</span>
	 */
	public ItemsBuilder.ConstantItemBuilder<EntityItemsBuilder<P>> constant(@Nonnull final Object constantValue,
	                                                                        @Nonnull final String aliasName) {
		return new ItemsBuilder.ConstantItemBuilder<>(this, constantValue, aliasName,
				EntityFactory.getInstance().jdbcType(constantValue.getClass()));
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
	public FunctionItemBuilder<EntityItemsBuilder<P>> function(@Nonnull final String functionName,
	                                                           @MagicConstant(valuesFromClass = Types.class) final int jdbcType) {
		return new FunctionItemBuilder<>(this, functionName, jdbcType);
	}

	/**
	 * <h3 class="en-US">Function query item information builder</h3>
	 * <h3 class="zh-CN">函数查询项构建器</h3>
	 *
	 * @return <span class="en-US">Function query item information builder instance object</span>
	 * <span class="zh-CN">函数查询项构建器实例对象</span>
	 */
	public SubQueryItemBuilder<EntityItemsBuilder<P>> subQuery() {
		return new SubQueryItemBuilder<>(this);
	}

	@Override
	public void confirm(final Object object) {
		if (object instanceof QueryItem) {
			this.itemList.add((QueryItem) object);
		}
	}

	@Override
	public ItemsBuilder.Items build() throws BuilderException {
		return new ItemsBuilder.Items(this.itemList);
	}

	/**
	 * <h2 class="en-US">Abstract class of query item information builder</h2>
	 * <h2 class="zh-CN">查询项信息构建器抽象类</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder instance object</span>
	 *            <span class="zh-CN">父构建器实例对象</span>
	 * @param <T> <span class="en-US">Query item generic type</span>
	 *            <span class="zh-CN">查询项泛型类型</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public static abstract class QueryItemBuilder<P extends ParentBuilder, T extends QueryItem>
			extends AbstractBuilder<P, T> {

		/**
		 * <span class="en-US">Entity factory instance object</span>
		 * <span class="zh-CN">实体类工厂实例对象</span>
		 */
		protected static final EntityFactory ENTITY_FACTORY = EntityFactory.getInstance();

		/**
		 * <span class="en-US">Query item instance object</span>
		 * <span class="zh-CN">查询项实例对象</span>
		 */
		protected final T item;

		/**
		 * <h3 class="en-US">Protected constructor for AbstractBuilder</h3>
		 * <h3 class="zh-CN">AbstractBuilder的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param item          <span class="en-US">Query item instance object</span>
		 *                      <span class="zh-CN">查询项实例对象</span>
		 */
		protected QueryItemBuilder(final P parentBuilder, final T item) {
			super(parentBuilder);
			this.item = item;
		}

		@Override
		public final T build() throws BuilderException {
			return this.item;
		}
	}

	/**
	 * <h2 class="en-US">Calculate query item information builder</h2>
	 * <h2 class="zh-CN">计算查询项信息构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder instance object</span>
	 *            <span class="zh-CN">父构建器实例对象</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public static final class CalculateItemBuilder<P extends ParentBuilder> extends QueryItemBuilder<P, CalculateItem> {

		/**
		 * <h3 class="en-US">Constructor method for the calculated query item information builder</h3>
		 * <h3 class="zh-CN">计算查询项信息构建器的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param calculateCode <span class="en-US">Enumeration value of calculate code</span>
		 *                      <span class="zh-CN">计算代码的枚举值</span>
		 * @param jdbcType      <span class="en-US">Jdbc type code</span>
		 *                      <span class="zh-CN">JDBC类型代码</span>
		 */
		CalculateItemBuilder(final P parentBuilder, @Nonnull final CalculateCode calculateCode,
		                     @MagicConstant(valuesFromClass = Types.class) final int jdbcType) {
			super(parentBuilder, new CalculateItem());
			this.item.setCalculateCode(calculateCode);
			this.item.setJdbcType(jdbcType);
		}

		/**
		 * <h3 class="en-US">Parameter items builder</h3>
		 * <h3 class="zh-CN">参数信息构建器</h3>
		 *
		 * @return <span class="en-US">Parameter items builder instance object</span>
		 * <span class="zh-CN">参数信息构建器实例对象</span>
		 */
		public EntityItemsBuilder<CalculateItemBuilder<P>> parameters() {
			return new EntityItemsBuilder<>(this, this.item.getCalculateItems());
		}

		/**
		 * <h3 class="en-US">Set sort code</h3>
		 * <h3 class="zh-CN">设置排序代码</h3>
		 *
		 * @param sortCode <span class="en-US">Sort code</span>
		 *                 <span class="zh-CN">排序代码</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public CalculateItemBuilder<P> sortCode(final int sortCode) {
			this.item.setSortCode(sortCode);
			return this;
		}

		/**
		 * <h3 class="en-US">Set alias name</h3>
		 * <h3 class="zh-CN">设置别名</h3>
		 *
		 * @param aliasName <span class="en-US">Alias name</span>
		 *                  <span class="zh-CN">别名</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public CalculateItemBuilder<P> aliasName(final String aliasName) {
			this.item.setAliasName(aliasName);
			return this;
		}

		@Override
		public void confirm(final Object object) {
			if (object instanceof ItemsBuilder.Items) {
				this.item.setCalculateItems(((ItemsBuilder.Items) object).getItemList());
			}
		}
	}

	/**
	 * <h2 class="en-US">Data column query item information builder</h2>
	 * <h2 class="zh-CN">数据列查询项信息构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder instance object</span>
	 *            <span class="zh-CN">父构建器实例对象</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public static final class ColumnItemBuilder<P extends ParentBuilder> extends QueryItemBuilder<P, ColumnItem> {

		/**
		 * <h3 class="en-US">Constructor method for the data column query item information builder</h3>
		 * <h3 class="zh-CN">数据列查询项信息构建器的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param entityClass   <span class="en-US">Data table entity class</span>
		 *                      <span class="zh-CN">数据表实体类</span>
		 * @param identifyName  <span class="en-US">Data column identify name</span>
		 *                      <span class="zh-CN">数据列识别名称</span>
		 * @throws SQLException <span class="en-US">If the configuration information is invalid</span>
		 *                      <span class="zh-CN">如果配置信息错误</span>
		 */
		ColumnItemBuilder(final P parentBuilder, @Nonnull final Class<?> entityClass,
		                  @Nonnull final String identifyName) throws SQLException {
			super(parentBuilder, new ColumnItem());
			if (!ENTITY_FACTORY.registeredTable(entityClass)) {
				throw new MultilingualSQLException(0x00DB00010012L, entityClass.getName());
			}
			this.item.setTableName(ENTITY_FACTORY.tableName(entityClass));
			this.item.setColumnName(ENTITY_FACTORY.columnName(entityClass, identifyName));
			this.item.setJdbcType(ENTITY_FACTORY.jdbcType(entityClass, identifyName));
		}

		/**
		 * <h3 class="en-US">Set sort code</h3>
		 * <h3 class="zh-CN">设置排序代码</h3>
		 *
		 * @param sortCode <span class="en-US">Sort code</span>
		 *                 <span class="zh-CN">排序代码</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public ColumnItemBuilder<P> sortCode(final int sortCode) {
			this.item.setSortCode(sortCode);
			return this;
		}

		/**
		 * <h3 class="en-US">Data column distinct flag</h3>
		 * <h3 class="zh-CN">数据列去重标记</h3>
		 *
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public ColumnItemBuilder<P> distinct() {
			this.item.setDistinct(Boolean.TRUE);
			return this;
		}

		/**
		 * <h3 class="en-US">Set alias name</h3>
		 * <h3 class="zh-CN">设置别名</h3>
		 *
		 * @param aliasName <span class="en-US">Alias name</span>
		 *                  <span class="zh-CN">别名</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public ColumnItemBuilder<P> aliasName(final String aliasName) {
			this.item.setAliasName(aliasName);
			return this;
		}
	}

	/**
	 * <h2 class="en-US">Function query item information builder</h2>
	 * <h2 class="zh-CN">函数查询项信息构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder instance object</span>
	 *            <span class="zh-CN">父构建器实例对象</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public static final class FunctionItemBuilder<P extends ParentBuilder> extends QueryItemBuilder<P, FunctionItem> {

		/**
		 * <h3 class="en-US">Constructor method for the function query item information builder</h3>
		 * <h3 class="zh-CN">函数查询项信息构建器的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param functionName  <span class="en-US">Function name</span>
		 *                      <span class="zh-CN">函数名</span>
		 */
		FunctionItemBuilder(final P parentBuilder, @Nonnull final String functionName,
		                    @MagicConstant(valuesFromClass = Types.class) final int jdbcType) {
			super(parentBuilder, new FunctionItem());
			this.item.setFunctionName(functionName);
			this.item.setJdbcType(jdbcType);
		}

		/**
		 * <h3 class="en-US">Set sort code</h3>
		 * <h3 class="zh-CN">设置排序代码</h3>
		 *
		 * @param sortCode <span class="en-US">Sort code</span>
		 *                 <span class="zh-CN">排序代码</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public FunctionItemBuilder<P> sortCode(final int sortCode) {
			this.item.setSortCode(sortCode);
			return this;
		}

		/**
		 * <h3 class="en-US">Set alias name</h3>
		 * <h3 class="zh-CN">设置别名</h3>
		 *
		 * @param aliasName <span class="en-US">Alias name</span>
		 *                  <span class="zh-CN">别名</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public FunctionItemBuilder<P> aliasName(final String aliasName) {
			this.item.setAliasName(aliasName);
			return this;
		}

		/**
		 * <h3 class="en-US">Function parameters builder</h3>
		 * <h3 class="zh-CN">函数参数信息构建器</h3>
		 *
		 * @return <span class="en-US">Function parameters builder instance object</span>
		 * <span class="zh-CN">函数参数信息构建器实例对象</span>
		 */
		public EntityParametersBuilder<FunctionItemBuilder<P>> parameters() {
			return new EntityParametersBuilder<>(this, this.item.getFunctionParams());
		}

		@Override
		public void confirm(final Object object) {
			if (object instanceof EntityParametersBuilder.Parameters) {
				this.item.setFunctionParams(((EntityParametersBuilder.Parameters) object).getFunctionParams());
			}
		}
	}

	/**
	 * <h2 class="en-US">Sub-query item information builder</h2>
	 * <h2 class="zh-CN">子查询信息构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder instance object</span>
	 *            <span class="zh-CN">父构建器实例对象</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public static final class SubQueryItemBuilder<P extends ParentBuilder> extends QueryItemBuilder<P, SubQueryItem> {

		/**
		 * <h3 class="en-US">Constructor method for the sub-query item information builder</h3>
		 * <h3 class="zh-CN">子查询信息构建器的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 */
		SubQueryItemBuilder(final P parentBuilder) {
			super(parentBuilder, new SubQueryItem());
		}

		/**
		 * <h3 class="en-US">Set sort code</h3>
		 * <h3 class="zh-CN">设置排序代码</h3>
		 *
		 * @param sortCode <span class="en-US">Sort code</span>
		 *                 <span class="zh-CN">排序代码</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public SubQueryItemBuilder<P> sortCode(final int sortCode) {
			this.item.setSortCode(sortCode);
			return this;
		}

		/**
		 * <h3 class="en-US">Set alias name</h3>
		 * <h3 class="zh-CN">设置别名</h3>
		 *
		 * @param aliasName <span class="en-US">Alias name</span>
		 *                  <span class="zh-CN">别名</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public SubQueryItemBuilder<P> aliasName(final String aliasName) {
			this.item.setAliasName(aliasName);
			return this;
		}

		/**
		 * <h3 class="en-US">Set JDBC type code</h3>
		 * <h3 class="zh-CN">设置JDBC类型代码</h3>
		 *
		 * @param jdbcType <span class="en-US">JDBC type code</span>
		 *                 <span class="zh-CN">JDBC类型代码</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public SubQueryItemBuilder<P> jdbcType(@MagicConstant(valuesFromClass = Types.class) final int jdbcType) {
			this.item.setJdbcType(jdbcType);
			return this;
		}

		/**
		 * <h3 class="en-US">Condition match sub-query information builder</h3>
		 * <h3 class="zh-CN">匹配子查询构建器</h3>
		 *
		 * @return <span class="en-US">Sub-query builder instance object</span>
		 * <span class="zh-CN">子查询构建器实例对象</span>
		 */
		public EntitySubQueryBuilder.EntityScalarSubQueryBuilder<SubQueryItemBuilder<P>> subQuery() {
			return new EntitySubQueryBuilder.EntityScalarSubQueryBuilder<>(this);
		}

		@Override
		public void confirm(final Object object) {
			if (object instanceof ScalarSubQuery) {
				this.item.setQueryData((ScalarSubQuery) object);
			}
		}
	}
}
