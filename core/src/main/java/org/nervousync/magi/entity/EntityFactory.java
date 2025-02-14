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

package org.nervousync.magi.entity;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;
import org.nervousync.annotations.beans.DataTransfer;
import org.nervousync.annotations.provider.Provider;
import org.nervousync.beans.config.TransferConfig;
import org.nervousync.beans.core.BeanObject;
import org.nervousync.brain.commons.BrainCommons;
import org.nervousync.brain.commons.DataUtils;
import org.nervousync.brain.configs.BrainConfigure;
import org.nervousync.brain.configs.transactional.TransactionalConfig;
import org.nervousync.brain.data.transfer.TransferColumn;
import org.nervousync.brain.defines.*;
import org.nervousync.brain.enumerations.ddl.DropOption;
import org.nervousync.brain.enumerations.ddl.GenerationType;
import org.nervousync.brain.enumerations.dialect.DialectType;
import org.nervousync.brain.enumerations.query.ConditionCode;
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.brain.query.condition.Condition;
import org.nervousync.brain.query.condition.impl.ColumnCondition;
import org.nervousync.brain.query.condition.impl.GroupCondition;
import org.nervousync.brain.query.core.AbstractItem;
import org.nervousync.brain.query.core.SortedItem;
import org.nervousync.brain.query.data.QueryData;
import org.nervousync.brain.query.filter.GroupBy;
import org.nervousync.brain.query.item.ColumnItem;
import org.nervousync.brain.query.item.FunctionItem;
import org.nervousync.brain.query.item.QueryItem;
import org.nervousync.brain.query.param.AbstractParameter;
import org.nervousync.brain.query.param.impl.ColumnParameter;
import org.nervousync.brain.query.param.impl.ConstantParameter;
import org.nervousync.brain.query.param.impl.FunctionParameter;
import org.nervousync.brain.query.param.impl.QueryParameter;
import org.nervousync.brain.sharding.Calculator;
import org.nervousync.brain.source.BrainDataSource;
import org.nervousync.cache.CacheUtils;
import org.nervousync.cache.api.CacheClient;
import org.nervousync.cache.commons.CacheGlobals;
import org.nervousync.commons.Globals;
import org.nervousync.enumerations.core.ConnectionCode;
import org.nervousync.magi.annotations.data.ExcelColumn;
import org.nervousync.magi.annotations.data.Sensitive;
import org.nervousync.magi.annotations.table.GeneratedData;
import org.nervousync.magi.annotations.table.Options;
import org.nervousync.magi.annotations.table.Schema;
import org.nervousync.magi.beans.defines.reference.JoinDefine;
import org.nervousync.magi.beans.defines.reference.ReferenceDefine;
import org.nervousync.magi.beans.defines.sensitive.SensitiveDefine;
import org.nervousync.magi.enumerations.reference.ReferenceType;
import org.nervousync.magi.interceptors.LazyLoadInterceptor;
import org.nervousync.magi.query.PartialCollection;
import org.nervousync.magi.query.optimizer.OptimizedResult;
import org.nervousync.magi.query.optimizer.QueryOptimizer;
import org.nervousync.magi.query.optimizer.step.AbstractStep;
import org.nervousync.magi.query.optimizer.step.MergeStep;
import org.nervousync.magi.query.optimizer.step.QueryStep;
import org.nervousync.utils.*;

import javax.sql.rowset.serial.SerialClob;
import java.io.Serializable;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.function.Predicate;

/**
 * <h2 class="en-US">Entity class factory</h2>
 * <h2 class="zh-CN">实体类工厂</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Mar 30, 2016 17:05:12 $
 */
public final class EntityFactory {

	/**
	 * <span class="en-US">Logger instance</span>
	 * <span class="zh-CN">日志实例</span>
	 */
	private static final LoggerUtils.Logger LOGGER = LoggerUtils.getLogger(EntityFactory.class);

	/**
	 * <span class="en-US">Entity class factory singleton instance object</span>
	 * <span class="zh-CN">实体类工厂单例对象</span>
	 */
	private static EntityFactory INSTANCE = null;
	/**
	 * <span class="en-US">Registered implementation class of query optimizer</span>
	 * <span class="zh-CN">注册的查询优化器实现类</span>
	 */
	private static final Hashtable<String, Class<?>> REGISTERED_OPTIMIZERS = new Hashtable<>();

	/**
	 * <span class="en-US">Data source instance object</span>
	 * <span class="zh-CN">数据源实例对象</span>
	 */
	private final BrainDataSource dataSource;
	/**
	 * <span class="en-US">Used identification code of query optimizer implementation class</span>
	 * <span class="zh-CN">使用的查询优化器实现类识别代码</span>
	 */
	private String optimizerName;
	/**
	 * <span class="en-US">The constant mapping between Java type and JDBC type code</span>
	 * <span class="zh-CN">常量映射表，用于映射Java类型和JDBC类型代码</span>
	 */
	private final Map<Class<?>, Integer> dataConvertMapping = new HashMap<>();

	/**
	 * <span class="en-US">Registered table configure mapping</span>
	 * <span class="zh-CN">已注册的数据表配置信息映射</span>
	 */
	private final Hashtable<String, TableConfig> registeredTables = new Hashtable<>();
	/**
	 * <span class="en-US">Mapping table of data table identification codes and data table names</span>
	 * <span class="zh-CN">数据表识别代码与数据表名的映射表</span>
	 */
	private final Hashtable<String, String> identifiedCodeMapping = new Hashtable<>();
	/**
	 * <span class="en-US">ByteBuddy enhanced and optimized entity class name list</span>
	 * <span class="zh-CN">ByteBuddy增强优化过的实体类名列表</span>
	 */
	private final List<String> redefinedClasses = new ArrayList<>();
	/**
	 * <span class="en-US">List of persistent configuration information for the current thread</span>
	 * <span class="zh-CN">当前线程的持久化配置信息列表</span>
	 */
	private final ThreadLocal<Hashtable<Long, PersistenceConfig>> threadLocal = new ThreadLocal<>();
	/**
	 * <span class="en-US">Restore mode flag for the current thread</span>
	 * <span class="zh-CN">当前线程的数据还原模式标记</span>
	 */
	private final ThreadLocal<Boolean> restoreMode = new ThreadLocal<>();
	/**
	 * <span class="en-US">Data read-only flag for the current thread</span>
	 * <span class="zh-CN">当前线程的数据只读标记</span>
	 */
	private final ThreadLocal<Boolean> readOnly = new ThreadLocal<>();

	static {
		ServiceLoader.load(QueryOptimizer.class)
				.forEach(queryOptimizer ->
						Optional.ofNullable(queryOptimizer.getClass().getAnnotation(Provider.class))
								.ifPresent(provider ->
										REGISTERED_OPTIMIZERS.put(provider.name(), queryOptimizer.getClass())));
	}

	/**
	 * <h3 class="en-US">Private constructor method for entity class factory</h3>
	 * <h3 class="zh-CN">实体类工厂的私有构造方法</h3>
	 */
	private EntityFactory(final String optimizerName) {
		ByteBuddyAgent.install();
		this.registerTypes();
		this.dataSource = BrainDataSource.getInstance();
		this.optimizerName = optimizerName;
	}

	/**
	 * <h3 class="en-US">Static method for initialize entity factory</h3>
	 * <h3 class="zh-CN">静态方法用于初始化实体类工厂</h3>
	 *
	 * @param configure <span class="en-US">Data source configure information instance object</span>
	 *                  <span class="zh-CN">数据源配置信息实例对象</span>
	 */
	public static void initialize(final BrainConfigure configure) {
		initialize(configure, Globals.DEFAULT_VALUE_STRING);
	}

	/**
	 * <h3 class="en-US">Static method for initialize entity factory</h3>
	 * <h3 class="zh-CN">静态方法用于初始化实体类工厂</h3>
	 *
	 * @param configure     <span class="en-US">Data source configure information instance object</span>
	 *                      <span class="zh-CN">数据源配置信息实例对象</span>
	 * @param optimizerName <span class="en-US">Used identification code of query optimizer implementation class</span>
	 *                      <span class="zh-CN">使用的查询优化器实现类识别代码</span>
	 */
	public static void initialize(final BrainConfigure configure, final String optimizerName) {
		BrainDataSource.getInstance().initialize(configure);
		if (INSTANCE == null) {
			synchronized (EntityFactory.class) {
				INSTANCE = new EntityFactory(optimizerName);
				//  Register execute destroy method when the system shutdown
				Runtime.getRuntime().addShutdownHook(new Thread(EntityFactory::destroy));
			}
		}
		INSTANCE.optimizerName = optimizerName;
	}

	/**
	 * <h3 class="en-US">Static getter method for entity class factory singleton instance object</h3>
	 * <h3 class="zh-CN">实体类工厂单例对象的静态Getter方法</h3>
	 *
	 * @return <span class="en-US">Entity class factory singleton instance object</span>
	 * <span class="zh-CN">实体类工厂单例对象</span>
	 */
	public static EntityFactory getInstance() {
		return getInstance(Boolean.FALSE);
	}

	/**
	 * <h3 class="en-US">Static getter method for entity class factory singleton instance object</h3>
	 * <h3 class="zh-CN">实体类工厂单例对象的静态Getter方法</h3>
	 *
	 * @param readOnly <span class="en-US">Read-only flag</span>
	 *                 <span class="zh-CN">只读模式标记</span>
	 * @return <span class="en-US">Entity class factory singleton instance object</span>
	 * <span class="zh-CN">实体类工厂单例对象</span>
	 */
	public static EntityFactory getInstance(final boolean readOnly) {
		return getInstance(readOnly, Boolean.FALSE);
	}

	/**
	 * <h3 class="en-US">Static getter method for entity class factory singleton instance object</h3>
	 * <h3 class="zh-CN">实体类工厂单例对象的静态Getter方法</h3>
	 *
	 * @param readOnly    <span class="en-US">Read-only flag</span>
	 *                    <span class="zh-CN">只读模式标记</span>
	 * @param restoreMode <span class="en-US">Data restore mode flag</span>
	 *                    <span class="zh-CN">数据还原模式标记</span>
	 * @return <span class="en-US">Entity class factory singleton instance object</span>
	 * <span class="zh-CN">实体类工厂单例对象</span>
	 */
	public static EntityFactory getInstance(final boolean readOnly, final boolean restoreMode) {
		if (INSTANCE != null) {
			INSTANCE.threadConfig(readOnly, restoreMode);
		}
		return INSTANCE;
	}

	/**
	 * <h3 class="en-US">Destroy current entity factory</h3>
	 * <h3 class="zh-CN">销毁当前实体类工厂</h3>
	 */
	public static void destroy() {
		if (INSTANCE != null) {
			synchronized (EntityFactory.class) {
				INSTANCE.registeredTables.clear();
				INSTANCE.identifiedCodeMapping.clear();
				INSTANCE.redefinedClasses.clear();
				BrainDataSource.destroy();
				INSTANCE = null;
			}
		}
	}

	/**
	 * <h3 class="en-US">Register the mapping relationship between Java type and JDBC type code</h3>
	 * <h3 class="zh-CN">注册Java类型和JDBC类型代码的映射关系</h3>
	 *
	 * @param typeClass <span class="en-US">Java type class</span>
	 *                  <span class="zh-CN">Java类型</span>
	 * @param jdbcType  <span class="en-US">JDBC type code</span>
	 *                  <span class="zh-CN">JDBC类型代码</span>
	 */
	public void registerType(final Class<?> typeClass, final int jdbcType) {
		if (this.dataConvertMapping.containsKey(typeClass)) {
			LOGGER.warn("Override type mapping: {}", typeClass.getName());
		}
		this.dataConvertMapping.put(typeClass, jdbcType);
	}

	/**
	 * <h3 class="en-US">Scan and register a data table definition class that matches the given package name list</h3>
	 * <h3 class="zh-CN">扫描并注册符合给定包名列表的数据表定义类</h3>
	 *
	 * @param scanPackages <span class="en-US">Package name list, which can be a regular expression list</span>
	 *                     <span class="zh-CN">包名列表，可以是正则表达式列表</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public void scanPackages(final List<String> scanPackages) throws Exception {
		if (scanPackages == null || scanPackages.isEmpty()) {
			return;
		}
		this.scanPackages(Thread.currentThread().getContextClassLoader(), scanPackages);
	}

	/**
	 * <h3 class="en-US">Parse the given array of entity classes and write the mapping relationship into the mapping table</h3>
	 * <h3 class="zh-CN">解析给定的实体类数组，并将映射关系写入映射表</h3>
	 *
	 * @param entityClasses <span class="en-US">Entity classes array</span>
	 *                      <span class="zh-CN">实体类数组</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public void registerTables(final Class<?>... entityClasses) throws Exception {
		for (Class<?> entityClass : entityClasses) {
			TableConfig tableConfig = new TableConfig(this, entityClass);
			String tableName = tableConfig.getTableDefine().getTableName();
			if (this.registeredTables.containsKey(tableName)) {
				LOGGER.warn("Table_Config_Override", entityClass.getName(), tableName);
			}
			this.registeredTables.put(tableName, tableConfig);
			String className = ClassUtils.originalClassName(entityClass);
			this.identifiedCodeMapping.put(className, tableName);
			this.identifiedCodeMapping.put(BrainCommons.identifyCode(tableName), tableName);
			this.identifiedCodeMapping.put(BrainCommons.identifyCode(className), tableName);
			if (tableConfig.containsLazyLoadField()) {
				redefineClass(entityClass);
			}
			if (!tableConfig.getTransferColumns().isEmpty()) {
				DataUtils.register(tableConfig.getTableDefine().getTableName(), tableConfig.getTransferColumns());
			}
		}
	}

	/**
	 * <h3 class="en-US">Checks whether the given identification code is a registry identification code</h3>
	 * <h3 class="zh-CN">检查给定的识别代码是否为注册表识别代码</h3>
	 *
	 * @param identifyCode <span class="en-US">Identified code</span>
	 *                     <span class="zh-CN">识别代码</span>
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 */
	public boolean registeredTable(final String identifyCode) {
		return this.registeredTables.containsKey(this.identifiedCodeMapping.getOrDefault(identifyCode, identifyCode));
	}

	/**
	 * <h3 class="en-US">Read sensitive data</h3>
	 * <h3 class="zh-CN">读取敏感信息</h3>
	 *
	 * @param object       <span class="en-US">Entity classes instance object</span>
	 *                     <span class="zh-CN">实体类实例对象</span>
	 * @param identifyName <span class="en-US">Field name</span>
	 *                     <span class="zh-CN">属性名</span>
	 * @return <span class="en-US">Field value</span>
	 * <span class="zh-CN">属性值</span>
	 */
	public String sensitiveData(final BaseObject object, final String identifyName) {
		if (object == null || StringUtils.isEmpty(identifyName)) {
			return Globals.DEFAULT_VALUE_STRING;
		}
		return Optional.ofNullable(this.tableConfig(object.getClass()))
				.map(tableConfig -> tableConfig.sensitiveData(object, identifyName))
				.orElse(Globals.DEFAULT_VALUE_STRING);
	}

