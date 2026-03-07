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
package org.nervousync.magi.data;

import jakarta.annotation.Nonnull;
import jakarta.persistence.LockModeType;
import org.nervousync.annotations.provider.Provider;
import org.nervousync.brain.commons.BrainCommons;
import org.nervousync.brain.configs.transactional.TransactionalConfig;
import org.nervousync.brain.enumerations.transactional.Propagation;
import org.nervousync.brain.exceptions.data.DropException;
import org.nervousync.brain.exceptions.data.InsertException;
import org.nervousync.brain.exceptions.data.UpdateException;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.brain.source.BrainDataSource;
import org.nervousync.brain.transactional.TransactionalProxy;
import org.nervousync.commons.Globals;
import org.nervousync.enumerations.beans.StringType;
import org.nervousync.enumerations.security.EncodeType;
import org.nervousync.exceptions.utils.DataInvalidException;
import org.nervousync.io.StandardFile;
import org.nervousync.magi.config.storage.StorageConfig;
import org.nervousync.magi.data.provider.TaskProvider;
import org.nervousync.magi.data.task.AbstractTask;
import org.nervousync.magi.data.task.impl.ExportTask;
import org.nervousync.magi.data.task.impl.ImportTask;
import org.nervousync.magi.data.transfer.TransferColumn;
import org.nervousync.magi.exceptions.data.DataParseException;
import org.nervousync.office.excel.ExcelWriter;
import org.nervousync.office.excel.SheetWriter;
import org.nervousync.utils.core.*;
import org.nervousync.utils.i18n.MultilingualUtils;
import org.nervousync.utils.id.IDUtils;
import org.nervousync.utils.logger.LoggerUtils;
import org.nervousync.utils.office.OfficeUtils;
import org.nervousync.utils.security.SecurityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h2 class="en-US">Data import/export utilities</h2>
 * <h2 class="zh-CN">数据导入导出工具</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Oct 26, 2023 15:04:37 $
 */
@SuppressWarnings("unused")
public final class DataUtils {

    /**
     * <span class="en-US">The number of threads executed simultaneously by the default data import and export task</span>
     * <span class="zh-CN">默认数据导入导出任务同时执行的线程数</span>
     */
    public static final int DEFAULT_PROCESS_THREAD_LIMIT = 20;
    /**
     * <span class="en-US">The default expiration time after the data import and export task is completed</span>
     * <span class="zh-CN">默认数据导入导出任务完成后的过期时间</span>
     */
    public static final long DEFAULT_STORAGE_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;
    /**
     * <span class="en-US">Data import and export task status: Create</span>
     * <span class="zh-CN">数据导入导出任务状态：创建</span>
     */
    public static final int DATA_TASK_STATUS_CREATE = 0;
    /**
     * <span class="en-US">Data import and export task status: Processing</span>
     * <span class="zh-CN">数据导入导出任务状态：处理中</span>
     */
    public static final int DATA_TASK_STATUS_PROCESS = 1;
    /**
     * <span class="en-US">Data import and export task status: Finished</span>
     * <span class="zh-CN">数据导入导出任务状态：已完成</span>
     */
    public static final int DATA_TASK_STATUS_FINISH = 2;
	/**
	 * <span class="en-US">Default temporary directory address</span>
	 * <span class="zh-CN">默认的临时目录地址</span>
	 */
	public static final String DEFAULT_TMP_PATH = SystemUtils.JAVA_TMP_DIR + Globals.DEFAULT_PAGE_SEPARATOR + "magi";
    /**
     * <span class="en-US">Data file extension</span>
     * <span class="zh-CN">数据文件的扩展名</span>
     */
    public static final String DATA_FILE_EXTENSION_NAME = ".dat";
    /**
     * <span class="en-US">Data file extension</span>
     * <span class="zh-CN">数据文件的扩展名</span>
     */
    public static final String DATA_TMP_FILE_EXTENSION_NAME = ".tmp";

    /**
     * <span class="en-US">Logger instance</span>
     * <span class="zh-CN">日志实例</span>
     */
    private static final LoggerUtils.Logger LOGGER = LoggerUtils.getLogger(DataUtils.class);
    private static final int TYPE_LENGTH = 64;
    /**
     * <span class="en-US">Registered task information adapter identify code and implementation class mapping table</span>
     * <span class="zh-CN">注册的任务信息适配器识别代码和实现类映射表</span>
     */
    private static final Hashtable<String, Class<?>> REGISTERED_TASK_PROVIDERS = new Hashtable<>();
    /**
     * <span class="en-US">Registered data transfer configure information</span>
     * <span class="zh-CN">注册的数据传输配置信息</span>
     */
    private static final Hashtable<String, List<TransferColumn>> REGISTERED_TRANSFER_CONFIGS = new Hashtable<>();
    /**
     * <span class="en-US">Last modified timestamp</span>
     * <span class="zh-CN">最后修改时间戳</span>
     */
    private final long lastModified;
    /**
     * <span class="en-US">Task storage provider instance</span>
     * <span class="zh-CN">任务存储适配器实例对象</span>
     */
    private final TaskProvider taskProvider;
    /**
     * <span class="en-US">The base path for system execution</span>
     * <span class="zh-CN">系统执行的基础路径</span>
     */
    private final String basePath;
    /**
     * <span class="en-US">Current node identify code, generate by system.</span>
     * <span class="zh-CN">当前节点的唯一识别代码，系统自动生成</span>
     */
    private final String identifyCode;
    /**
     * <span class="en-US">The maximum number of threads allowed to perform processing tasks</span>
     * <span class="zh-CN">允许执行处理任务的最大线程数</span>
     */
    private int threadLimit;

    /**
     * <span class="en-US">Start the scheduler execution status of the import and export task</span>
     * <span class="zh-CN">启动导入导出任务的调度程序执行状态</span>
     */
    private boolean scheduleRunning = Boolean.FALSE;
    /**
     * <span class="en-US">Remove scheduler execution status of completed tasks</span>
     * <span class="zh-CN">删除已完成任务的调度程序执行状态</span>
     */
    private boolean removeRunning = Boolean.FALSE;
    /**
     * <span class="en-US">
     * Automatically delete the currently delayed task information.
     * When the value is <code>-1</code>, the deletion operation will not be performed.
     * </span>
     * <span class="zh-CN">自动删除当前延时的任务信息，值为<code>-1</code>时不执行删除操作</span>
     */
    private long expireTime;
    /**
     * <span class="en-US">Remove scheduler execution status of completed tasks</span>
     * <span class="zh-CN">定时任务调度器服务</span>
     */
    private ScheduledExecutorService scheduledExecutorService;
    /**
     * <span class="en-US">List of processing threads being executed</span>
     * <span class="zh-CN">正在执行的处理线程列表</span>
     */
    private final List<ProcessThread> runningThreads;

    static {
        ServiceLoader.load(TaskProvider.class)
                .forEach(taskProvider ->
                        Optional.ofNullable(taskProvider.getClass().getAnnotation(Provider.class))
                                .ifPresent(provider -> REGISTERED_TASK_PROVIDERS.put(provider.name(),
                                        taskProvider.getClass())));
    }

