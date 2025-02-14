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

import org.nervousync.brain.enumerations.ddl.GenerationType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <h2 class="en-US">The annotation of data generated value</h2>
 * <h2 class="zh-CN">数据自动生成器注解</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Apr 11, 2018 17:36:43 $
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RUNTIME)
public @interface GeneratedData {

    /**
     * @return <span class="en-US">Generation type</span>
     * <span class="zh-CN">生成器类型</span>
     */
    GenerationType type() default GenerationType.ASSIGNED;

    /**
     * @return <span class="en-US">Generator name</span>
     * <span class="zh-CN">生成器名称</span>
     */
    String generator() default "";
}
