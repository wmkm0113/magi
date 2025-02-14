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

package org.nervousync.magi.query.optimizer;

import jakarta.annotation.Nonnull;
import org.nervousync.commons.Globals;
import org.nervousync.magi.query.optimizer.step.AbstractStep;

import java.util.List;

/**
 * <h2 class="en-US">Query optimize result</h2>
 * <h2 class="zh-CN">查询优化结果</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: 10/28/2020 17:02 $
 */
public final class OptimizedResult {

	/**
	 * <span class="en-US">Current step index value</span>
	 * <span class="zh-CN">当前步骤索引值</span>
	 */
	private int stepIndex = Globals.INITIALIZE_INT_VALUE;
	/**
	 * <span class="en-US">Query step information list</span>
	 * <span class="zh-CN">查询步骤信息列表</span>
	 */
	private final List<AbstractStep> stepList;

	/**
	 * <h3 class="en-US">Constructor method for query optimize result</h3>
	 * <h3 class="zh-CN">查询优化结果构造方法</h3>
	 *
	 * @param stepList <span class="en-US">Query step information list</span>
	 *                 <span class="zh-CN">查询步骤信息列表</span>
	 */
	OptimizedResult(@Nonnull final List<AbstractStep> stepList) {
		this.stepList = stepList;
		if (this.stepList.size() > 1) {
			this.stepList.sort(AbstractStep::compareTo);
		}
	}

	/**
	 * <h3 class="en-US">Whether the current step is the last step</h3>
	 * <h3 class="zh-CN">当前步骤是否为最后一步</h3>
	 *
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 */
	public boolean finalStep() {
		return this.stepIndex == this.stepList.size();
	}

	/**
	 * <h3 class="en-US">Get the next query step information. If there is no next step, return <code>null</code></h3>
	 * <h3 class="zh-CN">获取下一个查询步骤信息，如果没有下一步，则返回<code>null</code></h3>
	 *
	 * @return <span class="en-US">Query step information</span>
	 * <span class="zh-CN">查询步骤信息</span>
	 */
	public AbstractStep nextStep() {
		AbstractStep step = null;
		if (this.stepIndex < this.stepList.size()) {
			step = this.stepList.get(this.stepIndex);
			this.stepIndex++;
		}
		return step;
	}
}
