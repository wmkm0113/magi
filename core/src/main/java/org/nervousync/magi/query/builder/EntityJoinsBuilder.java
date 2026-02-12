package org.nervousync.magi.query.builder;

import jakarta.annotation.Nonnull;
import org.nervousync.brain.enumerations.query.ConditionCode;
import org.nervousync.brain.enumerations.query.ConnectionCode;
import org.nervousync.brain.enumerations.query.JoinType;
import org.nervousync.brain.query.builder.JoinsBuilder;
import org.nervousync.brain.query.join.QueryJoin;
import org.nervousync.brain.query.join.SubQueryJoin;
import org.nervousync.brain.query.join.TableQueryJoin;
import org.nervousync.brain.query.subqueries.TableSubQuery;
import org.nervousync.builder.AbstractBuilder;
import org.nervousync.builder.ParentBuilder;
import org.nervousync.exceptions.builder.BuilderException;
import org.nervousync.magi.beans.defines.reference.JoinDefine;
import org.nervousync.magi.entity.EntityFactory;
import org.nervousync.utils.core.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <h2 class="en-US">Entity query joins information list builder</h2>
 * <h2 class="zh-CN">实体类查询关联信息列表构建器</h2>
 *
 * @param <P> <span class="en-US">Parent builder generic type class</span>
 *            <span class="zh-CN">父构建器泛型类</span>
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
 */
@SuppressWarnings("unused")
public final class EntityJoinsBuilder<P extends ParentBuilder> extends AbstractBuilder<P, JoinsBuilder.Joins> {

	/**
	 * <span class="en-US">Entity factory instance object</span>
	 * <span class="zh-CN">实体类工厂实例对象</span>
	 */
	private static final EntityFactory ENTITY_FACTORY = EntityFactory.getInstance();

	/**
	 * <span class="en-US">Allowed condition code list of the query table join</span>
	 * <span class="zh-CN">数据列关联允许的匹配代码列表</span>
	 */
	private static final List<ConditionCode> JOIN_CONDITION_CODES =
			Arrays.asList(ConditionCode.LESS, ConditionCode.LESS_EQUAL, ConditionCode.EQUAL, ConditionCode.NOT_EQUAL,
					ConditionCode.GREATER, ConditionCode.GREATER_EQUAL);

	/**
	 * <span class="en-US">Related query joins information lists</span>
	 * <span class="zh-CN">关联查询信息列表</span>
	 */
	@Nonnull
	private final List<QueryJoin> joinList = new ArrayList<>();

	/**
	 * <h3 class="en-US">Constructor method for the entity query joins information list builder</h3>
	 * <h3 class="zh-CN">实体类查询关联信息列表构建器的构造方法</h3>
	 *
	 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
	 *                      <span class="zh-CN">父构建器实例对象</span>
	 */
	EntityJoinsBuilder(final P parentBuilder, final List<QueryJoin> joinList) {
		super(parentBuilder);
		if (joinList != null) {
			this.joinList.addAll(joinList);
		}
	}

