package com.dj.middle.kingdee.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.dj.common.annotation.Excel;
import com.dj.common.core.domain.BaseEntity;

/** 金蝶表单配置数据对象。 */
public class KingdeeTableConfig extends BaseEntity
{
    /** Java对象序列化版本。 */
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long configId;

    /** 表单名称 */
    @Excel(name = "表单名称")
    private String formName;

    /** 表单标识 */
    @Excel(name = "表单标识")
    private String formKey;

    /** 表单配置(JSON) */
    @Excel(name = "表单配置")
    private String formConfig;

    /** 排序号 */
    @Excel(name = "排序号")
    private Integer sortOrder;

    /** 扩展字段1 */
    @Excel(name = "扩展字段1")
    private String extend1;

    /** 扩展字段2 */
    @Excel(name = "扩展字段2")
    private String extend2;

    /** 扩展字段3 */
    @Excel(name = "扩展字段3")
    private String extend3;

    /** 读取连接配置主键。 */
    public Long getConfigId()
    {
        return configId;
    }

    /** 设置连接配置主键。 */
    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    /** 读取表单名称。 */
    public String getFormName()
    {
        return formName;
    }

    /** 设置表单名称。 */
    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    /** 读取表单标识。 */
    public String getFormKey()
    {
        return formKey;
    }

    /** 设置表单标识。 */
    public void setFormKey(String formKey)
    {
        this.formKey = formKey;
    }

    /** 读取表单配置。 */
    public String getFormConfig()
    {
        return formConfig;
    }

    /** 设置表单配置。 */
    public void setFormConfig(String formConfig)
    {
        this.formConfig = formConfig;
    }

    /** 读取排序号。 */
    public Integer getSortOrder()
    {
        return sortOrder;
    }

    /** 设置排序号。 */
    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    /** 读取扩展字段1。 */
    public String getExtend1()
    {
        return extend1;
    }

    /** 设置扩展字段1。 */
    public void setExtend1(String extend1)
    {
        this.extend1 = extend1;
    }

    /** 读取扩展字段2。 */
    public String getExtend2()
    {
        return extend2;
    }

    /** 设置扩展字段2。 */
    public void setExtend2(String extend2)
    {
        this.extend2 = extend2;
    }

    /** 读取扩展字段3。 */
    public String getExtend3()
    {
        return extend3;
    }

    /** 设置扩展字段3。 */
    public void setExtend3(String extend3)
    {
        this.extend3 = extend3;
    }

    /** 按现有格式生成对象字段的调试文本。 */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("formName", getFormName())
            .append("formKey", getFormKey())
            .append("formConfig", getFormConfig())
            .append("sortOrder", getSortOrder())
            .append("extend1", getExtend1())
            .append("extend2", getExtend2())
            .append("extend3", getExtend3())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
