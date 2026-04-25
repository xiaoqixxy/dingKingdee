package com.ruoyi.middle.kingdee.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class KingdeeTableConfig extends BaseEntity
{
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

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    public String getFormName()
    {
        return formName;
    }

    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    public String getFormKey()
    {
        return formKey;
    }

    public void setFormKey(String formKey)
    {
        this.formKey = formKey;
    }

    public String getFormConfig()
    {
        return formConfig;
    }

    public void setFormConfig(String formConfig)
    {
        this.formConfig = formConfig;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public String getExtend1()
    {
        return extend1;
    }

    public void setExtend1(String extend1)
    {
        this.extend1 = extend1;
    }

    public String getExtend2()
    {
        return extend2;
    }

    public void setExtend2(String extend2)
    {
        this.extend2 = extend2;
    }

    public String getExtend3()
    {
        return extend3;
    }

    public void setExtend3(String extend3)
    {
        this.extend3 = extend3;
    }

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
