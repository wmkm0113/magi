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
package org.nervousync.magi.config.builder;

import jakarta.annotation.Nonnull;
import org.nervousync.builder.AbstractBuilder;
import org.nervousync.builder.ParentBuilder;
import org.nervousync.exceptions.builder.BuilderException;
import org.nervousync.utils.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <h2 class="en-US">Implementation class of scan package rules information builder</h2>
 * <h2 class="zh-CN">扫描规则信息构建器的实现类</h2>
 *
 * @param <P> <span class="en-US">Generics type of the parent builder</span>
 *            <span class="zh-CN">父构建器泛型类</span>
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Apr 10, 2018 15:48:19 $
 */
@SuppressWarnings("unused")
public final class ScanPackagesBuilder<P extends ParentBuilder>
		extends AbstractBuilder<P, ScanPackagesBuilder.ScanRules> {

	/**
	 * <span class="en-US">Package name string list, which can be a regular expression list</span>
	 * <span class="zh-CN">包名列表，可以是正则表达式列表</span>
	 */
	@Nonnull
	private final List<String> scanPackages;

	/**
	 * <h3 class="en-US">Constructor method for the implementation class of scan package rules information builder</h3>
	 * <h3 class="zh-CN">扫描规则信息构建器的实现类的构造函数</h3>
	 *
	 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
	 *                      <span class="zh-CN">父构建器实例对象</span>
	 */
	ScanPackagesBuilder(final P parentBuilder, final List<String> scanPackages) {
		super(parentBuilder);
		this.scanPackages = (scanPackages == null) ? new ArrayList<>() : scanPackages;
	}

	/**
	 * <h3 class="en-US">Add rule</h3>
	 * <h3 class="zh-CN">添加规则</h3>
	 *
	 * @param string <span class="en-US">Scan rule string</span>
	 *               <span class="zh-CN">扫描规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实现类实例对象</span>
	 */
	public ScanPackagesBuilder<P> addRule(final String string) {
		if (StringUtils.notBlank(string) && !this.scanPackages.contains(string)) {
			this.scanPackages.add(string);
		}
		return this;
	}

	/**
	 * <h3 class="en-US">Remove rule</h3>
	 * <h3 class="zh-CN">移除规则</h3>
	 *
	 * @param string <span class="en-US">Scan rule string</span>
	 *               <span class="zh-CN">扫描规则字符串</span>
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实现类实例对象</span>
	 */
	public ScanPackagesBuilder<P> removeRule(final String string) {
		if (StringUtils.notBlank(string)) {
			this.scanPackages.remove(string);
		}
		return this;
	}

	@Override
	public ScanRules build() throws BuilderException {
		return new ScanRules(this.scanPackages);
	}

	/**
	 * <h2 class="en-US">Scan package rules information</h2>
	 * <h2 class="zh-CN">扫描规则信息结果</h2>
	 *
	 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
	 * @version $Revision: 1.0.0 $ $Date: Apr 10, 2018 15:48:19 $
	 */
	public static final class ScanRules {

		/**
		 * <span class="en-US">Package name string list, which can be a regular expression list</span>
		 * <span class="zh-CN">包名列表，可以是正则表达式列表</span>
		 */
		@Nonnull
		private final List<String> scanPackages;

		/**
		 * <h3 class="en-US">Private constructor method for the scan package rules information</h3>
		 * <h3 class="zh-CN">扫描规则信息结果的私有构造方法</h3>
		 *
		 * @param scanPackages <span class="en-US">Package name string list, which can be a regular expression list</span>
		 *                     <span class="zh-CN">包名列表，可以是正则表达式列表</span>
		 */
		private ScanRules(@Nonnull final List<String> scanPackages) {
			this.scanPackages = scanPackages;
		}

		/**
		 * <h3 class="en-US">Getter method for the package name string list</h3>
		 * <h3 class="zh-CN">包名列表的Getter方法</h3>
		 *
		 * @return <span class="en-US">Package name string list, which can be a regular expression list</span>
		 * <span class="zh-CN">包名列表，可以是正则表达式列表</span>
		 */
		@Nonnull
		public List<String> getScanPackages() {
			return this.scanPackages;
		}
	}
}