	/**
	 * <h3 class="en-US">Get the data table name based on the given entity class</h3>
	 * <h3 class="zh-CN">根据给定的实体类获取数据表名</h3>
	 *
	 * @param entityClass <span class="en-US">Entity class</span>
	 *                    <span class="zh-CN">实体类</span>
	 * @return <span class="en-US">Data table name</span>
	 * <span class="zh-CN">数据表名</span>
	 */
	public String tableName(final Class<?> entityClass) {
		return Optional.ofNullable(this.tableConfig(entityClass))
				.map(tableConfig -> tableConfig.getTableDefine().getTableName())
				.orElse(Globals.DEFAULT_VALUE_STRING);
	}

	/**
	 * <h3 class="en-US">Get the data column name based on the given entity class and identified code</h3>
	 * <h3 class="zh-CN">根据给定的实体类和识别代码获取数据列名</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class</span>
	 *                     <span class="zh-CN">实体类</span>
	 * @param identifyCode <span class="en-US">Identified code</span>
	 *                     <span class="zh-CN">识别代码</span>
	 * @return <span class="en-US">Data column name</span>
	 * <span class="zh-CN">数据列名</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public String columnName(final Class<?> entityClass, final String identifyCode) throws SQLException {
		return Optional.ofNullable(this.tableConfig(entityClass))
				.map(tableConfig -> tableConfig.columnName(identifyCode))
				.orElseThrow(() -> new MultilingualSQLException(0x00DB00010005L, entityClass.getName()));
	}

	/**
	 * <h3 class="en-US">Get the list of reference data columns based on the given entity class and reference class</h3>
	 * <h3 class="zh-CN">根据给定的实体类和关联类获取关联数据列列表</h3>
	 *
	 * @param entityClass    <span class="en-US">Entity class</span>
	 *                       <span class="zh-CN">实体类</span>
	 * @param referenceClass <span class="en-US">Reference class</span>
	 *                       <span class="zh-CN">关联类</span>
	 * @return <span class="en-US">List of reference data columns</span>
	 * <span class="zh-CN">关联数据列列表</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public List<JoinDefine> joinColumns(final Class<?> entityClass, final Class<?> referenceClass) throws SQLException {
		return Optional.ofNullable(this.tableConfig(entityClass))
				.map(tableConfig -> tableConfig.referenceDefine(referenceClass))
				.map(ReferenceDefine::getJoinColumnList)
				.orElseThrow(() ->
						new MultilingualSQLException(0x00DB00010015L, entityClass.getName(), referenceClass.getName()));
	}

	/**
	 * <h3 class="en-US">Truncate data table</h3>
	 * <h3 class="zh-CN">清空数据表</h3>
	 *
	 * @param entityClasses <span class="en-US">Entity classes array</span>
	 *                      <span class="zh-CN">实体类数组</span>
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	public void truncateTables(final Class<?>... entityClasses) throws Exception {
		for (Class<?> entityClass : entityClasses) {
			TableConfig tableConfig = this.tableConfig(entityClass);
			this.dataSource.truncateTable(tableConfig.getTableDefine().getTableName());
		}
	}

	/**
	 * <h3 class="en-US">Drop data table</h3>
	 * <h3 class="zh-CN">删除数据表</h3>
	 *
	 * @param entityClasses <span class="en-US">Entity classes array</span>
	 *                      <span class="zh-CN">实体类数组</span>
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	public void dropTables(final Class<?>... entityClasses) throws Exception {
		for (Class<?> entityClass : entityClasses) {
			TableConfig tableConfig = this.tableConfig(entityClass);
			this.dataSource.dropTable(tableConfig.getTableDefine().getTableName(), tableConfig.getDropOption());
		}
	}

	/**
	 * <h3 class="en-US">Initialize the current thread used operator based on the given transaction configuration information</h3>
	 * <h3 class="zh-CN">根据给定的事务配置信息初始化当前线程的操作器</h3>
	 *
	 * @param transactionalConfig <span class="en-US">Transactional configure information</span>
	 *                            <span class="zh-CN">事务配置信息</span>
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	public void beginTransactional(final TransactionalConfig transactionalConfig) throws Exception {
		this.dataSource.initTransactional(transactionalConfig);
		this.threadLocal.set(new Hashtable<>());
	}

	/**
	 * <h3 class="en-US">Rollback transactional</h3>
	 * <h3 class="zh-CN">回滚事务</h3>
	 *
	 * @param e <span class="en-US">Cached execution information</span>
	 *          <span class="zh-CN">捕获的异常信息</span>
	 * @throws Exception <span class="en-US">If an error occurs during execution</span>
	 *                   <span class="zh-CN">如果执行过程中出错</span>
	 */
	public void rollback(final Exception e) throws Exception {
		this.dataSource.rollback(e);
	}

	/**
	 * <h3 class="en-US">Submit transactional execute</h3>
	 * <h3 class="zh-CN">提交事务执行</h3>
	 *
	 * @throws Exception <span class="en-US">If an error occurs during execution</span>
	 *                   <span class="zh-CN">如果执行过程中出错</span>
	 */
	public void commit() throws Exception {
		this.dataSource.commit();
	}

