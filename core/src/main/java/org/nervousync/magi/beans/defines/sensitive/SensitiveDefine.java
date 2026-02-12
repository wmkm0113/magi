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
package org.nervousync.magi.beans.defines.sensitive;

import jakarta.annotation.Nonnull;
import org.nervousync.magi.annotations.data.Sensitive;
import org.nervousync.magi.entity.BaseObject;
import org.nervousync.magi.enumerations.sensitive.SensitiveType;
import org.nervousync.security.factory.SecureFactory;
import org.nervousync.utils.core.ObjectUtils;
import org.nervousync.utils.core.ReflectionUtils;
import org.nervousync.utils.core.StringUtils;

/**
 * <h2 class="en-US">Sensitive information handling configuration</h2>
 * <h2 class="zh-CN">敏感信息处理配置</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Sep 12, 2023 15:28:21 $
 */
@SuppressWarnings("unused")
public final class SensitiveDefine {

	/**
	 * <span class="en-US">Sensitive information field name</span>
	 * <span class="zh-CN">敏感信息属性名</span>
	 */
	private final String fieldName;
	/**
	 * <span class="en-US">Encryption result saving field name</span>
	 * <span class="zh-CN">加密结果保存属性</span>
	 */
	private final String encName;
	/**
	 * <span class="en-US">Security configuration name to use</span>
	 * <span class="zh-CN">使用的安全配置名称</span>
	 */
	private final String secureName;
	/**
	 * <span class="en-US">Sensitive information data type</span>
	 * <span class="zh-CN">敏感信息数据类型</span>
	 */
	private final SensitiveType sensitiveType;

	/**
	 * <h3 class="en-US">Private constructor for sensitive information processing configuration</h3>
	 * <h3 class="zh-CN">敏感信息处理配置的私有构造方法</h3>
	 *
	 * @param fieldName <span class="en-US">Sensitive information field name</span>
	 *                  <span class="zh-CN">敏感信息属性名</span>
	 * @param sensitive <span class="en-US">Sensitive data annotation</span>
	 *                  <span class="zh-CN">敏感信息注解</span>
	 */
	public SensitiveDefine(@Nonnull final String fieldName, @Nonnull final Sensitive sensitive) {
		this.fieldName = fieldName;
		this.encName = sensitive.encField();
		this.secureName = sensitive.secureName();
		this.sensitiveType = sensitive.type();
	}

	/**
	 * <h3 class="en-US">Checks whether the given attribute name is consistent with the current definition information</h3>
	 * <h3 class="zh-CN">检查给定的属性名是否与当前定义信息一致</h3>
	 *
	 * @param fieldName <span class="en-US">Sensitive information field name</span>
	 *                  <span class="zh-CN">敏感信息属性名</span>
	 * @return <span class="en-US">Check result</span>
	 * <span class="zh-CN">检查结果</span>
	 */
	public boolean match(final String fieldName) {
		return ObjectUtils.nullSafeEquals(this.fieldName, fieldName);
	}

	/**
	 * <h3 class="en-US">Decrypt sensitive information</h3>
	 * <h3 class="zh-CN">解密敏感信息</h3>
	 *
	 * @param object <span class="en-US">Entity instance object</span>
	 *               <span class="zh-CN">实体类实例对象</span>
	 */
	public void sensitiveData(@Nonnull final Object object) {
		Object fieldValue = ReflectionUtils.getFieldValue(this.fieldName, object);
		if (fieldValue instanceof String) {
			if (StringUtils.isEmpty(this.secureName) || !SecureFactory.registeredConfig(this.secureName)
					|| !((String) fieldValue).contains("*")) {
				return;
			}
			String encData = (String) ReflectionUtils.getFieldValue(this.encName, object);
			if (StringUtils.isEmpty(encData)) {
				return;
			}
			String decData = SecureFactory.decrypt(this.secureName, encData);
			if (StringUtils.isEmpty(decData)) {
				return;
			}

			int beginPosition = ((String) fieldValue).indexOf("*"), endPosition = ((String) fieldValue).lastIndexOf("*");
			StringBuilder stringBuilder = new StringBuilder(((String) fieldValue).substring(0, beginPosition));
			stringBuilder.append(decData);
			if (endPosition > beginPosition) {
				stringBuilder.append(((String) fieldValue).substring(endPosition + 1));
			}
			ReflectionUtils.setField(this.fieldName, object, stringBuilder.toString());
		}
	}

	/**
	 * <h3 class="en-US">Encrypt sensitive information</h3>
	 * <h3 class="zh-CN">处理敏感信息</h3>
	 *
	 * @param object <span class="en-US">Entity instance object</span>
	 *               <span class="zh-CN">实体类实例对象</span>
	 */
	public void desensitize(final BaseObject object) {
		if (StringUtils.isEmpty(this.secureName) || !SecureFactory.registeredConfig(this.secureName)) {
			return;
		}
		Object fieldValue = ReflectionUtils.getFieldValue(this.fieldName, object);
		if (fieldValue instanceof String) {
			String sensitiveData = (String) fieldValue;
			if (StringUtils.isEmpty(sensitiveData) || sensitiveData.contains("*")) {
				return;
			}
			int prefixLength, suffixLength;
			boolean validate;
			switch (sensitiveType) {
				case Luhn:
					prefixLength = 2;
					suffixLength = sensitiveData.length() % 4;
					if (suffixLength == 0) {
						suffixLength = 4;
					}
					validate = StringUtils.isLuhn(sensitiveData);
					break;
				case CHN_ID_Code:
					prefixLength = 3;
					suffixLength = 1;
					validate = StringUtils.isChnId(sensitiveData);
					break;
				case CHN_Social_Code:
					prefixLength = 5;
					suffixLength = 1;
					validate = StringUtils.isChnSocialCredit(sensitiveData);
					break;
				case US_EIN:
					prefixLength = 2;
					suffixLength = 1;
					validate = StringUtils.isEIN(sensitiveData);
					break;
				case US_ITIN:
					prefixLength = 3;
					suffixLength = 1;
					validate = StringUtils.isITIN(sensitiveData);
					break;
				case US_SSN:
					prefixLength = 3;
					suffixLength = 1;
					validate = StringUtils.isSSN(sensitiveData);
					break;
				case E_MAIL:
					prefixLength = 1;
					suffixLength = sensitiveData.length() - sensitiveData.indexOf("@");
					validate = StringUtils.isEMail(sensitiveData);
					break;
				case PHONE_NUMBER:
					prefixLength = 3;
					suffixLength = 2;
					validate = StringUtils.isPhoneNumber(sensitiveData);
					break;
				default:
					prefixLength = 1;
					suffixLength = (sensitiveData.length() > 2) ? 1 : 0;
					validate = Boolean.TRUE;
					break;
			}

			if (validate) {
				String encData = SecureFactory.encrypt(this.secureName,
						sensitiveData.substring(prefixLength, sensitiveData.length() - suffixLength));
				ReflectionUtils.setField(this.encName, object, encData);
				int index = prefixLength;
				StringBuilder stringBuilder = new StringBuilder(sensitiveData.substring(0, prefixLength));
				while (index < sensitiveData.length() - suffixLength) {
					stringBuilder.append("*");
					index++;
				}
				stringBuilder.append(sensitiveData.substring(sensitiveData.length() - suffixLength));
				ReflectionUtils.setField(this.fieldName, object, stringBuilder.toString());
			}
		}
	}
}