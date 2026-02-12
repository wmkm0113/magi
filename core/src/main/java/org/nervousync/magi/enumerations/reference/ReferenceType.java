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

package org.nervousync.magi.enumerations.reference;

/**
 * <h2 class="en-US">Enumeration value of reference types</h2>
 * <h2 class="zh-CN">关联类型枚举值</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Sep 12, 2023 15:16:08 $
 */
public enum ReferenceType {
	/**
     * <span class="en-US">Undefined</span>
     * <span class="zh-CN">未定义</span>
	 */
	Undefined,
	/**
     * <span class="en-US">Many to one</span>
     * <span class="zh-CN">多对一</span>
	 */
	ManyToOne,
	/**
     * <span class="en-US">Many to many</span>
     * <span class="zh-CN">多对多</span>
	 */
	ManyToMany,
	/**
     * <span class="en-US">One to one</span>
     * <span class="zh-CN">一对一</span>
	 */
	OneToOne,
	/**
     * <span class="en-US">One to many</span>
     * <span class="zh-CN">一对多</span>
	 */
	OneToMany
}
