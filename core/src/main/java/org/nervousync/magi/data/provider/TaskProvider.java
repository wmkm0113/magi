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

package org.nervousync.magi.data.provider;

import jakarta.annotation.Nonnull;
import org.nervousync.magi.data.task.AbstractTask;

import java.util.List;

/**
 * <h2 class="en-US">Data import and export task configure storage adapter</h2>
 * <h2 class="zh-CN">数据导入导出任务信息存储适配器</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Jun 20, 2019 14:09 $
 */
public interface TaskProvider {

	/**
	 * <h3 class="en-US">Initialize adapter</h3>
	 * <h3 class="zh-CN">初始化适配器</h3>
	 *
	 * @param basePath <span class="en-US">The base path for system execution</span>
	 *                 <span class="zh-CN">系统执行的基础路径</span>
	 */
	void initialize(final String basePath);

	/**
	 * <h3 class="en-US">Destroy the current adapter</h3>
	 * <h3 class="zh-CN">销毁当前适配器</h3>
	 */
	void destroy();

	/**
	 * <h3 class="en-US">Add data task</h3>
	 * <h3 class="zh-CN">添加数据任务</h3>
	 *
	 * @param taskInfo <span class="en-US">Data task information</span>
	 *                 <span class="zh-CN">数据任务信息</span>
	 * @return <span class="en-US">Operation result</span>
	 * <span class="zh-CN">操作结果</span>
	 */
	boolean addTask(@Nonnull final AbstractTask taskInfo);

	/**
	 * <h3 class="en-US">Update task status code</h3>
	 * <h3 class="zh-CN">更新任务状态代码</h3>
	 *
	 * @param taskCode     <span class="en-US">Data task identification code</span>
	 *                     <span class="zh-CN">数据任务识别代码</span>
	 * @param identifyCode <span class="en-US">Current node identify code, generate by system.</span>
	 *                     <span class="zh-CN">当前节点的唯一识别代码，系统自动生成</span>
	 */
	void processTask(@Nonnull final Long taskCode, final String identifyCode);

	/**
	 * <h3 class="en-US">Delete data tasks before a given expiration time</h3>
	 * <h3 class="zh-CN">删除给定过期时间以前的数据任务</h3>
	 *
	 * @param expireTime <span class="en-US">Expiration time</span>
	 *                   <span class="zh-CN">过期时间</span>
	 */
	void dropTask(@Nonnull final Long expireTime);

	/**
	 * <h3 class="en-US">Drop data task</h3>
	 * <h3 class="zh-CN">删除数据任务</h3>
	 *
	 * @param userCode <span class="en-US">User identification code</span>
	 *                 <span class="zh-CN">用户识别代码</span>
	 * @param taskCode <span class="en-US">Data task identification code</span>
	 *                 <span class="zh-CN">数据任务识别代码</span>
	 * @return <span class="en-US">Operation result</span>
	 * <span class="zh-CN">操作结果</span>
	 */
	boolean dropTask(@Nonnull final Long userCode, @Nonnull final Long taskCode);

	/**
	 * <h3 class="en-US">Read next task details</h3>
	 * <h3 class="zh-CN">读取下一个任务详细信息</h3>
	 *
	 * @param identifyCode <span class="en-US">Current node identify code, generate by system.</span>
	 *                     <span class="zh-CN">当前节点的唯一识别代码，系统自动生成</span>
	 * @return <span class="en-US">Data task details</span>
	 * <span class="zh-CN">数据任务详细信息</span>
	 */
	AbstractTask nextTask(@Nonnull final String identifyCode);

	/**
	 * <h3 class="en-US">Complete current task</h3>
	 * <h3 class="zh-CN">完成当前任务</h3>
	 *
	 * @param taskCode     <span class="en-US">Data task identification code</span>
	 *                     <span class="zh-CN">数据任务识别代码</span>
	 * @param hasError     <span class="en-US">Task execution error</span>
	 *                     <span class="zh-CN">任务执行报错</span>
	 * @param errorMessage <span class="en-US">Error message details</span>
	 *                     <span class="zh-CN">错误信息详情</span>
	 */
	void finishTask(@Nonnull final Long taskCode, @Nonnull final Boolean hasError, @Nonnull final String errorMessage);

	/**
	 * <h3 class="en-US">Read data task status information lists</h3>
	 * <h3 class="zh-CN">读取数据任务状态信息列表</h3>
	 *
	 * @param userCode  <span class="en-US">User identification code</span>
	 *                  <span class="zh-CN">用户识别代码</span>
	 * @param pageNo    <span class="en-US">Current page number</span>
	 *                  <span class="zh-CN">当前页数</span>
	 * @param limitSize <span class="en-US">Page limit size</span>
	 *                  <span class="zh-CN">每页记录数</span>
	 * @return <span class="en-US">Data task status information list</span>
	 * <span class="zh-CN">数据任务状态信息列表</span>
	 */
	List<AbstractTask> taskList(@Nonnull final Long userCode, final Integer pageNo, final Integer limitSize);

	/**
	 * <h3 class="en-US">Read data task details</h3>
	 * <h3 class="zh-CN">读取数据任务详细信息</h3>
	 *
	 * @param userCode <span class="en-US">User identification code</span>
	 *                 <span class="zh-CN">用户识别代码</span>
	 * @param taskCode <span class="en-US">Data task identification code</span>
	 *                 <span class="zh-CN">数据任务识别代码</span>
	 * @return <span class="en-US">Data task details</span>
	 * <span class="zh-CN">数据任务详细信息</span>
	 */
	AbstractTask taskInfo(@Nonnull final Long userCode, @Nonnull final Long taskCode);
}
