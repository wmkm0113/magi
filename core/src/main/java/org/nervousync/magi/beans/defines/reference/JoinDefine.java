package org.nervousync.magi.beans.defines.reference;

import jakarta.xml.bind.annotation.*;
import org.nervousync.beans.core.BeanObject;

/**
 * <h2 class="en-US">Reference join configure information</h2>
 * <h2 class="zh-CN">外键关联列配置信息</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Mar 30, 2016 17:48:56 $
 */
@XmlType(name = "join_define")
@XmlRootElement(name = "join_define")
@XmlAccessorType(XmlAccessType.NONE)
public final class JoinDefine extends BeanObject {
    /**
     * <span class="en-US">Serial version UID</span>
     * <span class="zh-CN">序列化UID</span>
     */
    private static final long serialVersionUID = -5091778742481427204L;
    /**
     * <span class="en-US">The field name in main table</span>
     * <span class="zh-CN">主表中的属性名称</span>
     */
    @XmlElement(name = "current_field")
    private String currentField;
    /**
     * <span class="en-US">The field name in reference table</span>
     * <span class="zh-CN">关联表中的属性名称</span>
     */
    @XmlElement(name = "reference_field")
    private String referenceField;

    /**
     * <h3 class="en-US">Constructor method for join configure information</h3>
     * <h3 class="zh-CN">外键关联列配置信息的构造方法</h3>
     */
    public JoinDefine() {
    }

    /**
     * <h3 class="en-US">Getter method for the field name in main table</h3>
     * <h3 class="zh-CN">主表中的属性名称的Getter方法</h3>
     *
     * @return <span class="en-US">The field name in main table</span>
     * <span class="zh-CN">主表中的属性名称</span>
     */
    public String getCurrentField() {
        return currentField;
    }

    /**
     * <h3 class="en-US">Setter method for the field name in main table</h3>
     * <h3 class="zh-CN">主表中的属性名称的Setter方法</h3>
     *
     * @param currentField <span class="en-US">The field name in main table</span>
     *                     <span class="zh-CN">主表中的属性名称</span>
     */
    public void setCurrentField(String currentField) {
        this.currentField = currentField;
    }

    /**
     * <h3 class="en-US">Getter method for the field name in reference table</h3>
     * <h3 class="zh-CN">关联表中的属性名称的Getter方法</h3>
     *
     * @return <span class="en-US">The field name in reference table</span>
     * <span class="zh-CN">关联表中的属性名称</span>
     */
    public String getReferenceField() {
        return referenceField;
    }

    /**
     * <h3 class="en-US">Setter method for the field name in reference table</h3>
     * <h3 class="zh-CN">关联表中的属性名称的Setter方法</h3>
     *
     * @param referenceField <span class="en-US">The field name in reference table</span>
     *                       <span class="zh-CN">关联表中的属性名称</span>
     */
    public void setReferenceField(String referenceField) {
        this.referenceField = referenceField;
    }
}
