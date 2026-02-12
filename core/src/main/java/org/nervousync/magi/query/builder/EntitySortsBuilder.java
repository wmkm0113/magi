package org.nervousync.magi.query.builder;

import jakarta.annotation.Nonnull;
import org.nervousync.brain.enumerations.query.OrderType;
import org.nervousync.brain.query.builder.SortsBuilder;
import org.nervousync.brain.query.sort.GroupBy;
import org.nervousync.brain.query.sort.OrderBy;
import org.nervousync.builder.AbstractBuilder;
import org.nervousync.builder.ParentBuilder;
import org.nervousync.exceptions.builder.BuilderException;
import org.nervousync.magi.entity.EntityFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2 class="en-US">Abstract class for the order or group by information list builder</h2>
 * <h2 class="zh-CN">排序分组信息列表构建器抽象类</h2>
 *
 * @param <P> <span class="en-US">Parent builder generic type class</span>
 *            <span class="zh-CN">父构建器泛型类</span>
 * @param <T> <span class="en-US">Generated result generic type class</span>
 *            <span class="zh-CN">生成结果泛型类</span>
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
 */
@SuppressWarnings("unused")
public abstract class EntitySortsBuilder<P extends ParentBuilder, T> extends AbstractBuilder<P, T> {

	/**
	 * <span class="en-US">Entity factory instance object</span>
	 * <span class="zh-CN">实体类工厂实例对象</span>
	 */
	private static final EntityFactory ENTITY_FACTORY = EntityFactory.getInstance();

	/**
	 * <h3 class="en-US">Protected constructor for AbstractBuilder</h3>
	 * <h3 class="zh-CN">AbstractBuilder的构造函数</h3>
	 *
	 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
	 *                      <span class="zh-CN">父构建器实例对象</span>
	 */
	protected EntitySortsBuilder(final P parentBuilder) {
		super(parentBuilder);
	}

	/**
	 * <h2 class="en-US">Query group by list information builder</h2>
	 * <h2 class="zh-CN">查询分组信息列表构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder generic type class</span>
	 *            <span class="zh-CN">父构建器泛型类</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 7， 2020 13:36：28 $
	 */
	public static final class GroupItemsBuilder<P extends ParentBuilder> extends EntitySortsBuilder<P, SortsBuilder.GroupByItems> {

		/**
		 * <span class="en-US">Query group by column information list</span>
		 * <span class="zh-CN">查询分组列信息列表</span>
		 */
		private final List<GroupBy> groupByList;

		/**
		 * <h3 class="en-US">Constructor method for the query group by list information builder</h3>
		 * <h3 class="zh-CN">查询分组信息列表构建器的构造方法</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param groupByList   <span class="en-US">Query group by column information list</span>
		 *                      <span class="zh-CN">查询分组列信息列表</span>
		 */
		public GroupItemsBuilder(final P parentBuilder, final List<GroupBy> groupByList) {
			super(parentBuilder);
			this.groupByList = new ArrayList<>();
			if (groupByList != null) {
				this.groupByList.addAll(groupByList);
			}
		}

		/**
		 * <h3 class="en-US">Query group by information builder</h3>
		 * <h3 class="zh-CN">查询分组列信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Query group by information builder instance object</span>
		 * <span class="zh-CN">查询分组列信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public GroupByBuilder<GroupItemsBuilder<P>> groupBy(@Nonnull final Class<?> entityClass,
		                                                    @Nonnull final String identifyName) throws SQLException {
			return new GroupByBuilder<>(this, entityClass, identifyName);
		}

		@Override
		public void confirm(final Object object) {
			if (object instanceof GroupBy) {
				GroupBy groupBy = (GroupBy) object;
				if (this.groupByList.stream().noneMatch(exist ->
						exist.match(groupBy.getTableName(), groupBy.getColumnName()))) {
					this.groupByList.add(groupBy);
				}
			}
		}

		@Override
		public SortsBuilder.GroupByItems build() throws BuilderException {
			return new SortsBuilder.GroupByItems(this.groupByList);
		}
	}

	/**
	 * <h2 class="en-US">Query group by information builder</h2>
	 * <h2 class="zh-CN">查询分组列信息构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder generic type class</span>
	 *            <span class="zh-CN">父构建器泛型类</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 7， 2020 13:36：28 $
	 */
	public static final class GroupByBuilder<P extends ParentBuilder> extends AbstractBuilder<P, GroupBy> {

		/**
		 * <span class="en-US">Query group by column information instance object</span>
		 * <span class="zh-CN">查询分组列信息实例对象</span>
		 */
		private final GroupBy groupBy;