	/**
	 * <h3 class="en-US">Finish current transactional</h3>
	 * <h3 class="zh-CN">结束当前事务</h3>
	 *
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	public void endTransactional() throws Exception {
		this.dataSource.endTransactional();
		this.threadLocal.remove();
	}

	/**
	 * <h3 class="en-US">Execute query plan and return query results</h3>
	 * <h3 class="zh-CN">执行查询计划并返回查询结果</h3>
	 *
	 * @param targetClass <span class="en-US">Query result class</span>
	 *                    <span class="zh-CN">查询结果类</span>
	 * @param queryInfo   <span class="en-US">Query information instance object</span>
	 *                    <span class="zh-CN">查询信息实例对象</span>
	 * @param <T>         <span class="en-US">Query result generic class</span>
	 *                    <span class="zh-CN">查询结果泛型类</span>
	 * @return <span class="en-US">Query results</span>
	 * <span class="zh-CN">查询结果</span>
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	public <T> PartialCollection<T> query(final Class<T> targetClass, final QueryInfo queryInfo) throws Exception {
		String cacheKey = queryInfo.isCacheables() ? this.cacheKey(queryInfo) : Globals.DEFAULT_VALUE_STRING;
		if (queryInfo.isCacheables()) {
			String cacheData =
					this.cacheClient().map(cacheClient -> cacheClient.get(cacheKey)).orElse(Globals.DEFAULT_VALUE_STRING);
			if (StringUtils.notBlank(cacheData)) {
				PartialCollection<T> partialCollection = PartialCollection.parse(cacheData, targetClass);
				if (partialCollection != null) {
					return partialCollection;
				}
			}
		}
		OptimizedResult optimizedResult = this.newOptimizer().optimize(queryInfo);
		Map<Long, List<Map<String, Object>>> subQueriesResult = new HashMap<>();

		if (optimizedResult.finalStep()) {
			return new PartialCollection<>(List.of(ObjectUtils.newArray(targetClass)), 0L);
		}
		AbstractStep abstractStep = optimizedResult.nextStep();
		do {
			switch (abstractStep.getStepType()) {
				case Query:
					this.query(abstractStep.unwrap(QueryStep.class), subQueriesResult);
					break;
				case Merge:
					this.merge(abstractStep.unwrap(MergeStep.class), subQueriesResult);
					break;
			}
			AbstractStep nextStep = optimizedResult.nextStep();
			if (nextStep != null) {
				abstractStep = nextStep;
			}
		} while (!optimizedResult.finalStep());

		TableConfig tableConfig = this.tableConfig(targetClass);
		List<Map<String, Object>> queryResults =
				subQueriesResult.getOrDefault(abstractStep.getStepCode(), Collections.emptyList());
		List<Map<String, Object>> recordList = new ArrayList<>();
		long totalCount = 0L;
		switch (abstractStep.getStepType()) {
			case Query:
				recordList.addAll(queryResults);
				totalCount = this.dataSource.queryTotal(abstractStep.unwrap(QueryStep.class).getQueryInfo());
				break;
			case Merge:
				MergeStep mergeStep = abstractStep.unwrap(MergeStep.class);
				if (mergeStep.getBeginIndex() < Globals.INITIALIZE_INT_VALUE) {
					recordList.addAll(queryResults);
				} else {
					if (mergeStep.getEndIndex() < Globals.INITIALIZE_INT_VALUE) {
						for (int i = mergeStep.getBeginIndex(); i < queryResults.size(); i++) {
							recordList.add(queryResults.get(i));
						}
					} else {
						for (int i = mergeStep.getBeginIndex(); i < mergeStep.getEndIndex(); i++) {
							recordList.add(queryResults.get(i));
						}
					}
				}
				totalCount = queryResults.size();
				break;
		}
		List<T> resultList = new ArrayList<>();
		for (Map<String, Object> resultMap : recordList) {
			T object = ObjectUtils.newInstance(targetClass);
			tableConfig.copyData(resultMap, object);
			resultList.add(object);
		}
		PartialCollection<T> partialCollection = new PartialCollection<>(resultList, totalCount);
		if (queryInfo.isCacheables() && StringUtils.notBlank(cacheKey)) {
			this.cacheClient().ifPresent(cacheClient -> cacheClient.set(cacheKey, partialCollection.cacheData()));
		}
		return partialCollection;
	}

	/**
	 * <h3 class="en-US">Save entity class to the database</h3>
	 * <h3 class="zh-CN">保存实体类到数据库</h3>
	 *
	 * @param object <span class="en-US">Entity classes instance object</span>
	 *               <span class="zh-CN">实体类实例对象</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public void saveRecord(@Nonnull final BaseObject object) throws Exception {
		if (this.checkExist(object)) {
			throw new MultilingualSQLException(0x00DB00010009L);
		}
		if (this.readOnly.get()) {
			throw new MultilingualSQLException(0x00DB0001000AL);
		}
		TableConfig tableConfig = this.tableConfig(object.getClass());
		boolean restoreMode = Optional.ofNullable(this.restoreMode.get()).orElse(Boolean.FALSE);
		if (!restoreMode) {
			tableConfig.generateKey(object, this);
		}
		tableConfig.desensitize(object);
		Map<String, Object> dataMap = tableConfig.dataMap(object);
		Map<String, Object> primaryKeyMap =
				this.dataSource.insert(tableConfig.getTableDefine().getTableName(), dataMap);
		if (!primaryKeyMap.isEmpty()) {
			tableConfig.primaryKey(object, primaryKeyMap);
		}
		this.newConfig(Boolean.FALSE, object, dataMap.keySet(), tableConfig);
		if (tableConfig.isCacheable()) {
			this.cacheClient().ifPresent(cacheClient ->
					cacheClient.set(tableConfig.cacheKey(object), object.toFormattedJson()));
		}
		if (!restoreMode) {
			this.mergeObjects(object, tableConfig.getReferenceDefineList(), List.of(CascadeType.ALL, CascadeType.PERSIST));
		}
	}

	/**
	 * <h3 class="en-US">Update entity class to database</h3>
	 * <h3 class="zh-CN">更新实体类到数据库</h3>
	 *
	 * @param object <span class="en-US">Entity classes instance object</span>
	 *               <span class="zh-CN">实体类实例对象</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public void updateRecord(@Nonnull final BaseObject object) throws Exception {
		if (this.readOnly.get()) {
			throw new MultilingualSQLException(0x00DB0001000AL);
		}
		this.checkModify(object);
		TableConfig tableConfig = this.tableConfig(object.getClass());
		tableConfig.desensitize(object);
		this.dataSource.update(tableConfig.getTableDefine().getTableName(),
				tableConfig.dataMap(object, ColumnDefine::isUpdatable),
				tableConfig.dataMap(object, ColumnDefine::isPrimaryKey));
		if (tableConfig.isCacheable()) {
			this.cacheClient().ifPresent(cacheClient ->
					cacheClient.set(tableConfig.cacheKey(object), object.toFormattedJson()));
		}
		this.mergeObjects(object, tableConfig.getReferenceDefineList(), List.of(CascadeType.ALL, CascadeType.MERGE));
	}

	/**
	 * <h3 class="en-US">Delete records corresponding to entity classes from the database</h3>
	 * <h3 class="zh-CN">从数据库中删除实体类对应的记录</h3>
	 *
	 * @param object <span class="en-US">Entity classes instance object</span>
	 *               <span class="zh-CN">实体类实例对象</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public void deleteRecord(@Nonnull final BaseObject object) throws Exception {
		if (this.readOnly.get()) {
			throw new MultilingualSQLException(0x00DB0001000AL);
		}
		this.checkModify(object);
		TableConfig tableConfig = this.tableConfig(object.getClass());
		tableConfig.desensitize(object);
		this.dataSource.delete(tableConfig.getTableDefine().getTableName(),
				tableConfig.dataMap(object, ColumnDefine::isPrimaryKey));
		if (tableConfig.isCacheable()) {
			this.cacheClient().ifPresent(cacheClient -> cacheClient.delete(tableConfig.cacheKey(object)));
		}
		List<CascadeType> cascadeTypes = List.of(CascadeType.ALL, CascadeType.REMOVE);
		for (ReferenceDefine<?> referenceDefine : tableConfig.getReferenceDefineList()) {
			if (Arrays.stream(referenceDefine.getCascadeTypes()).anyMatch(cascadeTypes::contains)) {
				Object referenceObject = ReflectionUtils.getFieldValue(referenceDefine.getFieldName(), object);
				if (referenceDefine.isReturnArray()) {
					for (Object reference : CollectionUtils.toList(referenceObject)) {
						if (reference instanceof BaseObject) {
							((BaseObject) reference).delete();
						}
					}
				} else if (referenceObject instanceof BaseObject) {
					((BaseObject) referenceObject).delete();
				}
			}
		}
		this.threadLocal.get().remove(object.identifiedCode());
	}

	/**
	 * <h3 class="en-US">Refresh the given entity class instance object</h3>
	 * <h3 class="zh-CN">刷新给定的实体类实例对象</h3>
	 *
	 * @param object <span class="en-US">Entity classes instance object</span>
	 *               <span class="zh-CN">实体类实例对象</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public void refreshRecord(@Nonnull final BaseObject object) throws Exception {
		PersistenceConfig persistenceConfig = this.persistenceConfig(object);
		TableConfig tableConfig = this.tableConfig(object.getClass());
		StringBuilder queryColumns = new StringBuilder();
		tableConfig.fieldColumnMapping.forEach((fieldName, columnName) -> {
			if (persistenceConfig.loadedField(fieldName)) {
				queryColumns.append(BrainCommons.DEFAULT_SPLIT_CHARACTER).append(columnName);
			}
		});
		Map<String, Object> dataMap = this.dataSource.retrieve(tableConfig.getTableDefine().getTableName(),
				queryColumns.substring(BrainCommons.DEFAULT_SPLIT_CHARACTER.length()),
				tableConfig.dataMap(object, ColumnDefine::isPrimaryKey), Boolean.FALSE);

		if (dataMap.isEmpty()) {
			throw new MultilingualSQLException(0x00DB00010011L);
		}
		tableConfig.copyData(dataMap, object);
		if (tableConfig.isCacheable()) {
			this.cacheClient().ifPresent(cacheClient ->
					cacheClient.set(tableConfig.cacheKey(object), object.toFormattedJson()));
		}
		List<CascadeType> cascadeTypes = List.of(CascadeType.ALL, CascadeType.REFRESH);
		for (ReferenceDefine<?> referenceDefine : tableConfig.getReferenceDefineList()) {
			if (Arrays.stream(referenceDefine.getCascadeTypes()).anyMatch(cascadeTypes::contains)
					&& persistenceConfig.loadedField(referenceDefine.getFieldName())) {
				Object referenceObject = ReflectionUtils.getFieldValue(referenceDefine.getFieldName(), object);
				if (referenceDefine.isReturnArray()) {
					for (Object reference : CollectionUtils.toList(referenceObject)) {
						if (reference instanceof BaseObject) {
							((BaseObject) reference).refresh();
						}
					}
				} else if (referenceObject instanceof BaseObject) {
					((BaseObject) referenceObject).refresh();
				}
			}
		}
	}

	/**
	 * <h3 class="en-US">Read related records from the database based on the given information</h3>
	 * <h3 class="zh-CN">根据给定的信息从数据库中读取相关记录</h3>
	 *
	 * @param primaryKey  <span class="en-US">Primary key instance object</span>
	 *                    <span class="zh-CN">主键信息</span>
	 * @param entityClass <span class="en-US">Entity classes</span>
	 *                    <span class="zh-CN">实体类</span>
	 * @param forUpdate   <span class="en-US">Retrieve record is using for update</span>
	 *                    <span class="zh-CN">读取的记录用于更新</span>
	 * @param <T>         <span class="en-US">Entity class generic class</span>
	 *                    <span class="zh-CN">实体类的泛型类</span>
	 * @return <span class="en-US">Entity classes instance object</span>
	 * <span class="zh-CN">实体类实例对象</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public <T> T retrieveRecord(final Serializable primaryKey, final Class<T> entityClass, final boolean forUpdate)
			throws Exception {
		TableConfig tableConfig = this.tableConfig(entityClass);
		return this.retrieveRecord(entityClass, tableConfig.filterMap(primaryKey), forUpdate);
	}

	/**
	 * <h3 class="en-US">Execute query commands for data updates</h3>
	 * <h3 class="zh-CN">执行用于数据更新的查询命令</h3>
	 *
	 * @param entityClass <span class="en-US">Entity classes</span>
	 *                    <span class="zh-CN">实体类</span>
	 * @param filterMap   <span class="en-US">Retrieve filter mapping</span>
	 *                    <span class="zh-CN">查询条件映射表</span>
	 * @param forUpdate   <span class="en-US">Retrieve record is using for update</span>
	 *                    <span class="zh-CN">读取的记录用于更新</span>
	 * @param <T>         <span class="en-US">Entity class generic class</span>
	 *                    <span class="zh-CN">实体类的泛型类</span>
	 * @return <span class="en-US">List of data mapping tables for retrieved records</span>
	 * <span class="zh-CN">检索到记录的数据映射表列表</span>
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	public <T> T retrieveRecord(@Nonnull final Class<T> entityClass, final Map<String, Object> filterMap,
	                            final boolean forUpdate) throws Exception {
		TableConfig tableConfig = this.tableConfig(entityClass);
		String cacheKey = tableConfig.cacheKey(filterMap);
		Map<String, Object> dataMap = null;
		boolean missed = Boolean.TRUE;
		if (tableConfig.isCacheable() && !forUpdate) {
			dataMap = this.cacheClient()
					.map(cacheClient -> cacheClient.get(cacheKey))
					.filter(StringUtils::notBlank)
					.map(cacheData -> StringUtils.dataToMap(cacheData, StringUtils.StringType.JSON))
					.orElse(null);
			missed = Boolean.FALSE;
		}
		if (missed) {
			dataMap = this.dataSource.retrieve(tableConfig.getTableDefine().getTableName(),
					Globals.DEFAULT_VALUE_STRING, filterMap, forUpdate);
		}
		if (dataMap == null || dataMap.isEmpty()) {
			return null;
		}
		T object = ObjectUtils.newInstance(entityClass);
		tableConfig.copyData(dataMap, object);
		this.newConfig(forUpdate, (BaseObject) object, dataMap.keySet(), tableConfig);
		if (missed) {
			this.cacheClient().ifPresent(cacheClient ->
					cacheClient.set(cacheKey, ((BaseObject) object).toFormattedJson()));
		}
		return object;
	}

	/**
	 * <h3 class="en-US">Execute query commands for data updates</h3>
	 * <h3 class="zh-CN">执行用于数据更新的查询命令</h3>
	 *
	 * @param entityClass <span class="en-US">Entity classes</span>
	 *                    <span class="zh-CN">实体类</span>
	 * @param filterMap   <span class="en-US">Retrieve filter mapping</span>
	 *                    <span class="zh-CN">查询条件映射表</span>
	 * @param <T>         <span class="en-US">Entity class generic class</span>
	 *                    <span class="zh-CN">实体类的泛型类</span>
	 * @return <span class="en-US">List of data mapping tables for retrieved records</span>
	 * <span class="zh-CN">检索到记录的数据映射表列表</span>
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	public <T> PartialCollection<T> queryForUpdate(@Nonnull final Class<T> entityClass,
	                                               final Map<String, Object> filterMap) throws Exception {
		TableConfig tableConfig = this.tableConfig(entityClass);
		TableDefine tableDefine = tableConfig.getTableDefine();
		List<Condition> conditionList = new ArrayList<>();
		filterMap.forEach((identifyName, identifyValue) ->
				Optional.ofNullable(tableDefine.column(identifyName))
						.map(columnDefine ->
								Condition.column(Globals.DEFAULT_VALUE_INT, ConnectionCode.AND, ConditionCode.EQUAL,
										tableDefine.getTableName(), columnDefine.getColumnName(),
										AbstractParameter.constant(identifyValue)))
						.ifPresent(conditionList::add));
		List<Map<String, Object>> recordList =
				this.dataSource.queryForUpdate(tableConfig.getTableDefine().getTableName(), conditionList,
						tableConfig.getTableDefine().getLockOption());
		List<T> resultList = new ArrayList<>();
		for (Map<String, Object> resultMap : recordList) {
			T object = ObjectUtils.newInstance(entityClass);
			tableConfig.copyData(resultMap, object);
			resultList.add(object);
		}
		return new PartialCollection<>(resultList, recordList.size());
	}

	/**
	 * <h3 class="en-US">Query total record count</h3>
	 * <h3 class="zh-CN">查询总记录数</h3>
	 *
	 * @param entityClass <span class="en-US">Entity classes</span>
	 *                    <span class="zh-CN">实体类</span>
	 * @param filterMap   <span class="en-US">Retrieve filter mapping</span>
	 *                    <span class="zh-CN">查询条件映射表</span>
	 * @return <span class="en-US">Total record count</span>
	 * <span class="zh-CN">总记录条数</span>
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	public Long queryTotal(@Nonnull final Class<?> entityClass, final Map<String, Object> filterMap)
			throws Exception {
		TableConfig tableConfig = this.tableConfig(entityClass);
		return this.dataSource.queryTotal(tableConfig.getTableDefine().getTableName(), filterMap);
	}

	/**
	 * <h3 class="en-US">Query total record count</h3>
	 * <h3 class="zh-CN">查询总记录数</h3>
	 *
	 * @param queryInfo <span class="en-US">Query record information</span>
	 *                  <span class="zh-CN">数据检索信息</span>
	 * @return <span class="en-US">Total record count</span>
	 * <span class="zh-CN">总记录条数</span>
	 * @throws SQLException <span class="en-US">An error occurred during execution</span>
	 *                      <span class="zh-CN">执行过程中出错</span>
	 */
	public Long queryTotal(@Nonnull final QueryInfo queryInfo) throws Exception {
		String cacheKey = queryInfo.isCacheables() ? this.cacheKey(queryInfo) : Globals.DEFAULT_VALUE_STRING;
		if (queryInfo.isCacheables()) {
			String cacheData = this.cacheClient()
					.map(cacheClient -> cacheClient.get(cacheKey))
					.orElse(Globals.DEFAULT_VALUE_STRING);
			if (StringUtils.notBlank(cacheData)) {
				return Long.parseLong(cacheData, 16);
			}
		}
		OptimizedResult optimizedResult = this.newOptimizer().optimize(queryInfo);
		Map<Long, List<Map<String, Object>>> subQueriesResult = new HashMap<>();

		if (optimizedResult.finalStep()) {
			return 0L;
		}
		AbstractStep abstractStep = optimizedResult.nextStep();
		do {
			switch (abstractStep.getStepType()) {
				case Query:
					this.query(abstractStep.unwrap(QueryStep.class), subQueriesResult);
					break;
				case Merge:
					this.merge(abstractStep.unwrap(MergeStep.class), subQueriesResult);
					break;
			}
			AbstractStep nextStep = optimizedResult.nextStep();
			if (nextStep != null) {
				abstractStep = nextStep;
			}
		} while (!optimizedResult.finalStep());

		switch (abstractStep.getStepType()) {
			case Query:
				return this.dataSource.queryTotal(abstractStep.unwrap(QueryStep.class).getQueryInfo());
			case Merge:
				return (long) subQueriesResult.getOrDefault(abstractStep.getStepCode(), Collections.emptyList()).size();
			default:
				return 0L;
		}
	}

	/**
	 * <h3 class="en-US">Read lazy loading data information</h3>
	 * <h3 class="zh-CN">读取懒加载数据信息</h3>
	 *
	 * @param object      <span class="en-US">Entity classes instance object</span>
	 *                    <span class="zh-CN">实体类实例对象</span>
	 * @param fieldName   <span class="en-US">Lazy loading of field names</span>
	 *                    <span class="zh-CN">懒加载属性名</span>
	 * @param returnArray <span class="en-US">Data value is an array</span>
	 *                    <span class="zh-CN">数据值为数组</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public void lazyLoad(@Nonnull final BaseObject object, final String fieldName, final boolean returnArray)
			throws Exception {
		if (!this.checkExist(object) || this.loadedField(object, fieldName)) {
			//  Is not an attached object, maybe a new record or current field was loaded
			return;
		}

		PersistenceConfig persistenceConfig = this.persistenceConfig(object);
		TableConfig tableConfig = this.tableConfig(object.getClass());
		String columnName = tableConfig.columnName(fieldName);
		Object fieldValue = null;
		if (StringUtils.notBlank(columnName)) {
			Map<String, Object> dataMap =
					this.dataSource.retrieve(tableConfig.getTableDefine().getTableName(),
							columnName, tableConfig.dataMap(object, ColumnDefine::isPrimaryKey), Boolean.FALSE);
			if (dataMap.containsKey(columnName)) {
				fieldValue = dataMap.get(columnName);
			}
		} else {
			ReferenceDefine<?> referenceDefine = tableConfig.referenceDefine(fieldName);
			Class<?> referenceClass = referenceDefine.getReferenceClass();
			TableConfig referenceTable = this.tableConfig(referenceClass);
			Map<String, Object> filterMap = new HashMap<>();
			final Object primaryKey;
			if (tableConfig.primaryKeyConfig.isCompositeId()) {
				primaryKey = ReflectionUtils.getFieldValue(tableConfig.primaryKeyConfig.getFieldName(), object);
			} else {
				primaryKey = object;
			}
			referenceDefine.getJoinColumnList().forEach(joinDefine ->
					filterMap.put(referenceTable.columnName(joinDefine.getReferenceField()),
							ReflectionUtils.getFieldValue(joinDefine.getCurrentField(), primaryKey)));
			if (referenceDefine.isReturnArray()) {
				List<Map<String, Object>> resultList =
						this.dataSource.query(referenceTable.getTableDefine().getTableName(),
								Globals.DEFAULT_VALUE_STRING, filterMap);
				List<Object> referenceList = new ArrayList<>();
				for (Map<String, Object> resultMap : resultList) {
					Object referenceObject = ObjectUtils.newInstance(referenceClass);
					referenceTable.copyData(resultMap, referenceObject);
					this.newConfig(persistenceConfig.isForUpdate(), (BaseObject) referenceObject,
							resultMap.keySet(), referenceTable);
					referenceList.add(referenceObject);
				}
				if (returnArray) {
					fieldValue = CollectionUtils.toArray(referenceList);
				} else {
					fieldValue = referenceList;
				}
			} else {
				try {
					Map<String, Object> dataMap =
							this.dataSource.retrieve(referenceTable.getTableDefine().getTableName(),
									Globals.DEFAULT_VALUE_STRING, filterMap, persistenceConfig.isForUpdate());
					if (!dataMap.isEmpty()) {
						fieldValue = ObjectUtils.newInstance(referenceDefine.getReferenceClass());
						referenceTable.copyData(dataMap, fieldValue);
						this.newConfig(persistenceConfig.isForUpdate(), (BaseObject) fieldValue,
								dataMap.keySet(), referenceTable);
					}
				} catch (Exception e) {
					LOGGER.error("");
				}
			}
		}
		ReflectionUtils.setField(fieldName, object, fieldValue);
		persistenceConfig.loadField(fieldName);
	}

	/**
	 * <h3 class="en-US">Get entity class information based on the given data table identification code</h3>
	 * <h3 class="zh-CN">根据给定的数据表识别代码获取实体类信息</h3>
	 *
	 * @param entityClass <span class="en-US">Data table entity class</span>
	 *                    <span class="zh-CN">数据表实体类</span>
	 * @return <span class="en-US">Table configure information</span>
	 * <span class="zh-CN">实体类信息</span>
	 */
	public TableConfig tableConfig(@Nonnull final Class<?> entityClass) {
		return this.tableConfig(ClassUtils.originalClassName(entityClass));
	}

	/**
	 * <h3 class="en-US">Get entity class information based on the given data table identification code</h3>
	 * <h3 class="zh-CN">根据给定的数据表识别代码获取实体类信息</h3>
	 *
	 * @param identifyCode <span class="en-US">Data table identify code</span>
	 *                     <span class="zh-CN">数据表识别代码</span>
	 * @return <span class="en-US">Table configure information</span>
	 * <span class="zh-CN">实体类信息</span>
	 */
	public TableConfig tableConfig(final String identifyCode) {
		return this.registeredTables.get(this.identifiedCodeMapping.getOrDefault(identifyCode, identifyCode));
	}

