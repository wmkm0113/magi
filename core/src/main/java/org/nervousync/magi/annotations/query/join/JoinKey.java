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

import org.nervousync.enumerations.core.ConnectionCode;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <h2 class="en-US">The annotation of query join columns information</h2>
 * <h2 class="zh-CN">关联数据列的注解</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 15, 2023 15:21:18 $
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinKey {

    /**
     * @return <span class="en-US">Connection code</span>
     * <span class="zh-CN">连接代码</span>
     */
	ConnectionCode connection() default ConnectionCode.AND;

    /**
     * @return <span class="en-US">Main table column information</span>
     * <span class="zh-CN">主表数据列</span>
     */
	String mainKey();

    /**
     * @return <span class="en-US">Related table column information</span>
     * <span class="zh-CN">关联数据表数据列</span>
     */
	String referenceKey();

}