	/**
	 * <h3 class="en-US">Customize reference information to join entity classes</h3>
	 * <h3 class="zh-CN">自定义关联信息进行实体类关联</h3>
	 *
	 * @param joinType   <span class="en-US">Table join type</span>
	 *                   <span class="zh-CN">数据表关联类型</span>
	 * @param mainEntity <span class="en-US">Main table entity class</span>
	 *                   <span class="zh-CN">主表实体类</span>
	 * @param joinEntity <span class="en-US">Join table entity class</span>
	 *                   <span class="zh-CN">关联表实体类</span>
	 * @return <span class="en-US">Entity query joins information builder instance object</span>
	 * <span class="zh-CN">实体类查询关联信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public EntityJoinBuilder entityJoin(@Nonnull final JoinType joinType, @Nonnull final Class<?> mainEntity,
	                                    @Nonnull final Class<?> joinEntity) throws SQLException {
		return new EntityJoinBuilder(this, joinType, mainEntity, joinEntity, Boolean.FALSE);
	}

	/**
	 * <h3 class="en-US">Use registered reference information to join entity classes</h3>
	 * <h3 class="zh-CN">使用注册的关联信息进行实体类关联</h3>
	 *
	 * @param joinType   <span class="en-US">Table join type</span>
	 *                   <span class="zh-CN">数据表关联类型</span>
	 * @param mainEntity <span class="en-US">Main table entity class</span>
	 *                   <span class="zh-CN">主表实体类</span>
	 * @param joinEntity <span class="en-US">Join table entity class</span>
	 *                   <span class="zh-CN">关联表实体类</span>
	 * @param aliasName  <span class="en-US">Data table alias name</span>
	 *                   <span class="zh-CN">数据表别名</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 * @throws SQLException <span class="en-US">Not allowed join condition code or column identify code is null or empty string</span>
	 *                      <span class="zh-CN">不允许的关联条件运算代码或数据列识别代码为空</span>
	 */
	public EntityJoinsBuilder<P> referenceJoin(@Nonnull final JoinType joinType, @Nonnull final Class<?> mainEntity,
	                                           @Nonnull final Class<?> joinEntity, final String aliasName)
			throws SQLException {
		return new EntityJoinBuilder(this, joinType, mainEntity, joinEntity, Boolean.TRUE)
				.aliasName(aliasName)
				.confirm();
	}

	/**
	 * <h3 class="en-US">Subquery Information Builder</h3>
	 * <h3 class="zh-CN">子查询信息构建器</h3>
	 *
	 * @param joinType   <span class="en-US">Table join type</span>
	 *                   <span class="zh-CN">数据表关联类型</span>
	 * @param mainEntity <span class="en-US">Driven table entity class</span>
	 *                   <span class="zh-CN">驱动表实体类</span>
	 * @return <span class="en-US">Subquery information builder instance object</span>
	 * <span class="zh-CN">子查询信息构建器实例对象</span>
	 */
	public SubQueryJoinBuilder subQueryJoin(@Nonnull final JoinType joinType, @Nonnull final Class<?> mainEntity) {
		return new SubQueryJoinBuilder(this, joinType, mainEntity);
	}

	@Override
	public void confirm(final Object object) {
		if (object instanceof QueryJoin) {
			this.joinList.add((QueryJoin) object);
		}
	}

	@Override
	public JoinsBuilder.Joins build() throws BuilderException {
		return new JoinsBuilder.Joins(this.joinList);
	}

