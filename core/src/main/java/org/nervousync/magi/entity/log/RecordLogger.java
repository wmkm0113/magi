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

package org.nervousync.magi.entity.log;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlElement;
import org.nervousync.brain.enumerations.ddl.GenerationType;
import org.nervousync.magi.annotations.table.GeneratedData;
import org.nervousync.magi.annotations.table.Options;
import org.nervousync.magi.entity.BaseObject;
import org.nervousync.utils.id.IDUtils;

/**
 * <h2 class="en-US">Record operate log</h2>
 * <h2 class="zh-CN">数据操作日志</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Jan 9, 2018 10:21:06 $
 */
@Options
@Table(name = "NSYC_Record_Operate_Log")
public final class RecordLogger extends BaseObject {

	/**
	 * <span class="en-US">Serial version UID</span>
	 * <span class="zh-CN">序列化UID</span>
	 */
	private static final long serialVersionUID = 7918492002362209869L;

	/**
	 * <span class="en-US">Log identified code</span>
	 * <span class="zh-CN">日志识别代码</span>
	 */
	@Id
	@GeneratedData(type = GenerationType.GENERATE, generator = IDUtils.SNOWFLAKE)
	@Column(nullable = false, updatable = false)
	@XmlElement(name = "log_code")
	private Long logCode;
	/**
	 * <span class="en-US">Record identified code</span>
	 * <span class="zh-CN">记录识别代码</span>
	 */
	@Column(nullable = false, updatable = false, length = 64)
	@XmlElement(name = "record_identifier")
	private String recordIdentifier;
	/**
	 * <span class="en-US">Data table identified code</span>
	 * <span class="zh-CN">数据表识别代码</span>
	 */
	@Column(nullable = false, updatable = false, length = 64)
	@XmlElement(name = "table_identifier")
	private String tableIdentifier;
	/**
	 * <span class="en-US">Operate code</span>
	 * <span class="zh-CN">操作代码</span>
	 */
	@Column(nullable = false, updatable = false)
	@XmlElement(name = "operate_code")
	private Integer operateCode;
	/**
	 * <span class="en-US">Operate user identified code</span>
	 * <span class="zh-CN">操作人识别代码</span>
	 */
	@Column(nullable = false, updatable = false)
	@XmlElement(name = "operate_user")
	private Long operateUser;
	/**
	 * <span class="en-US">Operate timestamp</span>
	 * <span class="zh-CN">操作时间戳</span>
	 */
	@Column(nullable = false, updatable = false)
	@XmlElement(name = "operate_timestamp")
	private Long operateTimestamp;

	/**
	 * <h3 class="en-US">Constructor method for the record operate log</h3>
	 * <h3 class="zh-CN">数据操作日志的构造方法</h3>
	 */
	public RecordLogger() {
	}

	/**
	 * <h3 class="en-US">Getter method for the log identified code</h3>
	 * <h3 class="zh-CN">日志识别代码的 Getter 方法</h3>
	 *
	 * @return <span class="en-US">Log identified code</span>
	 * <span class="zh-CN">日志识别代码</span>
	 */
	public Long getLogCode() {
		return this.logCode;
	}

	/**
	 * <h3 class="en-US">Setter method for the log identified code</h3>
	 * <h3 class="zh-CN">日志识别代码的 Setter 方法</h3>
	 *
	 * @param logCode <span class="en-US">Log identified code</span>
	 *                <span class="zh-CN">日志识别代码</span>
	 */
	public void setLogCode(final Long logCode) {
		this.logCode = logCode;
	}

	/**
	 * <h3 class="en-US">Getter method for the record identified code</h3>
	 * <h3 class="zh-CN">记录识别代码的 Getter 方法</h3>
	 *
	 * @return <span class="en-US">Record identified code</span>
	 * <span class="zh-CN">记录识别代码</span>
	 */
	public String getRecordIdentifier() {
		return this.recordIdentifier;
	}

	/**
	 * <h3 class="en-US">Setter method for the record identified code</h3>
	 * <h3 class="zh-CN">记录识别代码的 Setter 方法</h3>
	 *
	 * @param recordIdentifier <span class="en-US">Record identified code</span>
	 *                   <span class="zh-CN">记录识别代码</span>
	 */
	public void setRecordIdentifier(final String recordIdentifier) {
		this.recordIdentifier = recordIdentifier;
	}

	/**
	 * <h3 class="en-US">Getter method for the data table identified code</h3>
	 * <h3 class="zh-CN">数据表识别代码的 Getter 方法</h3>
	 *
	 * @return <span class="en-US">Data table identified code</span>
	 * <span class="zh-CN">数据表识别代码</span>
	 */
	public String getTableIdentifier() {
		return this.tableIdentifier;
	}

	/**
	 * <h3 class="en-US">Setter method for the data table identified code</h3>
	 * <h3 class="zh-CN">数据表识别代码的 Setter 方法</h3>
	 *
	 * @param tableIdentifier <span class="en-US">Data table identified code</span>
	 *                  <span class="zh-CN">数据表识别代码</span>
	 */
	public void setTableIdentifier(final String tableIdentifier) {
		this.tableIdentifier = tableIdentifier;
	}

	/**
	 * <h3 class="en-US">Getter method for the operate code</h3>
	 * <h3 class="zh-CN">操作代码的 Getter 方法</h3>
	 *
	 * @return <span class="en-US">Operate code</span>
	 * <span class="zh-CN">操作代码</span>
	 */
	public Integer getOperateCode() {
		return this.operateCode;
	}

	/**
	 * <h3 class="en-US">Setter method for the operate code</h3>
	 * <h3 class="zh-CN">操作代码的 Setter 方法</h3>
	 *
	 * @param operateCode <span class="en-US">Operate code</span>
	 *                    <span class="zh-CN">操作代码</span>
	 */
	public void setOperateCode(final Integer operateCode) {
		this.operateCode = operateCode;
	}

	/**
	 * <h3 class="en-US">Getter method for the operate user identified code</h3>
	 * <h3 class="zh-CN">操作人识别代码的 Getter 方法</h3>
	 *
	 * @return <span class="en-US">Operate user identified code</span>
	 * <span class="zh-CN">操作人识别代码</span>
	 */
	public Long getOperateUser() {
		return this.operateUser;
	}

	/**
	 * <h3 class="en-US">Setter method for the operate user identified code</h3>
	 * <h3 class="zh-CN">操作人识别代码的 Setter 方法</h3>
	 *
	 * @param operateUser <span class="en-US">Operate user identified code</span>
	 *                    <span class="zh-CN">操作人识别代码</span>
	 */
	public void setOperateUser(final Long operateUser) {
		this.operateUser = operateUser;
	}

	/**
	 * <h3 class="en-US">Getter method for the operate timestamp</h3>
	 * <h3 class="zh-CN">操作时间戳的 Getter 方法</h3>
	 *
	 * @return <span class="en-US">Operate timestamp</span>
	 * <span class="zh-CN">操作时间戳</span>
	 */
	public Long getOperateTimestamp() {
		return this.operateTimestamp;
	}

	/**
	 * <h3 class="en-US">Setter method for the operate timestamp</h3>
	 * <h3 class="zh-CN">操作时间戳的 Setter 方法</h3>
	 *
	 * @param operateTimestamp <span class="en-US">Operate timestamp</span>
	 *                         <span class="zh-CN">操作时间戳</span>
	 */
	public void setOperateTimestamp(final Long operateTimestamp) {
		this.operateTimestamp = operateTimestamp;
	}
}
