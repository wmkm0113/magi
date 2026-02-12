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

package org.nervousync.magi.data.transfer;

import org.nervousync.beans.config.TransferConfig;

/**
 * <h2 class="en-US">Data column transfer configuration information</h2>
 * <h2 class="zh-CN">数据列传输配置信息</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Oct 26, 2023 15:25:10 $
 */
public final class TransferColumn {

    /**
     * <span class="en-US">Column order number in Excel</span>
     * <span class="zh-CN">Excel表中的列排列序号</span>
     */
    private int columnIndex;
    /**
     * <span class="en-US">Data column name</span>
     * <span class="zh-CN">数据列名</span>
     */
    private String columnName;
    /**
     * <span class="en-US">Data column is primary key</span>
     * <span class="zh-CN">数据列是否为主键</span>
     */
    private boolean primaryKey;
    /**
     * <span class="en-US">Data convert configure</span>
     * <span class="zh-CN">数据转换配置信息</span>
     */
    private TransferConfig transferConfig;

    /**
     * <h3 class="en-US">Constructor method for data column transfer configuration information</h3>
     * <h3 class="zh-CN">数据列传输配置信息的构造方法</h3>
     */
    public TransferColumn() {
    }

    /**
     * <h3 class="en-US">Getter method for column order number in Excel</h3>
     * <h3 class="zh-CN">Excel表中的列排列序号的Getter方法</h3>
     *
     * @return <span class="en-US">Column order number in Excel</span>
     * <span class="zh-CN">Excel表中的列排列序号</span>
     */
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * <h3 class="en-US">Setter method for column order number in Excel</h3>
     * <h3 class="zh-CN">Excel表中的列排列序号的Setter方法</h3>
     *
     * @param columnIndex <span class="en-US">Column order number in Excel</span>
     *                    <span class="zh-CN">Excel表中的列排列序号</span>
     */
    public void setColumnIndex(final int columnIndex) {
        this.columnIndex = columnIndex;
    }

    /**
     * <h3 class="en-US">Getter method for data column name</h3>
     * <h3 class="zh-CN">数据列名的Getter方法</h3>
     *
     * @return <span class="en-US">Data column name</span>
     * <span class="zh-CN">数据列名</span>
     */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * <h3 class="en-US">Setter method for data column name</h3>
     * <h3 class="zh-CN">数据列名的Setter方法</h3>
     *
     * @param columnName <span class="en-US">Data column name</span>
     *                   <span class="zh-CN">数据列名</span>
     */
    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

    /**
     * <h3 class="en-US">Getter method for data column is primary key</h3>
     * <h3 class="zh-CN">数据列是否为主键的Getter方法</h3>
     *
     * @return <span class="en-US">Data column is primary key</span>
     * <span class="zh-CN">数据列是否为主键</span>
     */
    public boolean isPrimaryKey() {
        return this.primaryKey;
    }

    /**
     * <h3 class="en-US">Setter method for data column is primary key</h3>
     * <h3 class="zh-CN">数据列是否为主键的Setter方法</h3>
     *
     * @param primaryKey <span class="en-US">Data column is primary key</span>
     *                   <span class="zh-CN">数据列是否为主键</span>
     */
    public void setPrimaryKey(final boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * <h3 class="en-US">Setter method for data convert configure</h3>
     * <h3 class="zh-CN">数据转换配置信息的Setter方法</h3>
     *
     * @param transferConfig <span class="en-US">Data convert configure</span>
     *                       <span class="zh-CN">数据转换配置信息</span>
     */
    public void setTransferConfig(final TransferConfig transferConfig) {
        this.transferConfig = transferConfig;
    }

    /**
     * <h3 class="en-US">Convert the given data to the current field type</h3>
     * <h3 class="zh-CN">转换给定的数据为当前属性类型值</h3>
     *
     * @param object <span class="en-US">The given data</span>
     *               <span class="zh-CN">给定的数据</span>
     * @return <span class="en-US">Converted data</span>
     * <span class="zh-CN">转换后的数据</span>
     */
    public Object marshall(final Object object) {
        return this.transferConfig.marshal(object);
    }

    /**
     * <h3 class="en-US">Convert the given data to the current field type</h3>
     * <h3 class="zh-CN">转换给定的数据为当前属性类型值</h3>
     *
     * @param string <span class="en-US">The given data</span>
     *               <span class="zh-CN">给定的数据</span>
     * @return <span class="en-US">Converted data</span>
     * <span class="zh-CN">转换后的数据</span>
     */
    public Object unmarshall(final Object string) {
        return this.transferConfig.unmarshal(string);
    }
}
