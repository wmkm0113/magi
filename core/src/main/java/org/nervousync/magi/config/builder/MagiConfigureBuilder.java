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
import org.nervousync.brain.configs.BrainConfigure;
import org.nervousync.brain.configs.builder.BrainConfigureBuilder;
import org.nervousync.builder.AbstractBuilder;
import org.nervousync.builder.ParentBuilder;
import org.nervousync.cache.builder.CacheConfigBuilder;
import org.nervousync.cache.config.CacheConfig;
import org.nervousync.exceptions.builder.BuilderException;
import org.nervousync.magi.config.MagiConfigure;
import org.nervousync.magi.config.storage.StorageConfig;
import org.nervousync.utils.core.DateTimeUtils;

import java.util.List;

/**
 * <h2 class="en-US">Implementation class of Magi O/R mapping middleware configuration information builder</h2>
 * <h2 class="zh-CN">Magi对象关系映射中间件配置信息构建器的实现类</h2>
 *
 * @param <P> <span class="en-US">Generics type of the parent builder</span>
 *            <span class="zh-CN">父构建器泛型类</span>
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Apr 10, 2018 15:48:19 $
 */
@SuppressWarnings("unused")
public class MagiConfigureBuilder<P extends ParentBuilder> extends AbstractBuilder<P, MagiConfigure> {

	/**
	 * <span class="en-US">Data source configuration information instance object</span>
	 * <span class="zh-CN">数据源配置信息实例对象</span>
	 */
	private final MagiConfigure configure;
	/**
	 * <h2 class="en-US">Configure information modified flag</h2>
	 * <h2 class="zh-CN">配置信息修改标记</h2>
	 */
	private boolean modified = Boolean.FALSE;

	/**
	 * <h3 class="en-US">Protected constructor for AbstractBuilder</h3>
	 * <h3 class="zh-CN">AbstractBuilder的构造函数</h3>
	 *
	 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
	 *                      <span class="zh-CN">父构建器实例对象</span>
	 * @param configure     <span class="en-US">Data source configuration information instance object</span>
	 *                      <span class="zh-CN">数据源配置信息实例对象</span>
	 */
	private MagiConfigureBuilder(final P parentBuilder, final MagiConfigure configure) {
		super(parentBuilder);
		this.configure = (configure == null) ? new MagiConfigure() : configure;
	}

	/**
	 * <h3 class="en-US">Static method is used to initialize the current class of configuration information builder</h3>
	 * <h3 class="zh-CN">静态方法用于初始化配置信息构建器实现类</h3>
	 *
	 * @param <P>           <span class="en-US">Generics Type instance</span>
	 *                      <span class="zh-CN">泛型类实例对象</span>
	 * @param parentBuilder <span class="en-US">Parent builder instance object</span>
	 *                      <span class="zh-CN">父构建器实例对象</span>
	 * @param configure     <span class="en-US">Data source configuration information instance object</span>
	 *                      <span class="zh-CN">数据源配置信息实例对象</span>
	 * @return <span class="en-US">Configuration information instance object</span>
	 * <span class="zh-CN">配置信息构建器实现类实例对象</span>
	 */
	public static <P extends ParentBuilder> MagiConfigureBuilder<P> newBuilder(final P parentBuilder,
	                                                                           final MagiConfigure configure) {
		return new MagiConfigureBuilder<>(parentBuilder, configure);
	}

	/**
	 * <h3 class="en-US">Initialize data source configuration information builder</h3>
	 * <h3 class="zh-CN">初始化数据源配置信息构建器</h3>
	 *
	 * @return <span class="en-US">Data source configuration information builder instance object</span>
	 * <span class="zh-CN">数据源配置信息构建器实例对象</span>
	 */
	public BrainConfigureBuilder<MagiConfigureBuilder<P>> brainBuilder() {
		return BrainConfigureBuilder.newBuilder(this, this.configure.getBrainConfigure());
	}

	/**
	 * <h3 class="en-US">Initialize cache configuration information builder</h3>
	 * <h3 class="zh-CN">初始化缓存配置信息构建器</h3>
	 *
	 * @return <span class="en-US">Cache configuration information builder instance object</span>
	 * <span class="zh-CN">缓存配置信息构建器实例对象</span>
	 */
	public CacheConfigBuilder<MagiConfigureBuilder<P>> cacheConfig() {
		return CacheConfigBuilder.newBuilder(this, this.configure.getCacheConfig());
	}

