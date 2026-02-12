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
import org.intellij.lang.annotations.MagicConstant;
import org.nervousync.annotations.beans.DataTransfer;
import org.nervousync.annotations.provider.Provider;
import org.nervousync.beans.config.TransferConfig;
import org.nervousync.brain.commons.BrainCommons;
import org.nervousync.brain.configs.transactional.TransactionalConfig;
import org.nervousync.brain.defines.*;
import org.nervousync.brain.enumerations.ddl.DropOption;
import org.nervousync.brain.enumerations.dialect.DialectType;
import org.nervousync.brain.exceptions.sql.MultilingualSQLException;
import org.nervousync.brain.query.PartialCollection;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.brain.query.condition.Condition;
import org.nervousync.brain.query.condition.impl.ColumnCondition;
import org.nervousync.brain.query.condition.impl.GroupCondition;
import org.nervousync.brain.query.core.AbstractQuery;
import org.nervousync.brain.query.core.QueryFrom;
import org.nervousync.brain.query.core.QueryItem;
import org.nervousync.brain.query.from.FromSubQuery;
import org.nervousync.brain.query.from.FromTable;
import org.nervousync.brain.query.item.*;
import org.nervousync.brain.query.join.QueryJoin;
import org.nervousync.brain.query.join.SubQueryJoin;
import org.nervousync.brain.query.join.TableQueryJoin;
import org.nervousync.brain.query.param.AbstractParameter;
import org.nervousync.brain.query.param.impl.*;
import org.nervousync.brain.query.subqueries.ScalarSubQuery;
import org.nervousync.brain.query.subqueries.TableSubQuery;
import org.nervousync.brain.sharding.Calculator;
import org.nervousync.brain.source.BrainDataSource;
import org.nervousync.cache.CacheUtils;
import org.nervousync.cache.api.CacheClient;
import org.nervousync.commons.Globals;
import org.nervousync.commons.id.CUID;
import org.nervousync.commons.id.ULID;
import org.nervousync.enumerations.beans.StringType;
import org.nervousync.enumerations.security.EncodeType;
import org.nervousync.magi.annotations.data.ExcelColumn;
import org.nervousync.magi.annotations.data.HistoriesNames;
import org.nervousync.magi.annotations.data.Sensitive;
import org.nervousync.magi.annotations.sharding.Sharding;
import org.nervousync.magi.annotations.table.GeneratedData;
import org.nervousync.magi.annotations.table.Options;
import org.nervousync.magi.annotations.table.Schema;
import org.nervousync.magi.beans.defines.reference.JoinDefine;
import org.nervousync.magi.beans.defines.reference.ReferenceDefine;
import org.nervousync.magi.beans.defines.sensitive.SensitiveDefine;
import org.nervousync.magi.commons.MagiGlobals;
import org.nervousync.magi.config.MagiConfigure;
import org.nervousync.magi.data.DataUtils;
import org.nervousync.magi.data.tracker.SensitiveTracker;
import org.nervousync.magi.data.transfer.TransferColumn;
import org.nervousync.magi.entity.log.RecordLogger;
import org.nervousync.magi.enumerations.reference.ReferenceType;
import org.nervousync.magi.exceptions.core.DatabaseException;
import org.nervousync.magi.interceptors.LazyLoadInterceptor;
import org.nervousync.magi.query.builder.EntityQueryBuilder;
import org.nervousync.magi.query.builder.EntityConditionsBuilder;
import org.nervousync.magi.query.builder.EntityItemsBuilder;
import org.nervousync.utils.core.*;
import org.nervousync.utils.id.IDUtils;
import org.nervousync.utils.logger.LoggerUtils;
import org.nervousync.utils.security.SecurityUtils;

import javax.sql.rowset.serial.SerialClob;
import java.io.Serializable;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.Types;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h2 class="en-US">Entity class factory</h2>
 * <h2 class="zh-CN">实体类工厂</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Mar 30, 2016 17:05:12 $
 */
@SuppressWarnings("unused")
public final class EntityFactory {

	/**
	 * <span class="en-US">Logger instance</span>
	 * <span class="zh-CN">日志实例</span>
	 */
	private static final LoggerUtils.Logger LOGGER = LoggerUtils.getLogger(EntityFactory.class);

	/**
	 * <span class="en-US">Registered implement class of sensitive tracker information mapping table</span>
	 * <span class="zh-CN">注册的敏感信息追踪器实现类映射表</span>
	 */
	private static final Hashtable<String, Class<?>> REGISTERED_TRACKER_IMPLEMENTS = new Hashtable<>();

	/**
	 * <span class="en-US">Entity class factory singleton instance object</span>
	 * <span class="zh-CN">实体类工厂单例对象</span>
	 */
	private static EntityFactory INSTANCE = null;

