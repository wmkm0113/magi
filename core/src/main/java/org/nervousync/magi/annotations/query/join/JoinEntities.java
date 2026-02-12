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

package org.nervousync.magi.annotations.query.join;

import org.nervousync.brain.enumerations.query.JoinType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <h2 class="en-US">The annotation of query join information</h2>
 * <h2 class="zh-CN">关联信息的注解</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2023 15:21:18 $
 */
@SuppressWarnings("unused")
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinEntities {

	/**
	 * <h3 class="en-US">Obtain the join type</h3>
	 * <h3 class="zh-CN">获取关联类型</h3>
	 *
     * @return <span class="en-US">Join type</span>
     * <span class="zh-CN">关联类型</span>
	 */
	JoinType type();

	/**
	 * <h3 class="en-US">Obtain the driver table entity class</h3>
	 * <h3 class="zh-CN">获取驱动表实体类</h3>
	 *
     * @return <span class="en-US">Driver table entity class</span>
     * <span class="zh-CN">驱动表实体类</span>
	 */
	Class<?> mainEntity();

	/**
	 * <h3 class="en-US">Obtain the association table entity class</h3>
	 * <h3 class="zh-CN">获取关联表实体类</h3>
	 *
     * @return <span class="en-US">Association table entity class</span>
     * <span class="zh-CN">关联表实体类</span>
	 */
	Class<?> referenceEntity();

	/**
	 * <h3 class="en-US">Obtain the association information array</h3>
	 * <h3 class="zh-CN">获取关联信息数组</h3>
	 *
     * @return <span class="en-US">Association information array</span>
     * <span class="zh-CN">关联信息数组</span>
	 */
	JoinKey[] keys();
}
