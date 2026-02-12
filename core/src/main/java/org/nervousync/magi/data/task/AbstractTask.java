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

package org.nervousync.magi.data.task;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import org.nervousync.commons.Globals;
import org.nervousync.magi.data.DataUtils;

import java.io.Serializable;

/**
 * <h2 class="en-US">Data import and export task information</h2>
 * <h2 class="zh-CN">数据导入导出任务信息</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Mar 23, 2021 05:12:18 $
 */
@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractTask implements Serializable {

    /**
     * <span class="en-US">Serial version UID</span>
     * <span class="zh-CN">序列化UID</span>
     */
    private static final long serialVersionUID = -2334472339890788462L;

    /**
     * <span class="en-US">Task unique identification code</span>
     * <span class="zh-CN">任务唯一识别代码</span>
     */
    @XmlElement(name = "task_code")
    private Long taskCode;
    /**
     * <span class="en-US">Task creation user code</span>
     * <span class="zh-CN">任务创建用户代码</span>
     */
    @XmlElement(name = "user_code")
    private Long userCode;
    /**
     * <span class="en-US">Task creation time</span>
     * <span class="zh-CN">任务创建时间</span>
     */
    @XmlElement(name = "create_time")
    private Long createTime;
    /**
     * <span class="en-US">Task start processing time</span>
     * <span class="zh-CN">任务开始处理时间</span>
     */
    @XmlElement(name = "start_time")
    private Long startTime = Globals.DEFAULT_VALUE_LONG;
    /**
     * <span class="en-US">Task processing end time</span>
     * <span class="zh-CN">任务处理结束时间</span>
     */
    @XmlElement(name = "end_time")
    private Long endTime = Globals.DEFAULT_VALUE_LONG;
    /**
     * <span class="en-US">Task status code</span>
     * <span class="zh-CN">任务状态代码</span>
     */
    @XmlElement(name = "task_status")
    private Integer taskStatus = DataUtils.DATA_TASK_STATUS_CREATE;
    /**
     * <span class="en-US">Error status during task processing</span>
     * <span class="zh-CN">任务处理过程中出错状态</span>
     */
    @XmlElement(name = "has_error")
    private Boolean hasError = Boolean.FALSE;
    /**
     * <span class="en-US">Task processing status</span>
     * <span class="zh-CN">任务处理状态</span>
     */
    @XmlElement
    private Boolean processing = Boolean.FALSE;
    /**
     * <span class="en-US">Error message for task processing error</span>
     * <span class="zh-CN">任务处理出错的错误信息</span>
     */
    @XmlElement(name = "error_message")
    private String errorMessage;
    /**
     * <span class="en-US">Task processing node identification code</span>
     * <span class="zh-CN">任务处理节点识别代码</span>
     */
    @XmlElement(name = "identify_code")
    private String identifyCode;

    /**
     * <h3 class="en-US">Constructor method for abstract task information</h3>
     * <h3 class="zh-CN">抽象任务信息的构造方法</h3>
     */
    protected AbstractTask() {
    }

    /**
     * <h3 class="en-US">Getter method for task unique identification code</h3>
     * <h3 class="zh-CN">任务唯一识别代码的Getter方法</h3>
     *
     * @return <span class="en-US">Task unique identification code</span>
     * <span class="zh-CN">任务唯一识别代码</span>
     */
    public Long getTaskCode() {
        return taskCode;
    }

    /**
     * <h3 class="en-US">Setter method for task unique identification code</h3>
     * <h3 class="zh-CN">任务唯一识别代码的Setter方法</h3>
     *
     * @param taskCode <span class="en-US">Task unique identification code</span>
     *                 <span class="zh-CN">任务唯一识别代码</span>
     */
    public void setTaskCode(Long taskCode) {
        this.taskCode = taskCode;
    }

    /**
     * <h3 class="en-US">Getter method for task creation user code</h3>
     * <h3 class="zh-CN">任务创建用户代码的Getter方法</h3>
     *
     * @return <span class="en-US">Task creation user code</span>
     * <span class="zh-CN">任务创建用户代码</span>
     */
    public Long getUserCode() {
        return userCode;
    }

    /**
     * <h3 class="en-US">Setter method for task creation user code</h3>
     * <h3 class="zh-CN">任务创建用户代码的Setter方法</h3>
     *
     * @param userCode <span class="en-US">Task creation user code</span>
     *                 <span class="zh-CN">任务创建用户代码</span>
     */
    public void setUserCode(Long userCode) {
        this.userCode = userCode;
    }

    /**
     * <h3 class="en-US">Getter method for task creation time</h3>
     * <h3 class="zh-CN">任务创建时间的Getter方法</h3>
     *
     * @return <span class="en-US">Task creation time</span>
     * <span class="zh-CN">任务创建时间</span>
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * <h3 class="en-US">Setter method for task creation time</h3>
     * <h3 class="zh-CN">任务创建时间的Setter方法</h3>
     *
     * @param createTime <span class="en-US">Task creation time</span>
     *                   <span class="zh-CN">任务创建时间</span>
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * <h3 class="en-US">Getter method for task start processing time</h3>
     * <h3 class="zh-CN">任务开始处理时间的Getter方法</h3>
     *
     * @return <span class="en-US">Task start processing time</span>
     * <span class="zh-CN">任务开始处理时间</span>
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * <h3 class="en-US">Setter method for task start processing time</h3>
     * <h3 class="zh-CN">任务开始处理时间的Setter方法</h3>
     *
     * @param startTime <span class="en-US">Task start processing time</span>
     *                  <span class="zh-CN">任务开始处理时间</span>
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * <h3 class="en-US">Getter method for task processing end time</h3>
     * <h3 class="zh-CN">任务处理结束时间的Getter方法</h3>
     *
     * @return <span class="en-US">Task processing end time</span>
     * <span class="zh-CN">任务处理结束时间</span>
     */
    public Long getEndTime() {
        return endTime;
    }

    /**
     * <h3 class="en-US">Setter method for task processing end time</h3>
     * <h3 class="zh-CN">任务处理结束时间的Setter方法</h3>
     *
     * @param endTime <span class="en-US">Task processing end time</span>
     *                <span class="zh-CN">任务处理结束时间</span>
     */
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    /**
     * <h3 class="en-US">Getter method for task status code</h3>
     * <h3 class="zh-CN">任务状态代码的Getter方法</h3>
     *
     * @return <span class="en-US">Task status code</span>
     * <span class="zh-CN">任务状态代码</span>
     */
    public Integer getTaskStatus() {
        return taskStatus;
    }

    /**
     * <h3 class="en-US">Setter method for task status code</h3>
     * <h3 class="zh-CN">任务状态代码的Setter方法</h3>
     *
     * @param taskStatus <span class="en-US">Task status code</span>
     *                   <span class="zh-CN">任务状态代码</span>
     */
    public void setTaskStatus(Integer taskStatus) {
        this.taskStatus = taskStatus;
    }

    /**
     * <h3 class="en-US">Getter method for error status during task processing</h3>
     * <h3 class="zh-CN">任务处理过程中出错状态的Getter方法</h3>
     *
     * @return <span class="en-US">Error status during task processing</span>
     * <span class="zh-CN">任务处理过程中出错状态</span>
     */
    public Boolean getHasError() {
        return hasError;
    }

    /**
     * <h3 class="en-US">Setter method for error status during task processing</h3>
     * <h3 class="zh-CN">任务处理过程中出错状态的Setter方法</h3>
     *
     * @param hasError <span class="en-US">Error status during task processing</span>
     *                 <span class="zh-CN">任务处理过程中出错状态</span>
     */
    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    /**
     * <h3 class="en-US">Getter method for task processing status</h3>
     * <h3 class="zh-CN">任务处理状态的Getter方法</h3>
     *
     * @return <span class="en-US">Task processing status</span>
     * <span class="zh-CN">任务处理状态</span>
     */
    public Boolean getProcessing() {
        return processing;
    }

    /**
     * <h3 class="en-US">Setter method for task processing status</h3>
     * <h3 class="zh-CN">任务处理状态的Setter方法</h3>
     *
     * @param processing <span class="en-US">Task processing status</span>
     *                   <span class="zh-CN">任务处理状态</span>
     */
    public void setProcessing(Boolean processing) {
        this.processing = processing;
    }

    /**
     * <h3 class="en-US">Getter method for error message for task processing error</h3>
     * <h3 class="zh-CN">任务处理出错的错误信息的Getter方法</h3>
     *
     * @return <span class="en-US">Error message for task processing error</span>
     * <span class="zh-CN">任务处理出错的错误信息</span>
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * <h3 class="en-US">Setter method for error message for task processing error</h3>
     * <h3 class="zh-CN">任务处理出错的错误信息的Setter方法</h3>
     *
     * @param errorMessage <span class="en-US">Error message for task processing error</span>
     *                     <span class="zh-CN">任务处理出错的错误信息</span>
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * <h3 class="en-US">Getter method for task processing node identification code</h3>
     * <h3 class="zh-CN">任务处理节点识别代码的Getter方法</h3>
     *
     * @return <span class="en-US">Task processing node identification code</span>
     * <span class="zh-CN">任务处理节点识别代码</span>
     */
    public String getIdentifyCode() {
        return identifyCode;
    }

    /**
     * <h3 class="en-US">Setter method for task processing node identification code</h3>
     * <h3 class="zh-CN">任务处理节点识别代码的Setter方法</h3>
     *
     * @param identifyCode <span class="en-US">Task processing node identification code</span>
     *                     <span class="zh-CN">任务处理节点识别代码</span>
     */
    public void setIdentifyCode(String identifyCode) {
        this.identifyCode = identifyCode;
    }
}
