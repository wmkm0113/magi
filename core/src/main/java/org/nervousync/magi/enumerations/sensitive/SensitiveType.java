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

package org.nervousync.magi.enumerations.sensitive;

/**
 * <h2 class="en-US">Enumeration of Sensitive data type</h2>
 * <h2 class="zh-CN">代码类型的枚举类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Sep 12, 2023 15:16:08 $
 */
public enum SensitiveType {
    /**
     * <span class="en-US">Social Credit Code</span>
     * <span class="zh-CN">统一社会信用代码</span>
     */
    CHN_Social_Code,
    /**
     * <span class="en-US">ID Code</span>
     * <span class="zh-CN">身份证号码</span>
     */
    CHN_ID_Code,
    /**
     * <span class="en-US">US Employer Identification Number</span>
     * <span class="zh-CN">美国雇主身份识别号码</span>
     */
    US_EIN,
    /**
     * <span class="en-US">US Individual Taxpayer Identification Number</span>
     * <span class="zh-CN">美国个人报税识别号码</span>
     */
    US_ITIN,
    /**
     * <span class="en-US">US Social Security Number</span>
     * <span class="zh-CN">美国社会安全号码</span>
     */
    US_SSN,
    /**
     * <span class="en-US">Bank card number</span>
     * <span class="zh-CN">银行卡号</span>
     */
    Luhn,
    /**
     * <span class="en-US">E-mail address</span>
     * <span class="zh-CN">电子邮件地址</span>
     */
    E_MAIL,
    /**
     * <span class="en-US">Phone number</span>
     * <span class="zh-CN">电话号码</span>
     */
    PHONE_NUMBER,
    /**
     * <span class="en-US">Normal</span>
     * <span class="zh-CN">一般</span>
     */
    NORMAL
}
