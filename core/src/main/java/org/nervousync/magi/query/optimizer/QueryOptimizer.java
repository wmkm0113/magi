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

import org.nervousync.brain.query.QueryInfo;
import org.nervousync.magi.query.optimizer.step.QueryStep;

import java.util.Collections;

/**
 * <h2 class="en-US">Query optimizer interface</h2>
 * <h2 class="zh-CN">查询优化器接口</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: 10/28/2020 17:02 $
 */
public interface QueryOptimizer {

	/**
	 * <h3 class="en-US">Analyze and optimize query plan</h3>
	 * <h3 class="zh-CN">分析优化查询步骤</h3>
	 *
	 * @param queryInfo <span class="en-US">Query information instance object</span>
	 *                  <span class="zh-CN">查询信息实例对象</span>
	 * @return <span class="en-US">Query optimize result</span>
	 * <span class="zh-CN">查询优化结果</span>
	 */
	OptimizedResult optimize(final QueryInfo queryInfo);

	/**
	 * <h2 class="en-US">Default implementation class of query optimizer</h2>
	 * <h2 class="zh-CN">默认的查询优化器实现类</h2>
	 *
	 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
	 * @version $Revision: 1.0 $ $Date: 10/28/2020 17:02 $
	 */
	final class DefaultOptimizer implements QueryOptimizer {

		@Override
		public OptimizedResult optimize(final QueryInfo queryInfo) {
			return new OptimizedResult(Collections.singletonList(new QueryStep(queryInfo)));
		}
	}
}
