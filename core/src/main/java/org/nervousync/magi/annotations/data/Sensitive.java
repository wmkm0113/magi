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

package org.nervousync.magi.annotations.data;

import org.nervousync.commons.Globals;
import org.nervousync.magi.enumerations.sensitive.SensitiveType;

import java.lang.annotation.*;

/**
 * <h2 class="en-US">Annotations for sensitive information data columns</h2>
 * <h2 class="zh-CN">敏感信息数据列的注解</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Sep 12, 2023 18:17:43 $
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Sensitive {

    /**
     * <h3 class="en-US">Obtain the sensitive information types</h3>
     * <h3 class="zh-CN">获取敏感信息类型</h3>
     *
     * @return <span class="en-US">Sensitive information types</span>
     * <span class="zh-CN">敏感信息类型</span>
     */
    SensitiveType type() default SensitiveType.NORMAL;

    /**
     * <h3 class="en-US">Obtain the encryption result saving field name</h3>
     * <h3 class="zh-CN">获取加密结果保存属性</h3>
     *
     * @return <span class="en-US">Encryption result saving field name</span>
     * <span class="zh-CN">加密结果保存属性</span>
     */
    String encField() default Globals.DEFAULT_VALUE_STRING;

    /**
     * <h3 class="en-US">Obtain the security configuration name to use</h3>
     * <h3 class="zh-CN">获取使用的安全配置名称</h3>
     *
     * @return <span class="en-US">Security configuration name to use</span>
     * <span class="zh-CN">使用的安全配置名称</span>
     */
    String secureName() default Globals.DEFAULT_VALUE_STRING;
}
