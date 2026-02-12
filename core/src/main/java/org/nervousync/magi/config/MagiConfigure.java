package org.nervousync.magi.config;

import jakarta.xml.bind.annotation.*;
import org.nervousync.annotations.beans.OutputConfig;
import org.nervousync.brain.configs.BrainConfigure;
import org.nervousync.cache.config.CacheConfig;
import org.nervousync.commons.Globals;
import org.nervousync.magi.config.storage.StorageConfig;

import java.io.Serializable;
import java.util.List;

/**
 * <h2 class="en-US">Magi O/R mapping middleware configuration information definition</h2>
 * <h2 class="zh-CN">Magi对象关系映射中间件配置信息定义</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: Dec 20, 2018 15:43:52 $
 */
@SuppressWarnings("unused")
@OutputConfig
@XmlType(name = "magi_config", namespace = "https://nervousync.org/schemas/magi")
@XmlRootElement(name = "magi_config", namespace = "https://nervousync.org/schemas/magi")
@XmlAccessorType(XmlAccessType.NONE)
public final class MagiConfigure implements Serializable {

	/**
	 * <span class="en-US">Class name of used sensitive data tracker implement class</span>
	 * <span class="zh-CN">使用的敏感数据追踪器实现类名</span>
	 */
	@XmlElement(name = "sensitive_tracker")
	private String sensitiveTracker = Globals.DEFAULT_VALUE_STRING;
	/**
	 * <span class="en-US">Data source configure information instance object</span>
	 * <span class="zh-CN">数据源配置信息实例对象</span>
	 */
	@XmlElement(name = "brain_config", namespace = "https://nervousync.org/schemas/brain")
	private BrainConfigure brainConfigure = null;
	/**
	 * <span class="en-US">Data import/export configure information</span>
	 * <span class="zh-CN">数据导入导出配置</span>
	 */
	@XmlElement(name = "storage_config", namespace = "https://nervousync.org/schemas/magi")
	private StorageConfig storageConfig = null;
	/**
	 * <span class="en-US">Cache configure information</span>
	 * <span class="zh-CN">缓存配置信息</span>
	 */
	@XmlElement(name = "cache_config", namespace = "https://nervousync.org/schemas/cache")
	private CacheConfig cacheConfig = null;
	/**
	 * <span class="en-US">Package name string list, which can be a regular expression list</span>
	 * <span class="zh-CN">包名列表，可以是正则表达式列表</span>
	 */
	@XmlElement(name = "scan_packages")
	@XmlElementWrapper(name = "package_name")
	private List<String> scanPackages;
	/**
	 * <span class="en-US">Last modified timestamp</span>
	 * <span class="zh-CN">最后修改时间戳</span>
	 */
	@XmlElement(name = "last_modified")
	private long lastModified = Globals.DEFAULT_VALUE_LONG;

	/**
	 * <h3 class="en-US">Getter method for the class name of used sensitive data tracker implement class</h3>
	 * <h3 class="zh-CN">使用的敏感数据追踪器实现类名的Getter方法</h3>
	 *
	 * @return <span class="en-US">Class name of used sensitive data tracker implement class</span>
	 * <span class="zh-CN">使用的敏感数据追踪器实现类名</span>
	 */
	public String getSensitiveTracker() {
		return this.sensitiveTracker;
	}

	/**
	 * <h3 class="en-US">Setter method for the class name of used sensitive data tracker implement class</h3>
	 * <h3 class="zh-CN">使用的敏感数据追踪器实现类名的Setter方法</h3>
	 *
	 * @param sensitiveTracker <span class="en-US">Class name of used sensitive data tracker implement class</span>
	 *                         <span class="zh-CN">使用的敏感数据追踪器实现类名</span>
	 */
	public void setSensitiveTracker(final String sensitiveTracker) {
		this.sensitiveTracker = sensitiveTracker;
	}