		/**
		 * <h3 class="en-US">Protected constructor for AbstractBuilder</h3>
		 * <h3 class="zh-CN">AbstractBuilder的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param entityClass   <span class="en-US">Data table entity class</span>
		 *                      <span class="zh-CN">数据表实体类</span>
		 * @param identifyName  <span class="en-US">Data column identify name</span>
		 *                      <span class="zh-CN">数据列识别名称</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		GroupByBuilder(final P parentBuilder, @Nonnull final Class<?> entityClass, @Nonnull final String identifyName)
				throws SQLException {
			super(parentBuilder);
			this.groupBy = new GroupBy();
			this.groupBy.setTableName(ENTITY_FACTORY.tableName(entityClass));
			this.groupBy.setColumnName(ENTITY_FACTORY.columnName(entityClass, identifyName));
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
		public GroupByBuilder<P> sortCode(final int sortCode) {
			this.groupBy.setSortCode(sortCode);
			return this;
		}

		@Override
		public GroupBy build() throws BuilderException {
			return this.groupBy;
		}
	}

	/**
	 * <h2 class="en-US">Query group by list information builder</h2>
	 * <h2 class="zh-CN">查询分组信息列表构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder generic type class</span>
	 *            <span class="zh-CN">父构建器泛型类</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 7， 2020 13:36：28 $
	 */
	public static final class OrderItemsBuilder<P extends ParentBuilder> extends EntitySortsBuilder<P, SortsBuilder.OrderByItems> {

		/**
		 * <span class="en-US">Query group by column information list</span>
		 * <span class="zh-CN">查询分组列信息列表</span>
		 */
		private final List<OrderBy> orderByList;

		/**
		 * <h3 class="en-US">Constructor method for the query group by list information builder</h3>
		 * <h3 class="zh-CN">查询分组信息列表构建器的构造方法</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param orderByList   <span class="en-US">Query group by column information list</span>
		 *                      <span class="zh-CN">查询分组列信息列表</span>
		 */
		public OrderItemsBuilder(final P parentBuilder, final List<OrderBy> orderByList) {
			super(parentBuilder);
			this.orderByList = new ArrayList<>();
			if (orderByList != null) {
				this.orderByList.addAll(orderByList);
			}
		}

		/**
		 * <h3 class="en-US">Query group by information builder</h3>
		 * <h3 class="zh-CN">查询分组列信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Query group by information builder instance object</span>
		 * <span class="zh-CN">查询分组列信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public OrderByBuilder<OrderItemsBuilder<P>> orderBy(@Nonnull final Class<?> entityClass,
		                                                    @Nonnull final String identifyName) throws SQLException {
			return new OrderByBuilder<>(this, entityClass, identifyName);
		}

		@Override
		public void confirm(final Object object) {
			if (object instanceof OrderBy) {
				OrderBy orderBy = (OrderBy) object;
				if (this.orderByList.stream().noneMatch(exist ->
						exist.match(orderBy.getTableName(), orderBy.getColumnName()))) {
					this.orderByList.add(orderBy);
				}
			}
		}

		@Override
		public SortsBuilder.OrderByItems build() throws BuilderException {
			return new SortsBuilder.OrderByItems(this.orderByList);
		}
	}

	/**
	 * <h2 class="en-US">Query group by information builder</h2>
	 * <h2 class="zh-CN">查询分组列信息构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder generic type class</span>
	 *            <span class="zh-CN">父构建器泛型类</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 7， 2020 13:36：28 $
	 */
	public static final class OrderByBuilder<P extends ParentBuilder> extends AbstractBuilder<P, OrderBy> {

		/**
		 * <span class="en-US">Query group by column information instance object</span>
		 * <span class="zh-CN">查询分组列信息实例对象</span>
		 */
		private final OrderBy orderBy;

		/**
		 * <h3 class="en-US">Protected constructor for AbstractBuilder</h3>
		 * <h3 class="zh-CN">AbstractBuilder的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param entityClass   <span class="en-US">Data table entity class</span>
		 *                      <span class="zh-CN">数据表实体类</span>
		 * @param identifyName  <span class="en-US">Data column identify name</span>
		 *                      <span class="zh-CN">数据列识别名称</span>
		 */
		OrderByBuilder(final P parentBuilder, @Nonnull final Class<?> entityClass, @Nonnull final String identifyName)
				throws SQLException {
			super(parentBuilder);
			this.orderBy = new OrderBy();
			this.orderBy.setTableName(ENTITY_FACTORY.tableName(entityClass));
			this.orderBy.setColumnName(ENTITY_FACTORY.columnName(entityClass, identifyName));
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
		public OrderByBuilder<P> sortCode(final int sortCode) {
			this.orderBy.setSortCode(sortCode);
			return this;
		}

		/**
		 * <h3 class="en-US">Order type: ASC</h3>
		 * <h3 class="zh-CN">升序排序</h3>
		 *
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public OrderByBuilder<P> asc() {
			this.orderBy.setOrderType(OrderType.ASC);
			return this;
		}

		/**
		 * <h3 class="en-US">Order type: DESC</h3>
		 * <h3 class="zh-CN">降序排序</h3>
		 *
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public OrderByBuilder<P> desc() {
			this.orderBy.setOrderType(OrderType.DESC);
			return this;
		}

		@Override
		public OrderBy build() throws BuilderException {
			return this.orderBy;
		}
	}
}
