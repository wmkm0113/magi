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

package org.nervousync.magi.data.tracker;

import org.nervousync.magi.entity.BaseObject;

/**
 * <h2 class="en-US">Sensitive data tracker</h2>
 * <h2 class="zh-CN">敏感数据追踪器</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Mar 23, 2021 05:20:21 $
 */
public interface SensitiveTracker {

	/**
	 * <h3 class="en-US">Record the reader information who reading the sensitive data</h3>
	 * <h3 class="zh-CN">记录敏感数据读取人的信息</h3>
	 *
	 * @param className     <span class="en-US">Entity classes name</span>
	 *                      <span class="zh-CN">实体类名</span>
	 * @param primaryKeyMap <span class="en-US">Primary key data map</span>
	 *                      <span class="zh-CN">主键数据映射表</span>
	 * @param userCode      <span class="en-US">Identify code of the reader</span>
	 *                      <span class="zh-CN">读取人的识别代码</span>
	 */
	void track(final String className, final String primaryKeyMap, final String userCode);
}