	/**
	 * <h3 class="en-US">Getter method for the data source configure information instance object</h3>
	 * <h3 class="zh-CN">数据源配置信息实例对象的Getter方法</h3>
	 *
	 * @return <span class="en-US">Data source configure information instance object</span>
	 * <span class="zh-CN">数据源配置信息实例对象</span>
	 */
	public BrainConfigure getBrainConfigure() {
		return this.brainConfigure;
	}

	/**
	 * <h3 class="en-US">Setter method for the data source configure information instance object</h3>
	 * <h3 class="zh-CN">数据源配置信息实例对象的Setter方法</h3>
	 *
	 * @param brainConfigure <span class="en-US">Data source configure information instance object</span>
	 *                       <span class="zh-CN">数据源配置信息实例对象</span>
	 */
	public void setBrainConfigure(final BrainConfigure brainConfigure) {
		this.brainConfigure = brainConfigure;
	}

	/**
	 * <h3 class="en-US">Getter method for the data import/export configure information</h3>
	 * <h3 class="zh-CN">数据导入导出配置的Getter方法</h3>
	 *
	 * @return <span class="en-US">Data import/export configure information</span>
	 * <span class="zh-CN">数据导入导出配置</span>
	 */
	public StorageConfig getStorageConfig() {
		return this.storageConfig;
	}

	/**
	 * <h3 class="en-US">Setter method for the data import/export configure information</h3>
	 * <h3 class="zh-CN">数据导入导出配置的Setter方法</h3>
	 *
	 * @param storageConfig <span class="en-US">Data import/export configure information</span>
	 *                      <span class="zh-CN">数据导入导出配置</span>
	 */
	public void setStorageConfig(final StorageConfig storageConfig) {
		this.storageConfig = storageConfig;
	}

	/**
	 * <h3 class="en-US">Getter method for the cache configure information</h3>
	 * <h3 class="zh-CN">缓存配置信息的Getter方法</h3>
	 *
	 * @return <span class="en-US">Cache configure information</span>
	 * <span class="zh-CN">缓存配置信息</span>
	 */
	public CacheConfig getCacheConfig() {
		return this.cacheConfig;
	}

	/**
	 * <h3 class="en-US">Setter method for the cache configure information</h3>
	 * <h3 class="zh-CN">缓存配置信息的Setter方法</h3>
	 *
	 * @param cacheConfig <span class="en-US">Cache configure information</span>
	 *                    <span class="zh-CN">缓存配置信息</span>
	 */
	public void setCacheConfig(final CacheConfig cacheConfig) {
		this.cacheConfig = cacheConfig;
	}

	/**
	 * <h3 class="en-US">Getter method for the package name string list</h3>
	 * <h3 class="zh-CN">包名列表的Getter方法</h3>
	 *
	 * @return <span class="en-US">Package name string list, which can be a regular expression list</span>
	 * <span class="zh-CN">包名列表，可以是正则表达式列表</span>
	 */
	public List<String> getScanPackages() {
		return this.scanPackages;
	}

	/**
	 * <h3 class="en-US">Setter method for the package name string list</h3>
	 * <h3 class="zh-CN">包名列表的Setter方法</h3>
	 *
	 * @param scanPackages <span class="en-US">Package name string list, which can be a regular expression list</span>
	 *                     <span class="zh-CN">包名列表，可以是正则表达式列表</span>
	 */
	public void setScanPackages(final List<String> scanPackages) {
		this.scanPackages = scanPackages;
	}

	/**
	 * <h3 class="en-US">Getter method for the last modified timestamp</h3>
	 * <h3 class="zh-CN">最后修改时间戳的Getter方法</h3>
	 *
	 * @return <span class="en-US">Last modified timestamp</span>
	 * <span class="zh-CN">最后修改时间戳</span>
	 */
	public long getLastModified() {
		return this.lastModified;
	}

	/**
	 * <h3 class="en-US">Setter method for the last modified timestamp</h3>
	 * <h3 class="zh-CN">最后修改时间戳的Setter方法</h3>
	 *
	 * @param lastModified <span class="en-US">Last modified timestamp</span>
	 *                     <span class="zh-CN">最后修改时间戳</span>
	 */
	public void setLastModified(final long lastModified) {
		this.lastModified = lastModified;
	}
}
