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

import jakarta.persistence.LockModeType;
import org.nervousync.brain.enumerations.ddl.DropOption;
import org.nervousync.magi.annotations.sharding.Sharding;

import java.lang.annotation.*;

/**
 * <h2 class="en-US">The annotation of data record options</h2>
 * <h2 class="zh-CN">数据记录选项的注解</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Apr 11, 2018 17:36:43 $
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Options {

    /**
     * @return <span class="en-US">Cacheable data</span>
     * <span class="zh-CN">缓存数据</span>
     */
    boolean cacheable() default true;

    /**
     * @return <span class="en-US">Lock option</span>
     * <span class="zh-CN">数据锁选项</span>
     */
    LockModeType lockOption() default LockModeType.NONE;

    /**
     * @return <span class="en-US">Drop option</span>
     * <span class="zh-CN">删除选项</span>
     */
    DropOption dropOption() default DropOption.NONE;

    /**
     * @return <span class="en-US">Database sharding configure</span>
     * <span class="zh-CN">数据库分片配置</span>
     */
    Sharding databaseSharding() default @Sharding;

    /**
     * @return <span class="en-US">Table sharding configure</span>
     * <span class="zh-CN">数据表分片配置</span>
     */
    Sharding tableSharding() default @Sharding;
}
