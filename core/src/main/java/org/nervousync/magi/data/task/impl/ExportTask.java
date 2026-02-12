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

package org.nervousync.magi.data.task.impl;

import jakarta.xml.bind.annotation.*;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.magi.data.task.AbstractTask;

import java.util.ArrayList;
import java.util.List;

/**
 * <h2 class="en-US">Data export task information</h2>
 * <h2 class="zh-CN">数据导出任务信息</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Mar 23, 2021 05:20:21 $
 */
@SuppressWarnings("unused")
@XmlRootElement(name = "export_task", namespace = "https://nervousync.org/schemas/brain")
@XmlAccessorType(XmlAccessType.NONE)
public final class ExportTask extends AbstractTask {

	/**
	 * <span class="en-US">Serial version UID</span>
	 * <span class="zh-CN">序列化UID</span>
	 */
	private static final long serialVersionUID = -786666048200180488L;

	/**
	 * <span class="en-US">Export Excel using compatibility mode</span>
	 * <span class="zh-CN">使用兼容模式输出Excel</span>
	 */
	@XmlElement(name = "compatibility_mode")
	private Boolean compatibilityMode;
	/**
	 * <span class="en-US">Query information list for data export tasks</span>
	 * <span class="zh-CN">数据导出任务的查询信息列表</span>
	 */
	@XmlElement(name = "query_plan")
	@XmlElementWrapper(name = "query_plan_list")
	private List<QueryInfo> queryInfoList;

	/**
	 * <h3 class="en-US">Constructor method for data export task information</h3>
	 * <h3 class="zh-CN">数据导出任务信息的构建方法</h3>
	 */
	public ExportTask() {
		this.queryInfoList = new ArrayList<>();
	}

	/**
	 * <h3 class="en-US">Getter method for export Excel using compatibility mode</h3>
	 * <h3 class="zh-CN">使用兼容模式输出Excel的Getter方法</h3>
	 *
	 * @return <span class="en-US">Export Excel using compatibility mode</span>
	 * <span class="zh-CN">使用兼容模式输出Excel</span>
	 */
	public Boolean getCompatibilityMode() {
		return compatibilityMode;
	}

	/**
	 * <h3 class="en-US">Setter method for export Excel using compatibility mode</h3>
	 * <h3 class="zh-CN">使用兼容模式输出Excel的Setter方法</h3>
	 *
	 * @param compatibilityMode <span class="en-US">Export Excel using compatibility mode</span>
	 *                          <span class="zh-CN">使用兼容模式输出Excel</span>
	 */
	public void setCompatibilityMode(final Boolean compatibilityMode) {
		this.compatibilityMode = compatibilityMode;
	}

	/**
	 * <h3 class="en-US">Getter method for the query information list for data export tasks</h3>
	 * <h3 class="zh-CN">数据导出任务的查询信息列表的Getter方法</h3>
	 *
	 * @return <span class="en-US">Query information list for data export tasks</span>
	 * <span class="zh-CN">数据导出任务的查询信息列表</span>
	 */
	public List<QueryInfo> getQueryInfoList() {
		return queryInfoList;
	}

	/**
	 * <h3 class="en-US">Setter method for the query information list for data export tasks</h3>
	 * <h3 class="zh-CN">数据导出任务的查询信息列表的Setter方法</h3>
	 *
	 * @param databaseQueryList <span class="en-US">Query information list for data export tasks</span>
	 *                          <span class="zh-CN">数据导出任务的查询信息列表</span>
	 */
	public void setQueryInfoList(final List<QueryInfo> databaseQueryList) {
		this.queryInfoList = databaseQueryList;
	}
}
