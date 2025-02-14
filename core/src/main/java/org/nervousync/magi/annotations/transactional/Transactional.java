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
package org.nervousync.magi.annotations.transactional;

import org.nervousync.commons.Globals;
import org.nervousync.magi.enumerations.transactional.Isolation;

import java.lang.annotation.*;

/**
 * <h2 class="en-US">The annotation of transactional configure</h2>
 * <h2 class="zh-CN">事务配置信息的注解</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Mar 30, 2016 15:45:19 $
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Transactional {
    /**
     * @return <span class="en-US">The timeout value of transactional</span>
     * <span class="zh-CN">事务的超时时间</span>
     */
    int timeout() default Globals.DEFAULT_VALUE_INT;

    /**
     * @return <span class="en-US">The isolation value of transactional</span>
     * <span class="zh-CN">事务的等级代码</span>
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * @return <span class="en-US">The rollback exception class of transactional</span>
     * <span class="zh-CN">事务的回滚异常</span>
     */
    Class<?>[] rollbackFor() default {};
}
