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
package org.nervousync.magi.enumerations.transactional;

import org.intellij.lang.annotations.MagicConstant;

import java.sql.Connection;

/**
 * <h2 class="en-US">Enumeration value of transactional isolation</h2>
 * <h2 class="zh-CN">事务等级的枚举值</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Mar 30, 2016 15:52:00 $
 */
@SuppressWarnings("unused")
public enum Isolation {

	/**
	 * <span class="en-US">Default</span>
	 * <span class="zh-CN">默认</span>
	 */
	DEFAULT(Connection.TRANSACTION_READ_COMMITTED),
	/**
	 * <span class="en-US">Read uncommitted</span>
	 * <span class="zh-CN">读未提交</span>
	 */
	ISOLATION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),

	/**
	 * <span class="en-US">Read committed</span>
	 * <span class="zh-CN">读已提交</span>
	 */
	ISOLATION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

	/**
	 * <span class="en-US">Repeatable read</span>
	 * <span class="zh-CN">重复读</span>
	 */
	ISOLATION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ), 

	/**
	 * <span class="en-US">Serializable</span>
	 * <span class="zh-CN">序列</span>
	 */
	ISOLATION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

	/**
	 * <span class="en-US">Transactional level code</span>
	 * <span class="zh-CN">事务等级代码</span>
	 */
	@MagicConstant(valuesFromClass = Connection.class)
	private final int transactionLevel;

	/**
	 * <span class="en-US">Constructor method for enumeration value of transactional isolation</span>
	 * <span class="zh-CN">事务等级枚举值的构建方法</span>
	 *
	 * @param transactionLevel <span class="en-US">Transactional level code</span>
	 *                         <span class="zh-CN">事务等级代码</span>
	 */
	Isolation(@MagicConstant(valuesFromClass = Connection.class) final int transactionLevel) {
		this.transactionLevel = transactionLevel;
	}

	/**
	 * <span class="en-US">Obtain transactional level code</span>
	 * <span class="zh-CN">获取事务等级代码</span>
	 *
	 * @return <span class="en-US">Transactional level code</span>
	 * <span class="zh-CN">事务等级代码</span>
	 */
	@MagicConstant(valuesFromClass = Connection.class)
	public int value() {
		return this.transactionLevel;
	}
}
