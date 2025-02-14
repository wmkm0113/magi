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

import java.util.Map;

/**
 * <h2 class="en-US">Data merge step</h2>
 * <h2 class="zh-CN">数据合并步骤</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: 10/28/2020 17:02 $
 */
public final class MergeStep extends AbstractStep {

	/**
	 * <span class="en-US">Main query information identify code</span>
	 * <span class="zh-CN">主信息查询识别代码</span>
	 */
	private final long mainCode;
	/**
	 * <span class="en-US">Merge query information identify code</span>
	 * <span class="zh-CN">合并信息查询识别代码</span>
	 */
	private final long mergeCode;
	/**
	 * <span class="en-US">Merge match information mapping</span>
	 * <span class="zh-CN">合并条件映射表</span>
	 */
	private final Map<String, String> mappingKeys;
	/**
	 * <span class="en-US">Pager begin index</span>
	 * <span class="zh-CN">分页起始索引值</span>
	 */
	private final int beginIndex;
	/**
	 * <span class="en-US">Pager end index</span>
	 * <span class="zh-CN">分页终止索引值</span>
	 */
	private final int endIndex;

	/**
	 * <h3 class="en-US">Constructor method for data merge step</h3>
	 * <h3 class="zh-CN">数据合并步骤的构造方法</h3>
	 *
	 * @param mainCode    <span class="en-US">Main query information identify code</span>
	 *                    <span class="zh-CN">主信息查询识别代码</span>
	 * @param mergeCode   <span class="en-US">Merge query information identify code</span>
	 *                    <span class="zh-CN">合并信息查询识别代码</span>
	 * @param mappingKeys <span class="en-US">Merge match information mapping</span>
	 *                    <span class="zh-CN">合并条件映射表</span>
	 * @param beginIndex  <span class="en-US">Pager begin index</span>
	 *                    <span class="zh-CN">分页起始索引值</span>
	 * @param endIndex    <span class="en-US">Pager end index</span>
	 *                    <span class="zh-CN">分页终止索引值</span>
	 */
	public MergeStep(final long mainCode, final long mergeCode, @Nonnull final Map<String, String> mappingKeys,
	                 final int beginIndex, final int endIndex) {
		super(StepType.Merge);
		this.mainCode = mainCode;
		this.mergeCode = mergeCode;
		this.mappingKeys = mappingKeys;
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
	}

	/**
	 * <h3 class="en-US">Getter method for main query information identify code</h3>
	 * <h3 class="zh-CN">主信息查询识别代码的Getter方法</h3>
	 *
	 * @return <span class="en-US">Main query information identify code</span>
	 * <span class="zh-CN">主信息查询识别代码</span>
	 */
	public long getMainCode() {
		return this.mainCode;
	}

	/**
	 * <h3 class="en-US">Getter method for merge query information identify code</h3>
	 * <h3 class="zh-CN">合并信息查询识别代码的Getter方法</h3>
	 *
	 * @return <span class="en-US">Merge query information identify code</span>
	 * <span class="zh-CN">合并信息查询识别代码</span>
	 */
	public long getMergeCode() {
		return this.mergeCode;
	}

	/**
	 * <h3 class="en-US">Getter method for merge match information mapping</h3>
	 * <h3 class="zh-CN">合并条件映射表的Getter方法</h3>
	 *
	 * @return <span class="en-US">Merge match information mapping</span>
	 * <span class="zh-CN">合并条件映射表</span>
	 */
	public Map<String, String> getMappingKeys() {
		return this.mappingKeys;
	}

	/**
	 * <h3 class="en-US">Getter method for pager begin index</h3>
	 * <h3 class="zh-CN">分页起始索引值的Getter方法</h3>
	 *
	 * @return <span class="en-US">Pager begin index</span>
	 * <span class="zh-CN">分页起始索引值</span>
	 */
	public int getBeginIndex() {
		return this.beginIndex;
	}

	/**
	 * <h3 class="en-US">Getter method for pager end index</h3>
	 * <h3 class="zh-CN">分页终止索引值的Getter方法</h3>
	 *
	 * @return <span class="en-US">Pager end index</span>
	 * <span class="zh-CN">分页终止索引值</span>
	 */
	public int getEndIndex() {
		return this.endIndex;
	}
}