	/**
	 * <h3 class="en-US">Scan and register a data table definition class that matches the given package name list</h3>
	 * <h3 class="zh-CN">扫描并注册符合给定包名列表的数据表定义类</h3>
	 *
	 * @param classLoader  <span class="en-US">Which class loader need be scanned</span>
	 *                     <span class="zh-CN">需要扫描的类加载器</span>
	 * @param scanPackages <span class="en-US">Package name list, which can be a regular expression list</span>
	 *                     <span class="zh-CN">包名列表，可以是正则表达式列表</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	@SuppressWarnings("unchecked")
	private void scanPackages(final ClassLoader classLoader, @Nonnull final List<String> scanPackages)
			throws Exception {
		if (classLoader == null) {
			return;
		}
		this.scanPackages(classLoader.getParent(), scanPackages);
		List<Class<?>> entityClasses = new ArrayList<>();
		Optional.ofNullable((Vector<Class<?>>) ReflectionUtils.getFieldValue("classes", classLoader))
				.ifPresent(vector -> {
					for (final Class<?> clazz : vector) {
						String packageName = clazz.getPackageName();
						if (clazz.isAnnotationPresent(Table.class) &&
								scanPackages.stream().anyMatch(scanPackage ->
										packageName.startsWith(scanPackage)
												|| StringUtils.matches(packageName, scanPackage))) {
							entityClasses.add(clazz);
						}
					}
				});
		if (!entityClasses.isEmpty()) {
			this.registerTables(entityClasses.toArray(new Class<?>[0]));
		}
	}

	/**
	 * <h3 class="en-US">Set the working mode of the current thread</h3>
	 * <h3 class="zh-CN">设置当前线程的工作模式</h3>
	 *
	 * @param readOnly    <span class="en-US">Read-only flag</span>
	 *                    <span class="zh-CN">只读模式标记</span>
	 * @param restoreMode <span class="en-US">Data restore mode flag</span>
	 *                    <span class="zh-CN">数据还原模式标记</span>
	 */
	private void threadConfig(final boolean readOnly, final boolean restoreMode) {
		this.readOnly.set(readOnly);
		this.restoreMode.set(restoreMode);
	}

