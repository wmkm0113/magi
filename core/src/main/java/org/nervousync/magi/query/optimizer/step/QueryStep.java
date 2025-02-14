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

package org.nervousync.magi.query.optimizer.step;

import org.nervousync.brain.query.QueryInfo;

/**
 * <h2 class="en-US">Data query step</h2>
 * <h2 class="zh-CN">数据查询步骤</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: 10/28/2020 17:02 $
 */
public final class QueryStep extends AbstractStep {

	/**
	 * <span class="en-US">Query information instance object</span>
	 * <span class="zh-CN">查询信息实例对象</span>
	 */
	private final QueryInfo queryInfo;

	/**
	 * <h3 class="en-US">Constructor method for data query step</h3>
	 * <h3 class="zh-CN">数据查询步骤的构造方法</h3>
	 *
	 * @param queryInfo <span class="en-US">Query information instance object</span>
	 *                  <span class="zh-CN">查询信息实例对象</span>
	 */
	public QueryStep(final QueryInfo queryInfo) {
		super(StepType.Query);
		this.queryInfo = queryInfo;
	}

	/**
	 * <h3 class="en-US">Getter method for query information instance object</h3>
	 * <h3 class="zh-CN">查询信息实例对象的Getter方法</h3>
	 *
	 * @return <span class="en-US">Query information instance object</span>
	 * <span class="zh-CN">查询信息实例对象</span>
	 */
	public QueryInfo getQueryInfo() {
		return this.queryInfo;
	}
}