    /**
     * <h3 class="en-US">Private constructor</h3>
     * <h3 class="zh-CN">私有的构造方法</h3>
     *
     * @param storageConfig <span class="en-US">Data import/export configure information</span>
     *                      <span class="zh-CN">数据导入导出配置</span>
     */
    private DataUtils(@Nonnull final StorageConfig storageConfig) {
        this.basePath = StringUtils.isEmpty(storageConfig.getBasePath()) ? DEFAULT_TMP_PATH : storageConfig.getBasePath();
        FileUtils.makeDir(this.basePath);
        if (registeredProvider(storageConfig.getStorageProvider())) {
            this.taskProvider =
                    Optional.ofNullable(REGISTERED_TASK_PROVIDERS.get(storageConfig.getStorageProvider()))
                            .map(providerClass -> (TaskProvider) ObjectUtils.newInstance(providerClass))
                            .orElse(new MemoryTaskProviderImpl());
        } else {
            this.taskProvider = new MemoryTaskProviderImpl();
        }
        this.taskProvider.initialize(this.basePath);
        this.threadLimit = (storageConfig.getThreadLimit() <= Globals.INITIALIZE_INT_VALUE)
                ? DEFAULT_PROCESS_THREAD_LIMIT
                : storageConfig.getThreadLimit();
        this.expireTime = (storageConfig.getExpireTime() < Globals.DEFAULT_VALUE_LONG)
                ? DEFAULT_STORAGE_EXPIRE_TIME
                : storageConfig.getExpireTime();
        this.identifyCode = DataUtils.identifyCode(this.basePath);
        this.runningThreads = new ArrayList<>();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
        this.scheduledExecutorService.scheduleAtFixedRate(this::scheduleTask,
                0L, 1000L, TimeUnit.MILLISECONDS);
        this.scheduledExecutorService.scheduleAtFixedRate(this::removeTask,
                0L, 1000L, TimeUnit.MILLISECONDS);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Data_Utils_Config", this.threadLimit, this.expireTime);
        }
        this.lastModified = storageConfig.getLastModified();
    }

    /**
     * <h3 class="en-US">Static method is used to initialize data import and export tools</h3>
     * <h3 class="zh-CN">静态方法用于初始化数据导入导出工具</h3>
     *
     * @param storageConfig <span class="en-US">Data import/export configure information</span>
     *                      <span class="zh-CN">数据导入导出配置</span>
     */
    public static void initialize(@Nonnull final StorageConfig storageConfig) {
        DataUtilsHolder.initialize(storageConfig);
    }

    /**
     * <h3 class="en-US">Destroy the current instance</h3>
     * <h3 class="zh-CN">销毁当前实例</h3>
     */
    public static void destroy() {
        DataUtilsHolder.destroy();
    }

    /**
     * <h3 class="en-US">Get a list of registered task storage adapter identification codes</h3>
     * <h3 class="zh-CN">获取已注册的任务存储适配器识别代码列表</h3>
     *
     * @return <span class="en-US">Task storage adapter identification codes</span>
     * <span class="zh-CN">任务存储适配器识别代码列表</span>
     */
    public static List<String> registeredProviders() {
        return new ArrayList<>(REGISTERED_TASK_PROVIDERS.keySet());
    }

    /**
     * <h3 class="en-US">Get the given task storage adapter name</h3>
     * <h3 class="zh-CN">获取给定的任务存储适配器名称</h3>
     *
     * @param providerName <span class="en-US">Task store provider name</span>
     *                     <span class="zh-CN">任务存储适配器名称</span>
     * @param languageCode <span class="en-US">Language code</span>
     *                     <span class="zh-CN">语言代码</span>
     * @return <span class="en-US">Provider name</span>
     * <span class="zh-CN">适配器名称</span>
     */
    public static String providerName(final String providerName, final String languageCode) {
        if (StringUtils.isEmpty(providerName)) {
            return Globals.DEFAULT_VALUE_STRING;
        }
        return Optional.ofNullable(REGISTERED_TASK_PROVIDERS.get(providerName))
                .map(providerClass -> MultilingualUtils.providerName(providerClass, languageCode))
                .orElse(Globals.DEFAULT_VALUE_STRING);
    }

    /**
     * <h3 class="en-US">Checks whether the given task store provider identification code is registered</h3>
     * <h3 class="zh-CN">检查给定的任务存储适配器识别代码是否注册</h3>
     *
     * @param providerName <span class="en-US">Task store provider name</span>
     *                     <span class="zh-CN">任务存储适配器名称</span>
     * @return <span class="en-US">Check result</span>
     * <span class="zh-CN">检查结果</span>
     */
    public static boolean registeredProvider(final String providerName) {
        if (StringUtils.isEmpty(providerName)) {
            return Boolean.FALSE;
        }
        return REGISTERED_TASK_PROVIDERS.containsKey(providerName);
    }

    /**
     * <h3 class="en-US">Get the singleton instance object of the data import and export tool</h3>
     * <h3 class="zh-CN">获取数据导入导出工具的单例实例对象</h3>
     *
     * @return <span class="en-US">The singleton instance object of the data import and export tool</span>
     * <span class="zh-CN">导入导出工具的单例实例对象</span>
     */
    public static DataUtils getInstance() {
        return DataUtilsHolder.INSTANCE;
    }

    /**
     * <h3 class="en-US">Checks whether the given modification time is consistent with the current modification time</h3>
     * <h3 class="zh-CN">检查给定的修改时间与当前的修改时间是否一致</h3>
     *
     * @param lastModified <span class="en-US">Last modified timestamp</span>
     *                     <span class="zh-CN">最后修改时间戳</span>
     * @return <span class="en-US">Check result</span>
     * <span class="zh-CN">检查结果</span>
     */
    private boolean match(final long lastModified) {
        return lastModified != Globals.DEFAULT_VALUE_LONG && this.lastModified == lastModified;
    }

    /**
     * <h3 class="en-US">Add task information</h3>
     * <h3 class="zh-CN">添加任务信息</h3>
     *
     * @param inputStream <span class="en-US">data input stream</span>
     *                    <span class="zh-CN">数据输入流</span>
     * @param userCode    <span class="en-US">User identification code</span>
     *                    <span class="zh-CN">用户识别代码</span>
     * @return <span class="en-US">Task unique identification code</span>
     * <span class="zh-CN">任务唯一识别代码</span>
     */
    public long addTask(final InputStream inputStream, final Long userCode) {
        return this.addTask(inputStream, userCode, Boolean.FALSE, Globals.DEFAULT_VALUE_INT);
    }

    /**
     * <h3 class="en-US">Add task information</h3>
     * <h3 class="zh-CN">添加任务信息</h3>
     *
     * @param inputStream   <span class="en-US">data input stream</span>
     *                      <span class="zh-CN">数据输入流</span>
     * @param userCode      <span class="en-US">User identification code</span>
     *                      <span class="zh-CN">用户识别代码</span>
     * @param transactional <span class="en-US">Import tasks using transactions</span>
     *                      <span class="zh-CN">导入任务使用事务</span>
     * @param timeout       <span class="en-US">Transaction timeout</span>
     *                      <span class="zh-CN">事务超时时间</span>
     * @return <span class="en-US">Task unique identification code</span>
     * <span class="zh-CN">任务唯一识别代码</span>
     */
    public long addTask(final InputStream inputStream, final Long userCode,
                        final boolean transactional, final int timeout) {
        if (inputStream == null) {
            return Globals.DEFAULT_VALUE_LONG;
        }

        Long generateCode = IDUtils.snowflake();
        return Optional.ofNullable(generateCode)
                .filter(taskCode -> !ObjectUtils.nullSafeEquals(taskCode, Globals.DEFAULT_VALUE_LONG))
                .map(taskCode -> this.saveData(taskCode, inputStream))
                .map(dataPath -> {
                    ImportTask taskInfo = new ImportTask();
                    taskInfo.setTaskCode(generateCode);
                    taskInfo.setCreateTime(DateTimeUtils.currentUTCTimeMillis());
                    taskInfo.setDataPath(dataPath);
                    taskInfo.setUserCode(userCode);
                    taskInfo.setTransactional(transactional);
                    taskInfo.setTimeout(timeout);
                    return this.taskProvider.addTask(taskInfo) ? generateCode : Globals.DEFAULT_VALUE_LONG;
                })
                .orElse(Globals.DEFAULT_VALUE_LONG);
    }

    /**
     * <h3 class="en-US">Add task information</h3>
     * <h3 class="zh-CN">添加任务信息</h3>
     *
     * @param userCode   <span class="en-US">User identification code</span>
     *                   <span class="zh-CN">用户识别代码</span>
     * @param queryInfos <span class="en-US">Data query information array</span>
     *                   <span class="zh-CN">数据查询信息数组</span>
     * @return <span class="en-US">Task unique identification code</span>
     * <span class="zh-CN">任务唯一识别代码</span>
     */
    public long addTask(final Long userCode, final QueryInfo... queryInfos) {
        Long generateCode = IDUtils.snowflake();
        return Optional.ofNullable(generateCode)
                .filter(taskCode -> !ObjectUtils.nullSafeEquals(taskCode, Globals.DEFAULT_VALUE_LONG))
                .map(dataPath -> {
                    ExportTask taskInfo = new ExportTask();
                    taskInfo.setTaskCode(generateCode);
                    taskInfo.setCreateTime(DateTimeUtils.currentUTCTimeMillis());
                    taskInfo.setQueryInfoList(Arrays.asList(queryInfos));
                    taskInfo.setUserCode(userCode);
                    return this.taskProvider.addTask(taskInfo) ? generateCode : Globals.DEFAULT_VALUE_LONG;
                })
                .orElse(Globals.DEFAULT_VALUE_LONG);
    }

    /**
     * <h3 class="en-US">Update configure information</h3>
     * <h3 class="zh-CN">更新配置信息</h3>
     *
     * @param threadLimit <span class="en-US">Number of tasks allowed to be executed simultaneously</span>
     *                    <span class="zh-CN">允许同时执行的任务数</span>
     * @param expireTime  <span class="en-US">Expiration time for automatic deletion of completed tasks</span>
     *                    <span class="zh-CN">已完成任务自动删除的过期时间</span>
     */
    public void config(final int threadLimit, final long expireTime) {
        if (threadLimit > 0) {
            this.threadLimit = threadLimit;
        }
        this.expireTime = (expireTime < Globals.DEFAULT_VALUE_LONG)
                ? DEFAULT_STORAGE_EXPIRE_TIME
                : expireTime;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Data_Utils_Config", this.threadLimit, this.expireTime);
        }
    }

    /**
     * <h3 class="en-US">Drop task information based on the given task identification code and user identification code</h3>
     * <h3 class="zh-CN">根据给定的用户代码和任务识别代码删除任务信息</h3>
     *
     * @param userCode <span class="en-US">User identification code</span>
     *                 <span class="zh-CN">用户识别代码</span>
     * @param taskCode <span class="en-US">task identification code</span>
     *                 <span class="zh-CN">任务识别代码</span>
     * @return <span class="en-US">Process result</span>
     * <span class="zh-CN">执行结果</span>
     */
    public boolean dropTask(final Long userCode, final Long taskCode) {
        return this.taskProvider.dropTask(userCode, taskCode);
    }

    /**
     * <h3 class="en-US">Get the task information list based on the given user code and paging information</h3>
     * <h3 class="zh-CN">根据给定的用户代码和分页信息，获取任务信息列表</h3>
     *
     * @param userCode  <span class="en-US">User identification code</span>
     *                  <span class="zh-CN">用户识别代码</span>
     * @param pageNo    <span class="en-US">Current page number</span>
     *                  <span class="zh-CN">当前页数</span>
     * @param limitSize <span class="en-US">Maximum number of records per page</span>
     *                  <span class="zh-CN">每页的最大记录条数</span>
     * @return <span class="en-US">Task details list</span>
     * <span class="zh-CN">任务详细信息列表</span>
     */
    public List<AbstractTask> taskList(final Long userCode, final Integer pageNo, final Integer limitSize) {
        return this.taskProvider.taskList(userCode, pageNo, limitSize);
    }

    /**
     * <h3 class="en-US">Read task information based on the given task identification code and user identification code</h3>
     * <h3 class="zh-CN">根据给定的用户代码和任务识别代码读取任务信息</h3>
     *
     * @param userCode <span class="en-US">User identification code</span>
     *                 <span class="zh-CN">用户识别代码</span>
     * @param taskCode <span class="en-US">task identification code</span>
     *                 <span class="zh-CN">任务识别代码</span>
     * @return <span class="en-US">Task details</span>
     * <span class="zh-CN">任务详细信息</span>
     */
    public AbstractTask taskInfo(final Long userCode, final Long taskCode) {
        return this.taskProvider.taskInfo(userCode, taskCode);
    }

    /**
     * <h3 class="en-US">Initialize the data generator instance object</h3>
     * <h3 class="zh-CN">初始化数据生成器实例对象</h3>
     *
     * @param dataPath <span class="en-US">Data storage path</span>
     *                 <span class="zh-CN">数据保存地址</span>
     * @return <span class="en-US">Data generator instance object</span>
     * <span class="zh-CN">数据生成器实例对象</span>
     * @throws IOException <span class="en-US">Error creating data file</span>
     *                     <span class="zh-CN">创建数据文件出错</span>
     */
    public static DataGenerator newGenerator(final String dataPath) throws IOException {
        return new DataGenerator(dataPath);
    }

    /**
     * <h3 class="en-US">Initialize the data exporter instance object</h3>
     * <h3 class="zh-CN">初始化数据导出器实例对象</h3>
     *
     * @param dataPath <span class="en-US">Data storage path</span>
     *                 <span class="zh-CN">数据保存地址</span>
     * @return <span class="en-US">Data exporter instance object</span>
     * <span class="zh-CN">数据导出器实例对象</span>
     * @throws DataInvalidException <span class="en-US">File format error</span>
     *                              <span class="zh-CN">文件格式错误</span>
     */
    public static DataExporter newExporter(final String dataPath) throws DataInvalidException {
        return new DataExporter(dataPath);
    }

    /**
     * <h3 class="en-US">Close current instance</h3>
     * <h3 class="zh-CN">关闭当前实例</h3>
     */
    public void close() {
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdown();
            this.scheduledExecutorService = null;
        }
        this.taskProvider.destroy();
    }

    /**
     * <h3 class="en-US">Registration data table import and export data conversion configuration information</h3>
     * <h3 class="zh-CN">注册数据表导入导出数据转换配置信息</h3>
     *
     * @param tableName       <span class="en-US">Data table name</span>
     *                        <span class="zh-CN">数据表名称</span>
     * @param transferColumns <span class="en-US">Data conversion configuration information list</span>
     *                        <span class="zh-CN">数据转换配置信息列表</span>
     */
    public static void register(final String tableName, final List<TransferColumn> transferColumns) {
        if (StringUtils.isEmpty(tableName) || transferColumns == null || transferColumns.isEmpty()) {
            return;
        }
        String identifyCode = BrainCommons.identifyCode(tableName);
        if (REGISTERED_TRANSFER_CONFIGS.containsKey(identifyCode)) {
            LOGGER.warn("");
        }
        transferColumns.sort((o1, o2) -> {
            if (o1.getColumnIndex() == o2.getColumnIndex()) {
                return o1.getColumnName().compareTo(o2.getColumnName());
            }
            return Integer.compare(o1.getColumnIndex(), o2.getColumnIndex());
        });
        REGISTERED_TRANSFER_CONFIGS.put(identifyCode, transferColumns);
    }

    /**
     * <h3 class="en-US">Generate a unique identification code for the current node</h3>
     * <h3 class="zh-CN">生成当前节点的唯一识别代码</h3>
     *
     * @param basePath <span class="en-US">The base path for system execution</span>
     *                 <span class="zh-CN">系统执行的基础路径</span>
     * @return <span class="en-US">Generated unique identification code</span>
     * <span class="zh-CN">生成的唯一识别代码</span>
     */
    private static String identifyCode(final String basePath) {
        TreeMap<String, String> identifyMap = new TreeMap<>();
        identifyMap.put("IdentifyKey", SystemUtils.identifiedKey());
        identifyMap.put("BasePath", basePath);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Identify_Information_Data",
                    BeanUtils.objectToString(identifyMap, StringType.JSON));
        }
        return SecurityUtils.SHA256(identifyMap, EncodeType.HEX);
    }

    /**
     * <h3 class="en-US">Complete the thread task and save the task processing results to the task list</h3>
     * <h3 class="zh-CN">完成线程任务，并将任务处理结果保存到任务列表中</h3>
     *
     * @param processThread <span class="en-US">Task processing thread instance object</span>
     *                      <span class="zh-CN">任务处理线程实例对象</span>
     */
    private void finishTask(final ProcessThread processThread) {
        synchronized (this.runningThreads) {
            this.runningThreads.remove(processThread);
        }
        this.taskProvider.finishTask(processThread.getTaskCode(), processThread.isHasError(),
                processThread.errorMessage());
    }

    /**
     * <h3 class="en-US">Save the data in the input stream to a temporary directory to wait for processing</h3>
     * <h3 class="zh-CN">将输入流中的数据保存到临时目录中等待处理</h3>
     *
     * @param taskCode    <span class="en-US">task identification code</span>
     *                    <span class="zh-CN">任务识别代码</span>
     * @param inputStream <span class="en-US">data input stream</span>
     *                    <span class="zh-CN">数据输入流</span>
     * @return <span class="en-US">data file location</span>
     * <span class="zh-CN">数据文件位置</span>
     */
    private String saveData(@Nonnull final Long taskCode, final InputStream inputStream) {
        if (ObjectUtils.nullSafeEquals(taskCode, Globals.DEFAULT_VALUE_LONG)) {
            return Globals.DEFAULT_VALUE_STRING;
        }
        String dataPath = dataPath(this.basePath, taskCode);
        if (FileUtils.saveFile(inputStream, dataPath)) {
            return dataPath;
        }
        return Globals.DEFAULT_VALUE_STRING;
    }

    /**
     * <h3 class="en-US">Get the data file location for the given task code</h3>
     * <h3 class="zh-CN">获取给定任务代码的数据文件位置</h3>
     *
     * @param basePath <span class="en-US">The base path for system execution</span>
     *                 <span class="zh-CN">系统执行的基础路径</span>
     * @param taskCode <span class="en-US">task identification code</span>
     *                 <span class="zh-CN">任务识别代码</span>
     * @return <span class="en-US">data file location</span>
     * <span class="zh-CN">数据文件位置</span>
     */
    private static String dataPath(final String basePath, @Nonnull final Long taskCode) {
        return basePath + Globals.DEFAULT_PAGE_SEPARATOR + Long.toHexString(taskCode) + DATA_FILE_EXTENSION_NAME;
    }

    /**
     * <h3 class="en-US">Get the data file location for the given task code</h3>
     * <h3 class="zh-CN">获取给定任务代码的数据文件位置</h3>
     *
     * @param taskCode <span class="en-US">task identification code</span>
     *                 <span class="zh-CN">任务识别代码</span>
     * @return <span class="en-US">data file location</span>
     * <span class="zh-CN">数据文件位置</span>
     */
    private String exportPath(@Nonnull final Long taskCode, final boolean compatibilityMode) {
        return this.basePath + Globals.DEFAULT_PAGE_SEPARATOR + Long.toHexString(taskCode)
                + (compatibilityMode ? OfficeUtils.EXCEL_FILE_EXT_NAME_2003 : OfficeUtils.EXCEL_FILE_EXT_NAME_2007);
    }

    /**
     * <h3 class="en-US">Scheduling tasks, used to regularly start pending tasks in the task queue</h3>
     * <h3 class="zh-CN">调度任务，用于定时启动任务队列中的待处理任务</h3>
     */
    private void scheduleTask() {
        if (this.scheduleRunning) {
            return;
        }
        this.scheduleRunning = Boolean.TRUE;

        try {
            while (this.runningThreads.size() < this.threadLimit) {
                AbstractTask taskInfo = this.taskProvider.nextTask(this.identifyCode);
                if (taskInfo == null || this.runningThreads.stream().anyMatch(processThread ->
                        ObjectUtils.nullSafeEquals(processThread.taskCode, taskInfo.getTaskCode()))) {
                    break;
                }
                ProcessThread processThread;
                if (taskInfo instanceof ImportTask) {
                    processThread = new ImportThread((ImportTask) taskInfo, this);
                } else if (taskInfo instanceof ExportTask) {
                    processThread = new ExportThread((ExportTask) taskInfo, this);
                } else {
                    return;
                }
                synchronized (this.runningThreads) {
                    this.runningThreads.add(processThread);
                }
                processThread.start();
            }
        } catch (Exception e) {
            LOGGER.error("Data_Task_Schedule_Error");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Stack_Message_Error", e);
            }
        }

        this.scheduleRunning = Boolean.FALSE;
    }

    /**
     * <h3 class="en-US">Remove expired completed task information</h3>
     * <h3 class="zh-CN">移除过期已完成的任务信息</h3>
     */
    private void removeTask() {
        if (this.removeRunning || this.expireTime == Globals.DEFAULT_VALUE_LONG) {
            return;
        }
        this.removeRunning = Boolean.TRUE;
        this.taskProvider.dropTask(this.expireTime);
        this.removeRunning = Boolean.FALSE;
    }

    /**
     * <h3 class="en-US">Parse the data list to the data map which data lists read from the Excel file</h3>
     * <h3 class="zh-CN">解析Excel读取的数据列表为数据映射表</h3>
     *
     * @param dataValues <span class="en-US">Data list which read from Excel file</span>
     *                   <span class="zh-CN">Excel读取的数据列表</span>
     * @return <span class="en-US">Parsed data map</span>
     * <span class="zh-CN">解析的数据映射表</span>
     */
    private static Map<String, Object> parseList(@Nonnull final String identifyCode,
                                                 @Nonnull final List<String> dataValues) {
        Map<String, Object> transferMap = new HashMap<>();
        List<TransferColumn> transferColumnList =
                REGISTERED_TRANSFER_CONFIGS.getOrDefault(identifyCode, Collections.emptyList());
        transferColumnList.stream()
                .filter(transferColumn ->
                        transferColumn.getColumnIndex() >= Globals.INITIALIZE_INT_VALUE
                                && transferColumn.getColumnIndex() < dataValues.size())
                .forEach(transferColumn ->
                        transferMap.put(transferColumn.getColumnName(),
                                transferColumn.unmarshall(dataValues.get(transferColumn.getColumnIndex()))));
        return transferMap;
    }

    /**
     * <h2 class="en-US">Memory-only task adapter implementation class</h2>
     * <h2 class="zh-CN">仅使用内存的任务适配器实现类</h2>
     *
     * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
     * @version $Revision: 1.0.0 $ $Date: Oct 26, 2023 16:42:18 $
     */
    @Provider(name = "MemoryTaskProvider", titleKey = "memory.name.task.provider")
    private static final class MemoryTaskProviderImpl implements TaskProvider {

        /**
         * <span class="en-US">List of currently stored task information</span>
         * <span class="zh-CN">当前存储的任务信息列表</span>
         */
        private final List<AbstractTask> taskInfoList;
        /**
         * <span class="en-US">The base path for system execution</span>
         * <span class="zh-CN">系统执行的基础路径</span>
         */
        private String basePath = Globals.DEFAULT_VALUE_STRING;

        /**
         * <h3 class="en-US">Constructor of a memory-only task adapter implementation class</h3>
         * <h3 class="zh-CN">仅使用内存的任务适配器实现类的构造方法</h3>
         */
        public MemoryTaskProviderImpl() {
            this.taskInfoList = new ArrayList<>();
        }

        /*
         * (non-Javadoc)
         * @see org.nervousync.database.providers.data.TaskProvider#initialize()
         */
        @Override
        public void initialize(final String basePath) {
            if (StringUtils.notBlank(basePath)) {
                this.basePath = basePath;
                FileUtils.makeDir(this.basePath);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.nervousync.database.providers.data.TaskProvider#destroy()
         */
        @Override
        public void destroy() {
            this.taskInfoList.clear();
        }

        /*
         * (non-Javadoc)
         * @see org.nervousync.database.providers.data.TaskProvider#addTask(org.nervousync.database.bean.data.TaskInfo)
         */
        @Override
        public boolean addTask(@Nonnull final AbstractTask taskInfo) {
            synchronized (this.taskInfoList) {
                if (this.taskInfoList.stream().anyMatch(existTask -> ObjectUtils.nullSafeEquals(existTask, taskInfo))) {
                    return Boolean.TRUE;
                }
                return this.taskInfoList.add(taskInfo);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.nervousync.database.providers.data.TaskProvider#updateTask(java.lang.Long, java.lang.Integer)
         */
        @Override
        public void processTask(@Nonnull final Long taskCode, final String identifyCode) {
            synchronized (this.taskInfoList) {
                this.taskInfoList.replaceAll(taskInfo -> {
                    if (ObjectUtils.nullSafeEquals(taskInfo.getTaskCode(), taskCode)
                            && ObjectUtils.nullSafeEquals(taskInfo.getIdentifyCode(), identifyCode)) {
                        taskInfo.setStartTime(DateTimeUtils.currentUTCTimeMillis());
                        taskInfo.setTaskStatus(DATA_TASK_STATUS_PROCESS);
                    }
                    return taskInfo;
                });
            }
        }

        /*
         * (non-Javadoc)
         * @see org.nervousync.database.providers.data.TaskProvider#dropTask(java.lang.Long)
         */
        @Override
        public void dropTask(@Nonnull final Long expireTime) {
            if (ObjectUtils.nullSafeEquals(expireTime, Globals.DEFAULT_VALUE_LONG)) {
                return;
            }
            long expireEndTime = DateTimeUtils.currentUTCTimeMillis() + expireTime;
            synchronized (this.taskInfoList) {
                this.taskInfoList.removeIf(taskInfo ->
                        ObjectUtils.nullSafeEquals(taskInfo.getTaskStatus(), DATA_TASK_STATUS_FINISH)
                                && taskInfo.getEndTime() < expireEndTime);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.nervousync.database.providers.data.TaskProvider#dropTask(java.lang.Long, java.lang.Long)
         */
        @Override
        public boolean dropTask(@Nonnull final Long userCode, @Nonnull final Long taskCode) {
            synchronized (this.taskInfoList) {
                Iterator<AbstractTask> iterator = this.taskInfoList.iterator();
                while (iterator.hasNext()) {
                    AbstractTask abstractTask = iterator.next();
                    if (ObjectUtils.nullSafeEquals(abstractTask.getTaskCode(), taskCode)
                            && ObjectUtils.nullSafeEquals(abstractTask.getUserCode(), userCode)) {
                        if (FileUtils.removeFile(DataUtils.dataPath(this.basePath, abstractTask.getTaskCode()))) {
                            iterator.remove();
                        } else {
                            return Boolean.FALSE;
                        }
                    }
                }
                return Boolean.TRUE;
            }
        }

        /*
         * (non-Javadoc)
         * @see org.nervousync.database.providers.data.TaskProvider#nextTask()
         */
        @Override
        public AbstractTask nextTask(@Nonnull final String identifyCode) {
            synchronized (this.taskInfoList) {
                AbstractTask abstractTask =
                        this.taskInfoList.stream().filter(currentTask -> this.processingTask(currentTask, identifyCode))
                                .findFirst()
                                .orElseGet(() ->
                                        this.taskInfoList.stream().filter(this::waitingTask)
                                                .findFirst()
                                                .map(currentTask -> {
                                                    this.lockTask(currentTask.getTaskCode(), identifyCode);
                                                    return currentTask;
                                                })
                                                .orElse(null));
                if (abstractTask != null) {
                    this.lockTask(abstractTask.getTaskCode(), identifyCode);
                }
                return abstractTask;
            }
        }

        private boolean processingTask(final AbstractTask abstractTask, final String identifyCode) {
            return ObjectUtils.nullSafeEquals(abstractTask.getTaskStatus(), DATA_TASK_STATUS_PROCESS)
                    && ObjectUtils.nullSafeEquals(abstractTask.getIdentifyCode(), identifyCode);
        }

        private boolean waitingTask(final AbstractTask abstractTask) {
            return ObjectUtils.nullSafeEquals(abstractTask.getTaskStatus(), DATA_TASK_STATUS_CREATE);
        }

        private void lockTask(final long taskCode, final String identifyCode) {
            this.taskInfoList.replaceAll(abstractTask -> {
                if (ObjectUtils.nullSafeEquals(abstractTask.getTaskCode(), taskCode)) {
                    abstractTask.setIdentifyCode(identifyCode);
                }
                return abstractTask;
            });
        }

        /*
         * (non-Javadoc)
         * @see org.nervousync.database.providers.data.TaskProvider#finishTask(java.lang.Long, java.lang.Boolean, java.lang.String)
         */
        @Override
        public void finishTask(@Nonnull final Long taskCode, @Nonnull final Boolean hasError,
                               @Nonnull final String errorMessage) {
            synchronized (this.taskInfoList) {
                this.taskInfoList.replaceAll(taskInfo -> {
                    if (ObjectUtils.nullSafeEquals(taskInfo.getTaskCode(), taskCode)
                            && ObjectUtils.nullSafeEquals(taskInfo.getTaskStatus(), DATA_TASK_STATUS_PROCESS)) {
                        taskInfo.setTaskStatus(DATA_TASK_STATUS_FINISH);
                        taskInfo.setEndTime(DateTimeUtils.currentUTCTimeMillis());
                        taskInfo.setHasError(hasError);
                        taskInfo.setErrorMessage(errorMessage);
                    }
                    return taskInfo;
                });
            }
        }

        /*
         * (non-Javadoc)
         * @see org.nervousync.database.providers.data.TaskProvider#taskList(java.lang.Long, java.lang.Integer, java.lang.Integer)
         */
        @Override
        public List<AbstractTask> taskList(@Nonnull final Long userCode, Integer pageNo, Integer limitSize) {
            List<AbstractTask> taskList = new ArrayList<>();
            Integer currentPage = pageNo;
            if (currentPage == null || currentPage <= Globals.INITIALIZE_INT_VALUE) {
                currentPage = BrainCommons.DEFAULT_PAGE_NO;
            }
            Integer currentLimit = limitSize;
            if (currentLimit == null || currentLimit <= Globals.INITIALIZE_INT_VALUE) {
                currentLimit = BrainCommons.DEFAULT_PAGE_LIMIT;
            }

            int beginIndex = ((currentPage - 1) * currentLimit);
            int endIndex = Math.min(this.taskInfoList.size(), beginIndex + currentLimit);
            final AtomicInteger currentIndex = new AtomicInteger(Globals.INITIALIZE_INT_VALUE);
            this.taskInfoList.stream()
                    .filter(taskInfo -> ObjectUtils.nullSafeEquals(taskInfo.getUserCode(), userCode))
                    .forEach(taskInfo -> {
                        int index = currentIndex.get();
                        if (index >= beginIndex && index < endIndex) {
                            taskList.add(taskInfo);
                        }
                        currentIndex.incrementAndGet();
                    });
            return taskList;
        }

        /*
         * (non-Javadoc)
         * @see org.nervousync.database.providers.data.TaskProvider#taskInfo(java.lang.Long, java.lang.Long)
         */
        @Override
        public AbstractTask taskInfo(@Nonnull final Long userCode, @Nonnull final Long taskCode) {
            synchronized (this.taskInfoList) {
                return this.taskInfoList.stream()
                        .filter(taskInfo ->
                                ObjectUtils.nullSafeEquals(taskInfo.getTaskCode(), taskCode)
                                        && ObjectUtils.nullSafeEquals(taskInfo.getUserCode(), userCode))
                        .findFirst()
                        .orElse(null);
            }
        }
    }

    /**
     * <h2 class="en-US">Data operate content</h2>
     * <h2 class="zh-CN">数据操作内容</h2>
     *
     * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
     * @version $Revision: 1.0.0 $ $Date: Oct 26, 2023 16:42:18 $
     */
    private static final class DataRecord {
        /**
         * <span class="en-US">Remove record operate</span>
         * <span class="zh-CN">删除记录操作</span>
         */
        private final boolean removeOperate;
        /**
         * <span class="en-US">Data type identify code</span>
         * <span class="zh-CN">类型识别代码</span>
         */
        private final String identifyCode;
        /**
         * <span class="en-US">Primary key data map</span>
         * <span class="zh-CN">主键数据映射表</span>
         */
        private final Map<String, String> primaryKey;
        /**
         * <span class="en-US">Update data map</span>
         * <span class="zh-CN">更新数据映射表</span>
         */
        private final Map<String, String> dataMap;

        /**
         * <h3 class="en-US">Private constructor method for data operate content</h3>
         * <h3 class="zh-CN">数据操作内容的私有构造方法</h3>
         *
         * @param removeOperate <span class="en-US">Remove record operate</span>
         *                      <span class="zh-CN">删除记录操作</span>
         * @param identifyCode  <span class="en-US">Entity class</span>
         *                      <span class="zh-CN">实体类</span>
         * @param primaryKey    <span class="en-US">Primary key data map</span>
         *                      <span class="zh-CN">主键数据映射表</span>
         * @param dataMap       <span class="en-US">Update data map</span>
         *                      <span class="zh-CN">更新数据映射表</span>
         */
        private DataRecord(final boolean removeOperate, final String identifyCode,
                           final Map<String, String> primaryKey, final Map<String, String> dataMap) {
            this.removeOperate = removeOperate;
            this.identifyCode = identifyCode;
            this.primaryKey = primaryKey;
            this.dataMap = dataMap;
        }

        /**
         * <h3 class="en-US">Generate data operate content instance from binary data</h3>
         * <h3 class="zh-CN">从二进制数据生成数据操作内容</h3>
         *
         * @param identifyKeys <span class="en-US">Data table identification code list</span>
         *                     <span class="zh-CN">数据表识别代码列表</span>
         * @param dataBytes    <span class="en-US">Binary data</span>
         *                     <span class="zh-CN">二进制数据</span>
         * @return <span class="en-US">Generated data operate content</span>
         * <span class="zh-CN">生成的数据操作内容</span>
         * @throws DataInvalidException <span class="en-US">If binary data invalid</span>
         *                              <span class="zh-CN">如果二进制数据非法</span>
         */
        public static DataRecord fromBytes(final List<String> identifyKeys, final byte[] dataBytes)
                throws DataInvalidException {
            if (dataBytes.length < 2) {
                return null;
            }
            int index = RawUtils.readInt(dataBytes, 1, ByteOrder.LITTLE_ENDIAN);
            if (identifyKeys.size() < index) {
                return null;
            }
            boolean remove = (dataBytes[0] == ((byte) 1));
            String dataContent = RawUtils.readString(dataBytes, 5, dataBytes.length - 5);
            Map<String, Object> recordMap = BeanUtils.stringToMap(dataContent, StringType.JSON, Globals.DEFAULT_ENCODING);
            if (recordMap.isEmpty()) {
                return null;
            }
            String identifyCode = identifyKeys.get(index);
            List<TransferColumn> transferColumnList =
                    REGISTERED_TRANSFER_CONFIGS.getOrDefault(identifyCode, Collections.emptyList());
            Map<String, String> primaryKey = new HashMap<>();
            Map<String, String> dataMap = new HashMap<>();
            transferColumnList.stream()
                    .filter(transferColumn -> recordMap.containsKey(transferColumn.getColumnName()))
                    .forEach(transferColumn -> {
                        String columnName = transferColumn.getColumnName();
                        String columnValue = Optional.ofNullable(transferColumn.marshall(recordMap.get(columnName)))
                                .map(ConvertUtils::toByteArray)
                                .map(StringUtils::base64Encode)
                                .orElse(Globals.DEFAULT_VALUE_STRING);
                        if (transferColumn.isPrimaryKey()) {
                            primaryKey.put(columnName, columnValue);
                        }
                        dataMap.put(columnName, columnValue);
                    });
            if (primaryKey.isEmpty() || dataMap.isEmpty()) {
                return null;
            }
            return new DataRecord(remove, identifyCode, primaryKey, dataMap);
        }

        /**
         * <h3 class="en-US">Getter method for remove record operate</h3>
         * <h3 class="zh-CN">删除记录操作的Getter方法</h3>
         *
         * @return <span class="en-US">Remove record operate</span>
         * <span class="zh-CN">删除记录操作</span>
         */
        public boolean isRemoveOperate() {
            return removeOperate;
        }

        /**
         * <h3 class="en-US">Getter method for data type identify code</h3>
         * <h3 class="zh-CN">类型识别代码的Getter方法</h3>
         *
         * @return <span class="en-US">Data type identify code</span>
         * <span class="zh-CN">类型识别代码</span>
         */
        public String getIdentifyCode() {
            return this.identifyCode;
        }

        /**
         * <h3 class="en-US">Getter method for primary key data map</h3>
         * <h3 class="zh-CN">主键数据映射表的Getter方法</h3>
         *
         * @return <span class="en-US">Primary key data map</span>
         * <span class="zh-CN">主键数据映射表</span>
         */
        public Map<String, String> getPrimaryKey() {
            return primaryKey;
        }

        /**
         * <h3 class="en-US">Getter method for update data map</h3>
         * <h3 class="zh-CN">更新数据映射表的Getter方法</h3>
         *
         * @return <span class="en-US">Update data map</span>
         * <span class="zh-CN">更新数据映射表</span>
         */
        public Map<String, String> getDataMap() {
            return dataMap;
        }
    }

    /**
     * <h2 class="en-US">Data exporter</h2>
     * <h2 class="zh-CN">数据导出器</h2>
     *
     * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
     * @version $Revision: 1.0.0 $ $Date: Oct 26, 2023 16:42:18 $
     */
    public static final class DataExporter implements Closeable {

        /**
         * <span class="en-US">Excel writer instance</span>
         * <span class="zh-CN">Excel文件写入器</span>
         */
        private final ExcelWriter excelWriter;

        /**
         * <h3 class="en-US">Private constructor for data exporter</h3>
         * <h3 class="zh-CN">数据导出器的私有构造方法</h3>
         *
         * @param dataPath <span class="en-US">File storage path</span>
         *                 <span class="zh-CN">文件存储路径</span>
         * @throws DataInvalidException <span class="en-US">If the file format is incorrect</span>
         *                              <span class="zh-CN">如果文件格式不正确</span>
         */
        private DataExporter(final String dataPath) throws DataInvalidException {
            this.excelWriter = OfficeUtils.newWriter(dataPath);
        }

        /**
         * <h3 class="en-US">Write given entity object instance data to the Excel file</h3>
         * <h3 class="zh-CN">写入数据表实体类对象数据到Excel文件</h3>
         *
         * @param sheetName <span class="en-US">Sheet name</span>
         *                  <span class="zh-CN">数据表名称</span>
         * @param dataMap   <span class="en-US">Data mapping table</span>
         *                  <span class="zh-CN">数据映射表</span>
         */
        public void appendData(@Nonnull final String sheetName, @Nonnull final Map<String, Object> dataMap) {
            SheetWriter sheetWriter = this.excelWriter.sheetWriter(sheetName);
            List<Object> dataValues = new ArrayList<>();
            List<TransferColumn> transferColumnList =
                    REGISTERED_TRANSFER_CONFIGS.getOrDefault(BrainCommons.identifyCode(sheetName),
                            Collections.emptyList());
            if (transferColumnList.isEmpty()) {
                return;
            }
            int maxIndex = transferColumnList.get(transferColumnList.size() - 1).getColumnIndex();
            for (int i = 0; i <= maxIndex; i++) {
                dataValues.add(null);
            }
            transferColumnList.forEach(transferColumn ->
                    dataValues.set(transferColumn.getColumnIndex(),
                            transferColumn.marshall(dataMap.get(transferColumn.getColumnName()))));
            sheetWriter.appendData(dataValues);
        }

        /**
         * (Non-javadoc)
         *
         * @see Closeable#close()
         */
        @Override
        public void close() throws IOException {
            this.excelWriter.write();
            this.excelWriter.close();
        }
    }

    /**
     * <h2 class="en-US">Data generator</h2>
     * <h2 class="zh-CN">数据生成器</h2>
     *
     * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
     * @version $Revision: 1.0.0 $ $Date: Oct 26, 2023 16:42:18 $
     */
    public static final class DataGenerator implements Closeable {
        /**
         * <span class="en-US">Writable file object instance</span>
         * <span class="zh-CN">写入文件实例对象</span>
         */
        private final StandardFile dataFile;
        /**
         * <span class="en-US">Data identification code list</span>
         * <span class="zh-CN">数据识别代码列表</span>
         */
        private final List<String> recordTypes;
        /**
         * <span class="en-US">Total record count</span>
         * <span class="zh-CN">总记录数</span>
         */
        private long totalCount = 0L;
        /**
         * <span class="en-US">Pointer position</span>
         * <span class="zh-CN">指针位置</span>
         */
        private long position = 8L;

        /**
         * <h3 class="en-US">Private constructor method for data generator</h3>
         * <h3 class="zh-CN">数据生成器的私有构造方法</h3>
         *
         * @param dataPath <span class="en-US">File storage path</span>
         *                 <span class="zh-CN">文件存储路径</span>
         * @throws IOException <span class="en-US">If the file storage path is incorrect</span>
         *                     <span class="zh-CN">如果文件存储路径不正确</span>
         */
        private DataGenerator(final String dataPath) throws IOException {
            this.dataFile = new StandardFile(dataPath, Boolean.TRUE);
            this.dataFile.seek(this.position);
            this.recordTypes = new ArrayList<>();
        }

        /**
         * <h3 class="en-US">Append data record to the target file</h3>
         * <h3 class="zh-CN">向文件中追加记录</h3>
         *
         * @param removeRecord <span class="en-US">Entity object instance will be removed</span>
         *                     <span class="zh-CN">实体类对象为需要删除的记录</span>
         * @param tableName    <span class="en-US">Data table name</span>
         *                     <span class="zh-CN">数据表名</span>
         * @param dataMap      <span class="en-US">Data mapping table</span>
         *                     <span class="zh-CN">数据映射表</span>
         */
        public void appendData(final boolean removeRecord, @Nonnull final String tableName,
                               final Map<String, Object> dataMap) {
            this.writeBytes(removeRecord, BrainCommons.identifyCode(tableName), dataMap);
        }

        /**
         * <h3 class="en-US">Append data record to the target file, data records from given Excel files</h3>
         * <h3 class="zh-CN">向文件中追加Excel文件中的记录</h3>
         *
         * @param excelFilePath <span class="en-US">Excel file path</span>
         *                      <span class="zh-CN">Excel文件路径</span>
         */
        public void appendData(final String excelFilePath) {
            OfficeUtils.readExcel(excelFilePath)
                    .forEach((sheetName, dataList) ->
                            dataList.stream()
                                    .filter(dataValues -> !CollectionUtils.isEmpty(dataList))
                                    .forEach(dataValues ->
                                            this.writeBytes(Boolean.FALSE, sheetName,
                                                    DataUtils.parseList(BrainCommons.identifyCode(sheetName),
                                                            dataValues))));
        }

        /**
         * <h3 class="en-US">Write data to the temporary file</h3>
         * <h3 class="zh-CN">写入数据到临时文件</h3>
         *
         * @param removeRecord <span class="en-US">Entity object instance will be removed</span>
         *                     <span class="zh-CN">实体类对象为需要删除的记录</span>
         * @param string       <span class="en-US">Data identification code</span>
         *                     <span class="zh-CN">数据识别代码</span>
         * @param dataMap      <span class="en-US">Data mapping</span>
         *                     <span class="zh-CN">数据映射表</span>
         */
        private void writeBytes(final boolean removeRecord, final String string,
                                @Nonnull final Map<String, Object> dataMap) {
            if (dataMap.isEmpty()) {
                return;
            }
            String recordType = BrainCommons.identifyCode(string);
            if (!CollectionUtils.contains(this.recordTypes, recordType)) {
                this.recordTypes.add(recordType);
            }
            int index = this.recordTypes.indexOf(recordType);
            String dataContent = BeanUtils.objectToString(dataMap, StringType.JSON, Boolean.FALSE);
            if (StringUtils.isEmpty(dataContent)) {
                return;
            }
            int totalLength = dataContent.getBytes(StandardCharsets.UTF_8).length + 5;
            byte[] dataBytes = new byte[totalLength + 4];
            try {
                RawUtils.writeInt(dataBytes, ByteOrder.LITTLE_ENDIAN, totalLength);
                dataBytes[4] = removeRecord ? (byte) 1 : (byte) 0;
                RawUtils.writeInt(dataBytes, 5, ByteOrder.LITTLE_ENDIAN, index);
                RawUtils.writeString(dataBytes, 9, dataContent);
                this.dataFile.write(dataBytes);
                this.totalCount++;
                this.position += totalLength;
            } catch (DataInvalidException | IOException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Stack_Message_Error", e);
                }
            }
        }

        @Override
        public void close() throws IOException {
            try {
                byte[] buffer = new byte[8];
                RawUtils.writeLong(buffer, ByteOrder.LITTLE_ENDIAN, this.position);
                this.dataFile.seek(0L);
                this.dataFile.write(buffer);

                this.dataFile.seek(this.position);
                buffer = new byte[8];
                RawUtils.writeLong(buffer, ByteOrder.LITTLE_ENDIAN, this.totalCount);
                this.dataFile.write(buffer);
                buffer = new byte[4];
                RawUtils.writeInt(buffer, ByteOrder.LITTLE_ENDIAN, this.recordTypes.size());
                this.dataFile.write(buffer);
                for (String recordType : this.recordTypes) {
                    buffer = new byte[TYPE_LENGTH];
                    RawUtils.writeString(buffer, recordType);
                    this.dataFile.write(buffer);
                }
            } catch (DataInvalidException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * <h2 class="en-US">Data parser</h2>
     * <h2 class="zh-CN">数据解析器</h2>
     *
     * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
     * @version $Revision: 1.0.0 $ $Date: Oct 26, 2023 16:42:18 $
     */
    private static final class DataParser implements Closeable {
        /**
         * <span class="en-US">Process data using transactional mode</span>
         * <span class="zh-CN">使用事务模式处理数据</span>
         */
        private final boolean transactional;
        /**
         * <span class="en-US">Transactional timeout</span>
         * <span class="zh-CN">事务超时时间</span>
         */
        private final int timeout;
        /**
         * <span class="en-US">Temporary file instance</span>
         * <span class="zh-CN">临时文件实例对象</span>
         */
        private final StandardFile dataFile;
        /**
         * <span class="en-US">Data identification code list</span>
         * <span class="zh-CN">数据识别代码列表</span>
         */
        private final List<String> recordTypes;
        /**
         * <span class="en-US">Total record count</span>
         * <span class="zh-CN">总记录数</span>
         */
        private final long totalCount;
        /**
         * <span class="en-US">End position</span>
         * <span class="zh-CN">结尾地址</span>
         */
        private final long endPosition;
        /**
         * <span class="en-US">Process succeed record count</span>
         * <span class="zh-CN">处理成功记录数</span>
         */
        private long successCount = 0L;
        /**
         * <span class="en-US">Process failed record count</span>
         * <span class="zh-CN">处理失败记录数</span>
         */
        private long failedCount = 0L;
        /**
         * <span class="en-US">Error message builder</span>
         * <span class="zh-CN">错误信息收集器</span>
         */
        private final StringBuilder errorLog;

        /**
         * <h3 class="en-US">Default constructor method for data parser</h3>
         * <h3 class="zh-CN">数据解析器的默认构造方法</h3>
         *
         * @param transactional <span class="en-US">Process data using transactional mode</span>
         *                      <span class="zh-CN">使用事务模式处理数据</span>
         * @param timeout       <span class="en-US">Transactional timeout</span>
         *                      <span class="zh-CN">事务超时时间</span>
         * @param dataPath      <span class="en-US">Data file storage path</span>
         *                      <span class="zh-CN">数据文件存储路径</span>
         * @throws DataParseException <span class="en-US">If data file invalid</span>
         *                            <span class="zh-CN">如果数据文件非法</span>
         */
        public DataParser(final boolean transactional, final int timeout, final String dataPath)
                throws DataParseException {
            this.transactional = transactional;
            this.timeout = timeout;
            this.errorLog = new StringBuilder();
            if (StringUtils.isEmpty(dataPath)) {
                throw new DataParseException(0x00DB00000006L);
            }

            try {
                this.dataFile = new StandardFile(dataPath);
                byte[] positionBytes = new byte[8];
                this.dataFile.read(positionBytes);
                this.endPosition = RawUtils.readLong(positionBytes, ByteOrder.LITTLE_ENDIAN);
            } catch (IOException | DataInvalidException e) {
                this.errorLog.append(e.getMessage()).append(FileUtils.CRLF);
                throw new DataParseException(0x00DB00000006L, e);
            }

            try {
                this.dataFile.seek(this.endPosition);
                byte[] longBuffer = new byte[8];
                if (this.dataFile.read(longBuffer) == 8) {
                    this.totalCount = RawUtils.readLong(longBuffer, ByteOrder.LITTLE_ENDIAN);
                } else {
                    throw new DataParseException(0x00DB00000005L);
                }

                byte[] intBuffer = new byte[4];
                int headerCount;
                if (this.dataFile.read(intBuffer) == 4) {
                    headerCount = RawUtils.readInt(intBuffer, ByteOrder.LITTLE_ENDIAN);
                } else {
                    throw new DataParseException(0x00DB00000005L);
                }

                this.recordTypes = new ArrayList<>();
                byte[] readBuffer;
                do {
                    readBuffer = new byte[TYPE_LENGTH];
                    if (this.dataFile.read(readBuffer) == TYPE_LENGTH) {
                        this.recordTypes.add(RawUtils.readString(readBuffer));
                    } else {
                        throw new DataParseException(0x00DB00000005L);
                    }
                    headerCount--;
                } while (headerCount > 0);
                this.dataFile.seek(8L);
            } catch (IOException | DataInvalidException e) {
                this.errorLog.append(e.getMessage()).append(FileUtils.CRLF);
                throw new DataParseException(0x00DB00000007L, e);
            }
        }

        /**
         * <h3 class="en-US">Process data in the target file path</h3>
         * <h3 class="zh-CN">处理数据文件中的数据</h3>
         *
         * @throws DataParseException   <span class="en-US">File data length invalid</span>
         *                              <span class="zh-CN">数据文件长度错误</span>
         * @throws DataInvalidException <span class="en-US">File data content invalid</span>
         *                              <span class="zh-CN">数据文件内容错误</span>
         * @throws IOException          <span class="en-US">Read file data error</span>
         *                              <span class="zh-CN">读取数据文件出错</span>
         */
        public void process() throws Exception {
            Class<?>[] rollbackClasses = new Class[]{InsertException.class, UpdateException.class, DropException.class};
            TransactionalConfig txConfig = this.transactional
                    ? TransactionalConfig.newInstance(Propagation.REQUIRED, this.timeout, Connection.TRANSACTION_READ_COMMITTED, rollbackClasses)
                    : null;
            BrainDataSource dataSource = BrainDataSource.getInstance();
            boolean newTransactional = true;
            if (txConfig != null) {
                newTransactional = TransactionalProxy.getTransactionalManager().begin(txConfig);
            }
            byte[] intBuffer = new byte[4];
            byte[] readBuffer;
            try {
                while (this.dataFile.getFilePointer() < this.endPosition) {
                    boolean success = Boolean.FALSE;
                    if (this.dataFile.read(intBuffer) == 4) {
                        int dataLength = RawUtils.readInt(intBuffer, ByteOrder.LITTLE_ENDIAN);
                        if (dataLength > 0) {
                            readBuffer = new byte[dataLength];
                            if (this.dataFile.read(readBuffer) == dataLength) {
                                DataRecord dataRecord = DataRecord.fromBytes(this.recordTypes, readBuffer);
                                if (dataRecord != null) {
                                    this.process(dataSource, dataRecord);
                                    success = Boolean.TRUE;
                                }
                            }
                        }
                    } else {
                        throw new DataParseException(0x00DB00000008L, this.dataFile.getFilePointer());
                    }
                    if (success) {
                        this.successCount++;
                    } else {
                        this.failedCount++;
                    }
                }
                if (txConfig != null) {
                    TransactionalProxy.getTransactionalManager().commit(newTransactional);
                }
            } catch (Exception e) {
                if (txConfig != null) {
                    TransactionalProxy.getTransactionalManager().rollback(e);
                }
            } finally {
                if (txConfig != null) {
                    TransactionalProxy.getTransactionalManager().end(newTransactional);
                }
            }
        }

        /**
         * <h3 class="en-US">Has error when processing the data file</h3>
         * <h3 class="zh-CN">处理数据文件过程中出现错误</h3>
         *
         * @return <span class="en-US">Has error status</span>
         * <span class="zh-CN">出现错误</span>
         */
        public boolean hasError() {
            return (this.failedCount > 0) || ((this.successCount + this.failedCount) != this.totalCount);
        }

        /**
         * <h3 class="en-US">Read the error message</h3>
         * <h3 class="zh-CN">读取错误信息</h3>
         *
         * @return <span class="en-US">Error message</span>
         * <span class="zh-CN">错误信息</span>
         */
        public String errorMessage() {
            return this.errorLog.toString();
        }

        @Override
        public void close() throws IOException {
            this.dataFile.close();
        }

        /**
         * <h3 class="en-US">Process data</h3>
         * <h3 class="zh-CN">处理数据</h3>
         *
         * @param dataSource <span class="en-US">Data source instance object</span>
         *                   <span class="zh-CN">数据源实例对象</span>
         * @param dataRecord <span class="en-US">Data operate instance</span>
         *                   <span class="zh-CN">数据操作实例对象</span>
         * @throws Exception <span class="en-US">If throw error when processing data</span>
         *                   <span class="zh-CN">处理数据时出错</span>
         */
        private void process(final BrainDataSource dataSource, final DataRecord dataRecord) throws Exception {
            String identifyCode = dataRecord.getIdentifyCode();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Data_Parse_Result", dataRecord.isRemoveOperate(), identifyCode,
                        BeanUtils.objectToString(dataRecord.getPrimaryKey(), StringType.JSON, Boolean.TRUE),
                        BeanUtils.objectToString(dataRecord.getDataMap(), StringType.JSON, Boolean.TRUE));
            }
            List<TransferColumn> transferColumnList =
                    REGISTERED_TRANSFER_CONFIGS.getOrDefault(dataRecord.getIdentifyCode(), Collections.emptyList());
            Map<String, Object> filterMap = new HashMap<>();
            Map<String, Object> convertMap = new HashMap<>();
            Map<String, String> dataMap = dataRecord.getDataMap();
            transferColumnList.forEach(transferColumn ->
                    Optional.ofNullable(dataMap.get(transferColumn.getColumnName()))
                            .filter(StringUtils::notBlank)
                            .map(StringUtils::base64Decode)
                            .map(ConvertUtils::toObject)
                            .ifPresent(columnValue -> {
                                if (transferColumn.isPrimaryKey()) {
                                    filterMap.put(transferColumn.getColumnName(), transferColumn.unmarshall(columnValue));
                                } else {
                                    convertMap.put(transferColumn.getColumnName(), transferColumn.unmarshall(columnValue));
                                }
                            }));
            if (dataRecord.isRemoveOperate()) {
                dataSource.delete(identifyCode, filterMap);
            } else {
                if (dataSource.lockRecord(identifyCode, filterMap, LockModeType.NONE)) {
                    dataSource.update(identifyCode, convertMap, filterMap);
                } else {
                    Map<String, Object> allMap = new HashMap<>();
                    allMap.putAll(filterMap);
                    allMap.putAll(convertMap);
                    dataSource.insert(identifyCode, allMap);
                }
            }
        }
    }

    /**
     * <h2 class="en-US">Abstract class of task process thread</h2>
     * <h2 class="zh-CN">任务处理线程抽象类</h2>
     *
     * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
     * @version $Revision: 1.0.0 $ $Date: Oct 26, 2023 16:42:18 $
     */
    private static abstract class ProcessThread extends Thread {
        /**
         * <span class="en-US">Task unique identification code</span>
         * <span class="zh-CN">任务唯一识别代码</span>
         */
        private final Long taskCode;
        /**
         * <span class="en-US">Error message builder</span>
         * <span class="zh-CN">错误信息收集器</span>
         */
        protected final StringBuilder errorLog;
        /**
         * <span class="en-US">An exception occurred during task execution</span>
         * <span class="zh-CN">任务执行过程中出现异常</span>
         */
        protected boolean hasError = Boolean.FALSE;
        /**
         * <span class="en-US">Data source instance object</span>
         * <span class="zh-CN">数据源实例对象</span>
         */
        protected final BrainDataSource dataSource = BrainDataSource.getInstance();
        protected final DataUtils dataUtils;

        /**
         * @param taskCode <span class="en-US">Task unique identification code</span>
         *                 <span class="zh-CN">任务唯一识别代码</span>
         */
        protected ProcessThread(final long taskCode, final DataUtils dataUtils) {
            this.dataUtils = dataUtils;
            this.taskCode = taskCode;
            this.errorLog = new StringBuilder();
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            this.dataUtils.taskProvider.processTask(this.taskCode, this.dataUtils.identifyCode);
            this.process();
            this.dataUtils.finishTask(this);
        }

        public abstract void process();

        public Long getTaskCode() {
            return taskCode;
        }

        public boolean isHasError() {
            return hasError;
        }

        public String errorMessage() {
            return this.errorLog.toString();
        }
    }

    /**
     * <h2 class="en-US">Implementation class of export task process thread</h2>
     * <h2 class="zh-CN">导出任务处理线程实现类</h2>
     *
     * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
     * @version $Revision: 1.0.0 $ $Date: Oct 26, 2023 16:42:18 $
     */
    private static final class ExportThread extends ProcessThread {
        /**
         * <span class="en-US">Export Excel using compatibility mode</span>
         * <span class="zh-CN">使用兼容模式输出Excel</span>
         */
        private final Boolean compatibilityMode;
        /**
         * <span class="en-US">Query information list for data export tasks</span>
         * <span class="zh-CN">数据导出任务的查询信息列表</span>
         */
        private final List<QueryInfo> queryInfoList;

        /**
         * @param exportTask <span class="en-US">Data export task information</span>
         *                   <span class="zh-CN">数据导出任务信息</span>
         */
        public ExportThread(final ExportTask exportTask, final DataUtils dataUtils) {
            super(exportTask.getTaskCode(), dataUtils);
            this.compatibilityMode = exportTask.getCompatibilityMode();
            this.queryInfoList = exportTask.getQueryInfoList();
        }

        @Override
        public void process() {
            try (DataExporter dataExporter =
                         new DataExporter(this.dataUtils.exportPath(this.getTaskCode(), this.compatibilityMode))) {
                if (this.dataSource != null) {
                    for (QueryInfo queryInfo : this.queryInfoList) {
                        this.dataSource.query(queryInfo)
                                .asList()
                                .forEach(dataMap ->
                                        dataExporter.appendData(queryInfo.getSheetName(), dataMap));
                    }
                    this.hasError = Boolean.FALSE;
                } else {
                    this.hasError = Boolean.TRUE;
                }
            } catch (Exception e) {
                this.errorLog.append(e.getMessage()).append(FileUtils.CRLF);
                this.hasError = Boolean.TRUE;
            }
        }
    }

    /**
     * <h2 class="en-US">Implementation class of import task process thread</h2>
     * <h2 class="zh-CN">导入任务处理线程实现类</h2>
     *
     * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
     * @version $Revision: 1.0.0 $ $Date: Oct 26, 2023 16:42:18 $
     */
    private static final class ImportThread extends ProcessThread {

        /**
         * <span class="en-US">Task data storage path</span>
         * <span class="zh-CN">任务数据存储路径</span>
         */
        private final String dataPath;
        /**
         * <span class="en-US">Import tasks using transactions</span>
         * <span class="zh-CN">导入任务使用事务</span>
         */
        private final Boolean transactional;
        /**
         * <span class="en-US">Transaction timeout</span>
         * <span class="zh-CN">事务超时时间</span>
         */
        private final int timeout;

        public ImportThread(final ImportTask taskInfo, final DataUtils dataUtils) {
            super(taskInfo.getTaskCode(), dataUtils);
            this.dataPath = taskInfo.getDataPath();
            this.transactional = taskInfo.getTransactional();
            this.timeout = taskInfo.getTimeout();
        }

        @Override
        public void process() {
            try (final DataParser dataParser = new DataParser(this.transactional, this.timeout, this.dataPath)) {
                dataParser.process();
                this.hasError = dataParser.hasError();
                this.errorLog.append(dataParser.errorMessage());
            } catch (Exception e) {
                this.errorLog.append(e.getMessage()).append(FileUtils.CRLF);
                this.hasError = Boolean.TRUE;
            }
        }
    }

    private static final class DataUtilsHolder {

        /**
         * <span class="en-US">Singleton object of data import and export tool</span>
         * <span class="zh-CN">数据导入导出工具的单例对象</span>
         */
        private static DataUtils INSTANCE = null;

        static void initialize(final StorageConfig storageConfig) {
            if (INSTANCE != null && INSTANCE.match(storageConfig.getLastModified())) {
                return;
            }
            if (INSTANCE != null) {
                destroy();
            }
            INSTANCE = new DataUtils(storageConfig);
        }

        static void destroy() {
            if (INSTANCE != null) {
                INSTANCE.close();
                INSTANCE = null;
            }
        }
    }
}
