package org.nervousync.magi.exceptions.data;

import org.nervousync.exceptions.AbstractException;

/**
 * <h2 class="en-US">Data parse Exception</h2>
 * <h2 class="zh-CN">数据解析异常</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Jul 24, 2018 12:46:26 $
 */
public final class DataParseException extends AbstractException {
    /**
     * <span class="en-US">Serial version UID</span>
     * <span class="zh-CN">序列化UID</span>
     */
    private static final long serialVersionUID = -5359012199844533648L;

    /**
     * <h3 class="en-US">Constructor method for DataParseException</h3>
     * <span class="en-US">Create a new DataParseException with the specified message.</span>
     * <h3 class="zh-CN">DataParseException构造方法</h3>
     * <span class="zh-CN">使用特定的信息创建DataParseException实例对象。</span>
     *
     * @param errorCode   <span class="en-US">Error identified code</span>
     *                    <span class="zh-CN">错误识别代码</span>
     * @param collections <span class="en-US">given parameters of information formatter</span>
     *                    <span class="zh-CN">用于资源信息格式化的参数</span>
     */
    public DataParseException(final long errorCode, final Object... collections) {
        super(errorCode, collections);
    }

    /**
     * <h3 class="en-US">Constructor method for DataParseException</h3>
     * <span class="en-US">Create a new DataParseException with the specified message and root cause.</span>
     * <h3 class="zh-CN">DataParseException构造方法</h3>
     * <span class="zh-CN">使用特定的信息以及异常信息对象实例创建DataParseException实例对象。</span>
     *
     * @param errorCode   <span class="en-US">Error identified code</span>
     *                    <span class="zh-CN">错误识别代码</span>
     * @param cause       <span class="en-US">The root cause</span>
     *                    <span class="zh-CN">异常信息对象实例</span>
     * @param collections <span class="en-US">given parameters of information formatter</span>
     *                    <span class="zh-CN">用于资源信息格式化的参数</span>
     */
    public DataParseException(final long errorCode, final Throwable cause, final Object... collections) {
        super(errorCode, cause, collections);
    }
}
