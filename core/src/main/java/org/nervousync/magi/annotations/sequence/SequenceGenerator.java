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
package org.nervousync.magi.annotations.sequence;

import org.nervousync.commons.Globals;

import java.lang.annotation.*;

/**
 * <h2 class="en-US">The annotation of sequence generator configure information</h2>
 * <h2 class="zh-CN">序列生成器的配置信息</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Apr 11, 2018 16:12:43 $
 */
@SuppressWarnings("unused")
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SequenceGenerator {
    /**
     * <h3 class="en-US">Obtain the sequence name</h3>
     * <h3 class="zh-CN">获取序列名称</h3>
     *
     * @return <span class="en-US">Sequence name</span>
     * <span class="zh-CN">序列名称</span>
     */
    String name();

    /**
     * <h3 class="en-US">Obtain the minimum value of sequence</h3>
     * <h3 class="zh-CN">获取序列的最小值</h3>
     *
     * @return <span class="en-US">Minimum value of sequence</span>
     * <span class="zh-CN">序列的最小值</span>
     */
    int min() default Globals.INITIALIZE_INT_VALUE;

    /**
     * <h3 class="en-US">Obtain the maximum value of sequence</h3>
     * <h3 class="zh-CN">获取序列的最大值</h3>
     *
     * @return <span class="en-US">Maximum value of sequence</span>
     * <span class="zh-CN">序列的最大值</span>
     */
    int max() default Globals.DEFAULT_VALUE_INT;

    /**
     * <h3 class="en-US">Obtain the step value of sequence</h3>
     * <h3 class="zh-CN">获取序列的步进值</h3>
     *
     * @return <span class="en-US">Step value of sequence</span>
     * <span class="zh-CN">序列的步进值</span>
     */
    int step();

    /**
     * <h3 class="en-US">Obtain the initialize value of sequence</h3>
     * <h3 class="zh-CN">获取序列的初始值</h3>
     *
     * @return <span class="en-US">Initialize value of sequence</span>
     * <span class="zh-CN">序列的初始值</span>
     */
    int init() default Globals.INITIALIZE_INT_VALUE;

    /**
     * <h3 class="en-US">Obtain the sequence is cycle</h3>
     * <h3 class="zh-CN">获取序列值是否循环</h3>
     *
     * @return <span class="en-US">Sequence is cycle, default value is <code>false</code></span>
     * <span class="zh-CN">序列值是否循环，默认值为<code>false</code></span>
     */
    boolean cycle() default false;

}
