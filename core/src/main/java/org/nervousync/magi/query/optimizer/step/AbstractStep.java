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

import jakarta.annotation.Nonnull;
import org.nervousync.commons.Globals;
import org.nervousync.utils.ClassUtils;
import org.nervousync.utils.IDUtils;

import java.sql.SQLException;
import java.sql.Wrapper;

/**
 * <h2 class="en-US">Abstract class for query step</h2>
 * <h2 class="zh-CN">查询步骤抽象类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: 10/28/2020 17:02 $
 */
public abstract class AbstractStep implements Comparable<AbstractStep>, Wrapper {

	/**
	 * <span class="en-US">Step identify code</span>
	 * <span class="zh-CN">步骤识别代码</span>
	 */
	private final long stepCode;
	/**
	 * <span class="en-US">Enumeration value of query step type</span>
	 * <span class="zh-CN">查询步骤类型枚举值</span>
	 */
	private final StepType stepType;
	/**
	 * <span class="en-US">Sort code</span>
	 * <span class="zh-CN">排序代码</span>
	 */
	private int sortCode = Globals.INITIALIZE_INT_VALUE;

	/**
	 * <h3 class="en-US">Constructor method for abstract class for query step</h3>
	 * <h3 class="zh-CN">查询步骤抽象类的构造方法</h3>
	 *
	 * @param stepType <span class="en-US">Enumeration value of query step type</span>
	 *                 <span class="zh-CN">查询步骤类型枚举值</span>
	 */
	protected AbstractStep(final StepType stepType) {
		this.stepCode = IDUtils.snowflake();
		this.stepType = stepType;
	}

	/**
	 * <h3 class="en-US">Getter method for step identify code</h3>
	 * <h3 class="zh-CN">步骤识别代码的Getter方法</h3>
	 *
	 * @return <span class="en-US">Step identify code</span>
	 * <span class="zh-CN">步骤识别代码</span>
	 */
	public final long getStepCode() {
		return this.stepCode;
	}

	/**
	 * <h3 class="en-US">Getter method for enumeration value of query step type</h3>
	 * <h3 class="zh-CN">查询步骤类型枚举值的Getter方法</h3>
	 *
	 * @return <span class="en-US">Enumeration value of query step type</span>
	 * <span class="zh-CN">查询步骤类型枚举值</span>
	 */
	public final StepType getStepType() {
		return this.stepType;
	}

	/**
	 * <h3 class="en-US">Getter method for sort code</h3>
	 * <h3 class="zh-CN">排序代码的Getter方法</h3>
	 *
	 * @return <span class="en-US">Sort code</span>
	 * <span class="zh-CN">排序代码</span>
	 */
	public int getSortCode() {
		return this.sortCode;
	}

	/**
	 * <h3 class="en-US">Setter method for sort code</h3>
	 * <h3 class="zh-CN">排序代码的Setter方法</h3>
	 *
	 * @param sortCode <span class="en-US">Sort code</span>
	 *                 <span class="zh-CN">排序代码</span>
	 */
	public void setSortCode(final int sortCode) {
		this.sortCode = sortCode;
	}

	@Override
	public final <T> T unwrap(final Class<T> clazz) throws SQLException {
		try {
			return clazz.cast(this);
		} catch (ClassCastException e) {
			throw new SQLException(e);
		}
	}

	@Override
	public final boolean isWrapperFor(final Class<?> clazz) {
		return ClassUtils.isAssignable(clazz, this.getClass());
	}

	@Override
	public int compareTo(@Nonnull final AbstractStep o) {
		if (this.sortCode == o.getSortCode()) {
			return Long.compare(this.stepCode, o.getStepCode());
		}
		return Integer.compare(this.sortCode, o.getSortCode());
	}

	/**
	 * <h2 class="en-US">Enumeration class for query step type</h2>
	 * <h2 class="zh-CN">查询步骤类型枚举类</h2>
	 *
	 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
	 * @version $Revision: 1.0 $ $Date: 10/28/2020 17:02 $
	 */
	public enum StepType {
		/**
		 * <span class="en-US">Data query step</span>
		 * <span class="zh-CN">数据查询步骤</span>
		 */
		Query,
		/**
		 * <span class="en-US">Data merge step</span>
		 * <span class="zh-CN">数据合并步骤</span>
		 */
		Merge
	}
}
