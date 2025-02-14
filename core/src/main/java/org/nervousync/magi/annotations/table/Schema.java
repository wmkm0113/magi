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

package org.nervousync.magi.annotations.table;

import org.nervousync.brain.enumerations.dialect.DialectType;

import java.lang.annotation.*;

/**
 * <h2 class="en-US">Annotations for the data source to which the data table belongs</h2>
 * <h2 class="zh-CN">数据表所属数据源的注解</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Apr 11, 2018 17:36:43 $
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Schema {

	/**
	 * @return <span class="en-US">Data source name</span>
	 * <span class="zh-CN">数据源名称</span>
	 */
	String name();

	/**
	 * @return <span class="en-US">Data source dialect type enumeration value</span>
	 * <span class="zh-CN">数据源方言类型枚举值</span>
	 */
	DialectType type() default DialectType.Default;
}