	/**
	 * <h3 class="en-US">Delete current cache configure information</h3>
	 * <h3 class="zh-CN">删除当前的缓存配置信息</h3>
	 *
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public MagiConfigureBuilder<P> removeCacheConfig() {
		if (this.configure.getCacheConfig() != null) {
			this.configure.setCacheConfig(null);
			this.modified = Boolean.TRUE;
		}
		return this;
	}

	/**
	 * <h3 class="en-US">Initialize the data import and export tool configuration information builder instance object</h3>
	 * <h3 class="zh-CN">初始化数据导入导出工具配置信息构建器实例对象</h3>
	 *
	 * @return <span class="en-US">Data import and export tool configure information builder instance object</span>
	 * <span class="zh-CN">数据导入导出工具配置信息构建器实例对象</span>
	 */
	public StorageConfigBuilder<MagiConfigureBuilder<P>> storageConfig() {
		StorageConfig storageConfig = this.configure.getStorageConfig();
		if (storageConfig == null) {
			storageConfig = new StorageConfig();
		}
		return new StorageConfigBuilder<>(this, storageConfig);
	}

	/**
	 * <h3 class="en-US">Delete current data import and export tool configure information</h3>
	 * <h3 class="zh-CN">删除当前的导入导出工具配置信息</h3>
	 *
	 * @return <span class="en-US">Current builder instance object</span>
	 * <span class="zh-CN">当前构建器实例对象</span>
	 */
	public MagiConfigureBuilder<P> removeStorageConfig() {
		if (this.configure.getStorageConfig() != null) {
			this.configure.setStorageConfig(null);
			this.modified = Boolean.TRUE;
		}
		return this;
	}

	/**
	 * <h3 class="en-US">Initialize the scan package rules information builder instance object</h3>
	 * <h3 class="zh-CN">初始化扫描规则信息构建器实例对象</h3>
	 *
	 * @return <span class="en-US">Scan package rules information builder instance object</span>
	 * <span class="zh-CN">扫描规则信息构建器实例对象</span>
	 */
	public ScanPackagesBuilder<MagiConfigureBuilder<P>> scanPackages() {
		return new ScanPackagesBuilder<>(this, this.configure.getScanPackages());
	}

	@Override
	public MagiConfigure build() throws BuilderException {
		if (this.modified) {
			this.configure.setLastModified(DateTimeUtils.currentUTCTimeMillis());
		}
		return this.configure;
	}

	@Override
	public void confirm(@Nonnull final Object object) {
		if (object instanceof BrainConfigure) {
			BrainConfigure brainConfigure = (BrainConfigure) object;
			if (this.configure.getBrainConfigure() == null
					|| this.configure.getBrainConfigure().getLastModified() != brainConfigure.getLastModified()) {
				this.configure.setBrainConfigure(brainConfigure);
				this.modified = Boolean.TRUE;
			}
		} else if (object instanceof StorageConfig) {
			StorageConfig storageConfig = (StorageConfig) object;
			if (this.configure.getStorageConfig() == null
					|| this.configure.getStorageConfig().getLastModified() != storageConfig.getLastModified()) {
				this.configure.setStorageConfig(storageConfig);
				this.modified = Boolean.TRUE;
			}
		} else if (object instanceof CacheConfig) {
			CacheConfig cacheConfig = (CacheConfig) object;
			if (this.configure.getCacheConfig() == null
					|| this.configure.getCacheConfig().getLastModified() != cacheConfig.getLastModified()) {
				this.configure.setCacheConfig(cacheConfig);
				this.modified = Boolean.TRUE;
			}
		} else if (object instanceof ScanPackagesBuilder.ScanRules) {
			List<String> scanPackages = ((ScanPackagesBuilder.ScanRules) object).getScanPackages();
			List<String> existRules = this.configure.getScanPackages();
			if (existRules == null || scanPackages.size() != existRules.size()
					|| scanPackages.stream().anyMatch(string -> !existRules.contains(string))
					|| existRules.stream().anyMatch(string -> !scanPackages.contains(string))) {
				this.configure.setScanPackages(scanPackages);
				this.modified = Boolean.TRUE;
			}
		}
	}
}