	/**
	 * <h3 class="en-US">Generate cache unique identifier</h3>
	 * <h3 class="zh-CN">生成缓存唯一识别码</h3>
	 *
	 * @param queryInfo <span class="en-US">Query information instance object</span>
	 *                  <span class="zh-CN">查询信息实例对象</span>
	 * @return <span class="en-US">Unique identifier</span>
	 * <span class="zh-CN">唯一识别代码</span>
	 */
	private String cacheKey(@Nonnull final QueryInfo queryInfo) throws SQLException {
		TreeMap<String, TreeMap<String, TreeMap<String, Object>>> joinMaps = new TreeMap<>();
		queryInfo.getQueryJoins().forEach(queryJoin -> {
			TreeMap<String, TreeMap<String, Object>> joinMap = joinMaps.getOrDefault(queryJoin.getDriverTable(), new TreeMap<>());
			TreeMap<String, String> joinColumns = new TreeMap<>();
			queryJoin.getJoinInfos()
					.forEach(joinInfo -> joinColumns.put(joinInfo.getJoinKey(), joinInfo.getReferenceKey()));
			TreeMap<String, Object> joinData = joinMap.getOrDefault(queryJoin.getJoinTable(), new TreeMap<>());
			joinData.put("Join_Type", queryJoin.getJoinType().toString());
			joinData.put("Join_Columns", joinColumns);
			joinMap.put(queryJoin.getJoinTable(), joinData);
			joinMaps.put(queryJoin.getDriverTable(), joinMap);
		});

		List<AbstractItem> itemList = queryInfo.getItemList();
		itemList.sort(Comparator.comparing(SortedItem::getSortCode));
		TreeMap<String, List<TreeMap<String, Object>>> itemMaps = new TreeMap<>();
		for (AbstractItem abstractItem : itemList) {
			Optional.of(this.itemMap(abstractItem))
					.filter(itemMap -> !itemMap.isEmpty())
					.ifPresent(itemMap -> {
						List<TreeMap<String, Object>> dataList =
								itemMaps.getOrDefault(abstractItem.getItemType().toString(), new ArrayList<>());
						dataList.add(itemMap);
						itemMaps.put(abstractItem.getItemType().toString(), dataList);
					});
		}
		TreeMap<String, String> orderByData = new TreeMap<>();
		queryInfo.getOrderByList().forEach(orderBy ->
				orderByData.put(orderBy.getTableName() + BrainCommons.DEFAULT_NAME_SPLIT + orderBy.getColumnName(),
						orderBy.getOrderType().toString()));
		List<GroupBy> groupByList = queryInfo.getGroupByList();
		groupByList.sort(Comparator.comparing(SortedItem::getSortCode));
		List<String> groupDataList = new ArrayList<>();
		groupByList.forEach(groupBy ->
				groupDataList.add(groupBy.getTableName() + BrainCommons.DEFAULT_NAME_SPLIT + groupBy.getColumnName()));

		TreeMap<String, Object> queryMap = new TreeMap<>();
		queryMap.put("Driven_Table", queryInfo.getTableName());
		queryMap.put("Query_Joins", joinMaps);
		queryMap.put("Query_Items", itemMaps);
		queryMap.put("Condition_List", this.conditionList(queryInfo.getConditionList()));
		queryMap.put("Order_By", orderByData);
		if (!groupDataList.isEmpty()) {
			queryMap.put("Group_By", groupDataList);
			queryMap.put("Having_List", this.conditionList(queryInfo.getHavingList()));
		}
		if (queryInfo.getPageLimit() > Globals.INITIALIZE_INT_VALUE) {
			queryMap.put("Page_No", Integer.max(queryInfo.getPageNo(), BrainCommons.DEFAULT_PAGE_NO));
			queryMap.put("Page_Limit", queryInfo.getPageLimit());
		}

		String jsonData = StringUtils.objectToString(queryMap, StringUtils.StringType.JSON, Boolean.TRUE);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Query_Cache_Key_Data", jsonData);
		}
		return ConvertUtils.toHex(SecurityUtils.SHA256(jsonData));
	}

	/**
	 * <h3 class="en-US">Convert query item information into data mapping table</h3>
	 * <h3 class="zh-CN">转换查询项信息为数据映射表</h3>
	 *
	 * @param abstractItem <span class="en-US">Query item information</span>
	 *                     <span class="zh-CN">查询项信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 * @throws SQLException <span class="en-US">Error converting data type</span>
	 *                      <span class="zh-CN">转换数据类型时出错</span>
	 */
	private TreeMap<String, Object> itemMap(@Nonnull final AbstractItem abstractItem) throws SQLException {
		TreeMap<String, Object> dataMap = new TreeMap<>();
		switch (abstractItem.getItemType()) {
			case COLUMN:
				dataMap.putAll(this.columnDataMap(abstractItem.unwrap(ColumnItem.class)));
				break;
			case FUNCTION:
				dataMap.putAll(this.functionDataMap(abstractItem.unwrap(FunctionItem.class)));
				break;
			case QUERY:
				QueryItem queryItem = abstractItem.unwrap(QueryItem.class);
				if (queryItem.getQueryData() != null) {
					dataMap.putAll(this.queryDataMap(queryItem.getQueryData()));
				}
				break;
		}

		TreeMap<String, Object> itemMap = new TreeMap<>();
		if (!dataMap.isEmpty()) {
			itemMap.put("Sort_Code", abstractItem.getSortCode());
			itemMap.put("Item_Data", dataMap);
		}
		return itemMap;
	}

	/**
	 * <h3 class="en-US">Convert query column information into data mapping table</h3>
	 * <h3 class="zh-CN">转换查询数据列信息为数据映射表</h3>
	 *
	 * @param columnItem <span class="en-US">Query data column information</span>
	 *                   <span class="zh-CN">查询数据列信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 */
	private TreeMap<String, Object> columnDataMap(@Nonnull final ColumnItem columnItem) {
		TreeMap<String, Object> columnDataMap = new TreeMap<>();
		columnDataMap.put("Identify_Name",
				columnItem.getTableName() + BrainCommons.DEFAULT_NAME_SPLIT + columnItem.getColumnName());
		columnDataMap.put("Distinct", columnItem.isDistinct());
		return columnDataMap;
	}

	/**
	 * <h3 class="en-US">Convert query function information into data mapping table</h3>
	 * <h3 class="zh-CN">转换查询函数信息为数据映射表</h3>
	 *
	 * @param functionItem <span class="en-US">Query function information</span>
	 *                     <span class="zh-CN">查询函数信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 * @throws SQLException <span class="en-US">Error converting data type</span>
	 *                      <span class="zh-CN">转换数据类型时出错</span>
	 */
	private TreeMap<String, Object> functionDataMap(@Nonnull final FunctionItem functionItem) throws SQLException {
		TreeMap<String, Object> functionDataMap = new TreeMap<>();
		functionDataMap.put("Function_Name", functionItem.getFunctionName());
		List<TreeMap<String, Object>> parameterList = new ArrayList<>();
		List<AbstractParameter<?>> parameters = functionItem.getFunctionParams();
		parameters.sort(Comparator.comparingInt(SortedItem::getSortCode));
		for (AbstractParameter<?> abstractParameter : parameters) {
			Optional.of(this.parameterMap(abstractParameter))
					.filter(parameterMap -> !parameterMap.isEmpty())
					.ifPresent(parameterList::add);
		}
		functionDataMap.put("Function_Parameters", parameterList);
		return functionDataMap;

	}

	/**
	 * <h3 class="en-US">Convert sub-query information into data mapping table</h3>
	 * <h3 class="zh-CN">转换子查询信息为数据映射表</h3>
	 *
	 * @param queryData <span class="en-US">Sub-query information</span>
	 *                  <span class="zh-CN">子查询信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 * @throws SQLException <span class="en-US">Error converting data type</span>
	 *                      <span class="zh-CN">转换数据类型时出错</span>
	 */
	private TreeMap<String, Object> queryDataMap(final QueryData queryData) throws SQLException {
		TreeMap<String, Object> queryDataMap = new TreeMap<>();
		queryDataMap.put("Sub_Query_Table", queryData.getTableName());
		queryDataMap.put("Sub_Query_Item", this.itemMap(queryData.getQueryItem()));
		queryDataMap.put("Sub_Condition_List", this.conditionList(queryData.getConditions()));
		if (queryData.getGroupBy().isEmpty()) {
			queryDataMap.put("Sub_Group_List", Collections.emptyList());
			queryDataMap.put("Sub_Having_List", Collections.emptyList());
		} else {
			queryDataMap.put("Sub_Group_List", queryData.getGroupBy());
			queryDataMap.put("Sub_Having_List", this.conditionList(queryData.getHavingList()));
		}
		return queryDataMap;
	}

	/**
	 * <h3 class="en-US">Convert parameter information into data mapping table</h3>
	 * <h3 class="zh-CN">转换参数信息为数据映射表</h3>
	 *
	 * @param abstractParameter <span class="en-US">Parameter information</span>
	 *                          <span class="zh-CN">参数信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 * @throws SQLException <span class="en-US">Error converting data type</span>
	 *                      <span class="zh-CN">转换数据类型时出错</span>
	 */
	private TreeMap<String, Object> parameterMap(@Nonnull final AbstractParameter<?> abstractParameter)
			throws SQLException {
		TreeMap<String, Object> parameterData = new TreeMap<>();
		switch (abstractParameter.getItemType()) {
			case COLUMN:
				ColumnParameter columnParameter = abstractParameter.unwrap(ColumnParameter.class);
				parameterData.putAll(this.columnDataMap(columnParameter.getItemValue()));
				break;
			case CONSTANT:
				ConstantParameter constantParameter = abstractParameter.unwrap(ConstantParameter.class);
				parameterData.put("Constant_Data", constantParameter.getItemValue());
				break;
			case FUNCTION:
				FunctionParameter functionParameter = abstractParameter.unwrap(FunctionParameter.class);
				parameterData.putAll(this.functionDataMap(functionParameter.getItemValue()));
				break;
			case QUERY:
				QueryParameter queryParameter = abstractParameter.unwrap(QueryParameter.class);
				parameterData.putAll(this.queryDataMap(queryParameter.getItemValue()));
				break;
		}

		TreeMap<String, Object> parameterMap = new TreeMap<>();
		if (!parameterData.isEmpty()) {
			parameterMap.put("Parameter_Type", abstractParameter.getItemType().toString());
			parameterMap.put("Parameter_Data", parameterData);
		}
		return parameterMap;
	}

	/**
	 * <h3 class="en-US">Convert the query condition information list into data mapping table</h3>
	 * <h3 class="zh-CN">转换查询匹配信息列表为数据映射表</h3>
	 *
	 * @param conditionList <span class="en-US">Query condition information list</span>
	 *                      <span class="zh-CN">查询匹配信息列表</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 * @throws SQLException <span class="en-US">Error converting data type</span>
	 *                      <span class="zh-CN">转换数据类型时出错</span>
	 */
	private List<TreeMap<String, Object>> conditionList(@Nonnull final List<Condition> conditionList)
			throws SQLException {
		conditionList.sort(Comparator.comparingInt(SortedItem::getSortCode));
		List<TreeMap<String, Object>> returnList = new ArrayList<>();
		for (Condition condition : conditionList) {
			Optional.of(this.conditionMap(condition))
					.filter(conditionMap -> !conditionMap.isEmpty())
					.ifPresent(returnList::add);
		}
		return returnList;
	}

	/**
	 * <h3 class="en-US">Convert query condition information into data mapping table</h3>
	 * <h3 class="zh-CN">转换查询匹配信息为数据映射表</h3>
	 *
	 * @param condition <span class="en-US">Query condition information</span>
	 *                  <span class="zh-CN">查询匹配信息</span>
	 * @return <span class="en-US">Data mapping table</span>
	 * <span class="zh-CN">数据映射表</span>
	 * @throws SQLException <span class="en-US">Error converting data type</span>
	 *                      <span class="zh-CN">转换数据类型时出错</span>
	 */
	private TreeMap<String, Object> conditionMap(final Condition condition) throws SQLException {
		TreeMap<String, Object> conditionMap = new TreeMap<>();
		switch (condition.getConditionType()) {
			case COLUMN:
				ColumnCondition columnCondition = condition.unwrap(ColumnCondition.class);
				conditionMap.put("Condition_Code", columnCondition.getConditionCode().toString());
				conditionMap.put("Function_Name", columnCondition.getFunctionName());
				conditionMap.put("Identify_Name",
						columnCondition.getTableName() + BrainCommons.DEFAULT_NAME_SPLIT + columnCondition.getColumnName());
				conditionMap.put("Condition_Data", this.parameterMap(columnCondition.getConditionParameter()));
				break;
			case GROUP:
				GroupCondition groupCondition = condition.unwrap(GroupCondition.class);
				Optional.of(this.conditionList(groupCondition.getConditionList()))
						.filter(conditionList -> !conditionList.isEmpty())
						.ifPresent(conditionList -> conditionMap.put("Condition_Group", conditionList));
				break;
		}
		if (conditionMap.isEmpty()) {
			return conditionMap;
		}
		conditionMap.put("Connection_Code", condition.getConnectionCode().toString());
		conditionMap.put("Condition_Type", condition.getConditionType().toString());
		return conditionMap;
	}

	/**
	 * <h3 class="en-US">Read lazy loading data information</h3>
	 * <h3 class="zh-CN">生成持久化配置信息</h3>
	 *
	 * @param object      <span class="en-US">Entity classes instance object</span>
	 *                    <span class="zh-CN">实体类实例对象</span>
	 * @param keySet      <span class="en-US">Data mapping table key value collection</span>
	 *                    <span class="zh-CN">数据映射表键值集合</span>
	 * @param tableConfig <span class="en-US">Table configure information</span>
	 *                    <span class="zh-CN">实体类信息</span>
	 */
	private void newConfig(final boolean forUpdate, final BaseObject object, final Set<String> keySet,
	                       final TableConfig tableConfig) {
		Hashtable<Long, PersistenceConfig> configHashtable = this.threadLocal.get();
		if (configHashtable.containsKey(object.identifiedCode())) {
			return;
		}
		PersistenceConfig persistenceConfig = new PersistenceConfig(forUpdate);
		keySet.forEach(columnName ->
				Optional.ofNullable(tableConfig.tableDefine.column(columnName))
						.map(ColumnDefine::getColumnName)
						.ifPresent(persistenceConfig::loadField));
		for (ReferenceDefine<?> referenceDefine : tableConfig.getReferenceDefineList()) {
			Field field = ReflectionUtils.getFieldIfAvailable(object.getClass(), referenceDefine.getFieldName());
			if (ReflectionUtils.getFieldValue(field, object) != null) {
				persistenceConfig.loadField(referenceDefine.getFieldName());
			}
		}
		this.threadLocal.get().put(object.identifiedCode(), persistenceConfig);
	}

	/**
	 * <h3 class="en-US">Register default data type mapping</h3>
	 * <h3 class="zh-CN">注册默认的数据类型映射</h3>
	 */
	private void registerTypes() {
		this.dataConvertMapping.clear();
		this.registerType(String.class, Types.VARCHAR);
		this.registerType(Integer.class, Types.INTEGER);
		this.registerType(int.class, Types.INTEGER);
		this.registerType(Short.class, Types.SMALLINT);
		this.registerType(short.class, Types.SMALLINT);
		this.registerType(Long.class, Types.BIGINT);
		this.registerType(long.class, Types.BIGINT);
		this.registerType(Byte.class, Types.TINYINT);
		this.registerType(byte.class, Types.TINYINT);
		this.registerType(Float.class, Types.REAL);
		this.registerType(float.class, Types.REAL);
		this.registerType(Double.class, Types.DOUBLE);
		this.registerType(double.class, Types.DOUBLE);
		this.registerType(Boolean.class, Types.BOOLEAN);
		this.registerType(boolean.class, Types.BOOLEAN);
		this.registerType(Date.class, Types.TIMESTAMP);
		this.registerType(Calendar.class, Types.TIMESTAMP);
		this.registerType(Byte[].class, Types.BLOB);
		this.registerType(byte[].class, Types.BLOB);
		this.registerType(Character[].class, Types.CLOB);
		this.registerType(char[].class, Types.CLOB);
		this.registerType(BigDecimal.class, Types.DECIMAL);
	}

	/**
	 * <h3 class="en-US">Use Bytebuddy to modify entity classes to implement lazy loading function</h3>
	 * <h3 class="zh-CN">使用Bytebuddy对实体类进行修改，以实现懒加载功能</h3>
	 *
	 * @param entityClass <span class="en-US">Entity define class</span>
	 *                    <span class="zh-CN">实体类定义</span>
	 */
	private void redefineClass(final Class<?> entityClass) {
		String className = ClassUtils.originalClassName(entityClass);
		if (this.redefinedClasses.contains(className)) {
			return;
		}
		Optional.ofNullable(entityClass.getSuperclass())
				.ifPresent(this::redefineClass);
		if (entityClass.isAnnotationPresent(MappedSuperclass.class) || entityClass.isAnnotationPresent(Table.class)) {
			try (final DynamicType.Unloaded<?> unloaded = new ByteBuddy().redefine(entityClass)
					.visit(Advice.to(LazyLoadInterceptor.class)
							.on(ElementMatchers.isGetter().and(ElementMatchers.not(ElementMatchers.isStatic()))))
					.make()) {
				unloaded.load(entityClass.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
			}
			this.redefinedClasses.add(className);
		}
	}

	/**
	 * <h3 class="en-US">Get the associated column annotation for the given member information</h3>
	 * <h3 class="zh-CN">获取给定成员信息的关联列注解</h3>
	 *
	 * @param member <span class="en-US">Member instance object obtained by reflection</span>
	 *               <span class="zh-CN">反射获取的成员实例对象</span>
	 * @return <span class="en-US">Associated column annotation array</span>
	 * <span class="zh-CN">关联列注解数组</span>
	 */
	private static JoinColumn[] joinColumns(@Nonnull final AccessibleObject member) {
		if (member.isAnnotationPresent(JoinColumns.class)) {
			JoinColumns annotationColumns = member.getAnnotation(JoinColumns.class);
			return annotationColumns.value();
		} else if (member.isAnnotationPresent(JoinColumn.class)) {
			return new JoinColumn[]{member.getAnnotation(JoinColumn.class)};
		}
		return new JoinColumn[0];
	}

	/**
	 * <h3 class="en-US">Checks whether the given entity class instance object has been persisted</h3>
	 * <h3 class="zh-CN">检查给定的实体类实例对象是否已经持久化</h3>
	 *
	 * @param object <span class="en-US">Entity classes instance object</span>
	 *               <span class="zh-CN">实体类实例对象</span>
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 */
	private boolean checkExist(@Nonnull final BaseObject object) {
		return Optional.ofNullable(this.threadLocal.get())
				.map(persistenceObjects -> persistenceObjects.containsKey(object.identifiedCode()))
				.orElse(Boolean.FALSE);
	}

	/**
	 * <h3 class="en-US">Checks whether the given entity class instance object can be modified</h3>
	 * <h3 class="zh-CN">检查给定的实体类实例对象是否允许修改</h3>
	 *
	 * @param object <span class="en-US">Entity classes instance object</span>
	 *               <span class="zh-CN">实体类实例对象</span>
	 * @throws SQLException <span class="en-US">If the entity class instance object is not persisted or is in a read-only state</span>
	 *                      <span class="zh-CN">如果实体类实例对象未持久化或处于只读状态</span>
	 */
	private void checkModify(@Nonnull final BaseObject object) throws SQLException {
		PersistenceConfig persistenceConfig = this.persistenceConfig(object);
		if (persistenceConfig == null) {
			throw new MultilingualSQLException(0x00DB00010010L);
		}
		if (!persistenceConfig.isForUpdate()) {
			throw new MultilingualSQLException(0x00DB00010008L);
		}
	}

	/**
	 * <h3 class="en-US">Cascade save/update reference information</h3>
	 * <h3 class="zh-CN">级联保存/更新关联信息</h3>
	 *
	 * @param object              <span class="en-US">Entity classes instance object</span>
	 *                            <span class="zh-CN">实体类实例对象</span>
	 * @param referenceDefineList <span class="en-US">Reference information definition list</span>
	 *                            <span class="zh-CN">关联信息定义列表</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	private void mergeObjects(@Nonnull final BaseObject object, final List<ReferenceDefine<?>> referenceDefineList,
	                          final List<CascadeType> cascadeTypes) throws Exception {
		for (ReferenceDefine<?> referenceDefine : referenceDefineList) {
			if (Arrays.stream(referenceDefine.getCascadeTypes()).anyMatch(cascadeTypes::contains)) {
				Object referenceObject = ReflectionUtils.getFieldValue(referenceDefine.getFieldName(), object);
				if (referenceDefine.isReturnArray()) {
					for (Object reference : CollectionUtils.toList(referenceObject)) {
						if (reference instanceof BaseObject) {
							if (this.checkExist((BaseObject) reference)) {
								((BaseObject) reference).update();
							} else {
								((BaseObject) reference).save();
							}
						}
					}
				} else if (referenceObject instanceof BaseObject) {
					if (this.checkExist((BaseObject) referenceObject)) {
						((BaseObject) referenceObject).update();
					} else {
						((BaseObject) referenceObject).save();
					}
				}
			}
		}
	}

	/**
	 * <h3 class="en-US">Initialize persistence configure information</h3>
	 * <h3 class="zh-CN">初始化持久化配置信息</h3>
	 *
	 * @param object <span class="en-US">Entity classes instance object</span>
	 *               <span class="zh-CN">实体类实例对象</span>
	 * @return <span class="en-US">Persistence configure information instance object</span>
	 * <span class="zh-CN">持久化配置信息实例对象</span>
	 * @throws SQLException <span class="en-US">If the thread's persistent configuration information list is not initialized</span>
	 *                      <span class="zh-CN">如果线程的持久化配置信息列表未初始化</span>
	 */
	private PersistenceConfig persistenceConfig(@Nonnull final BaseObject object) throws SQLException {
		return Optional.ofNullable(this.threadLocal.get())
				.map(persistenceObjects -> persistenceObjects.get(object.identifiedCode()))
				.orElseThrow(() -> new MultilingualSQLException(0x00DB00010008L));
	}

	/**
	 * <h3 class="en-US">Checks the loading status by the given property of the given entity class instance object</h3>
	 * <h3 class="zh-CN">检查给定实体类实例对象的给定属性的加载状态</h3>
	 *
	 * @param object    <span class="en-US">Entity classes instance object</span>
	 *                  <span class="zh-CN">实体类实例对象</span>
	 * @param fieldName <span class="en-US">Field name</span>
	 *                  <span class="zh-CN">属性名称</span>
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 * @throws SQLException <span class="en-US">If the thread's persistent configuration information list is not initialized</span>
	 *                      <span class="zh-CN">如果线程的持久化配置信息列表未初始化</span>
	 */
	private boolean loadedField(@Nonnull final BaseObject object, @Nonnull final String fieldName)
			throws SQLException {
		PersistenceConfig persistenceConfig = Optional.ofNullable(this.persistenceConfig(object))
				.orElseThrow(() -> new MultilingualSQLException(0x00DB00010008L));
		return persistenceConfig.loadedField(fieldName);
	}

	/**
	 * <h3 class="en-US">Initialize the query optimizer implementation class instance object</h3>
	 * <h3 class="zh-CN">初始化查询优化器实现类实例对象</h3>
	 *
	 * @return <span class="en-US">Query optimizer implementation class instance object</span>
	 * <span class="zh-CN">查询优化器实现类实例对象</span>
	 */
	private QueryOptimizer newOptimizer() {
		QueryOptimizer queryOptimizer;
		if (StringUtils.isEmpty(this.optimizerName) || !REGISTERED_OPTIMIZERS.containsKey(this.optimizerName)) {
			queryOptimizer = new QueryOptimizer.DefaultOptimizer();
		} else {
			queryOptimizer = (QueryOptimizer) ObjectUtils.newInstance(REGISTERED_OPTIMIZERS.get(this.optimizerName));
		}
		return queryOptimizer;
	}

	/**
	 * <h3 class="en-US">Execute data query step</h3>
	 * <h3 class="zh-CN">执行数据查询步骤</h3>
	 *
	 * @param queryStep        <span class="en-US">Query step instance object</span>
	 *                         <span class="zh-CN">数据查询步骤实例对象</span>
	 * @param subQueriesResult <span class="en-US">Pre-query result set mapping table</span>
	 *                         <span class="zh-CN">前置查询结果集映射表</span>
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	private void query(final QueryStep queryStep, final Map<Long, List<Map<String, Object>>> subQueriesResult)
			throws Exception {
		subQueriesResult.put(queryStep.getStepCode(), this.dataSource.query(queryStep.getQueryInfo()));
	}

	/**
	 * <h3 class="en-US">Execute data merge step</h3>
	 * <h3 class="zh-CN">执行数据合并步骤</h3>
	 *
	 * @param mergeStep        <span class="en-US">Merge step instance object</span>
	 *                         <span class="zh-CN">数据合并步骤实例对象</span>
	 * @param subQueriesResult <span class="en-US">Pre-query result set mapping table</span>
	 *                         <span class="zh-CN">前置查询结果集映射表</span>
	 */
	private void merge(final MergeStep mergeStep, final Map<Long, List<Map<String, Object>>> subQueriesResult) {
		List<Map<String, Object>> mergeResults = new ArrayList<>();
		List<Map<String, Object>> mainResults =
				subQueriesResult.getOrDefault(mergeStep.getMainCode(), Collections.emptyList());
		List<Map<String, Object>> childResults =
				subQueriesResult.getOrDefault(mergeStep.getMergeCode(), Collections.emptyList());
		for (Map<String, Object> mainResult : mainResults) {
			for (Map<String, Object> childResult : childResults) {
				if (mergeStep.getMappingKeys().entrySet().stream().allMatch(entry ->
						ObjectUtils.nullSafeEquals(mainResult.get(entry.getKey()), childResult.get(entry.getValue())))) {
					Map<String, Object> mergedResult = new HashMap<>(childResult);
					mergedResult.putAll(mainResult);
					mergeResults.add(mergedResult);
				}
			}
		}
		subQueriesResult.put(mergeStep.getStepCode(), mergeResults);
	}

	/**
	 * <h3 class="en-US">Generate data column definition information based on given member information</h3>
	 * <h3 class="zh-CN">根据给定的成员信息生成数据列定义信息</h3>
	 *
	 * @param schemaName <span class="en-US">Data schema name</span>
	 *                   <span class="zh-CN">数据源名称</span>
	 * @param field      <span class="en-US">Property instance object obtained by reflection</span>
	 *                   <span class="zh-CN">反射获取的属性实例对象</span>
	 * @param object     <span class="en-US">Instance object of the entity class where the attribute is located</span>
	 *                   <span class="zh-CN">属性所在实体类的实例对象</span>
	 * @param primaryKey <span class="en-US">Primary key attribute</span>
	 *                   <span class="zh-CN">主键属性</span>
	 * @return <span class="en-US">Generated data column definition information</span>
	 * <span class="zh-CN">生成的数据列定义信息</span>
	 */
	private Optional<ColumnDefine> newInstance(final String schemaName, final Field field, final Object object,
	                                           final boolean primaryKey) {
		return Optional.ofNullable(field.getAnnotation(Column.class))
				.map(column -> {
					int jdbcType = this.dataConvertMapping.get(field.getType());
					String defaultValue =
							Optional.ofNullable(ReflectionUtils.getFieldValue(field, object))
									.map(fieldValue ->
											dataSource.defaultValue(schemaName, jdbcType, column.length(),
													column.precision(), column.scale(), fieldValue))
									.orElse(Globals.DEFAULT_VALUE_STRING);
					ColumnDefine columnDefine = new ColumnDefine();
					columnDefine.setColumnName(StringUtils.isEmpty(column.name()) ? field.getName() : column.name());
					columnDefine.setPrimaryKey(primaryKey);
					columnDefine.setDefaultValue(defaultValue);
					columnDefine.setUnique(column.unique());
					columnDefine.setJdbcType(jdbcType);
					columnDefine.setNullable(column.nullable());
					columnDefine.setLength(column.length());
					columnDefine.setPrecision(column.precision());
					columnDefine.setScale(column.scale());
					if (primaryKey) {
						columnDefine.setUpdatable(Boolean.FALSE);
					} else {
						columnDefine.setUpdatable(column.updatable());
					}
					columnDefine.setVersion(field.isAnnotationPresent(Version.class));

					Optional.ofNullable(field.getAnnotation(GeneratedData.class))
							.ifPresent(generatedData -> {
								GeneratorDefine generatorDefine = new GeneratorDefine();
								generatorDefine.setGenerationType(generatedData.type());
								generatorDefine.setGeneratorName(generatedData.generator());
								columnDefine.setGeneratorDefine(generatorDefine);
							});
					return columnDefine;
				});
	}

	/**
	 * <h3 class="en-US">Get cache client instance object</h3>
	 * <h3 class="zh-CN">获取缓存客户端实例对象</h3>
	 *
	 * @return <span class="en-US">Cache client instance object</span>
	 * <span class="zh-CN">缓存客户端实例对象</span>
	 */
	private Optional<CacheClient> cacheClient() {
		return Optional.ofNullable(
				CacheUtils.client(CacheUtils.registered(BrainCommons.CACHE_NAME)
						? BrainCommons.CACHE_NAME
						: CacheGlobals.DEFAULT_CACHE_NAME));
	}

	/**
	 * <h3 class="en-US">Get all non-static methods of a given class</h3>
	 * <h3 class="zh-CN">获取给定类的所有非静态方法</h3>
	 *
	 * @param entityClass <span class="en-US">The given class</span>
	 *                    <span class="zh-CN">给定类</span>
	 * @return <span class="en-US">List of non-static methods</span>
	 * <span class="zh-CN">非静态方法列表</span>
	 */
	private static List<Method> declaredMethods(@Nonnull final Class<?> entityClass) {
		List<Method> fieldList = new ArrayList<>();
		if (entityClass.isAnnotationPresent(Table.class) || entityClass.isAnnotationPresent(MappedSuperclass.class)) {
			Optional.ofNullable(entityClass.getSuperclass())
					.ifPresent(superClass -> fieldList.addAll(declaredMethods(superClass)));
			Arrays.stream(entityClass.getDeclaredMethods())
					.filter(method -> !Modifier.isStatic(method.getModifiers()))
					.forEach(fieldList::add);
		}
		return fieldList;
	}

	/**
	 * <h3 class="en-US">Get all non-static fields of a given class</h3>
	 * <h3 class="zh-CN">获取给定类的所有非静态属性</h3>
	 *
	 * @param entityClass <span class="en-US">The given class</span>
	 *                    <span class="zh-CN">给定类</span>
	 * @return <span class="en-US">List of non-static fields</span>
	 * <span class="zh-CN">非静态属性列表</span>
	 */
	private static List<Field> declaredFields(@Nonnull final Class<?> entityClass) {
		List<Field> fieldList = new ArrayList<>();
		Optional.ofNullable(entityClass.getSuperclass())
				.ifPresent(superClass -> fieldList.addAll(declaredFields(superClass)));
		Arrays.stream(entityClass.getDeclaredFields())
				.filter(method -> !Modifier.isStatic(method.getModifiers()))
				.forEach(fieldList::add);
		return fieldList;
	}

	/**
	 * <h3 class="en-US">Checks whether the given member information contains associated annotations</h3>
	 * <h3 class="zh-CN">检查给定成员信息是否包含关联注解</h3>
	 *
	 * @param member <span class="en-US">Member instance object obtained by reflection</span>
	 *               <span class="zh-CN">反射获取的成员实例对象</span>
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 */
	private static boolean referenceMember(@Nonnull final AccessibleObject member) {
		return member.isAnnotationPresent(OneToMany.class) || member.isAnnotationPresent(ManyToOne.class)
				|| member.isAnnotationPresent(OneToOne.class);
	}

	private static ReferenceDefine<?> referenceDefine(@Nonnull final AccessibleObject member) throws SQLException {
		ReferenceType referenceType;
		Class<?> referenceClass;
		CascadeType[] cascadeType;
		boolean lazyLoad;

		if (member.isAnnotationPresent(OneToMany.class)) {
			referenceType = ReferenceType.OneToMany;
			OneToMany oneToMany = member.getAnnotation(OneToMany.class);
			referenceClass = oneToMany.targetEntity();
			lazyLoad = FetchType.LAZY.equals(oneToMany.fetch());
			cascadeType = oneToMany.cascade();
		} else if (member.isAnnotationPresent(ManyToOne.class)) {
			referenceType = ReferenceType.ManyToOne;
			ManyToOne manyToOne = member.getAnnotation(ManyToOne.class);
			referenceClass = manyToOne.targetEntity();
			lazyLoad = FetchType.LAZY.equals(manyToOne.fetch());
			cascadeType = manyToOne.cascade();
		} else if (member.isAnnotationPresent(OneToOne.class)) {
			referenceType = ReferenceType.OneToOne;
			OneToOne oneToOne = member.getAnnotation(OneToOne.class);
			referenceClass = oneToOne.targetEntity();
			lazyLoad = FetchType.LAZY.equals(oneToOne.fetch());
			cascadeType = oneToOne.cascade();
		} else if (member.isAnnotationPresent(ManyToMany.class)) {
			referenceType = ReferenceType.ManyToMany;
			ManyToMany manyToMany = member.getAnnotation(ManyToMany.class);
			referenceClass = manyToMany.targetEntity();
			lazyLoad = FetchType.LAZY.equals(manyToMany.fetch());
			cascadeType = manyToMany.cascade();
		} else {
			referenceType = ReferenceType.Undefined;
			referenceClass = void.class;
			lazyLoad = Boolean.FALSE;
			cascadeType = new CascadeType[0];
		}

		boolean returnArray;
		String fieldName;
		if (member instanceof Field) {
			returnArray = (List.class.isAssignableFrom(((Field) member).getType()) || ((Field) member).getType().isArray());
			if (void.class.equals(referenceClass)) {
				if (returnArray) {
					Type type = ((Field) member).getGenericType();
					if (type instanceof ParameterizedType) {
						Type[] fieldTypes = ((ParameterizedType) type).getActualTypeArguments();
						if (fieldTypes.length == 1) {
							referenceClass = (Class<?>) fieldTypes[0];
						} else {
							throw new MultilingualSQLException(0x00DB00010004L);
						}
					}
				} else {
					referenceClass = ((Field) member).getType();
				}
			}
			fieldName = ((Field) member).getName();
		} else if (member instanceof Method) {
			returnArray = List.class.isAssignableFrom(((Method) member).getReturnType())
					|| ((Method) member).getReturnType().isArray();
			fieldName = ReflectionUtils.fieldName(((Method) member).getName());
		} else {
			returnArray = Boolean.FALSE;
			fieldName = Globals.DEFAULT_VALUE_STRING;
		}

		if (StringUtils.isEmpty(fieldName) || void.class.equals(referenceClass)) {
			return null;
		}
		return new ReferenceDefine<>(referenceType, referenceClass, fieldName, lazyLoad,
				returnArray, cascadeType, joinColumns(member));
	}

	/**
	 * <h2 class="en-US">Persistent configuration information</h2>
	 * <h2 class="zh-CN">持久化配置信息</h2>
	 *
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Mar 30, 2016 17:05:12 $
	 */
	private static final class PersistenceConfig {

		/**
		 * <span class="en-US">Using for update record</span>
		 * <span class="zh-CN">用于更新记录</span>
		 */
		private final boolean forUpdate;
		/**
		 * <span class="en-US">Loaded field list</span>
		 * <span class="zh-CN">已加载属性名列表</span>
		 */
		private final List<String> loadedFields = new ArrayList<>();

		/**
		 * <h3 class="en-US">Constructor method for persistent configuration information</h3>
		 * <h3 class="zh-CN">持久化配置信息构造方法</h3>
		 *
		 * @param forUpdate <span class="en-US">Using for update record</span>
		 *                  <span class="zh-CN">用于更新记录</span>
		 */
		PersistenceConfig(final boolean forUpdate) {
			this.forUpdate = forUpdate;
		}

		/**
		 * <h3 class="en-US">Getter method for using for update record</h3>
		 * <h3 class="zh-CN">用于更新记录的Getter方法</h3>
		 *
		 * @return <span class="en-US">Using for update record</span>
		 * <span class="zh-CN">用于更新记录</span>
		 */
		boolean isForUpdate() {
			return this.forUpdate;
		}

		/**
		 * <h3 class="en-US">Load the field value according to the given field identification code</h3>
		 * <h3 class="zh-CN">根据给定的字段识别代码加载字段值</h3>
		 *
		 * @param fieldName <span class="en-US">Field identification code</span>
		 *                  <span class="zh-CN">字段识别代码</span>
		 */
		void loadField(final String fieldName) {
			if (!this.loadedFields.contains(fieldName)) {
				this.loadedFields.add(fieldName);
			}
		}

		/**
		 * <h3 class="en-US">Checks if the given field identification code has been loaded</h3>
		 * <h3 class="zh-CN">检查给定的字段识别代码是否已经加载</h3>
		 *
		 * @param fieldName <span class="en-US">Field identification code</span>
		 *                  <span class="zh-CN">字段识别代码</span>
		 * @return <span class="en-US">Check result</span>
		 * <span class="zh-CN">检查结果</span>
		 */
		boolean loadedField(final String fieldName) {
			return this.loadedFields.contains(fieldName);
		}
	}

	/**
	 * <h2 class="en-US">Primary key defines information</h2>
	 * <h2 class="zh-CN">主键定义信息</h2>
	 *
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Mar 30, 2016 20:12:36 $
	 */
	private static final class PrimaryKeyConfig {

		/**
		 * <span class="en-US">Is composite id</span>
		 * <span class="zh-CN">是否为联合主键</span>
		 */
		private final boolean compositeId;
		/**
		 * <span class="en-US">Primary key field name</span>
		 * <span class="zh-CN">主键属性名</span>
		 */
		private final String fieldName;
		/**
		 * <span class="en-US">Primary key data type</span>
		 * <span class="zh-CN">主键数据类型</span>
		 */
		private final Class<?> primaryKeyType;

		/**
		 * <h3 class="en-US">Constructor method for primary key define information</h3>
		 * <h3 class="zh-CN">主键定义信息的构造方法</h3>
		 *
		 * @param compositeId    <span class="en-US">Is composite id</span>
		 *                       <span class="zh-CN">是否为联合主键</span>
		 * @param fieldName      <span class="en-US">Primary key field name</span>
		 *                       <span class="zh-CN">主键属性名</span>
		 * @param primaryKeyType <span class="en-US">Primary key data type</span>
		 *                       <span class="zh-CN">主键数据类型</span>
		 */
		PrimaryKeyConfig(final boolean compositeId, final String fieldName, final Class<?> primaryKeyType) {
			this.compositeId = compositeId;
			this.fieldName = fieldName;
			this.primaryKeyType = primaryKeyType;
		}

		/**
		 * <h3 class="en-US">Getter method for is composite id</h3>
		 * <h3 class="zh-CN">是否为联合主键的Getter方法</h3>
		 *
		 * @return <span class="en-US">Is composite id</span>
		 * <span class="zh-CN">是否为联合主键</span>
		 */
		boolean isCompositeId() {
			return this.compositeId;
		}

		/**
		 * <h3 class="en-US">Getter method for primary key field name</h3>
		 * <h3 class="zh-CN">主键属性名的Getter方法</h3>
		 *
		 * @return <span class="en-US">Primary key field name</span>
		 * <span class="zh-CN">主键属性名</span>
		 */
		String getFieldName() {
			return this.fieldName;
		}

		/**
		 * <h3 class="en-US">Getter method for primary key data type</h3>
		 * <h3 class="zh-CN">主键数据类型的Getter方法</h3>
		 *
		 * @return <span class="en-US">Primary key data type</span>
		 * <span class="zh-CN">主键数据类型</span>
		 */
		Class<?> getPrimaryKeyType() {
			return this.primaryKeyType;
		}
	}

	/**
	 * <h2 class="en-US">Entity class define information</h2>
	 * <h2 class="zh-CN">实体类定义信息</h2>
	 *
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Mar 30, 2016 20:12:36 $
	 */
	public static final class TableConfig {

		/**
		 * <span class="en-US">Cacheable data</span>
		 * <span class="zh-CN">缓存数据</span>
		 */
		private final boolean cacheable;
		/**
		 * <span class="en-US">Primary key define information</span>
		 * <span class="zh-CN">主键定义信息</span>
		 */
		private final PrimaryKeyConfig primaryKeyConfig;
		/**
		 * <span class="en-US">Data column name and attribute name mapping table</span>
		 * <span class="zh-CN">数据列名与属性名映射表</span>
		 */
		private final Hashtable<String, String> columnFieldMapping;
		/**
		 * <span class="en-US">Field name and data column name mapping table</span>
		 * <span class="zh-CN">属性名与数据列名映射表</span>
		 */
		private final Hashtable<String, String> fieldColumnMapping;
		/**
		 * <span class="en-US">Data column transmission configuration information list</span>
		 * <span class="zh-CN">数据列传输配置信息列表</span>
		 */
		private final List<TransferColumn> transferColumns;
		/**
		 * <span class="en-US">Sensitive information configuration list</span>
		 * <span class="zh-CN">敏感信息配置列表</span>
		 */
		private final List<SensitiveDefine> sensitiveDefines;
		/**
		 * <span class="en-US">Table defines information</span>
		 * <span class="zh-CN">数据表定义信息</span>
		 */
		private final TableDefine tableDefine;
		/**
		 * <span class="en-US">Reference configuration information define list</span>
		 * <span class="zh-CN">外键引用配置信息列表</span>
		 */
		private final List<ReferenceDefine<?>> referenceDefineList;
		/**
		 * <span class="en-US">Drop option</span>
		 * <span class="zh-CN">删除选项</span>
		 */
		private final DropOption dropOption;

		/**
		 * <h3 class="en-US">Constructor method for entity class define information</h3>
		 * <h3 class="zh-CN">实体类定义信息的构造方法</h3>
		 *
		 * @param entityFactory <span class="en-US">Entity factory instance object</span>
		 *                      <span class="zh-CN">实体类工厂实例对象</span>
		 * @param entityClass   <span class="en-US">Table entity class</span>
		 *                      <span class="zh-CN">数据表实体类</span>
		 */
		private TableConfig(@Nonnull final EntityFactory entityFactory, @Nonnull final Class<?> entityClass)
				throws Exception {
			if (!entityClass.isAnnotationPresent(Table.class)) {
				throw new MultilingualSQLException(0x00DB00010002L);
			}
			Table table = entityClass.getAnnotation(Table.class);
			Options options = entityClass.getAnnotation(Options.class);

			String schemaName;
			DialectType dialectType;
			Schema schema = entityClass.getAnnotation(Schema.class);
			if (schema == null) {
				schemaName = table.schema();
				dialectType = DialectType.Default;
			} else {
				schemaName = schema.name();
				dialectType = schema.type();
			}

			if (!entityFactory.dataSource.registered(schemaName, dialectType)) {
				throw new MultilingualSQLException(0x00DB00010006L);
			}

			String tableName = StringUtils.isEmpty(table.name()) ? entityClass.getSimpleName() : table.name();

			List<ColumnDefine> columnDefineList = new ArrayList<>();
			this.referenceDefineList = new ArrayList<>();

			this.columnFieldMapping = new Hashtable<>();
			this.fieldColumnMapping = new Hashtable<>();
			this.transferColumns = new ArrayList<>();
			this.sensitiveDefines = new ArrayList<>();
			Map<String, Class<?>> fieldTypeMapping = new HashMap<>();
			Object object = ObjectUtils.newInstance(entityClass);
			PrimaryKeyConfig primaryKeyConfig = null;
			for (Field field : EntityFactory.declaredFields(entityClass)) {
				if (field.isAnnotationPresent(Column.class)) {
					if (field.isAnnotationPresent(Id.class)) {
						if (primaryKeyConfig != null) {
							throw new MultilingualSQLException(0x00DB00010003L);
						}
						primaryKeyConfig = new PrimaryKeyConfig(Boolean.FALSE, field.getName(), field.getType());
					}
					entityFactory.newInstance(table.schema(), field, object, field.isAnnotationPresent(Id.class))
							.ifPresent(columnDefine -> {
								columnDefineList.add(columnDefine);
								fieldTypeMapping.put(field.getName(), field.getType());
								this.registerColumn(columnDefine, field);
							});
				} else if (field.isAnnotationPresent(EmbeddedId.class)) {
					if (primaryKeyConfig != null) {
						throw new MultilingualSQLException(0x00DB00010003L);
					}
					primaryKeyConfig = new PrimaryKeyConfig(Boolean.TRUE, field.getName(), field.getType());
					Object compositeId = ReflectionUtils.getFieldValue(field, object);
					for (Field pkField : EntityFactory.declaredFields(field.getType())) {
						entityFactory.newInstance(table.schema(), pkField, compositeId, Boolean.TRUE)
								.ifPresent(columnDefine -> {
									columnDefineList.add(columnDefine);
									fieldTypeMapping.put(pkField.getName(), pkField.getType());
									this.registerColumn(columnDefine, pkField);
								});
					}
				} else if (EntityFactory.referenceMember(field)) {
					Optional.ofNullable(EntityFactory.referenceDefine(field)).ifPresent(referenceDefineList::add);
				}
			}

			for (Method method : EntityFactory.declaredMethods(entityClass)) {
				if (EntityFactory.referenceMember(method)
						&& (method.getName().startsWith("get") || method.getName().startsWith("is"))) {
					Optional.ofNullable(EntityFactory.referenceDefine(method)).ifPresent(referenceDefineList::add);
				}
			}

			if (primaryKeyConfig == null) {
				LOGGER.warn("");
			}
			this.primaryKeyConfig = primaryKeyConfig;

			List<IndexDefine> indexDefineList = new ArrayList<>();
			for (Index index : table.indexes()) {
				List<String> columnList = new ArrayList<>();

				for (String columnName : StringUtils.tokenizeToStringArray(index.columnList(),
						BrainCommons.DEFAULT_SPLIT_CHARACTER.trim())) {
					if (columnDefineList.stream()
							.anyMatch(columnDefine -> columnDefine.getColumnName().equalsIgnoreCase(columnName))) {
						columnList.add(columnName);
					}
				}

				if (!columnList.isEmpty()) {
					IndexDefine indexDefine = new IndexDefine();
					indexDefine.setIndexName(index.name());
					indexDefine.setUnique(index.unique());
					indexDefine.setColumnList(columnList);
					indexDefineList.add(indexDefine);
				}
			}

			this.tableDefine = new TableDefine();
			this.tableDefine.setSchemaName(schemaName);
			this.tableDefine.setDialectType(dialectType);
			this.tableDefine.setTableName(tableName);
			this.tableDefine.setColumnDefines(columnDefineList);
			this.tableDefine.setIndexDefines(indexDefineList);
			ShardingDefine<?> shardingDatabase = null;
			ShardingDefine<?> shardingTable = null;
			if (options == null) {
				this.dropOption = DropOption.NONE;
				this.tableDefine.setLockOption(LockModeType.NONE);
				this.cacheable = Boolean.TRUE;
			} else {
				this.dropOption = options.dropOption();
				shardingDatabase = Optional.of(options.databaseSharding())
						.filter(sharding -> !Calculator.class.equals(sharding.calculatorClass()))
						.filter(sharding -> this.columnFieldMapping.containsKey(sharding.column()))
						.map(sharding -> {
							String fieldName = this.columnFieldMapping.get(sharding.column());
							if (fieldTypeMapping.containsKey(fieldName)) {
								return new ShardingDefine<>(sharding.value(), sharding.column(),
										sharding.template(), sharding.calculatorClass(),
										fieldTypeMapping.get(fieldName));
							}
							return null;
						})
						.orElse(null);
				shardingTable = Optional.of(options.tableSharding())
						.filter(sharding -> !Calculator.class.equals(sharding.calculatorClass()))
						.filter(sharding -> this.columnFieldMapping.containsKey(sharding.column()))
						.map(sharding -> {
							String fieldName = this.columnFieldMapping.get(sharding.column());
							if (fieldTypeMapping.containsKey(fieldName)) {
								return new ShardingDefine<>(sharding.value(), sharding.column(),
										sharding.template(), sharding.calculatorClass(),
										fieldTypeMapping.get(fieldName));
							}
							return null;
						})
						.orElse(null);
				this.tableDefine.setLockOption(options.lockOption());
				this.cacheable = options.cacheable();
			}

			entityFactory.dataSource.initTable(this.tableDefine, shardingDatabase, shardingTable);
		}

		/**
		 * <h3 class="en-US">Registration data column configuration information</h3>
		 * <h3 class="zh-CN">注册数据列配置信息</h3>
		 *
		 * @param columnDefine <span class="en-US">Data column configure information</span>
		 *                     <span class="zh-CN">数据列配置信息</span>
		 * @param field        <span class="en-US">Data column field reflection object</span>
		 *                     <span class="zh-CN">数据列属性反射对象</span>
		 */
		private void registerColumn(final ColumnDefine columnDefine, final Field field) {
			this.columnFieldMapping.put(columnDefine.getColumnName(), field.getName());
			this.fieldColumnMapping.put(field.getName(), columnDefine.getColumnName());
			Optional.ofNullable(field.getAnnotation(ExcelColumn.class))
					.ifPresent(excelColumn -> {
						TransferColumn transferColumn = new TransferColumn();
						transferColumn.setColumnIndex(excelColumn.value());
						transferColumn.setColumnName(columnDefine.getColumnName());
						transferColumn.setPrimaryKey(columnDefine.isPrimaryKey());
						transferColumn.setTransferConfig(new TransferConfig(field.getAnnotation(DataTransfer.class)));
						this.transferColumns.add(transferColumn);
					});
			Optional.ofNullable(field.getAnnotation(Sensitive.class))
					.ifPresent(sensitive -> this.sensitiveDefines.add(new SensitiveDefine(field.getName(), sensitive)));
		}

		/**
		 * <h3 class="en-US">Generate cache unique identifier</h3>
		 * <h3 class="zh-CN">生成缓存唯一识别码</h3>
		 *
		 * @param object <span class="en-US">Entity classes instance object</span>
		 *               <span class="zh-CN">实体类实例对象</span>
		 * @return <span class="en-US">Unique identifier</span>
		 * <span class="zh-CN">唯一识别代码</span>
		 */
		String cacheKey(@Nonnull final BaseObject object) {
			return this.cacheKey(this.dataMap(object, ColumnDefine::isPrimaryKey));
		}

		/**
		 * <h3 class="en-US">Generate cache unique identifier</h3>
		 * <h3 class="zh-CN">生成缓存唯一识别码</h3>
		 *
		 * @param filterMap <span class="en-US">Data mapping table</span>
		 *                  <span class="zh-CN">数据映射表</span>
		 * @return <span class="en-US">Unique identifier</span>
		 * <span class="zh-CN">唯一识别代码</span>
		 */
		String cacheKey(final Map<String, Object> filterMap) {
			return ConvertUtils.toHex(SecurityUtils.SHA256(new TreeMap<>(filterMap)));
		}

		/**
		 * <h3 class="en-US">Getter method for cacheable data</h3>
		 * <h3 class="zh-CN">缓存数据的Getter方法</h3>
		 *
		 * @return <span class="en-US">Cacheable data</span>
		 * <span class="zh-CN">缓存数据</span>
		 */
		boolean isCacheable() {
			return this.cacheable;
		}

		/**
		 * <h3 class="en-US">Getter method for table defines information</h3>
		 * <h3 class="zh-CN">数据表定义信息的Getter方法</h3>
		 *
		 * @return <span class="en-US">Table defines information</span>
		 * <span class="zh-CN">数据表定义信息</span>
		 */
		public TableDefine getTableDefine() {
			return this.tableDefine;
		}

		/**
		 * <h3 class="en-US">Getter method for data column transmission configuration information list</h3>
		 * <h3 class="zh-CN">数据列传输配置信息列表的Getter方法</h3>
		 *
		 * @return <span class="en-US">Data column transmission configuration information list</span>
		 * <span class="zh-CN">数据列传输配置信息列表</span>
		 */
		List<TransferColumn> getTransferColumns() {
			return this.transferColumns;
		}

		/**
		 * <h3 class="en-US">Getter method for the list of reference configuration information defines</h3>
		 * <h3 class="zh-CN">外键引用配置信息列表的Getter方法</h3>
		 *
		 * @return <span class="en-US">Reference configuration information define list</span>
		 * <span class="zh-CN">外键引用配置信息列表</span>
		 */
		List<ReferenceDefine<?>> getReferenceDefineList() {
			return this.referenceDefineList;
		}

		/**
		 * <h3 class="en-US">Getter method for drop option</h3>
		 * <h3 class="zh-CN">删除选项的Getter方法</h3>
		 *
		 * @return <span class="en-US">Drop option</span>
		 * <span class="zh-CN">删除选项</span>
		 */
		DropOption getDropOption() {
			return this.dropOption;
		}

		/**
		 * <h3 class="en-US">The current data table contains lazy loading attributes</h3>
		 * <h3 class="zh-CN">当前数据表包含懒加载属性</h3>
		 *
		 * @return <span class="en-US">Check result</span>
		 * <span class="zh-CN">检查结果</span>
		 */
		boolean containsLazyLoadField() {
			return this.tableDefine.getColumnDefines().stream().anyMatch(ColumnDefine::isLazyLoad)
					|| this.referenceDefineList.stream().anyMatch(ReferenceDefine::isLazyLoad);
		}

		/**
		 * <h3 class="en-US">Write field value</h3>
		 * <h3 class="zh-CN">写入属性值</h3>
		 *
		 * @param object    <span class="en-US">Entity classes instance object</span>
		 *                  <span class="zh-CN">实体类实例对象</span>
		 * @param fieldName <span class="en-US">Field name</span>
		 *                  <span class="zh-CN">属性名</span>
		 * @param value     <span class="en-US">Field value</span>
		 *                  <span class="zh-CN">属性值</span>
		 */
		private void writeFieldValue(final BaseObject object, final String fieldName, final Object value) {
			if (value == null) {
				return;
			}
			if (this.primaryKey(fieldName) && this.primaryKeyConfig.isCompositeId()) {
				String pkField = this.primaryKeyConfig.getFieldName();
				Object primaryKey = ReflectionUtils.getFieldValue(pkField, object);
				ReflectionUtils.setField(fieldName, primaryKey, value);
				ReflectionUtils.setField(pkField, object, primaryKey);
			} else {
				ReflectionUtils.setField(fieldName, object, value);
			}
		}

		/**
		 * <h3 class="en-US">Read field value</h3>
		 * <h3 class="zh-CN">读取属性值</h3>
		 *
		 * @param object       <span class="en-US">Entity classes instance object</span>
		 *                     <span class="zh-CN">实体类实例对象</span>
		 * @param identifyName <span class="en-US">Field name</span>
		 *                     <span class="zh-CN">属性名</span>
		 * @return <span class="en-US">Field value</span>
		 * <span class="zh-CN">属性值</span>
		 */
		public Object readFieldValue(final BaseObject object, final String identifyName) {
			String fieldName = this.columnFieldMapping.getOrDefault(identifyName, identifyName);
			if (this.primaryKey(fieldName)) {
				Object primaryKey = object;
				if (this.primaryKeyConfig.isCompositeId()) {
					primaryKey = ReflectionUtils.getFieldValue(this.primaryKeyConfig.getFieldName(), object);
				}
				return ReflectionUtils.getFieldValue(fieldName, primaryKey, Boolean.FALSE);
			} else {
				return ReflectionUtils.getFieldValue(fieldName, object, Boolean.FALSE);
			}
		}

		/**
		 * <h3 class="en-US">Read sensitive data</h3>
		 * <h3 class="zh-CN">读取敏感信息</h3>
		 *
		 * @param object       <span class="en-US">Entity classes instance object</span>
		 *                     <span class="zh-CN">实体类实例对象</span>
		 * @param identifyName <span class="en-US">Field name</span>
		 *                     <span class="zh-CN">属性名</span>
		 * @return <span class="en-US">Field value</span>
		 * <span class="zh-CN">属性值</span>
		 */
		public String sensitiveData(final BaseObject object, final String identifyName) {
			String fieldName = this.columnFieldMapping.getOrDefault(identifyName, identifyName);
			return this.sensitiveDefines.stream()
					.filter(sensitiveDefine -> sensitiveDefine.match(fieldName))
					.findFirst()
					.map(sensitiveDefine -> sensitiveDefine.sensitiveData(object))
					.orElse((String) ReflectionUtils.getFieldValue(fieldName, object));
		}

		private boolean primaryKey(final String fieldName) {
			return Optional.ofNullable(this.columnName(fieldName))
					.filter(StringUtils::notBlank)
					.map(this.tableDefine::column)
					.map(ColumnDefine::isPrimaryKey)
					.orElse(Boolean.FALSE);
		}

		/**
		 * <h3 class="en-US">Write the primary key information generated by the database into the entity class</h3>
		 * <h3 class="zh-CN">将数据库生成的主键信息写入到实体类中</h3>
		 *
		 * @param object        <span class="en-US">Entity classes instance object</span>
		 *                      <span class="zh-CN">实体类实例对象</span>
		 * @param primaryKeyMap <span class="en-US">Primary key value mapping table generated by database</span>
		 *                      <span class="zh-CN">数据库生成的主键值映射表</span>
		 */
		private void primaryKey(final BaseObject object, final Map<String, Object> primaryKeyMap) {
			Object primaryKey;
			if (this.primaryKeyConfig.isCompositeId()) {
				primaryKey =
						Optional.ofNullable(ReflectionUtils.getFieldValue(this.primaryKeyConfig.getFieldName(), object))
								.orElse(ObjectUtils.newInstance(this.primaryKeyConfig.getPrimaryKeyType()));
			} else {
				primaryKey = object;
			}

			this.tableDefine.getColumnDefines()
					.stream()
					.filter(ColumnDefine::isPrimaryKey)
					.forEach(columnDefine -> {
						String columnName = columnDefine.getColumnName();
						if (primaryKeyMap.containsKey(columnName)) {
							ReflectionUtils.setField(this.columnFieldMapping.get(columnName), primaryKey,
									primaryKeyMap.get(columnName));
						}
					});
			if (this.primaryKeyConfig.isCompositeId()) {
				ReflectionUtils.setField(this.primaryKeyConfig.getFieldName(), object, primaryKey);
			}
		}

		/**
		 * <h3 class="en-US">Convert entity class to data mapping table</h3>
		 * <h3 class="zh-CN">转换实体类为数据映射表</h3>
		 *
		 * @param object <span class="en-US">Entity classes instance object</span>
		 *               <span class="zh-CN">实体类实例对象</span>
		 * @return <span class="en-US">Converted data mapping table</span>
		 * <span class="zh-CN">转换后的数据映射表</span>
		 */
		private Map<String, Object> dataMap(@Nonnull final BaseObject object) {
			Map<String, Object> dataMap = new HashMap<>();
			this.tableDefine.getColumnDefines()
					.forEach(columnDefine ->
							Optional.ofNullable(this.readFieldValue(object,
											this.columnFieldMapping.get(columnDefine.getColumnName())))
									.ifPresent(fieldValue ->
											dataMap.put(columnDefine.getColumnName(),
													this.convertValue(columnDefine.getJdbcType(),
															columnDefine.getScale(), fieldValue))));
			return dataMap;
		}

		/**
		 * <h3 class="en-US">Get the primary key data mapping table</h3>
		 * <h3 class="zh-CN">获取主键数据映射表</h3>
		 *
		 * @param object    <span class="en-US">Instance object that reads attribute data</span>
		 *                  <span class="zh-CN">读取属性数据的实例对象</span>
		 * @param predicate <span class="en-US">Filter conditions for reading attributes</span>
		 *                  <span class="zh-CN">读取属性的过滤条件</span>
		 * @return <span class="en-US">Converted data mapping table</span>
		 * <span class="zh-CN">转换后的数据映射表</span>
		 */
		private Map<String, Object> dataMap(@Nonnull final BaseObject object,
		                                    final Predicate<? super ColumnDefine> predicate) {
			Map<String, Object> retrieveMap = new HashMap<>();
			this.tableDefine.getColumnDefines()
					.stream()
					.filter(predicate)
					.forEach(columnDefine ->
							Optional.ofNullable(this.readFieldValue(object,
											this.columnFieldMapping.get(columnDefine.getColumnName())))
									.ifPresent(fieldValue ->
											retrieveMap.put(columnDefine.getColumnName(),
													this.convertValue(columnDefine.getJdbcType(),
															columnDefine.getScale(), fieldValue))));
			return retrieveMap;
		}

		/**
		 * <h3 class="en-US">Convert Java data types to SQL data types</h3>
		 * <h3 class="zh-CN">转换Java数据类型为SQL数据类型</h3>
		 *
		 * @param jdbcType   <span class="en-US">JDBC data type code</span>
		 *                   <span class="zh-CN">JDBC数据类型代码</span>
		 * @param scale      <span class="en-US">Data column scale</span>
		 *                   <span class="zh-CN">数据列小数位数</span>
		 * @param fieldValue <span class="en-US">Data that needs to be converted</span>
		 *                   <span class="zh-CN">需要转换的数据</span>
		 * @return <span class="en-US">Converted data</span>
		 * <span class="zh-CN">转换后的数据</span>
		 */
		private Object convertValue(final int jdbcType, final int scale, @Nonnull final Object fieldValue) {
			switch (jdbcType) {
				case Types.DATE:
					long dateLong = ((Date) fieldValue).getTime();
					return new java.sql.Date(dateLong);
				case Types.TIME:
					long timeLong = ((Date) fieldValue).getTime();
					return new java.sql.Time(timeLong);
				case Types.TIMESTAMP:
					long timestamp = ((Date) fieldValue).getTime();
					return new java.sql.Timestamp(timestamp);
				case Types.DECIMAL:
					return ((BigDecimal) fieldValue).setScale(scale, RoundingMode.HALF_UP);
				case Types.BLOB:
					if (fieldValue instanceof BeanObject) {
						return ConvertUtils.toByteArray(fieldValue.toString());
					} else {
						return ConvertUtils.toByteArray(fieldValue);
					}
				case Types.CLOB:
					try {
						if (fieldValue instanceof String) {
							return new SerialClob(((String) fieldValue).toCharArray());
						} else if (fieldValue instanceof char[]) {
							return new SerialClob((char[]) fieldValue);
						} else if (fieldValue instanceof Character[]) {
							Character[] characters = (Character[]) fieldValue;
							char[] charArrays = new char[characters.length];
							for (int i = 0; i < characters.length; i++) {
								charArrays[i] = characters[i];
							}
							return new SerialClob(charArrays);
						} else {
							return new SerialClob(StringUtils.base64Encode(ConvertUtils.toByteArray(fieldValue)).toCharArray());
						}
					} catch (SQLException ignore) {
						return fieldValue;
					}
				default:
					return fieldValue;
			}
		}

		/**
		 * <h3 class="en-US">Get the primary key data mapping table</h3>
		 * <h3 class="zh-CN">获取主键数据映射表</h3>
		 *
		 * @param object <span class="en-US">Primary key instance object</span>
		 *               <span class="zh-CN">主键实例对象</span>
		 * @return <span class="en-US">Converted data mapping table</span>
		 * <span class="zh-CN">转换后的数据映射表</span>
		 */
		private Map<String, Object> filterMap(@Nonnull final Serializable object) {
			Map<String, Object> retrieveMap = new HashMap<>();
			if (this.primaryKeyConfig.isCompositeId()) {
				this.tableDefine.getColumnDefines()
						.stream()
						.filter(ColumnDefine::isPrimaryKey)
						.forEach(columnDefine -> {
							String fieldName = this.columnFieldMapping.get(columnDefine.getColumnName());
							Optional.ofNullable(ReflectionUtils.getFieldValue(fieldName, object))
									.ifPresent(fieldValue ->
											retrieveMap.put(columnDefine.getColumnName(),
													this.convertValue(columnDefine.getJdbcType(),
															columnDefine.getScale(), fieldValue)));
						});
			} else {
				this.tableDefine.getColumnDefines()
						.stream()
						.filter(ColumnDefine::isPrimaryKey)
						.findFirst()
						.ifPresent(columnDefine -> retrieveMap.put(columnDefine.getColumnName(),
								this.convertValue(columnDefine.getJdbcType(), columnDefine.getScale(), object)));
			}
			return retrieveMap;
		}

		/**
		 * <h3 class="en-US">Generate primary key data</h3>
		 * <h3 class="zh-CN">生成主键信息数据</h3>
		 *
		 * @param object        <span class="en-US">Entity classes instance object</span>
		 *                      <span class="zh-CN">实体类实例对象</span>
		 * @param entityFactory <span class="en-US">Entity factory instance object</span>
		 *                      <span class="zh-CN">实体类工厂实例对象</span>
		 */
		private void generateKey(final BaseObject object, final EntityFactory entityFactory) {
			this.tableDefine.getColumnDefines()
					.forEach(columnDefine ->
							Optional.ofNullable(columnDefine.getGeneratorDefine())
									.filter(generatorDefine ->
											GenerationType.GENERATE.equals(generatorDefine.getGenerationType()))
									.ifPresent(generatorDefine -> {
										String fieldName = this.columnFieldMapping.get(columnDefine.getColumnName());
										ReflectionUtils.setField(fieldName, object,
												IDUtils.generate(generatorDefine.getGeneratorName(), new byte[0]));
									}));
			this.referenceDefineList.forEach(referenceDefine ->
					this.processReference(object, referenceDefine, entityFactory));
		}

		/**
		 * <h3 class="en-US">Process reference column values of reference information</h3>
		 * <h3 class="zh-CN">处理关联信息的关联列数据</h3>
		 *
		 * @param object          <span class="en-US">Entity classes instance object</span>
		 *                        <span class="zh-CN">实体类实例对象</span>
		 * @param referenceDefine <span class="en-US">Reference defines information</span>
		 *                        <span class="zh-CN">关联信息定义</span>
		 * @param entityFactory   <span class="en-US">Entity factory instance object</span>
		 *                        <span class="zh-CN">实体类工厂实例对象</span>
		 */
		private void processReference(final BaseObject object, final ReferenceDefine<?> referenceDefine,
		                              final EntityFactory entityFactory) {
			TableConfig referenceTable = entityFactory.tableConfig(referenceDefine.getReferenceClass());
			if (referenceTable == null) {
				return;
			}
			BaseObject reference =
					(BaseObject) ReflectionUtils.getFieldValue(referenceDefine.getFieldName(), object, Boolean.FALSE);
			if (entityFactory.checkExist(reference)) {
				return;
			}
			referenceTable.generateKey(reference, entityFactory);
			switch (referenceDefine.getReferenceType()) {
				case OneToOne:
				case OneToMany:
					for (JoinDefine joinDefine : referenceDefine.getJoinColumnList()) {
						Optional.ofNullable(this.readFieldValue(object, joinDefine.getCurrentField()))
								.ifPresent(fieldValue ->
										referenceTable.writeFieldValue(reference,
												joinDefine.getReferenceField(), fieldValue));
					}
					break;
				case ManyToOne:
					for (JoinDefine joinDefine : referenceDefine.getJoinColumnList()) {
						Optional.ofNullable(referenceTable.readFieldValue(reference,
										joinDefine.getReferenceField()))
								.ifPresent(fieldValue ->
										this.writeFieldValue(object, joinDefine.getCurrentField(),
												fieldValue));
					}
					break;
			}
		}

		/**
		 * <h3 class="en-US">Handling sensitive information and data</h3>
		 * <h3 class="zh-CN">处理敏感信息数据</h3>
		 *
		 * @param object <span class="en-US">Entity classes instance object</span>
		 *               <span class="zh-CN">实体类实例对象</span>
		 */
		private void desensitize(final BaseObject object) {
			this.sensitiveDefines.forEach(sensitiveDefine -> sensitiveDefine.desensitize(object));
		}

		/**
		 * <h3 class="en-US">Get the data column name corresponding to the attribute name</h3>
		 * <h3 class="zh-CN">获取属性名对应的数据列名</h3>
		 *
		 * @param fieldName <span class="en-US">Field name</span>
		 *                  <span class="zh-CN">属性名</span>
		 * @return <span class="en-US">Column name</span>
		 * <span class="zh-CN">数据列名</span>
		 */
		private String columnName(final String fieldName) {
			return this.fieldColumnMapping.get(fieldName);
		}

		/**
		 * <h3 class="en-US">Get the reference defines information corresponding to the attribute name</h3>
		 * <h3 class="zh-CN">获取属性名对应的关联配置信息</h3>
		 *
		 * @param fieldName <span class="en-US">Field name</span>
		 *                  <span class="zh-CN">属性名</span>
		 * @return <span class="en-US">Reference defines information</span>
		 * <span class="zh-CN">关联配置信息</span>
		 */
		private ReferenceDefine<?> referenceDefine(final String fieldName) {
			return this.referenceDefineList
					.stream()
					.filter(referenceDefine -> ObjectUtils.nullSafeEquals(referenceDefine.getFieldName(), fieldName))
					.findFirst()
					.orElse(null);
		}

		/**
		 * <h3 class="en-US">Get the reference defines information</h3>
		 * <h3 class="zh-CN">获取关联配置信息</h3>
		 *
		 * @param referenceClass <span class="en-US">Reference entity class</span>
		 *                       <span class="zh-CN">关联实体类</span>
		 * @return <span class="en-US">Reference defines information</span>
		 * <span class="zh-CN">关联配置信息</span>
		 */
		private ReferenceDefine<?> referenceDefine(final Class<?> referenceClass) {
			return this.referenceDefineList
					.stream()
					.filter(referenceDefine -> referenceDefine.getReferenceClass().equals(referenceClass))
					.findFirst()
					.orElse(null);
		}

		/**
		 * <h3 class="en-US">Write the data mapping table information to the given entity class instance object</h3>
		 * <h3 class="zh-CN">将数据映射表信息写入到给定的实体类实例对象</h3>
		 *
		 * @param dataMap <span class="en-US">Data mapping table</span>
		 *                <span class="zh-CN">数据映射表</span>
		 * @param object  <span class="en-US">Entity classes instance object</span>
		 *                <span class="zh-CN">实体类实例对象</span>
		 */
		private void copyData(final Map<String, Object> dataMap, final Object object) {
			Object primaryKey;
			if (this.primaryKeyConfig.isCompositeId()) {
				primaryKey = ReflectionUtils.getFieldValue(this.primaryKeyConfig.getFieldName(), object);
			} else {
				primaryKey = object;
			}
			for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
				ColumnDefine columnDefine = this.tableDefine.column(entry.getKey());
				if (columnDefine != null) {
					String columnName = columnDefine.getColumnName();
					String fieldName = this.columnFieldMapping.get(columnName);
					if (columnDefine.isPrimaryKey()) {
						ReflectionUtils.setField(fieldName, primaryKey, entry.getValue());
					} else {
						ReflectionUtils.setField(fieldName, object, entry.getValue());
					}
				}
			}
			if (this.primaryKeyConfig.isCompositeId()) {
				ReflectionUtils.setField(this.primaryKeyConfig.getFieldName(), object, primaryKey);
			}
		}
	}
}
