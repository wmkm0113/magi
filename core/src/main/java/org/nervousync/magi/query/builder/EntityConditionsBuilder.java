package org.nervousync.magi.query.builder;

import jakarta.annotation.Nonnull;
import org.nervousync.brain.enumerations.query.ConditionCode;
import org.nervousync.brain.enumerations.query.ConnectionCode;
import org.nervousync.brain.query.builder.ConditionsBuilder;
import org.nervousync.brain.query.condition.Condition;
import org.nervousync.brain.query.condition.impl.ColumnCondition;
import org.nervousync.brain.query.condition.impl.GroupCondition;
import org.nervousync.brain.query.param.AbstractParameter;
import org.nervousync.builder.AbstractBuilder;
import org.nervousync.builder.ParentBuilder;
import org.nervousync.exceptions.builder.BuilderException;
import org.nervousync.magi.entity.EntityFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2 class="en-US">Query conditions information list builder</h2>
 * <h2 class="zh-CN">查询条件信息列表构建器</h2>
 *
 * @param <P> <span class="en-US">Parent builder generic type class</span>
 *            <span class="zh-CN">父构建器泛型类</span>
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
 */
@SuppressWarnings("unused")
public final class EntityConditionsBuilder<P extends ParentBuilder>
		extends AbstractBuilder<P, ConditionsBuilder.Conditions> {

	/**
	 * <span class="en-US">Query condition instance list</span>
	 * <span class="zh-CN">查询条件实例对象列表</span>
	 */
	@Nonnull
	private final List<Condition> conditions = new ArrayList<>();
	/**
	 * <span class="en-US">Having condition flag</span>
	 * <span class="zh-CN">Having字句条件标记</span>
	 */
	private final boolean having;

	/**
	 * <h3 class="en-US">Protected constructor for AbstractBuilder</h3>
	 * <h3 class="zh-CN">AbstractBuilder的构造函数</h3>
	 *
	 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
	 *                      <span class="zh-CN">父构建器实例对象</span>
	 * @param having        <span class="en-US">Having condition flag</span>
	 *                      <span class="zh-CN">Having字句条件标记</span>
	 * @param conditions    <span class="en-US">Query condition instance list</span>
	 *                      <span class="zh-CN">查询条件实例对象列表</span>
	 */
	EntityConditionsBuilder(final P parentBuilder, final boolean having, final List<Condition> conditions) {
		super(parentBuilder);
		if (conditions != null) {
			this.conditions.addAll(conditions);
		}
		this.having = having;
	}

	/**
	 * <h3 class="en-US">Data column less condition information builder</h3>
	 * <h3 class="zh-CN">数据列小于条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> less(@Nonnull final Class<?> entityClass,
	                                                               @Nonnull final String identifyName)
			throws SQLException {
		return this.less(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column less or equal condition information builder</h3>
	 * <h3 class="zh-CN">数据列小于等于条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> lessEqual(@Nonnull final Class<?> entityClass,
	                                                                    @Nonnull final String identifyName)
			throws SQLException {
		return this.lessEqual(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column greater condition information builder</h3>
	 * <h3 class="zh-CN">数据列大于条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> greater(@Nonnull final Class<?> entityClass,
	                                                                  @Nonnull final String identifyName)
			throws SQLException {
		return this.greater(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column greater or equal condition information builder</h3>
	 * <h3 class="zh-CN">数据列大于等于条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> greaterEqual(@Nonnull final Class<?> entityClass,
	                                                                       @Nonnull final String identifyName)
			throws SQLException {
		return this.greaterEqual(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column equal condition information builder</h3>
	 * <h3 class="zh-CN">数据列等于条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> equalTo(@Nonnull final Class<?> entityClass,
	                                                                  @Nonnull final String identifyName)
			throws SQLException {
		return this.equalTo(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column not equal condition information builder</h3>
	 * <h3 class="zh-CN">数据列不等于条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> notEqual(@Nonnull final Class<?> entityClass,
	                                                                   @Nonnull final String identifyName)
			throws SQLException {
		return this.notEqual(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column in arrays condition information builder</h3>
	 * <h3 class="zh-CN">数据列在数组中条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> in(@Nonnull final Class<?> entityClass,
	                                                             @Nonnull final String identifyName)
			throws SQLException {
		return this.in(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column not in arrays condition information builder</h3>
	 * <h3 class="zh-CN">数据列不在数组中条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> notIn(@Nonnull final Class<?> entityClass,
	                                                                @Nonnull final String identifyName)
			throws SQLException {
		return this.notIn(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column in ranges condition information builder</h3>
	 * <h3 class="zh-CN">数据列在指定区间条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> betweenAnd(@Nonnull final Class<?> entityClass,
	                                                                     @Nonnull final String identifyName)
			throws SQLException {
		return this.betweenAnd(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column in ranges condition information builder</h3>
	 * <h3 class="zh-CN">数据列不在指定区间条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> notBetweenAnd(@Nonnull final Class<?> entityClass,
	                                                                        @Nonnull final String identifyName)
			throws SQLException {
		return this.notBetweenAnd(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column is null condition information builder</h3>
	 * <h3 class="zh-CN">数据列为空条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public EntityConditionsBuilder<P> isNull(@Nonnull final Class<?> entityClass,
	                                         @Nonnull final String identifyName) throws SQLException {
		return this.isNull(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column not null condition information builder</h3>
	 * <h3 class="zh-CN">数据列不为空条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public EntityConditionsBuilder<P> notNull(@Nonnull final Class<?> entityClass,
	                                          @Nonnull final String identifyName) throws SQLException {
		return this.notNull(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column like condition information builder</h3>
	 * <h3 class="zh-CN">数据列模糊匹配条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> like(@Nonnull final Class<?> entityClass,
	                                                               @Nonnull final String identifyName)
			throws SQLException {
		return this.like(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column is not like condition information builder</h3>
	 * <h3 class="zh-CN">数据列非模糊匹配条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> notLike(@Nonnull final Class<?> entityClass,
	                                                                  @Nonnull final String identifyName)
			throws SQLException {
		return this.notLike(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data exists condition information builder</h3>
	 * <h3 class="zh-CN">数据存在条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> exists(@Nonnull final Class<?> entityClass,
	                                                                 @Nonnull final String identifyName)
			throws SQLException {
		return this.exists(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data don't exist condition information builder</h3>
	 * <h3 class="zh-CN">数据不存在条件信息构建器</h3>
	 *
	 * @param entityClass  <span class="en-US">Data table entity class</span>
	 *                     <span class="zh-CN">数据表实体类</span>
	 * @param identifyName <span class="en-US">Data column identify name</span>
	 *                     <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> notExists(@Nonnull final Class<?> entityClass,
	                                                                    @Nonnull final String identifyName)
			throws SQLException {
		return this.notExists(ConnectionCode.AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column less condition information builder</h3>
	 * <h3 class="zh-CN">数据列小于条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> less(final ConnectionCode connectionCode,
	                                                               @Nonnull final Class<?> entityClass,
	                                                               @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.LESS, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column less or equal condition information builder</h3>
	 * <h3 class="zh-CN">数据列小于等于条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> lessEqual(final ConnectionCode connectionCode,
	                                                                    @Nonnull final Class<?> entityClass,
	                                                                    @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.LESS_EQUAL, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column greater condition information builder</h3>
	 * <h3 class="zh-CN">数据列大于条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> greater(final ConnectionCode connectionCode,
	                                                                  @Nonnull final Class<?> entityClass,
	                                                                  @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.GREATER, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column greater or equal condition information builder</h3>
	 * <h3 class="zh-CN">数据列大于等于条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> greaterEqual(final ConnectionCode connectionCode,
	                                                                       @Nonnull final Class<?> entityClass,
	                                                                       @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.GREATER_EQUAL, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column equal condition information builder</h3>
	 * <h3 class="zh-CN">数据列等于条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> equalTo(final ConnectionCode connectionCode,
	                                                                  @Nonnull final Class<?> entityClass,
	                                                                  @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.EQUAL, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column not equal condition information builder</h3>
	 * <h3 class="zh-CN">数据列不等于条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> notEqual(final ConnectionCode connectionCode,
	                                                                   @Nonnull final Class<?> entityClass,
	                                                                   @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.NOT_EQUAL, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column in arrays condition information builder</h3>
	 * <h3 class="zh-CN">数据列在数组中条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> in(final ConnectionCode connectionCode,
	                                                             @Nonnull final Class<?> entityClass,
	                                                             @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.IN, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column not in arrays condition information builder</h3>
	 * <h3 class="zh-CN">数据列不在数组中条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> notIn(final ConnectionCode connectionCode,
	                                                                @Nonnull final Class<?> entityClass,
	                                                                @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.NOT_IN, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column in ranges condition information builder</h3>
	 * <h3 class="zh-CN">数据列在指定区间条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> betweenAnd(final ConnectionCode connectionCode,
	                                                                     @Nonnull final Class<?> entityClass,
	                                                                     @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.BETWEEN_AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column in ranges condition information builder</h3>
	 * <h3 class="zh-CN">数据列不在指定区间条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> notBetweenAnd(final ConnectionCode connectionCode,
	                                                                        @Nonnull final Class<?> entityClass,
	                                                                        @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.NOT_BETWEEN_AND, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column is null condition information builder</h3>
	 * <h3 class="zh-CN">数据列为空条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public EntityConditionsBuilder<P> isNull(final ConnectionCode connectionCode,
	                                         @Nonnull final Class<?> entityClass, @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.IS_NULL, entityClass, identifyName).confirm();
	}

	/**
	 * <h3 class="en-US">Data column not null condition information builder</h3>
	 * <h3 class="zh-CN">数据列不为空条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public EntityConditionsBuilder<P> notNull(final ConnectionCode connectionCode,
	                                          @Nonnull final Class<?> entityClass, @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.NOT_NULL, entityClass, identifyName).confirm();
	}

	/**
	 * <h3 class="en-US">Data column like condition information builder</h3>
	 * <h3 class="zh-CN">数据列模糊匹配条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> like(final ConnectionCode connectionCode,
	                                                               @Nonnull final Class<?> entityClass,
	                                                               @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.LIKE, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column is not like condition information builder</h3>
	 * <h3 class="zh-CN">数据列非模糊匹配条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> notLike(final ConnectionCode connectionCode,
	                                                                  @Nonnull final Class<?> entityClass,
	                                                                  @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.NOT_LIKE, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data exists condition information builder</h3>
	 * <h3 class="zh-CN">数据存在条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> exists(final ConnectionCode connectionCode,
	                                                                 @Nonnull final Class<?> entityClass,
	                                                                 @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.EXISTS, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data don't exist condition information builder</h3>
	 * <h3 class="zh-CN">数据不存在条件信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public ColumnConditionBuilder<EntityConditionsBuilder<P>> notExists(final ConnectionCode connectionCode,
	                                                                    @Nonnull final Class<?> entityClass,
	                                                                    @Nonnull final String identifyName)
			throws SQLException {
		return this.column(connectionCode, ConditionCode.NOT_EXISTS, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Data column query condition information builder</h3>
	 * <h3 class="zh-CN">数据列查询信息构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @param condition      <span class="en-US">Query condition code</span>
	 *                       <span class="zh-CN">查询条件运算代码</span>
	 * @param entityClass    <span class="en-US">Data table entity class</span>
	 *                       <span class="zh-CN">数据表实体类</span>
	 * @param identifyName   <span class="en-US">Data column identify name</span>
	 *                       <span class="zh-CN">数据列识别名称</span>
	 * @return <span class="en-US">Data column query condition information builder instance object</span>
	 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	private ColumnConditionBuilder<EntityConditionsBuilder<P>> column(final ConnectionCode connectionCode,
	                                                                  @Nonnull final ConditionCode condition,
	                                                                  @Nonnull final Class<?> entityClass,
	                                                                  @Nonnull final String identifyName)
			throws SQLException {
		return new ColumnConditionBuilder<>(this, connectionCode, condition, entityClass, identifyName);
	}

	/**
	 * <h3 class="en-US">Group query condition information builder</h3>
	 * <h3 class="zh-CN">查询信息组构建器</h3>
	 *
	 * @return <span class="en-US">Group query condition information builder instance object</span>
	 * <span class="zh-CN">查询信息组构建器实例对象</span>
	 */
	public GroupConditionBuilder<EntityConditionsBuilder<P>> group() {
		return this.group(ConnectionCode.AND);
	}

	/**
	 * <h3 class="en-US">Group query condition information builder</h3>
	 * <h3 class="zh-CN">查询信息组构建器</h3>
	 *
	 * @param connectionCode <span class="en-US">Query connection code</span>
	 *                       <span class="zh-CN">查询条件连接代码</span>
	 * @return <span class="en-US">Group query condition information builder instance object</span>
	 * <span class="zh-CN">查询信息组构建器实例对象</span>
	 */
	public GroupConditionBuilder<EntityConditionsBuilder<P>> group(final ConnectionCode connectionCode) {
		return new GroupConditionBuilder<>(this, connectionCode);
	}

	@Override
	public void confirm(final Object object) {
		if (object instanceof Condition) {
			this.conditions.add((Condition) object);
		}
	}

	@Override
	public ConditionsBuilder.Conditions build() throws BuilderException {
		return new ConditionsBuilder.Conditions(this.conditions, this.having);
	}

	/**
	 * <h2 class="en-US">Abstract class for query conditions information builder</h2>
	 * <h2 class="zh-CN">查询条件信息构建器抽象类</h2>
	 *
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	private static abstract class ConditionBuilder<P extends ParentBuilder, T extends Condition>
			extends AbstractBuilder<P, T> {

		/**
		 * <span class="en-US">Entity factory instance object</span>
		 * <span class="zh-CN">实体类工厂实例对象</span>
		 */
		protected static final EntityFactory ENTITY_FACTORY = EntityFactory.getInstance();

		/**
		 * <span class="en-US">Query condition instance object</span>
		 * <span class="zh-CN">查询条件实例对象</span>
		 */
		protected final T condition;

		/**
		 * <h3 class="en-US">Protected constructor for AbstractBuilder</h3>
		 * <h3 class="zh-CN">AbstractBuilder的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 */
		protected ConditionBuilder(final P parentBuilder, final T condition) {
			super(parentBuilder);
			this.condition = condition;
		}
	}

	/**
	 * <h2 class="en-US">Data column conditions information builder</h2>
	 * <h2 class="zh-CN">数据列查询条件信息构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder generic type class</span>
	 *            <span class="zh-CN">父构建器泛型类</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public static final class ColumnConditionBuilder<P extends ParentBuilder>
			extends ConditionBuilder<P, ColumnCondition> {

		/**
		 * <h3 class="en-US">Constructor method for the data column conditions information builder</h3>
		 * <h3 class="zh-CN">数据列查询条件信息构建器的构造函数</h3>
		 *
		 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
		 *                      <span class="zh-CN">父构建器实例对象</span>
		 * @param connection    <span class="en-US">Query connection code</span>
		 *                      <span class="zh-CN">查询条件连接代码</span>
		 * @param condition     <span class="en-US">Query condition code</span>
		 *                      <span class="zh-CN">查询条件运算代码</span>
		 * @param entityClass   <span class="en-US">Data table entity class</span>
		 *                      <span class="zh-CN">数据表实体类</span>
		 * @param identifyName  <span class="en-US">Data column identify name</span>
		 *                      <span class="zh-CN">数据列识别名称</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder(final P parentBuilder, @Nonnull final ConnectionCode connection,
		                              @Nonnull final ConditionCode condition,
		                              @Nonnull final Class<?> entityClass, @Nonnull final String identifyName)
				throws SQLException {
			super(parentBuilder, new ColumnCondition());
			this.condition.setConnectionCode(connection);
			this.condition.setConditionCode(condition);
			this.condition.setTableName(ENTITY_FACTORY.tableName(entityClass));
			this.condition.setColumnName(ENTITY_FACTORY.columnName(entityClass, identifyName));
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
		public ColumnConditionBuilder<P> sortCode(final int sortCode) {
			this.condition.setSortCode(sortCode);
			return this;
		}

		/**
		 * <h3 class="en-US">Set the execute function name</h3>
		 * <h3 class="zh-CN">设置查询匹配值运算函数名</h3>
		 *
		 * @param functionName <span class="en-US">Execute function name</span>
		 *                     <span class="zh-CN">运算函数名</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public ColumnConditionBuilder<P> function(final String functionName) {
			this.condition.setFunctionName(functionName);
			return this;
		}

		/**
		 * <h3 class="en-US">Set condition match value</h3>
		 * <h3 class="zh-CN">设置查询匹配值</h3>
		 *
		 * @param matchValue <span class="en-US">Condition match value</span>
		 *                   <span class="zh-CN">查询匹配值</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public ColumnConditionBuilder<P> matchValue(final Object matchValue) {
			this.condition.setConditionParameter(AbstractParameter.constant(matchValue));
			return this;
		}

		/**
		 * <h3 class="en-US">Set condition match column information</h3>
		 * <h3 class="zh-CN">设置查询匹配数据列</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<P> matchColumn(@Nonnull final Class<?> entityClass,
		                                             @Nonnull final String identifyName) throws SQLException {
			this.condition.setConditionParameter(
					AbstractParameter.column(ENTITY_FACTORY.tableName(entityClass),
							ENTITY_FACTORY.columnName(entityClass, identifyName)));
			return this;
		}

		/**
		 * <h3 class="en-US">Query condition match function information builder</h3>
		 * <h3 class="zh-CN">查询匹配函数构建器</h3>
		 *
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public EntityParametersBuilder.FunctionParameterBuilder<ColumnConditionBuilder<P>> matchFunction() {
			return new EntityParametersBuilder.FunctionParameterBuilder<>(this);
		}

		/**
		 * <h3 class="en-US">Set condition ranges begin value and end value</h3>
		 * <h3 class="zh-CN">设置查询区间起始值和终止值</h3>
		 *
		 * @param beginValue <span class="en-US">Ranges begin value</span>
		 *                   <span class="zh-CN">区间起始值</span>
		 * @param endValue   <span class="en-US">Ranges end value</span>
		 *                   <span class="zh-CN">区间终止值</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public ColumnConditionBuilder<P> inRanges(@Nonnull final Object beginValue, @Nonnull final Object endValue) {
			this.condition.setConditionParameter(AbstractParameter.ranges(beginValue, endValue));
			return this;
		}

		/**
		 * <h3 class="en-US">Set condition value array</h3>
		 * <h3 class="zh-CN">设置查询匹配值数组</h3>
		 *
		 * @param matchValues <span class="en-US">Condition value array</span>
		 *                    <span class="zh-CN">查询匹配值数组</span>
		 * @return <span class="en-US">Current builder instance object</span>
		 * <span class="zh-CN">当前构建器实例对象</span>
		 */
		public ColumnConditionBuilder<P> inArray(@Nonnull final Object... matchValues) {
			this.condition.setConditionParameter(AbstractParameter.arrays(matchValues));
			return this;
		}

		/**
		 * <h3 class="en-US">Condition match sub-query information builder</h3>
		 * <h3 class="zh-CN">匹配子查询构建器</h3>
		 *
		 * @return <span class="en-US">Sub-query builder instance object</span>
		 * <span class="zh-CN">子查询构建器实例对象</span>
		 */
		public EntitySubQueryBuilder.EntityScalarSubQueryBuilder<ColumnConditionBuilder<P>> matchQuery() {
			return new EntitySubQueryBuilder.EntityScalarSubQueryBuilder<>(this);
		}

		@Override
		public void confirm(final Object object) {
			if (object instanceof AbstractParameter) {
				this.condition.setConditionParameter((AbstractParameter<?>) object);
			}
		}

		@Override
		public ColumnCondition build() throws BuilderException {
			return this.condition;
		}
	}

	/**
	 * <h2 class="en-US">Group conditions information builder</h2>
	 * <h2 class="zh-CN">查询条件组信息构建器</h2>
	 *
	 * @param <P> <span class="en-US">Parent builder generic type class</span>
	 *            <span class="zh-CN">父构建器泛型类</span>
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Oct 28, 2020 11:46:08 $
	 */
	public static final class GroupConditionBuilder<P extends ParentBuilder>
			extends ConditionBuilder<P, GroupCondition> {

		/**
		 * <span class="en-US">Match condition list</span>
		 * <span class="zh-CN">匹配条件列表</span>
		 */
		private final List<Condition> conditionList;

		/**
		 * <h3 class="en-US">Constructor method for the group conditions information builder</h3>
		 * <h3 class="zh-CN">查询条件组信息构建器的构造函数</h3>
		 *
		 * @param parentBuilder  <span class="en-US">Parent builder instance object</span>
		 *                       <span class="zh-CN">父构建器实例对象</span>
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 */
		public GroupConditionBuilder(final P parentBuilder, final ConnectionCode connectionCode) {
			super(parentBuilder, new GroupCondition());
			this.condition.setConnectionCode(connectionCode);
			this.conditionList = new ArrayList<>();
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
		public GroupConditionBuilder<P> sortCode(final int sortCode) {
			this.condition.setSortCode(sortCode);
			return this;
		}

		/**
		 * <h3 class="en-US">Data column less condition information builder</h3>
		 * <h3 class="zh-CN">数据列小于条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> less(@Nonnull final Class<?> entityClass,
		                                                             @Nonnull final String identifyName)
				throws SQLException {
			return this.less(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column less or equal condition information builder</h3>
		 * <h3 class="zh-CN">数据列小于等于条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> lessEqual(@Nonnull final Class<?> entityClass,
		                                                                  @Nonnull final String identifyName)
				throws SQLException {
			return this.lessEqual(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column greater condition information builder</h3>
		 * <h3 class="zh-CN">数据列大于条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> greater(@Nonnull final Class<?> entityClass,
		                                                                @Nonnull final String identifyName)
				throws SQLException {
			return this.greater(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column greater or equal condition information builder</h3>
		 * <h3 class="zh-CN">数据列大于等于条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> greaterEqual(@Nonnull final Class<?> entityClass,
		                                                                     @Nonnull final String identifyName)
				throws SQLException {
			return this.greaterEqual(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column equal condition information builder</h3>
		 * <h3 class="zh-CN">数据列等于条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> equalTo(@Nonnull final Class<?> entityClass,
		                                                                @Nonnull final String identifyName)
				throws SQLException {
			return this.equalTo(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column not equal condition information builder</h3>
		 * <h3 class="zh-CN">数据列不等于条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> notEqual(@Nonnull final Class<?> entityClass,
		                                                                 @Nonnull final String identifyName)
				throws SQLException {
			return this.notEqual(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column in arrays condition information builder</h3>
		 * <h3 class="zh-CN">数据列在数组中条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> in(@Nonnull final Class<?> entityClass,
		                                                           @Nonnull final String identifyName)
				throws SQLException {
			return this.in(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column not in arrays condition information builder</h3>
		 * <h3 class="zh-CN">数据列不在数组中条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> notIn(@Nonnull final Class<?> entityClass,
		                                                              @Nonnull final String identifyName)
				throws SQLException {
			return this.notIn(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column in ranges condition information builder</h3>
		 * <h3 class="zh-CN">数据列在指定区间条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> betweenAnd(@Nonnull final Class<?> entityClass,
		                                                                   @Nonnull final String identifyName)
				throws SQLException {
			return this.betweenAnd(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column in ranges condition information builder</h3>
		 * <h3 class="zh-CN">数据列不在指定区间条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> notBetweenAnd(@Nonnull final Class<?> entityClass,
		                                                                      @Nonnull final String identifyName)
				throws SQLException {
			return this.notBetweenAnd(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column is null condition information builder</h3>
		 * <h3 class="zh-CN">数据列为空条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public GroupConditionBuilder<P> isNull(@Nonnull final Class<?> entityClass,
		                                       @Nonnull final String identifyName) throws SQLException {
			return this.isNull(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column not null condition information builder</h3>
		 * <h3 class="zh-CN">数据列不为空条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public GroupConditionBuilder<P> notNull(@Nonnull final Class<?> entityClass,
		                                        @Nonnull final String identifyName) throws SQLException {
			return this.notNull(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column like condition information builder</h3>
		 * <h3 class="zh-CN">数据列模糊匹配条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> like(@Nonnull final Class<?> entityClass,
		                                                             @Nonnull final String identifyName)
				throws SQLException {
			return this.like(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column is not like condition information builder</h3>
		 * <h3 class="zh-CN">数据列非模糊匹配条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> notLike(@Nonnull final Class<?> entityClass,
		                                                                @Nonnull final String identifyName)
				throws SQLException {
			return this.notLike(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data exists condition information builder</h3>
		 * <h3 class="zh-CN">数据存在条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> exists(@Nonnull final Class<?> entityClass,
		                                                               @Nonnull final String identifyName)
				throws SQLException {
			return this.exists(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data don't exist condition information builder</h3>
		 * <h3 class="zh-CN">数据不存在条件信息构建器</h3>
		 *
		 * @param entityClass  <span class="en-US">Data table entity class</span>
		 *                     <span class="zh-CN">数据表实体类</span>
		 * @param identifyName <span class="en-US">Data column identify name</span>
		 *                     <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> notExists(@Nonnull final Class<?> entityClass,
		                                                                  @Nonnull final String identifyName)
				throws SQLException {
			return this.notExists(ConnectionCode.AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column less condition information builder</h3>
		 * <h3 class="zh-CN">数据列小于条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> less(final ConnectionCode connectionCode,
		                                                             @Nonnull final Class<?> entityClass,
		                                                             @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.LESS, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column less or equal condition information builder</h3>
		 * <h3 class="zh-CN">数据列小于等于条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> lessEqual(final ConnectionCode connectionCode,
		                                                                  @Nonnull final Class<?> entityClass,
		                                                                  @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.LESS_EQUAL, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column greater condition information builder</h3>
		 * <h3 class="zh-CN">数据列大于条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> greater(final ConnectionCode connectionCode,
		                                                                @Nonnull final Class<?> entityClass,
		                                                                @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.GREATER, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column greater or equal condition information builder</h3>
		 * <h3 class="zh-CN">数据列大于等于条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> greaterEqual(final ConnectionCode connectionCode,
		                                                                     @Nonnull final Class<?> entityClass,
		                                                                     @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.GREATER_EQUAL, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column equal condition information builder</h3>
		 * <h3 class="zh-CN">数据列等于条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> equalTo(final ConnectionCode connectionCode,
		                                                                @Nonnull final Class<?> entityClass,
		                                                                @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.EQUAL, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column not equal condition information builder</h3>
		 * <h3 class="zh-CN">数据列不等于条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> notEqual(final ConnectionCode connectionCode,
		                                                                 @Nonnull final Class<?> entityClass,
		                                                                 @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.NOT_EQUAL, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column in arrays condition information builder</h3>
		 * <h3 class="zh-CN">数据列在数组中条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> in(final ConnectionCode connectionCode,
		                                                           @Nonnull final Class<?> entityClass,
		                                                           @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.IN, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column not in arrays condition information builder</h3>
		 * <h3 class="zh-CN">数据列不在数组中条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> notIn(final ConnectionCode connectionCode,
		                                                              @Nonnull final Class<?> entityClass,
		                                                              @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.NOT_IN, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column in ranges condition information builder</h3>
		 * <h3 class="zh-CN">数据列在指定区间条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> betweenAnd(final ConnectionCode connectionCode,
		                                                                   @Nonnull final Class<?> entityClass,
		                                                                   @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.BETWEEN_AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column in ranges condition information builder</h3>
		 * <h3 class="zh-CN">数据列不在指定区间条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> notBetweenAnd(final ConnectionCode connectionCode,
		                                                                      @Nonnull final Class<?> entityClass,
		                                                                      @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.NOT_BETWEEN_AND, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column is null condition information builder</h3>
		 * <h3 class="zh-CN">数据列为空条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public GroupConditionBuilder<P> isNull(final ConnectionCode connectionCode,
		                                       @Nonnull final Class<?> entityClass, @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.IS_NULL, entityClass, identifyName).confirm();
		}

		/**
		 * <h3 class="en-US">Data column not null condition information builder</h3>
		 * <h3 class="zh-CN">数据列不为空条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public GroupConditionBuilder<P> notNull(final ConnectionCode connectionCode,
		                                        @Nonnull final Class<?> entityClass, @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.NOT_NULL, entityClass, identifyName).confirm();
		}

		/**
		 * <h3 class="en-US">Data column like condition information builder</h3>
		 * <h3 class="zh-CN">数据列模糊匹配条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> like(final ConnectionCode connectionCode,
		                                                             @Nonnull final Class<?> entityClass,
		                                                             @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.LIKE, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column is not like condition information builder</h3>
		 * <h3 class="zh-CN">数据列非模糊匹配条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> notLike(final ConnectionCode connectionCode,
		                                                                @Nonnull final Class<?> entityClass,
		                                                                @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.NOT_LIKE, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data exists condition information builder</h3>
		 * <h3 class="zh-CN">数据存在条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> exists(final ConnectionCode connectionCode,
		                                                               @Nonnull final Class<?> entityClass,
		                                                               @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.EXISTS, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data don't exist condition information builder</h3>
		 * <h3 class="zh-CN">数据不存在条件信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		public ColumnConditionBuilder<GroupConditionBuilder<P>> notExists(final ConnectionCode connectionCode,
		                                                                  @Nonnull final Class<?> entityClass,
		                                                                  @Nonnull final String identifyName)
				throws SQLException {
			return this.column(connectionCode, ConditionCode.NOT_EXISTS, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Data column query condition information builder</h3>
		 * <h3 class="zh-CN">数据列查询信息构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @param condition      <span class="en-US">Query condition code</span>
		 *                       <span class="zh-CN">查询条件运算代码</span>
		 * @param entityClass    <span class="en-US">Data table entity class</span>
		 *                       <span class="zh-CN">数据表实体类</span>
		 * @param identifyName   <span class="en-US">Data column identify name</span>
		 *                       <span class="zh-CN">数据列识别名称</span>
		 * @return <span class="en-US">Data column query condition information builder instance object</span>
		 * <span class="zh-CN">数据列查询信息构建器实例对象</span>
		 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
		 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
		 */
		private ColumnConditionBuilder<GroupConditionBuilder<P>> column(final ConnectionCode connectionCode,
		                                                                @Nonnull final ConditionCode condition,
		                                                                @Nonnull final Class<?> entityClass,
		                                                                @Nonnull final String identifyName)
				throws SQLException {
			return new ColumnConditionBuilder<>(this, connectionCode, condition, entityClass, identifyName);
		}

		/**
		 * <h3 class="en-US">Group query condition information builder</h3>
		 * <h3 class="zh-CN">查询信息组构建器</h3>
		 *
		 * @return <span class="en-US">Group query condition information builder instance object</span>
		 * <span class="zh-CN">查询信息组构建器实例对象</span>
		 */
		public GroupConditionBuilder<GroupConditionBuilder<P>> group() {
			return this.group(ConnectionCode.AND);
		}

		/**
		 * <h3 class="en-US">Group query condition information builder</h3>
		 * <h3 class="zh-CN">查询信息组构建器</h3>
		 *
		 * @param connectionCode <span class="en-US">Query connection code</span>
		 *                       <span class="zh-CN">查询条件连接代码</span>
		 * @return <span class="en-US">Group query condition information builder instance object</span>
		 * <span class="zh-CN">查询信息组构建器实例对象</span>
		 */
		public GroupConditionBuilder<GroupConditionBuilder<P>> group(final ConnectionCode connectionCode) {
			return new GroupConditionBuilder<>(this, connectionCode);
		}

		@Override
		public void confirm(final Object object) throws BuilderException {
			if (object instanceof Condition) {
				this.conditionList.add((Condition) object);
			}
		}

		@Override
		public GroupCondition build() {
			this.condition.setConditionList(this.conditionList);
			return this.condition;
		}
	}
}
