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

package org.nervousync.magi.annotations.query;

import jakarta.persistence.LockModeType;
import org.nervousync.commons.Globals;
import org.nervousync.magi.annotations.query.join.JoinEntities;

import java.lang.annotation.*;

/**
 * <h2 class="en-US">The annotation of query result set</h2>
 * <h2 class="zh-CN">查询结果集的注解</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2023 15:21:18 $
 */
@SuppressWarnings("unused")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ResultSet {

    /**
     * <h3 class="en-US">Obtain the query name</h3>
     * <h3 class="zh-CN">获取查询名称</h3>
     *
     * @return <span class="en-US">Query name</span>
     * <span class="zh-CN">查询名称</span>
     */
    String name() default Globals.DEFAULT_VALUE_STRING;

	/**
	 * <h3 class="en-US">Obtain the query driven table entity class</h3>
	 * <h3 class="zh-CN">获取查询驱动表实体类</h3>
	 *
	 * @return <span class="en-US">Query driven table entity class</span>
	 * <span class="zh-CN">查询驱动表实体类</span>
	 */
	Class<?> mainEntity();

    /**
     * <h3 class="en-US">Obtain the related query information</h3>
     * <h3 class="zh-CN">获取关联查询信息</h3>
     *
	 * @return <span class="en-US">Related query information</span>
	 * <span class="zh-CN">关联查询信息</span>
     */
    JoinEntities[] joinConfigs() default {};

    /**
     * <h3 class="en-US">Obtain the sort data column definition array</h3>
     * <h3 class="zh-CN">获取排序数据列定义数组</h3>
     *
     * @return <span class="en-US">Sort data column definition array</span>
     * <span class="zh-CN">排序数据列定义数组</span>
     */
    OrderColumn[] orderColumns() default {};

    /**
     * <h3 class="en-US">Obtain the group data column definition array</h3>
     * <h3 class="zh-CN">获取分组数据列定义数组</h3>
     *
     * @return <span class="en-US">Group data column definition array</span>
     * <span class="zh-CN">分组数据列定义数组</span>
     */
    GroupColumn[] groupColumns() default {};

    /**
     * <h3 class="en-US">Obtain the query result can cacheable</h3>
     * <h3 class="zh-CN">获取查询结果可以缓存</h3>
     *
     * @return <span class="en-US">Query result can cacheable</span>
     * <span class="zh-CN">查询结果可以缓存</span>
     */
    boolean cacheables() default false;
    /**
     * <h3 class="en-US">Obtain the query record lock option</h3>
     * <h3 class="zh-CN">获取查询记录锁定选项</h3>
     *
     * @return <span class="en-US">Query record lock option</span>
     * <span class="zh-CN">查询记录锁定选项</span>
     */
    LockModeType lockOption() default LockModeType.NONE;
}