	/**
	 * <h2 class="en-US">Entity query joins information builder</h2>
	 * <h2 class="zh-CN">实体类查询关联信息构建器</h2>
	 *
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public final class EntityJoinBuilder extends JoinsBuilder.QueryJoinBuilder<EntityJoinsBuilder<P>, TableQueryJoin> {

		private final Class<?> mainEntity;
		private final Class<?> joinEntity;

		/**
		 * <h3 class="en-US">Constructor method for the entity query joins information builder</h3>
		 * <h3 class="zh-CN">实体类查询关联信息构建器的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param mainEntity    <span class="en-US">Main table entity class</span>
		 *                      <span class="zh-CN">主表实体类</span>
		 * @param joinEntity    <span class="en-US">Join table entity class</span>
		 *                      <span class="zh-CN">关联表实体类</span>
		 * @param joinType      <span class="en-US">Table join type</span>
		 *                      <span class="zh-CN">数据表关联类型</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		private EntityJoinBuilder(final EntityJoinsBuilder<P> parentBuilder, @Nonnull final JoinType joinType,
		                          @Nonnull final Class<?> mainEntity, @Nonnull final Class<?> joinEntity,
		                          final boolean reference) throws SQLException {
			super(parentBuilder, new TableQueryJoin(), ENTITY_FACTORY.tableName(mainEntity));
			this.queryJoin.setJoinType(joinType);
			this.queryJoin.setJoinTable(ENTITY_FACTORY.tableName(joinEntity));
			this.mainEntity = mainEntity;
			this.joinEntity = joinEntity;
			if (reference) {
				for (JoinDefine joinDefine : ENTITY_FACTORY.joinColumns(mainEntity, joinEntity)) {
					super.joinOn(ConnectionCode.AND, ConditionCode.EQUAL,
							joinDefine.getCurrentField(), joinDefine.getReferenceField());
				}
			}
		}

		/**
		 * <h3 class="en-US">Set join table alias name</h3>
		 * <h3 class="zh-CN">设置关联表别名</h3>
		 *
		 * @param aliasName <span class="en-US">Data table alias name</span>
		 *                  <span class="zh-CN">数据表别名</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public EntityJoinBuilder aliasName(final String aliasName) {
			if (StringUtils.notBlank(aliasName)) {
				this.queryJoin.setAliasName(aliasName);
			}
			return this;
		}

		/**
		 * <h3 class="en-US">Add join column information</h3>
		 * <h3 class="zh-CN">添加关联数据列信息</h3>
		 *
		 * @param connectionCode <span class="en-US">Join connection code</span>
		 *                       <span class="zh-CN">关联条件连接代码</span>
		 * @param conditionCode  <span class="en-US">Query condition code</span>
		 *                       <span class="zh-CN">查询条件运算代码</span>
		 * @param mainKey        <span class="en-US">Column names identify code of main table</span>
		 *                       <span class="zh-CN">主表数据列识别代码</span>
		 * @param joinKey        <span class="en-US">Column names identify code of join table</span>
		 *                       <span class="zh-CN">关联表数据列识别代码</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 * @throws SQLException <span class="en-US">Not allowed join condition code or column identify code is null or empty string</span>
		 *                      <span class="zh-CN">不允许的关联条件运算代码或数据列识别代码为空</span>
		 */
		public EntityJoinBuilder on(@Nonnull final ConnectionCode connectionCode,
		                            @Nonnull final ConditionCode conditionCode,
		                            @Nonnull final String mainKey, @Nonnull final String joinKey) throws SQLException {
			super.joinOn(connectionCode, conditionCode,
					ENTITY_FACTORY.columnName(this.mainEntity, mainKey),
					ENTITY_FACTORY.columnName(this.joinEntity, joinKey));
			return this;
		}
	}

	/**
	 * <h2 class="en-US">Sub-query item information builder</h2>
	 * <h2 class="zh-CN">子查询信息构建器</h2>
	 *
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public final class SubQueryJoinBuilder extends JoinsBuilder.QueryJoinBuilder<EntityJoinsBuilder<P>, SubQueryJoin> {

		/**
		 * <h3 class="en-US">Constructor method for the sub-query item information builder</h3>
		 * <h3 class="zh-CN">子查询信息构建器的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param joinType      <span class="en-US">Table join type</span>
		 *                      <span class="zh-CN">数据表关联类型</span>
		 * @param mainEntity    <span class="en-US">Driven table entity class</span>
		 *                      <span class="zh-CN">驱动表实体类</span>
		 */
		SubQueryJoinBuilder(final EntityJoinsBuilder<P> parentBuilder, @Nonnull final JoinType joinType,
		                    @Nonnull final Class<?> mainEntity) {
			super(parentBuilder, new SubQueryJoin(), ENTITY_FACTORY.tableName(mainEntity));
			this.queryJoin.setJoinType(joinType);
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
		public SubQueryJoinBuilder aliasName(final String aliasName) {
			if (StringUtils.notBlank(aliasName)) {
				this.queryJoin.setAliasName(aliasName);
			}
			return this;
		}

		/**
		 * <h3 class="en-US">Sub-query builder instance object</h3>
		 * <h3 class="zh-CN">子查询构建器实例对象</h3>
		 *
		 * @return <span class="en-US">Sub-query builder instance object</span>
		 * <span class="zh-CN">子查询构建器实例对象</span>
		 */
		public EntitySubQueryBuilder.EntityTableSubQueryBuilder<SubQueryJoinBuilder> subQueryBuilder() {
			return new EntitySubQueryBuilder.EntityTableSubQueryBuilder<>(this);
		}

		@Override
		public void confirm(final Object object) {
			if (object instanceof TableSubQuery) {
				this.queryJoin.setSubQuery((TableSubQuery) object);
			} else {
				super.confirm(object);
			}
		}
	}
}
