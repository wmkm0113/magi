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

package org.nervousync.magi.commons;

/**
 * <h2 class="en-US">Constant value define</h2>
 * <h2 class="zh-CN">常量定义</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Feb 27, 2018 10:21:28 $
 */
public final class MagiGlobals {

	/**
	 * <span class="en-US">Cache name</span>
	 * <span class="zh-CN">缓存名称</span>
	 */
	public static final String CACHE_NAME = "Magi_Cache";

	public static final Integer OPERATE_CODE_CREATE = 0;
	public static final Integer OPERATE_CODE_UPDATE = 1;
	public static final Integer OPERATE_CODE_DELETE = 2;
	public static final Integer OPERATE_CODE_RECOVERY = 3;
	public static final Integer OPERATE_CODE_CONFIRM = 4;
	public static final Integer OPERATE_CODE_REJECT = 5;
	public static final Integer OPERATE_CODE_LOCK = 6;
	public static final Integer OPERATE_CODE_UNLOCK = 7;

	private MagiGlobals() {
	}
}
