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

package org.nervousync.magi.annotations.sharding;

import org.nervousync.brain.sharding.Calculator;
import org.nervousync.commons.Globals;

import java.lang.annotation.*;

/**
 * <h2 class="en-US">Sharding configure information</h2>
 * <h2 class="zh-CN">分片配置信息</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Apr 11, 2018 17:36:43 $
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Sharding {

	/**
	 * <h3 class="en-US">Obtain the sharding default value</h3>
	 * <h3 class="zh-CN">获取分片默认值</h3>
	 *
     * @return <span class="en-US">Sharding default value</span>
     * <span class="zh-CN">分片默认值</span>
	 */
	String value() default Globals.DEFAULT_VALUE_STRING;

	/**
	 * <h3 class="en-US">Obtain the sharding column name</h3>
	 * <h3 class="zh-CN">获取分片数据列名称</h3>
	 *
     * @return <span class="en-US">Sharding column name</span>
     * <span class="zh-CN">分片数据列名称</span>
	 */
	String[] columns() default {};

	/**
	 * <h3 class="en-US">Obtain the sharding calculate utility class</h3>
	 * <h3 class="zh-CN">获取分片计算工具类</h3>
	 *
     * @return <span class="en-US">Sharding calculate utility class</span>
     * <span class="zh-CN">分片计算工具类</span>
	 */
	Class<?> calculatorClass() default Calculator.class;
}