	/**
	 * <span class="en-US">Sensitive data tracker</span>
	 * <span class="zh-CN">敏感信息追踪器</span>
	 */
	private final SensitiveTracker sensitiveTracker;
	/**
	 * <span class="en-US">Data source instance object</span>
	 * <span class="zh-CN">数据源实例对象</span>
	 */
	private final BrainDataSource dataSource;
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
		ServiceLoader.load(SensitiveTracker.class)
				.forEach(sensitiveTracker ->
						Optional.ofNullable(sensitiveTracker.getClass().getAnnotation(Provider.class))
								.ifPresent(provider ->
										REGISTERED_TRACKER_IMPLEMENTS.put(provider.name(), sensitiveTracker.getClass())));
	}

	/**
	 * <h3 class="en-US">Private constructor method for entity class factory</h3>
	 * <h3 class="zh-CN">实体类工厂的私有构造方法</h3>
	 *
	 * @param magiConfigure <span class="en-US">Used identification code of query optimizer implementation class</span>
	 *                      <span class="zh-CN">使用的查询优化器实现类识别代码</span>
	 */
	private EntityFactory(final MagiConfigure magiConfigure) throws Exception {
		ByteBuddyAgent.install();
		this.registerTypes();
		this.dataSource = BrainDataSource.getInstance();
		this.dataSource.initialize(magiConfigure.getBrainConfigure());
		Optional.ofNullable(magiConfigure.getStorageConfig()).ifPresent(DataUtils::initialize);
		Optional.ofNullable(magiConfigure.getCacheConfig())
				.map(cacheConfig -> CacheUtils.register(MagiGlobals.CACHE_NAME, cacheConfig))
				.ifPresent(registered -> LOGGER.info("", registered));
		this.scanPackages(magiConfigure.getScanPackages());
		if (StringUtils.isEmpty(magiConfigure.getSensitiveTracker())) {
			this.sensitiveTracker = null;
		} else {
			this.sensitiveTracker =
					Optional.ofNullable(ClassUtils.forName(magiConfigure.getSensitiveTracker()))
							.filter(trackerClass -> ClassUtils.isAssignable(trackerClass, SensitiveTracker.class))
							.map(trackerClass -> (SensitiveTracker) ObjectUtils.newInstance(trackerClass))
							.orElse(null);
		}
	}

	/**
	 * <h3 class="en-US">Get registered implement class of sensitive tracker information mapping table</h3>
	 * <h3 class="zh-CN">获取已注册的敏感信息追踪器实现类映射表</h3>
	 *
	 * @return <span class="en-US">Registered implement class of sensitive tracker information mapping table</span>
	 * <span class="zh-CN">注册的敏感信息追踪器实现类映射表</span>
	 */
	public static Hashtable<String, Class<?>> registeredTrackers() {
		return REGISTERED_TRACKER_IMPLEMENTS;
	}

	/**
	 * <h3 class="en-US">Static method for initializing entity factory</h3>
	 * <h3 class="zh-CN">静态方法用于初始化实体类工厂</h3>
	 *
	 * @param magiConfigure <span class="en-US">Used identification code of query optimizer implementation class</span>
	 *                      <span class="zh-CN">使用的查询优化器实现类识别代码</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public static void initialize(final MagiConfigure magiConfigure) throws Exception {
		if (INSTANCE == null) {
			synchronized (EntityFactory.class) {
				INSTANCE = new EntityFactory(magiConfigure);
				//  Register execute destroy method when the system shutdown
				SystemUtils.registerShutdownHook(new Thread(EntityFactory::destroy));
			}
		}
	}

	/**
	 * <h3 class="en-US">Static getter method for the entity class factory singleton instance object</h3>
	 * <h3 class="zh-CN">实体类工厂单例对象的静态Getter方法</h3>
	 *
	 * @return <span class="en-US">Entity class factory singleton instance object</span>
	 * <span class="zh-CN">实体类工厂单例对象</span>
	 * @throws DatabaseException <span class="en-US">If the entity class factory wasn't initialized</span>
	 *                           <span class="zh-CN">如果实体类工厂未初始化</span>
	 */
	public static EntityFactory getInstance() throws DatabaseException {
		return getInstance(Boolean.FALSE);
	}

	/**
	 * <h3 class="en-US">Static getter method for the entity class factory singleton instance object</h3>
	 * <h3 class="zh-CN">实体类工厂单例对象的静态Getter方法</h3>
	 *
	 * @param readOnly <span class="en-US">Read-only flag</span>
	 *                 <span class="zh-CN">只读模式标记</span>
	 * @return <span class="en-US">Entity class factory singleton instance object</span>
	 * <span class="zh-CN">实体类工厂单例对象</span>
	 * @throws DatabaseException <span class="en-US">If the entity class factory wasn't initialized</span>
	 *                           <span class="zh-CN">如果实体类工厂未初始化</span>
	 */
	public static EntityFactory getInstance(final boolean readOnly) throws DatabaseException {
		return getInstance(readOnly, Boolean.FALSE);
	}

	/**
	 * <h3 class="en-US">Static getter method for the entity class factory singleton instance object</h3>
	 * <h3 class="zh-CN">实体类工厂单例对象的静态Getter方法</h3>
	 *
	 * @param readOnly    <span class="en-US">Read-only flag</span>
	 *                    <span class="zh-CN">只读模式标记</span>
	 * @param restoreMode <span class="en-US">Data restore mode flag</span>
	 *                    <span class="zh-CN">数据还原模式标记</span>
	 * @return <span class="en-US">Entity class factory singleton instance object</span>
	 * <span class="zh-CN">实体类工厂单例对象</span>
	 * @throws DatabaseException <span class="en-US">If the entity class factory wasn't initialized</span>
	 *                           <span class="zh-CN">如果实体类工厂未初始化</span>
	 */
	public static EntityFactory getInstance(final boolean readOnly, final boolean restoreMode) throws DatabaseException {
		if (INSTANCE != null) {
			INSTANCE.threadConfig(readOnly, restoreMode);
			if (readOnly) {
				try {
					INSTANCE.beginTransactional(null);
				} catch (Exception e) {
					LOGGER.error("Transactional_Init_Error", e);
				}
			}
			return INSTANCE;
		}
		throw new DatabaseException(0x00DB00010007L);
	}

	/**
	 * <h3 class="en-US">Destroy the current entity factory</h3>
	 * <h3 class="zh-CN">销毁当前实体类工厂</h3>
	 */
	public static void destroy() {
		if (INSTANCE != null) {
			synchronized (EntityFactory.class) {
				INSTANCE.registeredTables.clear();
				INSTANCE.identifiedCodeMapping.clear();
				INSTANCE.redefinedClasses.clear();
				DataUtils.destroy();
				BrainDataSource.destroy();
				CacheUtils.deregister(MagiGlobals.CACHE_NAME);
				INSTANCE = null;
			}
		}
	}

	/**
	 * <h3 class="en-US">Get the JDBC type code by the given Java type class</h3>
	 * <h3 class="zh-CN">根据给定的Java类型获取JDBC类型代码</h3>
	 *
	 * @param typeClass <span class="en-US">Java type class</span>
	 *                  <span class="zh-CN">Java类型</span>
	 * @return <span class="en-US">JDBC type code</span>
	 * <span class="zh-CN">JDBC类型代码</span>
	 */
	@MagicConstant(valuesFromClass = Types.class)
	public int jdbcType(final Class<?> typeClass) {
		return this.dataConvertMapping.getOrDefault(typeClass, Types.OTHER);
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
	 * @param scanPackages <span class="en-US">Package name string array, which can be a regular expression list</span>
	 *                     <span class="zh-CN">包名数组，可以是正则表达式列表</span>
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
	 * <h3 class="en-US">Checks whether the given entity class is a registry data table</h3>
	 * <h3 class="zh-CN">检查给定的实体类是否为注册数据表</h3>
	 *
	 * @param entityClass <span class="en-US">Entity class</span>
	 *                    <span class="zh-CN">实体类</span>
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 */
	public boolean registeredTable(@Nonnull final Class<?> entityClass) {
		return Optional.ofNullable(ClassUtils.originalClassName(entityClass))
				.filter(StringUtils::notBlank)
				.map(BrainCommons::identifyCode)
				.map(this.identifiedCodeMapping::get)
				.map(this.registeredTables::containsKey)
				.orElse(Boolean.FALSE);
	}

	/**
	 * <h3 class="en-US">Checks whether the given identification code is a registry identification code</h3>
	 * <h3 class="zh-CN">检查给定的识别代码是否为注册表识别代码</h3>
	 *
	 * @param tableName <span class="en-US">Data table name</span>
	 *                  <span class="zh-CN">数据表名</span>
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 */
	public boolean registeredTable(final String tableName) {
		return Optional.ofNullable(tableName)
				.filter(StringUtils::notBlank)
				.map(BrainCommons::identifyCode)
				.map(this.identifiedCodeMapping::get)
				.map(this.registeredTables::containsKey)
				.orElse(Boolean.FALSE);
	}

	/**
	 * <h3 class="en-US">Checks whether the data source of the given table name supports relational queries</h3>
	 * <h3 class="zh-CN">检查给定的数据表名所在的数据源是否支持关联查询</h3>
	 *
	 * @param tableName <span class="en-US">Data table name</span>
	 *                  <span class="zh-CN">数据表名</span>
	 * @return <span class="en-US">Support join query</span>
	 * <span class="zh-CN">支持关联查询</span>
	 */
	public boolean supportJoin(final String tableName) {
		return Optional.ofNullable(tableName)
				.filter(StringUtils::notBlank)
				.map(BrainCommons::identifyCode)
				.map(this.identifiedCodeMapping::get)
				.map(this.registeredTables::get)
				.map(TableConfig::getTableDefine)
				.map(TableDefine::getSchemaName)
				.map(this.dataSource::supportJoin)
				.orElse(Boolean.FALSE);

	}

	/**
	 * <h3 class="en-US">Read sensitive data</h3>
	 * <h3 class="zh-CN">读取敏感信息</h3>
	 *
	 * @param object   <span class="en-US">Entity classes instance object</span>
	 *                 <span class="zh-CN">实体类实例对象</span>
	 * @param userCode <span class="en-US">Identify code of the reader</span>
	 *                 <span class="zh-CN">读取人的识别代码</span>
	 */
	public void sensitiveData(@Nonnull final BaseObject object, @Nonnull final String userCode) {
		Optional.ofNullable(this.tableConfig(object.getClass()))
				.ifPresent(tableConfig -> tableConfig.sensitiveData(object, userCode));
	}

	/**
	 * <h3 class="en-US">Checks whether two tables are in the same database, according to the given query conditions</h3>
	 * <h3 class="zh-CN">根据给定的查询条件检查两个数据表是否在同一数据库中</h3>
	 *
	 * @param queryInfo <span class="en-US">Query information instance object</span>
	 *                  <span class="zh-CN">查询信息实例对象</span>
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 * @throws SQLException <span class="en-US">The data source or data table is not registered</span>
	 *                      <span class="zh-CN">数据源或数据表未注册</span>
	 */
	public boolean sameCatalog(@Nonnull final QueryInfo queryInfo) throws SQLException {
		List<String> identifyCodes = this.identifyCodes(queryInfo);
		if (identifyCodes.isEmpty() || identifyCodes.size() == 1) {
			return Boolean.TRUE;
		}

		if (identifyCodes.stream().anyMatch(identifyCode -> !this.registeredTable(identifyCode))) {
			throw new MultilingualSQLException(0x00DB00010012L);
		}

		Set<String> schemaNames = new HashSet<>();
		identifyCodes.forEach(identifyCode ->
				Optional.ofNullable(this.tableConfig(identifyCode))
						.map(TableConfig::getTableDefine)
						.map(TableDefine::getSchemaName)
						.map(schemaName ->
								StringUtils.isEmpty(schemaName) ? this.dataSource.getDefaultSchema() : schemaName)
						.ifPresent(schemaNames::add));
		return schemaNames.size() == 1;
	}

	/**
	 * <h3 class="en-US">Get the data column name based on the given entity class and identified code</h3>
	 * <h3 class="zh-CN">根据给定的实体类和识别代码获取数据列名</h3>
	 *
	 * @param tableCode  <span class="en-US">Data table identify code</span>
	 *                   <span class="zh-CN">数据表识别代码</span>
	 * @param columnCode <span class="en-US">Identified code</span>
	 *                   <span class="zh-CN">识别代码</span>
	 * @return <span class="en-US">Data column name</span>
	 * <span class="zh-CN">数据列名</span>
	 * @throws SQLException <span class="en-US">If the data table configuration information is not found</span>
	 *                      <span class="zh-CN">如果数据表配置信息未找到</span>
	 */
	public String columnName(final String tableCode, final String columnCode) throws SQLException {
		return Optional.ofNullable(this.tableConfig(tableCode))
				.map(tableConfig -> tableConfig.columnName(columnCode))
				.orElseThrow(() -> new MultilingualSQLException(0x00DB00010005L, tableCode));
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
	 * <h3 class="en-US">Get the JDBC type code of the data column in the registration data table</h3>
	 * <h3 class="zh-CN">获取注册数据表中数据列的JDBC类型代码</h3>
	 *
	 * @param tableCode    <span class="en-US">Data table identify code</span>
	 *                     <span class="zh-CN">数据表识别代码</span>
	 * @param identifyCode <span class="en-US">Data column identify code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">JDBC type code</span>
	 * <span class="zh-CN">JDBC类型代码</span>
	 * @throws SQLException <span class="en-US">The data table is not registered or data column not exists</span>
	 *                      <span class="zh-CN">数据表未注册或数据列不存在</span>
	 */
	public int jdbcType(final String tableCode, @Nonnull final String identifyCode) throws SQLException {
		TableConfig tableConfig = this.tableConfig(tableCode);
		if (tableConfig == null) {
			throw new MultilingualSQLException(0x00DB00010005L, tableCode);
		}
		return tableConfig.jdbcType(identifyCode);
	}

	/**
	 * <h3 class="en-US">Get the JDBC type code of the data column in the registration data table</h3>
	 * <h3 class="zh-CN">获取注册数据表中数据列的JDBC类型代码</h3>
	 *
	 * @param entityClass  <span class="en-US">Entity class</span>
	 *                     <span class="zh-CN">实体类</span>
	 * @param identifyCode <span class="en-US">Data column identify code</span>
	 *                     <span class="zh-CN">数据列识别代码</span>
	 * @return <span class="en-US">JDBC type code</span>
	 * <span class="zh-CN">JDBC类型代码</span>
	 * @throws SQLException <span class="en-US">The data table is not registered or data column not exists</span>
	 *                      <span class="zh-CN">数据表未注册或数据列不存在</span>
	 */
	public int jdbcType(final Class<?> entityClass, @Nonnull final String identifyCode) throws SQLException {
		return this.jdbcType(this.tableName(entityClass), identifyCode);
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
	 * <h3 class="en-US">Drop the data tables</h3>
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
	 * <h3 class="en-US">Save entity class to the database</h3>
	 * <h3 class="zh-CN">保存实体类到数据库</h3>
	 *
	 * @param object      <span class="en-US">Entity classes instance object</span>
	 *                    <span class="zh-CN">实体类实例对象</span>
	 * @param operateUser <span class="en-US">Operate user identified code</span>
	 *                    <span class="zh-CN">操作人识别代码</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public void saveRecord(@Nonnull final BaseObject object, final Long operateUser) throws Exception {
		if (this.checkExist(object)) {
			throw new MultilingualSQLException(0x00DB00010009L);
		}
		if (this.readOnly.get()) {
			throw new MultilingualSQLException(0x00DB00010010L);
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
		this.newConfig(Boolean.TRUE, object, dataMap.keySet(), tableConfig);
		this.cacheData(tableConfig, object);
		if (!restoreMode) {
			this.logOperate(object, operateUser, MagiGlobals.OPERATE_CODE_CREATE);
			this.mergeObjects(object, tableConfig.getReferenceDefineList(),
					List.of(CascadeType.ALL, CascadeType.PERSIST), operateUser, MagiGlobals.OPERATE_CODE_CREATE);
		}
	}

	/**
	 * <h3 class="en-US">Update entity class to database</h3>
	 * <h3 class="zh-CN">更新实体类到数据库</h3>
	 *
	 * @param object      <span class="en-US">Entity classes instance object</span>
	 *                    <span class="zh-CN">实体类实例对象</span>
	 * @param operateUser <span class="en-US">Operate user identified code</span>
	 *                    <span class="zh-CN">操作人识别代码</span>
	 * @param operateCode <span class="en-US">Operate code</span>
	 *                    <span class="zh-CN">操作代码</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public void updateRecord(@Nonnull final BaseObject object, final Long operateUser,
	                         final Integer operateCode) throws Exception {
		if (this.readOnly.get()) {
			throw new MultilingualSQLException(0x00DB00010010L);
		}
		this.checkModify(object);
		TableConfig tableConfig = this.tableConfig(object.getClass());
		tableConfig.desensitize(object);
		int resultCount = this.dataSource.update(tableConfig.getTableDefine().getTableName(),
				tableConfig.updateMap(object), tableConfig.filterMap(object));
		if (resultCount != 1) {
			throw new MultilingualSQLException(0x00DB00010023L);
		}
		this.cacheData(tableConfig, object);
		boolean restoreMode = Optional.ofNullable(this.restoreMode.get()).orElse(Boolean.FALSE);
		if (!restoreMode) {
			this.logOperate(object, operateUser, operateCode);
			this.mergeObjects(object, tableConfig.getReferenceDefineList(), List.of(CascadeType.ALL, CascadeType.MERGE),
					operateUser, operateCode);
		}
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
			throw new MultilingualSQLException(0x00DB00010010L);
		}
		this.checkModify(object);
		TableConfig tableConfig = this.tableConfig(object.getClass());
		int resultCount = this.dataSource.delete(tableConfig.getTableDefine().getTableName(), tableConfig.filterMap(object));
		if (resultCount != 1) {
			throw new MultilingualSQLException(0x00DB00010024L);
		}
		if (tableConfig.isCacheable()) {
			String cacheKey = tableConfig.identifier(object);
			if (StringUtils.notBlank(cacheKey)) {
				this.cacheClient().ifPresent(cacheClient -> cacheClient.del(cacheKey));
			}
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
		if (!Optional.ofNullable(this.restoreMode.get()).orElse(Boolean.FALSE)) {
			try {
				this.dataSource.delete("NSYC_Record_Operate_Log",
						Map.of("tableIdentifier", tableConfig.identifier()));
			} catch (Exception ignore) {
			}
		}
	}

	private void cacheData(final TableConfig tableConfig, final BaseObject record) throws SQLException {
		if (tableConfig.isCacheable()) {
			Optional.of(tableConfig.identifier(record))
					.filter(StringUtils::notBlank)
					.ifPresent(cacheKey ->
							Optional.of(BeanUtils.objectToString(record, StringType.JSON))
									.filter(StringUtils::notBlank)
									.ifPresent(cacheData ->
											this.cacheClient().ifPresent(cacheClient ->
													cacheClient.set(cacheKey, cacheData))));
		}
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
				tableConfig.filterMap(object), Boolean.FALSE, LockModeType.NONE);

		if (dataMap.isEmpty()) {
			throw new MultilingualSQLException(0x00DB00010011L);
		}
		tableConfig.copyData(dataMap, object);
		this.cacheData(tableConfig, object);
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
		return this.retrieveRecord(entityClass, tableConfig.primaryKeyMap(primaryKey), forUpdate);
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
	public <T> T retrieveRecord(@Nonnull final Class<T> entityClass, final TreeMap<String, Object> filterMap,
	                            final boolean forUpdate) throws Exception {
		TableConfig tableConfig = this.tableConfig(entityClass);
		String cacheKey = tableConfig.identifier(filterMap);
		Map<String, Object> dataMap = null;
		boolean missed = Boolean.TRUE;
		if (tableConfig.isCacheable() && !forUpdate) {
			dataMap = this.cacheClient()
					.map(cacheClient -> cacheClient.get(cacheKey))
					.filter(StringUtils::notBlank)
					.map(cacheData -> BeanUtils.stringToMap(cacheData, StringType.JSON, Globals.DEFAULT_ENCODING))
					.orElse(null);
			missed = (dataMap == null || dataMap.isEmpty());
		}
		if (missed) {
			StringBuilder queryColumns = new StringBuilder();
			tableConfig.queryColumns().forEach(columnDefine ->
					queryColumns.append(BrainCommons.DEFAULT_SPLIT_CHARACTER).append(columnDefine.getColumnName()));
			dataMap = this.dataSource.retrieve(tableConfig.getTableDefine().getTableName(),
					queryColumns.substring(BrainCommons.DEFAULT_SPLIT_CHARACTER.length()),
					filterMap, forUpdate, tableConfig.getLockOption());
		}
		if (dataMap == null || dataMap.isEmpty()) {
			return null;
		}
		T object = this.dataMapToObject(entityClass, dataMap, forUpdate);
		if (missed) {
			String cacheData = BeanUtils.objectToString(new HashMap<>(dataMap), StringType.JSON);
			this.cacheClient().ifPresent(cacheClient -> cacheClient.set(cacheKey, cacheData));
		}
		return object;
	}

	/**
	 * <h3 class="en-US">Convert the data mapping table to the target entity object</h3>
	 * <h3 class="zh-CN">转换数据集映射表为目标对象</h3>
	 *
	 * @param entityClass <span class="en-US">Entity classes</span>
	 *                    <span class="zh-CN">实体类</span>
	 * @param dataMap     <span class="en-US">Data mapping table</span>
	 *                    <span class="zh-CN">数据集映射表</span>
	 * @param <T>         <span class="en-US">Entity class generic class</span>
	 *                    <span class="zh-CN">实体类的泛型类</span>
	 * @return <span class="en-US">Entity classes instance object</span>
	 * <span class="zh-CN">实体类实例对象</span>
	 */
	public <T> T dataMapToObject(@Nonnull final Class<T> entityClass, final Map<String, Object> dataMap) {
		return this.dataMapToObject(entityClass, dataMap, Boolean.FALSE);
	}

	/**
	 * <h3 class="en-US">Execute a query plan and return query results</h3>
	 * <h3 class="zh-CN">执行查询计划并返回查询结果</h3>
	 *
	 * @param queryInfo <span class="en-US">Query information instance object</span>
	 *                  <span class="zh-CN">查询信息实例对象</span>
	 * @return <span class="en-US">Query results</span>
	 * <span class="zh-CN">查询结果</span>
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	public PartialCollection query(@Nonnull final QueryInfo queryInfo) throws Exception {
		String cacheKey = queryInfo.getCacheKey();
		if (queryInfo.isCacheables() && StringUtils.notBlank(cacheKey)) {
			String cacheData = this.cacheClient()
					.map(cacheClient -> cacheClient.get(cacheKey))
					.orElse(Globals.DEFAULT_VALUE_STRING);
			if (StringUtils.notBlank(cacheData)) {
				PartialCollection partialCollection = PartialCollection.parse(cacheData);
				if (partialCollection != null) {
					return partialCollection;
				}
			}
		}

		PartialCollection partialCollection = this.dataSource.query(queryInfo);
		if (queryInfo.isCacheables() && StringUtils.notBlank(cacheKey)) {
			this.cacheClient()
					.ifPresent(cacheClient -> cacheClient.set(cacheKey, partialCollection.toString()));
		}
		return partialCollection;
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
		String cacheKey = queryInfo.getCacheKey();
		if (queryInfo.isCacheables() && StringUtils.notBlank(cacheKey)) {
			String cacheData = this.cacheClient()
					.map(cacheClient -> cacheClient.get(cacheKey))
					.orElse(Globals.DEFAULT_VALUE_STRING);
			if (StringUtils.notBlank(cacheData)) {
				return Long.parseLong(cacheData, 16);
			}
		}
		long totalCount = this.dataSource.queryTotal(queryInfo);
		if (totalCount >= 0L && queryInfo.isCacheables() && StringUtils.notBlank(cacheKey)) {
			this.cacheClient().ifPresent(cacheClient ->
					cacheClient.set(cacheKey, Long.toString(totalCount, 16)));
		}
		return totalCount;
	}

	/**
	 * <h3 class="en-US">Read data record operate log list</h3>
	 * <h3 class="zh-CN">读取数据记录操作日志</h3>
	 *
	 * @param object <span class="en-US">Entity classes instance object</span>
	 *               <span class="zh-CN">实体类实例对象</span>
	 * @return <span class="en-US">Operate log information list</span>
	 * <span class="zh-CN">操作日志信息列表</span>
	 */
	public List<RecordLogger> recordLogs(@Nonnull final BaseObject object) {
		final List<RecordLogger> logList = new ArrayList<>();
		try {
			TableConfig tableConfig = this.tableConfig(object.getClass());
			if (tableConfig != null) {
				QueryInfo queryInfo = EntityQueryBuilder.newBuilder(RecordLogger.class)
						.where()
						.equalTo(RecordLogger.class, "tableIdentifier").matchValue(tableConfig.identifier()).confirm()
						.equalTo(RecordLogger.class, "recordIdentifier").matchValue(tableConfig.identifier(object)).confirm()
						.confirm()
						.build();
				this.dataSource.query(queryInfo).asList()
						.forEach(dataMap ->
								Optional.ofNullable(this.dataMapToObject(RecordLogger.class, dataMap, Boolean.FALSE))
										.ifPresent(logList::add));
			}
		} catch (Exception ignore) {
			logList.clear();
		}
		return logList;
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
		if (!this.checkExist(object) || this.persistenceConfig(object).loadedField(fieldName)) {
			//  Is not an attached object, maybe a new record or current field was loaded
			return;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Lazy_Load_Field_Debug", ClassUtils.originalClassName(object.getClass()), fieldName);
		}

		PersistenceConfig persistenceConfig = this.persistenceConfig(object);
		TableConfig tableConfig = this.tableConfig(object.getClass());
		String columnName = tableConfig.columnName(fieldName);
		Object fieldValue = null;
		if (StringUtils.notBlank(columnName)) {
			Map<String, Object> dataMap =
					this.dataSource.retrieve(tableConfig.getTableDefine().getTableName(),
							columnName, tableConfig.filterMap(object),
							persistenceConfig.isForUpdate(), tableConfig.getLockOption());
			if (dataMap.containsKey(columnName)) {
				fieldValue = dataMap.get(columnName);
			}
		} else {
			ReferenceDefine<?> referenceDefine = tableConfig.referenceDefine(fieldName);
			Class<?> referenceClass = referenceDefine.getReferenceClass();
			TableConfig referenceTable = this.tableConfig(referenceClass);
			TreeMap<String, Object> filterMap = new TreeMap<>();
			final Object primaryKey;
			if (tableConfig.primaryKeyConfig.isCompositeId()) {
				primaryKey = ReflectionUtils.getFieldValue(tableConfig.primaryKeyConfig.getFieldName(), object);
			} else {
				primaryKey = object;
			}
			referenceDefine.getJoinColumnList()
					.forEach(joinDefine ->
							filterMap.put(referenceTable.columnName(joinDefine.getReferenceField()),
									ReflectionUtils.getFieldValue(joinDefine.getCurrentField(),
											tableConfig.primaryKey(joinDefine.getCurrentField()) ? primaryKey : object)));
			if (referenceDefine.isReturnArray()) {
				List<Map<String, Object>> dataList;
				if (persistenceConfig.isForUpdate()) {
					dataList = this.queryForUpdate(referenceClass, filterMap).asList();
				} else {
					EntityConditionsBuilder<EntityQueryBuilder> conditionsBuilder =
							EntityQueryBuilder.newBuilder(referenceClass).where();
					for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
						conditionsBuilder =
								conditionsBuilder.equalTo(referenceClass, entry.getKey())
										.matchValue(entry.getValue())
										.confirm();
					}
					dataList = this.query(conditionsBuilder.confirm().build()).asList();
				}
				List<Object> referenceList = new ArrayList<>();
				for (Map<String, Object> dataMap : dataList) {
					referenceList.add(this.dataMapToObject(referenceClass, dataMap, persistenceConfig.isForUpdate()));
				}
				if (returnArray) {
					fieldValue = CollectionUtils.toArray(referenceList);
				} else {
					fieldValue = referenceList;
				}
			} else {
				try {
					fieldValue = this.retrieveRecord(referenceClass, filterMap, persistenceConfig.isForUpdate());
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
	 * <h3 class="en-US">Save data record operate log</h3>
	 * <h3 class="zh-CN">记录数据操作日志</h3>
	 *
	 * @param object      <span class="en-US">Entity classes instance object</span>
	 *                    <span class="zh-CN">实体类实例对象</span>
	 * @param operateUser <span class="en-US">Operate user identified code</span>
	 *                    <span class="zh-CN">操作人识别代码</span>
	 * @param operateCode <span class="en-US">Operate code</span>
	 *                    <span class="zh-CN">操作代码</span>
	 */
	private void logOperate(@Nonnull final BaseObject object, final Long operateUser, final Integer operateCode) {
		if (operateUser != null && operateCode != null) {
			try {
				TableConfig tableConfig = this.tableConfig(object.getClass());
				RecordLogger recordLogger = new RecordLogger();
				recordLogger.setTableIdentifier(tableConfig.identifier());
				recordLogger.setRecordIdentifier(tableConfig.identifier(object));
				recordLogger.setOperateUser(operateUser);
				recordLogger.setOperateCode(operateCode);
				recordLogger.setOperateTimestamp(DateTimeUtils.currentUTCTimeMillis());
				recordLogger.save();
			} catch (Exception ignore) {
			}
		}
	}

	/**
	 * <h3 class="en-US">Scan and register a data table definition class that matches the given package name list</h3>
	 * <h3 class="zh-CN">扫描并注册符合给定包名列表的数据表定义类</h3>
	 *
	 * @param classLoader  <span class="en-US">Which class loader need to be scanned</span>
	 *                     <span class="zh-CN">需要扫描的类加载器</span>
	 * @param scanPackages <span class="en-US">Package name list, which can be a regular expression list</span>
	 *                     <span class="zh-CN">包名列表，可以是正则表达式列表</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	@SuppressWarnings("unchecked")
	private void scanPackages(final ClassLoader classLoader, @Nonnull final List<String> scanPackages)
			throws Exception {
		if (classLoader == null || scanPackages.isEmpty()) {
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
	 * <h3 class="en-US">Execute query commands for data updates</h3>
	 * <h3 class="zh-CN">执行用于数据更新的查询命令</h3>
	 *
	 * @param entityClass <span class="en-US">Entity classes</span>
	 *                    <span class="zh-CN">实体类</span>
	 * @param filterMap   <span class="en-US">Retrieve filter mapping</span>
	 *                    <span class="zh-CN">查询条件映射表</span>
	 * @return <span class="en-US">List of data mapping tables for retrieved records</span>
	 * <span class="zh-CN">检索到记录的数据映射表列表</span>
	 * @throws Exception <span class="en-US">An error occurred during execution</span>
	 *                   <span class="zh-CN">执行过程中出错</span>
	 */
	private PartialCollection queryForUpdate(@Nonnull final Class<?> entityClass, final Map<String, Object> filterMap)
			throws Exception {
		TableConfig tableConfig = this.tableConfig(entityClass);
		TableDefine tableDefine = tableConfig.getTableDefine();
		EntityQueryBuilder queryBuilder = EntityQueryBuilder.newBuilder(entityClass);
		EntityItemsBuilder<EntityQueryBuilder> itemsBuilder = queryBuilder.items();
		for (ColumnDefine columnDefine : tableDefine.getColumnDefines()) {
			itemsBuilder = itemsBuilder.column(entityClass, columnDefine.getColumnName()).confirm();
		}
		EntityConditionsBuilder<EntityQueryBuilder> conditionsBuilder = itemsBuilder.confirm().where();
		for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
			ColumnDefine columnDefine = tableDefine.column(entry.getKey());
			if (columnDefine == null) {
				continue;
			}
			conditionsBuilder =
					conditionsBuilder.equalTo(entityClass, columnDefine.getColumnName())
							.matchValue(entry.getValue())
							.confirm();
		}
		return this.dataSource.query(conditionsBuilder.confirm().forUpdate(tableConfig.getLockOption()).build());
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
	 * <h3 class="en-US">Convert the data mapping table to the target entity object</h3>
	 * <h3 class="zh-CN">转换数据集映射表为目标对象</h3>
	 *
	 * @param entityClass <span class="en-US">Entity classes</span>
	 *                    <span class="zh-CN">实体类</span>
	 * @param dataMap     <span class="en-US">Data mapping table</span>
	 *                    <span class="zh-CN">数据集映射表</span>
	 * @param forUpdate   <span class="en-US">Retrieve record is using for update</span>
	 *                    <span class="zh-CN">读取的记录用于更新</span>
	 * @param <T>         <span class="en-US">Entity class generic class</span>
	 *                    <span class="zh-CN">实体类的泛型类</span>
	 * @return <span class="en-US">Entity classes instance object</span>
	 * <span class="zh-CN">实体类实例对象</span>
	 */
	private <T> T dataMapToObject(@Nonnull final Class<T> entityClass, final Map<String, Object> dataMap,
	                              final boolean forUpdate) {
		TableConfig tableConfig = this.tableConfig(entityClass);
		T object = ObjectUtils.newInstance(entityClass);
		tableConfig.copyData(dataMap, object);
		this.newConfig(forUpdate, (BaseObject) object, dataMap.keySet(), tableConfig);
		return object;
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
		long identifyCode = object.identifiedCode();
		if (this.threadLocal.get() == null) {
			this.threadLocal.set(new Hashtable<>());
		}
		if (this.threadLocal.get().containsKey(identifyCode)) {
			return;
		}
		PersistenceConfig persistenceConfig = new PersistenceConfig(forUpdate);
		keySet.forEach(columnName ->
				Optional.ofNullable(tableConfig.tableDefine.column(columnName))
						.map(ColumnDefine::getColumnName)
						.ifPresent(persistenceConfig::loadField));
		for (ReferenceDefine<?> referenceDefine : tableConfig.queryReferences()) {
			String fieldName = referenceDefine.getFieldName();
			Optional.ofNullable(ReflectionUtils.getFieldIfAvailable(object.getClass(), fieldName))
					.map(field -> ReflectionUtils.getFieldValue(field, object))
					.ifPresent(fieldValue -> persistenceConfig.loadField(fieldName));
		}
		for (ReferenceDefine<?> referenceDefine : tableConfig.getReferenceDefineList()) {
			Field field = ReflectionUtils.getFieldIfAvailable(object.getClass(), referenceDefine.getFieldName());
			if (ReflectionUtils.getFieldValue(field, object) != null) {
				persistenceConfig.loadField(referenceDefine.getFieldName());
			}
		}
		this.threadLocal.get().put(identifyCode, persistenceConfig);
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
		this.registerType(LocalDate.class, Types.DATE);
		this.registerType(LocalTime.class, Types.TIME);
		this.registerType(LocalDateTime.class, Types.TIMESTAMP);
		this.registerType(Instant.class, Types.TIMESTAMP);
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
		Optional.ofNullable(entityClass.getSuperclass()).ifPresent(this::redefineClass);
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
		if (!this.persistenceConfig(object).isForUpdate()) {
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
	                          final List<CascadeType> cascadeTypes, final Long operateUser, final Integer operateCode)
			throws Exception {
		for (ReferenceDefine<?> referenceDefine : referenceDefineList) {
			if (Arrays.stream(referenceDefine.getCascadeTypes()).anyMatch(cascadeTypes::contains)) {
				Object referenceObject = ReflectionUtils.getFieldValue(referenceDefine.getFieldName(), object);
				if (referenceDefine.isReturnArray()) {
					for (Object reference : CollectionUtils.toList(referenceObject)) {
						if (reference instanceof BaseObject) {
							if (this.checkExist((BaseObject) reference)) {
								((BaseObject) reference).update(operateUser, operateCode);
							} else {
								((BaseObject) reference).save(operateUser);
							}
						}
					}
				} else if (referenceObject instanceof BaseObject) {
					if (this.checkExist((BaseObject) referenceObject)) {
						((BaseObject) referenceObject).update(operateUser, operateCode);
					} else {
						((BaseObject) referenceObject).save(operateUser);
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

	private int jdbcType(@Nonnull final Field field) {
		return Optional.ofNullable(field.getAnnotation(Temporal.class))
				.map(temporal -> {
					switch (field.getAnnotation(Temporal.class).value()) {
						case DATE:
							return Types.DATE;
						case TIME:
							return Types.TIME;
						case TIMESTAMP:
							return Types.TIMESTAMP;
						default:
							return this.dataConvertMapping.get(field.getType());
					}
				})
				.orElse(this.dataConvertMapping.get(field.getType()));
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
					int jdbcType = this.jdbcType(field);
					if (Date.class.equals(field.getType()) || Calendar.class.equals(field.getType())) {
						switch (jdbcType) {
							case Types.DATE:
								LOGGER.warn("Date_Type_Warning",
										"java.sql.Date", LocalDate.class.getName(), field.getType().getName());
								break;
							case Types.TIME:
								LOGGER.warn("Date_Type_Warning",
										"java.sql.Time", LocalTime.class.getName(), field.getType().getName());
								break;
							case Types.TIMESTAMP:
								LOGGER.warn("Date_Type_Warning",
										"java.sql.Timestamp", Instant.class.getName(), field.getType().getName());
								break;
						}
					}
					ColumnDefine columnDefine = new ColumnDefine();
					columnDefine.setColumnName(StringUtils.isEmpty(column.name()) ? field.getName() : column.name());
					columnDefine.setPrimaryKey(primaryKey);
					columnDefine.setUnique(column.unique());
					columnDefine.setJdbcType(jdbcType);
					columnDefine.setNullable(column.nullable());
					columnDefine.setLength(column.length());
					columnDefine.setPrecision(column.precision());
					columnDefine.setScale(column.scale());
					String defaultValue =
							Optional.ofNullable(ReflectionUtils.getFieldValue(field, object))
									.map(fieldValue ->
											dataSource.defaultValue(schemaName, columnDefine, fieldValue))
									.orElse(Globals.DEFAULT_VALUE_STRING);
					columnDefine.setDefaultValue(defaultValue);
					if (primaryKey) {
						columnDefine.setUpdatable(Boolean.FALSE);
					} else {
						columnDefine.setUpdatable(column.updatable());
					}
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
		if (CacheUtils.registered(MagiGlobals.CACHE_NAME)) {
			return Optional.ofNullable(CacheUtils.client(MagiGlobals.CACHE_NAME));
		}
		return Optional.empty();
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

		if (member.isAnnotationPresent(OneToMany.class)) {
			referenceType = ReferenceType.OneToMany;
			OneToMany oneToMany = member.getAnnotation(OneToMany.class);
			assert oneToMany != null;
			referenceClass = oneToMany.targetEntity();
			cascadeType = oneToMany.cascade();
		} else if (member.isAnnotationPresent(ManyToOne.class)) {
			referenceType = ReferenceType.ManyToOne;
			ManyToOne manyToOne = member.getAnnotation(ManyToOne.class);
			assert manyToOne != null;
			referenceClass = manyToOne.targetEntity();
			cascadeType = manyToOne.cascade();
		} else if (member.isAnnotationPresent(OneToOne.class)) {
			referenceType = ReferenceType.OneToOne;
			OneToOne oneToOne = member.getAnnotation(OneToOne.class);
			assert oneToOne != null;
			referenceClass = oneToOne.targetEntity();
			cascadeType = oneToOne.cascade();
		} else if (member.isAnnotationPresent(ManyToMany.class)) {
			referenceType = ReferenceType.ManyToMany;
			ManyToMany manyToMany = member.getAnnotation(ManyToMany.class);
			assert manyToMany != null;
			referenceClass = manyToMany.targetEntity();
			cascadeType = manyToMany.cascade();
		} else {
			referenceType = ReferenceType.Undefined;
			referenceClass = void.class;
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
		return new ReferenceDefine<>(referenceType, referenceClass, fieldName,
				returnArray, cascadeType, member.getAnnotationsByType(JoinColumn.class));
	}

	@Nonnull
	private List<String> identifyCodes(@Nonnull final AbstractQuery abstractQuery) {
		Set<String> identifyCodes = new HashSet<>();
		QueryFrom queryFrom = abstractQuery.getQueryFrom();
		if (queryFrom instanceof FromTable) {
			identifyCodes.add(((FromTable) queryFrom).getTableName());
		} else if (queryFrom instanceof FromSubQuery) {
			identifyCodes.addAll(this.identifyCodes(((FromSubQuery) queryFrom).getQueryData()));
		}
		List<QueryItem> itemList = new ArrayList<>();
		switch (abstractQuery.getQueryType()) {
			case NORMAL:
				itemList.addAll(((QueryInfo) abstractQuery).getItemList());
				break;
			case TABLE:
				itemList.addAll(((TableSubQuery) abstractQuery).getItemList());
				break;
			case SCALAR:
				itemList.add(((ScalarSubQuery) abstractQuery).getQueryItem());
				break;
		}
		itemList.forEach(queryItem -> identifyCodes.addAll(this.identifyCodes(queryItem)));
		abstractQuery.getQueryJoins().forEach(queryJoin ->
				identifyCodes.addAll(this.identifyCodes(queryJoin)));
		abstractQuery.getConditionList().forEach(condition ->
				identifyCodes.addAll(this.identifyCodes(condition)));
		abstractQuery.getHavingList().forEach(condition ->
				identifyCodes.addAll(this.identifyCodes(condition)));
		return new ArrayList<>(identifyCodes);
	}

	@Nonnull
	private List<String> identifyCodes(final QueryItem queryItem) {
		if (queryItem instanceof ColumnItem) {
			return List.of(((ColumnItem) queryItem).getTableName());
		} else if (queryItem instanceof FunctionItem) {
			Set<String> identifyCodes = new HashSet<>();
			((FunctionItem) queryItem).getFunctionParams()
					.forEach(abstractParameter ->
							identifyCodes.addAll(this.identifyCodes(abstractParameter)));
			return new ArrayList<>(identifyCodes);
		} else if (queryItem instanceof SubQueryItem) {
			return this.identifyCodes(((SubQueryItem) queryItem).getQueryData());
		}
		return Collections.emptyList();
	}

	@Nonnull
	private List<String> identifyCodes(final QueryJoin queryJoin) {
		if (queryJoin instanceof TableQueryJoin) {
			return List.of(((TableQueryJoin) queryJoin).getJoinTable());
		} else if (queryJoin instanceof SubQueryJoin) {
			return this.identifyCodes(((SubQueryJoin) queryJoin).getSubQuery());
		}
		return Collections.emptyList();
	}

	@Nonnull
	private List<String> identifyCodes(final AbstractParameter<?> functionParam) {
		Set<String> identifyCodes = new HashSet<>();
		if (functionParam instanceof CalculateParameter) {
			((CalculateParameter) functionParam).getItemValue()
					.getCalculateItems()
					.forEach(queryItem -> identifyCodes.addAll(this.identifyCodes(queryItem)));
		} else if (functionParam instanceof ColumnParameter) {
			identifyCodes.add(((ColumnParameter) functionParam).getItemValue().getTableName());
		} else if (functionParam instanceof FunctionParameter) {
			((FunctionParameter) functionParam).getItemValue().getFunctionParams()
					.forEach(abstractParameter ->
							identifyCodes.addAll(this.identifyCodes(abstractParameter)));
		} else if (functionParam instanceof QueryParameter) {
			identifyCodes.addAll(this.identifyCodes(((QueryParameter) functionParam).getItemValue()));
		}
		return new ArrayList<>(identifyCodes);
	}

	@Nonnull
	private List<String> identifyCodes(final Condition condition) {
		Set<String> identifyCodes = new HashSet<>();
		if (condition instanceof ColumnCondition) {
			return this.identifyCodes(((ColumnCondition) condition).getConditionParameter());
		} else if (condition instanceof GroupCondition) {
			((GroupCondition) condition).getConditionList()
					.forEach(itemCondition -> identifyCodes.addAll(this.identifyCodes(itemCondition)));
		}
		return new ArrayList<>(identifyCodes);
	}

	private static boolean lazyLoad(@Nonnull final Class<?> entityClass, @Nonnull final Field field) {
		if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) {
			return Boolean.FALSE;
		}
		FetchType fetchType = null;
		if (referenceMember(field)) {
			fetchType = referenceFetchType(field);
			if (fetchType == null) {
				fetchType = referenceFetchType(ReflectionUtils.getterMethod(field.getName(), entityClass));
			}
		} else if (field.isAnnotationPresent(Column.class) && field.isAnnotationPresent(Lob.class)) {
			fetchType = columnFetchType(field);
			if (fetchType == null) {
				fetchType = columnFetchType(ReflectionUtils.getterMethod(field.getName(), entityClass));
			}
		}
		return FetchType.LAZY.equals(fetchType);
	}

	private static FetchType referenceFetchType(final AccessibleObject member) {
		if (member.isAnnotationPresent(OneToMany.class)) {
			return member.getAnnotation(OneToMany.class).fetch();
		}
		if (member.isAnnotationPresent(ManyToOne.class)) {
			return member.getAnnotation(ManyToOne.class).fetch();
		}
		if (member.isAnnotationPresent(OneToOne.class)) {
			return member.getAnnotation(OneToOne.class).fetch();
		}
		if (member.isAnnotationPresent(ManyToMany.class)) {
			return member.getAnnotation(ManyToMany.class).fetch();
		}
		return null;
	}

	private static FetchType columnFetchType(final AccessibleObject member) {
		if (member.isAnnotationPresent(Basic.class)) {
			return member.getAnnotation(Basic.class).fetch();
		}
		return null;
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
		 * <span class="en-US">Lock option</span>
		 * <span class="zh-CN">数据锁选项</span>
		 */
		private final LockModeType lockOption;
		private final Map<String, Class<?>> fieldTypes = new HashMap<>();
		private String versionField = Globals.DEFAULT_VALUE_STRING;
		private final List<String> lazyLoadFields;

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

			Options options = entityClass.getAnnotation(Options.class);
			this.columnFieldMapping = new Hashtable<>();
			StrategyDefine databaseStrategy = null;
			StrategyDefine tableStrategy = null;
			if (options == null) {
				this.dropOption = DropOption.NONE;
				this.lockOption = LockModeType.NONE;
				this.cacheable = Boolean.TRUE;
			} else {
				this.dropOption = options.dropOption();
				databaseStrategy = this.strategyDefine(options.databaseSharding());
				tableStrategy = this.strategyDefine(options.tableSharding());
				this.lockOption = options.lockOption();
				this.cacheable = options.cacheable();
			}

			Table table = entityClass.getAnnotation(Table.class);
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
			this.lazyLoadFields = new ArrayList<>();

			this.fieldColumnMapping = new Hashtable<>();
			this.transferColumns = new ArrayList<>();
			this.sensitiveDefines = new ArrayList<>();
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
								this.registerColumn(columnDefine, field);
								this.fieldTypes.put(field.getName(), field.getType());
								if (EntityFactory.lazyLoad(entityClass, field)) {
									this.lazyLoadFields.add(field.getName());
								}
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
									this.fieldTypes.put(pkField.getName(), pkField.getType());
									this.registerColumn(columnDefine, pkField);
								});
					}
				} else if (EntityFactory.referenceMember(field)) {
					Optional.ofNullable(EntityFactory.referenceDefine(field))
							.ifPresent(referenceDefine -> {
								referenceDefineList.add(referenceDefine);
								if (EntityFactory.lazyLoad(entityClass, field)) {
									this.lazyLoadFields.add(field.getName());
								}
							});
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

			entityFactory.dataSource.initTable(this.tableDefine, databaseStrategy, tableStrategy);
		}

		private StrategyDefine strategyDefine(@Nonnull final Sharding sharding) {
			if (!Calculator.class.equals(sharding.calculatorClass()) && sharding.columns().length > 0
					&& Stream.of(sharding.columns()).allMatch(this.columnFieldMapping::containsKey)) {
				List<StrategyField> strategyFields = new ArrayList<>();
				int index = Globals.INITIALIZE_INT_VALUE;
				for (String column : sharding.columns()) {
					StrategyField strategyField = new StrategyField();
					strategyField.setSortCode(index);
					strategyField.setFieldName(column);
					strategyFields.add(strategyField);
					index++;
				}
				StrategyDefine strategyDefine = new StrategyDefine();
				strategyDefine.setDefaultValue(sharding.value());
				strategyDefine.setStrategyFields(strategyFields);
				strategyDefine.setCalculatorClass(sharding.calculatorClass().getName());
				return strategyDefine;
			}
			return null;
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
			if (field.isAnnotationPresent(Version.class) && Integer.class.equals(field.getType())) {
				this.versionField = field.getName();
			}

			Optional.ofNullable(field.getAnnotation(GeneratedData.class))
					.ifPresent(generatedData -> {
						columnDefine.setGenerationType(generatedData.type());
						columnDefine.setGeneratorName(generatedData.generator());
					});
			Optional.ofNullable(field.getAnnotation(HistoriesNames.class))
					.ifPresent(historiesNames ->
							columnDefine.setHistoriesNames(Arrays.asList(historiesNames.value())));
		}

		private boolean optimisticLock() {
			return LockModeType.OPTIMISTIC.equals(this.lockOption)
					|| LockModeType.OPTIMISTIC_FORCE_INCREMENT.equals(this.lockOption);
		}

		/**
		 * <h3 class="en-US">Data table unique identifier</h3>
		 * <h3 class="zh-CN">数据表唯一识别码</h3>
		 *
		 * @return <span class="en-US">Unique identifier</span>
		 * <span class="zh-CN">唯一识别代码</span>
		 */
		String identifier() {
			return StringUtils.base64Encode(SecurityUtils.SHA256(this.tableDefine.getTableName()));
		}

		/**
		 * <h3 class="en-US">Generate unique identifier</h3>
		 * <h3 class="zh-CN">生成唯一识别码</h3>
		 *
		 * @param object <span class="en-US">Entity classes instance object</span>
		 *               <span class="zh-CN">实体类实例对象</span>
		 * @return <span class="en-US">Unique identifier</span>
		 * <span class="zh-CN">唯一识别代码</span>
		 */
		String identifier(@Nonnull final BaseObject object) throws SQLException {
			return this.identifier(this.filterMap(object));
		}

		/**
		 * <h3 class="en-US">Generate unique identifier</h3>
		 * <h3 class="zh-CN">生成唯一识别码</h3>
		 *
		 * @param filterMap <span class="en-US">Data mapping table</span>
		 *                  <span class="zh-CN">数据映射表</span>
		 * @return <span class="en-US">Unique identifier</span>
		 * <span class="zh-CN">唯一识别代码</span>
		 */
		String identifier(final TreeMap<String, Object> filterMap) {
			return SecurityUtils.SHA256(filterMap, EncodeType.HEX);
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
		 * <h3 class="en-US">Get the JDBC type code of the data column in the registration data table</h3>
		 * <h3 class="zh-CN">获取注册数据表中数据列的JDBC类型代码</h3>
		 *
		 * @param identifyCode <span class="en-US">Data column identify code</span>
		 *                     <span class="zh-CN">数据列识别代码</span>
		 * @return <span class="en-US">JDBC type code</span>
		 * <span class="zh-CN">JDBC类型代码</span>
		 * @throws SQLException <span class="en-US">The data table is not registered or data column not exists</span>
		 *                      <span class="zh-CN">数据表未注册或数据列不存在</span>
		 */
		public int jdbcType(@Nonnull final String identifyCode) throws SQLException {
			return Optional.ofNullable(this.tableDefine.column(identifyCode))
					.map(ColumnDefine::getJdbcType)
					.orElseThrow(() -> new MultilingualSQLException(0x00DB00000011L));
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

		public String fieldName(final String identifyName) {
			return this.columnFieldMapping.getOrDefault(identifyName, identifyName);
		}

		public List<ColumnDefine> queryColumns() {
			return this.tableDefine.getColumnDefines()
					.stream()
					.filter(columnDefine ->
							!this.lazyLoadFields.contains(this.columnFieldMapping.get(columnDefine.getColumnName())))
					.collect(Collectors.toList());
		}

		public List<ReferenceDefine<?>> queryReferences() {
			return this.referenceDefineList
					.stream()
					.filter(referenceDefine ->
							!this.lazyLoadFields.contains(referenceDefine.getFieldName()))
					.collect(Collectors.toList());
		}

		/**
		 * <h3 class="en-US">Getter method for the data column transmission configuration information list</h3>
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
		 * <h3 class="en-US">Getter method for the lock option</h3>
		 * <h3 class="zh-CN">数据锁选项的Getter方法</h3>
		 *
		 * @return <span class="en-US">Lock option</span>
		 * <span class="zh-CN">数据锁选项</span>
		 */
		LockModeType getLockOption() {
			return this.lockOption;
		}

		/**
		 * <h3 class="en-US">The current data table contains lazy loading attributes</h3>
		 * <h3 class="zh-CN">当前数据表包含懒加载属性</h3>
		 *
		 * @return <span class="en-US">Check result</span>
		 * <span class="zh-CN">检查结果</span>
		 */
		boolean containsLazyLoadField() {
			return !this.lazyLoadFields.isEmpty();
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
		private void writeFieldValue(final Object object, final String fieldName, final Object value) {
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
		public Object readFieldValue(final Object object, final String identifyName) {
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
		 * @param object   <span class="en-US">Entity classes instance object</span>
		 *                 <span class="zh-CN">实体类实例对象</span>
		 * @param userCode <span class="en-US">Identify code of the reader</span>
		 *                 <span class="zh-CN">读取人的识别代码</span>
		 */
		private void sensitiveData(@Nonnull final Object object, @Nonnull final String userCode) {
			if (this.sensitiveDefines.isEmpty()) {
				return;
			}
			this.sensitiveDefines.forEach(sensitiveDefine -> sensitiveDefine.sensitiveData(object));
			if (INSTANCE.sensitiveTracker != null) {
				INSTANCE.sensitiveTracker.track(ClassUtils.originalClassName(object.getClass()),
						BeanUtils.objectToString(this.primaryKeyMap(object), StringType.JSON),
						userCode);
			}
		}

		/**
		 * <h3 class="en-US">Check weather the given field name is the primary key</h3>
		 * <h3 class="zh-CN">检查给定的属性名是否为主键</h3>
		 *
		 * @param fieldName <span class="en-US">Field name</span>
		 *                  <span class="zh-CN">属性名</span>
		 * @return <span class="en-US">Check result</span>
		 * <span class="zh-CN">检查结果</span>
		 */
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
		private void primaryKey(final Object object, final Map<String, Object> primaryKeyMap) {
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
			if (this.optimisticLock() && StringUtils.notBlank(this.versionField)) {
				if (ReflectionUtils.getFieldValue(this.versionField, object) == null) {
					ReflectionUtils.setField(this.versionField, object, Globals.INITIALIZE_INT_VALUE);
				}
			}
			this.tableDefine.getColumnDefines()
					.forEach(columnDefine ->
							Optional.ofNullable(this.readFieldValue(object, columnDefine.getColumnName()))
									.map(fieldValue -> this.convertValue(columnDefine, fieldValue))
									.ifPresent(fieldValue ->
											dataMap.put(columnDefine.getColumnName(), fieldValue)));
			return dataMap;
		}

		/**
		 * <h3 class="en-US">Get the update data mapping table</h3>
		 * <h3 class="zh-CN">获取更新数据映射表</h3>
		 *
		 * @param object <span class="en-US">Instance object that reads attribute data</span>
		 *               <span class="zh-CN">读取属性数据的实例对象</span>
		 * @return <span class="en-US">Converted data mapping table</span>
		 * <span class="zh-CN">转换后的数据映射表</span>
		 */
		private Map<String, Object> updateMap(@Nonnull final BaseObject object) throws SQLException {
			Map<String, Object> retrieveMap = new HashMap<>();
			this.tableDefine.getColumnDefines()
					.stream()
					.filter(ColumnDefine::isUpdatable)
					.forEach(columnDefine ->
							Optional.ofNullable(this.readFieldValue(object, columnDefine.getColumnName()))
									.map(fieldValue -> this.convertValue(columnDefine, fieldValue))
									.ifPresent(fieldValue -> retrieveMap.put(columnDefine.getColumnName(), fieldValue)));
			if (this.optimisticLock() && StringUtils.notBlank(this.versionField)) {
				ColumnDefine columnDefine = this.tableDefine.column(this.versionField);
				Object fieldValue = this.readFieldValue(object, this.versionField);
				if (fieldValue == null) {
					throw new MultilingualSQLException(0x00DB00010021L);
				}
				fieldValue = ((Integer) fieldValue) + 1;
				retrieveMap.put(columnDefine.getColumnName(), this.convertValue(columnDefine, fieldValue));
				ReflectionUtils.setField(this.versionField, object, fieldValue);
			}
			return retrieveMap;
		}

		/**
		 * <h3 class="en-US">Convert Java data types to SQL data types</h3>
		 * <h3 class="zh-CN">转换Java数据类型为SQL数据类型</h3>
		 *
		 * @param columnDefine <span class="en-US">Data column configure information</span>
		 *                     <span class="zh-CN">数据列配置信息</span>
		 * @param fieldValue   <span class="en-US">Data that needs to be converted</span>
		 *                     <span class="zh-CN">需要转换的数据</span>
		 * @return <span class="en-US">Converted data</span>
		 * <span class="zh-CN">转换后的数据</span>
		 */
		private Object convertValue(final ColumnDefine columnDefine, @Nonnull final Object fieldValue) {
			switch (columnDefine.getJdbcType()) {
				case Types.DATE:
					if (fieldValue instanceof LocalDate) {
						return java.sql.Date.valueOf((LocalDate) fieldValue);
					} else {
						return new java.sql.Date(((Date) fieldValue).getTime());
					}
				case Types.TIME:
					if (fieldValue instanceof LocalTime) {
						return java.sql.Time.valueOf((LocalTime) fieldValue);
					} else {
						return new java.sql.Time(((Date) fieldValue).getTime());
					}
				case Types.TIMESTAMP:
					if (fieldValue instanceof LocalDateTime) {
						return java.sql.Timestamp.valueOf((LocalDateTime) fieldValue);
					} else if (fieldValue instanceof Calendar) {
						return new java.sql.Timestamp(((Calendar) fieldValue).getTimeInMillis());
					} else if (fieldValue instanceof Instant) {
						return new java.sql.Timestamp(((Instant) fieldValue).toEpochMilli());
					} else {
						return new java.sql.Timestamp(((Date) fieldValue).getTime());
					}
				case Types.DECIMAL:
					return ((BigDecimal) fieldValue).setScale(columnDefine.getScale(), RoundingMode.HALF_UP);
				case Types.BLOB:
					return ConvertUtils.toByteArray(fieldValue);
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

		private TreeMap<String, Object> primaryKeyMap(@Nonnull final Object object) {
			TreeMap<String, Object> retrieveMap = new TreeMap<>();
			if (object instanceof CompositeId) {
				this.tableDefine.getColumnDefines()
						.stream()
						.filter(ColumnDefine::isPrimaryKey)
						.forEach(columnDefine -> {
							String fieldName = this.columnFieldMapping.getOrDefault(columnDefine.getColumnName(), columnDefine.getColumnName());
							Object fieldValue = ReflectionUtils.getFieldValue(fieldName, object);
							if (fieldValue != null) {
								retrieveMap.put(columnDefine.getColumnName(), this.convertValue(columnDefine, fieldValue));
							}
						});
			} else {
				this.tableDefine.getColumnDefines()
						.stream()
						.filter(ColumnDefine::isPrimaryKey)
						.findFirst()
						.ifPresent(columnDefine ->
								Optional.ofNullable(this.convertValue(columnDefine, object))
										.ifPresent(fieldValue ->
												retrieveMap.put(columnDefine.getColumnName(), fieldValue)));
			}
			return retrieveMap;
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
		private TreeMap<String, Object> filterMap(@Nonnull final BaseObject object) throws SQLException {
			TreeMap<String, Object> retrieveMap = new TreeMap<>();
			this.tableDefine.getColumnDefines()
					.stream()
					.filter(ColumnDefine::isPrimaryKey)
					.forEach(columnDefine ->
							Optional.ofNullable(this.readFieldValue(object, columnDefine.getColumnName()))
									.map(fieldValue -> this.convertValue(columnDefine, fieldValue))
									.ifPresent(fieldValue ->
											retrieveMap.put(columnDefine.getColumnName(), fieldValue)));
			if (this.optimisticLock() && !(object instanceof CompositeId) && StringUtils.notBlank(this.versionField)) {
				ColumnDefine columnDefine = this.tableDefine.column(this.versionField);
				Object fieldValue = Optional.ofNullable(this.readFieldValue(object, this.versionField))
						.map(value -> this.convertValue(columnDefine, value))
						.orElseThrow(() -> new MultilingualSQLException(0x00DB00010021L));
				retrieveMap.put(columnDefine.getColumnName(), fieldValue);
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
					.forEach(columnDefine -> {
						String fieldName = this.columnFieldMapping.get(columnDefine.getColumnName());
						Object fieldValue = null;
						Class<?> fieldType = this.fieldTypes.get(fieldName);
						switch (columnDefine.getGenerationType()) {
							case GENERATE:
								fieldValue = IDUtils.generate(columnDefine.getGeneratorName(), new byte[0]);
								if (fieldValue instanceof UUID || fieldValue instanceof ULID
										|| fieldValue instanceof CUID) {
									fieldValue = fieldValue.toString();
								}
								break;
							case CURRENT_DATE:
								if (LocalDate.class.equals(fieldType)) {
									fieldValue = LocalDate.now();
								} else {
									fieldValue = new Date();
								}
								break;
							case CURRENT_TIME:
								if (LocalTime.class.equals(fieldType)) {
									fieldValue = LocalTime.now();
								} else {
									fieldValue = new Date();
								}
								break;
							case CURRENT_TIMESTAMP:
								if (LocalDateTime.class.equals(fieldType)) {
									fieldValue = LocalDateTime.now();
								} else if (Calendar.class.equals(fieldType)) {
									fieldValue = Calendar.getInstance();
								} else if (Instant.class.equals(fieldType)) {
									fieldValue = Instant.now();
								} else {
									fieldValue = new Date();
								}
								break;
						}
						if (fieldValue != null) {
							ReflectionUtils.setField(fieldName, object, fieldValue);
						}
					});
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

			Optional.ofNullable(ReflectionUtils.getFieldValue(referenceDefine.getFieldName(), object, Boolean.FALSE))
					.filter(reference -> reference instanceof BaseObject)
					.filter(reference -> !entityFactory.checkExist((BaseObject) reference))
					.ifPresent(reference -> {
						referenceTable.generateKey((BaseObject) reference, entityFactory);
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
									Optional.ofNullable(referenceTable.readFieldValue(reference, joinDefine.getReferenceField()))
											.ifPresent(fieldValue ->
													this.writeFieldValue(object, joinDefine.getCurrentField(),
															fieldValue));
								}
								break;
						}
					});
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
		public void copyData(final Map<String, Object> dataMap, final Object object) {
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
					Object fieldValue = entry.getValue();
					if (fieldValue == null) {
						continue;
					}
					Class<?> fieldType = this.fieldTypes.get(fieldName);
					if (fieldType != null) {
						if (!fieldType.equals(fieldValue.getClass())) {
							switch (columnDefine.getJdbcType()) {
								case Types.DATE:
									if (Date.class.equals(fieldType)) {
										fieldValue = new Date(((java.sql.Date) fieldValue).getTime());
									} else if (LocalDate.class.equals(fieldType)) {
										fieldValue = ((java.sql.Date) fieldValue).toLocalDate();
									}
									break;
								case Types.TIME:
									if (Date.class.equals(fieldType)) {
										fieldValue = new Date(((java.sql.Time) fieldValue).getTime());
									} else if (LocalTime.class.equals(fieldType)) {
										fieldValue = ((java.sql.Time) fieldValue).toLocalTime();
									}
									break;
								case Types.TIMESTAMP:
									if (Date.class.equals(fieldType)) {
										fieldValue = new Date(((java.sql.Timestamp) fieldValue).getTime());
									} else if (Instant.class.equals(fieldType)) {
										fieldValue = ((java.sql.Timestamp) fieldValue).toInstant();
									} else if (LocalDateTime.class.equals(fieldType)) {
										fieldValue = ((java.sql.Timestamp) fieldValue).toLocalDateTime();
									} else if (Calendar.class.equals(fieldType)) {
										Calendar calendar = Calendar.getInstance();
										calendar.setTimeInMillis(((java.sql.Timestamp) fieldValue).getTime());
										fieldValue = calendar;
									}
									break;
							}
						}
						if (columnDefine.isPrimaryKey()) {
							ReflectionUtils.setField(fieldName, primaryKey, fieldValue);
						} else {
							ReflectionUtils.setField(fieldName, object, fieldValue);
						}
					}
				}
			}
			if (this.primaryKeyConfig.isCompositeId()) {
				ReflectionUtils.setField(this.primaryKeyConfig.getFieldName(), object, primaryKey);
			}
		}
	}
}
