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
package org.nervousync.magi.entity;

import jakarta.annotation.Nonnull;
import jakarta.persistence.MappedSuperclass;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import org.nervousync.annotations.beans.OutputConfig;
import org.nervousync.enumerations.beans.StringType;
import org.nervousync.magi.entity.log.RecordLogger;
import org.nervousync.utils.id.IDUtils;

import java.io.Serializable;
import java.util.List;

/**
 * <h2 class="en-US">Abstract Entity Class</h2>
 * <h2 class="zh-CN">实体类抽象父类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Jan 9, 2018 10:21:06 $
 */
@SuppressWarnings("unused")
@OutputConfig(type = StringType.JSON)
@MappedSuperclass
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseObject implements Serializable {

	/**
	 * <span class="en-US">Serial version UID</span>
	 * <span class="zh-CN">序列化UID</span>
	 */
	private static final long serialVersionUID = -1860456342847902560L;

	/**
	 * <span class="en-US">Persistence identified code</span>
	 * <span class="zh-CN">持久化识别代码</span>
	 */
	private final long persistenceCode = IDUtils.snowflake();

	/**
	 * <h3 class="en-US">Save the current entity class to the database</h3>
	 * <h3 class="zh-CN">保存当前实体类到数据库</h3>
	 *
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public final void save() throws Exception {
		this.save(null);
	}

	/**
	 * <h3 class="en-US">Update the current entity class to the database</h3>
	 * <h3 class="zh-CN">更新当前实体类到数据库</h3>
	 *
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public final void update() throws Exception {
		this.update(null, null);
	}

	/**
	 * <h3 class="en-US">Save the current entity class to the database</h3>
	 * <h3 class="zh-CN">保存当前实体类到数据库</h3>
	 *
	 * @param operateUser <span class="en-US">Operate user identified code</span>
	 *                    <span class="zh-CN">操作人识别代码</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public final void save(final Long operateUser) throws Exception {
		EntityFactory.getInstance().saveRecord(this, operateUser);
	}

	/**
	 * <h3 class="en-US">Update the current entity class to the database</h3>
	 * <h3 class="zh-CN">更新当前实体类到数据库</h3>
	 *
	 * @param operateUser <span class="en-US">Operate user identified code</span>
	 *                    <span class="zh-CN">操作人识别代码</span>
	 * @param operateCode <span class="en-US">Operate code</span>
	 *                    <span class="zh-CN">操作代码</span>
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public final void update(final Long operateUser, final Integer operateCode) throws Exception {
		EntityFactory.getInstance().updateRecord(this, operateUser, operateCode);
	}

	/**
	 * <h3 class="en-US">Delete the record information corresponding to the current entity class from the database</h3>
	 * <h3 class="zh-CN">从数据库中删除当前实体类对应的记录信息</h3>
	 *
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public final void delete() throws Exception {
		EntityFactory.getInstance().deleteRecord(this);
	}

	/**
	 * <h3 class="en-US">Refresh the current entity class to the database</h3>
	 * <h3 class="zh-CN">刷新当前实体类到数据库</h3>
	 *
	 * @throws Exception <span class="en-US">If an error occurs during operation</span>
	 *                   <span class="zh-CN">如果操作过程中出错</span>
	 */
	public final void refresh() throws Exception {
		EntityFactory.getInstance().refreshRecord(this);
	}

	/**
	 * <h3 class="en-US">Read sensitive data</h3>
	 * <h3 class="zh-CN">读取敏感信息</h3>
	 *
	 * @param userCode <span class="en-US">Identify code of the reader</span>
	 *                 <span class="zh-CN">读取人的识别代码</span>
	 */
	public final void sensitiveData(@Nonnull final String userCode) {
		EntityFactory.getInstance().sensitiveData(this, userCode);
	}

	/**
	 * <h3 class="en-US">Getter method for persistence identified code</h3>
	 * <h3 class="zh-CN">持久化识别代码的Getter方法</h3>
	 *
	 * @return <span class="en-US">Persistence identified code</span>
	 * <span class="zh-CN">持久化识别代码</span>
	 */
	public long identifiedCode() {
		return this.persistenceCode;
	}


	/**
	 * <h3 class="en-US">Read data record operate log list</h3>
	 * <h3 class="zh-CN">读取数据记录操作日志</h3>
	 *
	 * @return <span class="en-US">Operate log information list</span>
	 * <span class="zh-CN">操作日志信息列表</span>
	 */
	public List<RecordLogger> recordLogs() {
		return EntityFactory.getInstance().recordLogs(this);
	}
}
