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
package org.nervousync.magi.beans.defines.reference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import org.nervousync.magi.enumerations.reference.ReferenceType;
import org.nervousync.utils.core.ObjectUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <h2 class="en-US">Reference to configure information</h2>
 * <h2 class="zh-CN">外键引用配置信息</h2>
 *
 * @param <T> <span class="en-US">Reference entity class</span>
 *            <span class="zh-CN">外键实体类</span>
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Mar 30, 2016 17:48:56 $
 */
@SuppressWarnings("unused")
public final class ReferenceDefine<T> implements Serializable {
	/**
	 * <span class="en-US">Serial version UID</span>
	 * <span class="zh-CN">序列化UID</span>
	 */
	private static final long serialVersionUID = 352330256621053556L;
	/**
	 * <span class="en-US">Enumeration value of reference type</span>
	 * <span class="zh-CN">关联类型枚举值</span>
	 */
	private final ReferenceType referenceType;
	/**
	 * <span class="en-US">Return value is array</span>
	 * <span class="zh-CN">返回值是数组或列表</span>
	 */
	private final boolean returnArray;
	/**
	 * <span class="en-US">Target reference entity class</span>
	 * <span class="zh-CN">目标外键实体类</span>
	 */
	private final Class<T> referenceClass;
	/**
	 * <span class="en-US">Column mapping field name</span>
	 * <span class="zh-CN">列映射的属性名</span>
	 */
	private final String fieldName;
	/**
	 * <span class="en-US">Reference cascade type array</span>
	 * <span class="zh-CN">外键级联状态数组</span>
	 */
	private final CascadeType[] cascadeTypes;
	/**
	 * <span class="en-US">Reference join column configure list</span>
	 * <span class="zh-CN">外键关联列配置信息列表</span>
	 */
	private final List<JoinDefine> joinColumnList;

	/**
	 * <h3 class="en-US">Constructor method for reference to configure information</h3>
	 * <h3 class="zh-CN">外键引用配置信息的构造方法</h3>
	 *
	 * @param referenceType  <span class="en-US">Enumeration value of reference type</span>
	 *                       <span class="zh-CN">关联类型枚举值</span>
	 * @param referenceClass <span class="en-US">Target reference entity class</span>
	 *                       <span class="zh-CN">目标外键实体类</span>
	 * @param fieldName      <span class="en-US">Column mapping field name</span>
	 *                       <span class="zh-CN">列映射的属性名</span>
	 * @param returnArray    <span class="en-US">Return value is array</span>
	 *                       <span class="zh-CN">返回值是数组或列表</span>
	 * @param cascadeTypes   <span class="en-US">Reference cascade type array</span>
	 *                       <span class="zh-CN">外键级联状态数组</span>
	 * @param joinColumns    <span class="en-US">The annotation instance array of JoinColumn</span>
	 *                       <span class="zh-CN">注解 JoinColumn 的实例对象数组</span>
	 */
	public ReferenceDefine(final ReferenceType referenceType, final Class<T> referenceClass, final String fieldName,
	                       final boolean returnArray, final CascadeType[] cascadeTypes, final JoinColumn[] joinColumns) {
		this.referenceType = referenceType;
		this.referenceClass = referenceClass;
		this.fieldName = fieldName;
		this.returnArray = returnArray;
		this.cascadeTypes = cascadeTypes;
		this.joinColumnList = new ArrayList<>();
		Arrays.asList(joinColumns)
				.forEach(joinColumn -> {
					JoinDefine joinDefine = new JoinDefine();
					joinDefine.setCurrentField(joinColumn.columnDefinition());
					joinDefine.setReferenceField(joinColumn.referencedColumnName());
					this.joinColumnList.add(joinDefine);
				});
	}

	/**
	 * <h3 class="en-US">Getter method for enumeration value of the reference type</h3>
	 * <h3 class="zh-CN">关联类型枚举值的Getter方法</h3>
	 *
	 * @return <span class="en-US">Enumeration value of reference type</span>
	 * <span class="zh-CN">关联类型枚举值</span>
	 */
	public ReferenceType getReferenceType() {
		return this.referenceType;
	}

	/**
	 * <h3 class="en-US">Getter method for return value is array</h3>
	 * <h3 class="zh-CN">返回值是数组或列表的Getter方法</h3>
	 *
	 * @return <span class="en-US">Return value is array</span>
	 * <span class="zh-CN">返回值是数组或列表</span>
	 */
	public boolean isReturnArray() {
		return returnArray;
	}

	/**
	 * <h3 class="en-US">Getter method for target reference entity class</h3>
	 * <h3 class="zh-CN">目标外键实体类的Getter方法</h3>
	 *
	 * @return <span class="en-US">Target reference entity class</span>
	 * <span class="zh-CN">目标外键实体类</span>
	 */
	public Class<T> getReferenceClass() {
		return referenceClass;
	}

	/**
	 * <h3 class="en-US">Getter method for column mapping field name</h3>
	 * <h3 class="zh-CN">列映射的属性名的Getter方法</h3>
	 *
	 * @return <span class="en-US">Column mapping field name</span>
	 * <span class="zh-CN">列映射的属性名</span>
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * <h3 class="en-US">Getter method for the reference cascade type array</h3>
	 * <h3 class="zh-CN">外键级联状态数组的Getter方法</h3>
	 *
	 * @return <span class="en-US">Reference cascade type array</span>
	 * <span class="zh-CN">外键级联状态数组</span>
	 */
	public CascadeType[] getCascadeTypes() {
		return cascadeTypes;
	}

	/**
	 * <h3 class="en-US">Getter method for the reference join column configure list</h3>
	 * <h3 class="zh-CN">外键关联列配置信息列表的Getter方法</h3>
	 *
	 * @return <span class="en-US">Reference join column configure list</span>
	 * <span class="zh-CN">外键关联列配置信息列表</span>
	 */
	public List<JoinDefine> getJoinColumnList() {
		return joinColumnList;
	}

	/**
	 * <h3 class="en-US">Match the given entity class was same as current target reference entity class</h3>
	 * <h3 class="zh-CN">匹配给定的实体类对象是否与当前目标外键实体类信息一致</h3>
	 *
	 * @param entityClass <span class="en-US">Given entity class</span>
	 *                    <span class="zh-CN">给定的实体类</span>
	 * @return <span class="en-US">Match result</span>
	 * <span class="zh-CN">匹配结果</span>
	 */
	public boolean match(final Class<?> entityClass) {
		return ObjectUtils.nullSafeEquals(this.referenceClass, entityClass);
	}
}
