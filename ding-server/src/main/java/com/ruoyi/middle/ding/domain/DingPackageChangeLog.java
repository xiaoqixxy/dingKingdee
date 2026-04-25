package com.ruoyi.middle.ding.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class DingPackageChangeLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    @Excel(name = "原套餐ID")
    private Long oldPackageId;

    @Excel(name = "新套餐ID")
    private Long newPackageId;

    @Excel(name = "变更类型", readConverterExp = "1=升级,2=降级")
    private Integer changeType;

    @Excel(name = "关联订单号")
    private String orderNo;

    @Excel(name = "操作人")
    private String operator;

    private String ext1;
    private String ext2;
    private String ext3;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getDingCorpId()
    {
        return dingCorpId;
    }

    public void setDingCorpId(String dingCorpId)
    {
        this.dingCorpId = dingCorpId;
    }

    public Long getOldPackageId()
    {
        return oldPackageId;
    }

    public void setOldPackageId(Long oldPackageId)
    {
        this.oldPackageId = oldPackageId;
    }

    public Long getNewPackageId()
    {
        return newPackageId;
    }

    public void setNewPackageId(Long newPackageId)
    {
        this.newPackageId = newPackageId;
    }

    public Integer getChangeType()
    {
        return changeType;
    }

    public void setChangeType(Integer changeType)
    {
        this.changeType = changeType;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public String getExt1()
    {
        return ext1;
    }

    public void setExt1(String ext1)
    {
        this.ext1 = ext1;
    }

    public String getExt2()
    {
        return ext2;
    }

    public void setExt2(String ext2)
    {
        this.ext2 = ext2;
    }

    public String getExt3()
    {
        return ext3;
    }

    public void setExt3(String ext3)
    {
        this.ext3 = ext3;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("dingCorpId", getDingCorpId())
            .append("oldPackageId", getOldPackageId())
            .append("newPackageId", getNewPackageId())
            .append("changeType", getChangeType())
            .append("orderNo", getOrderNo())
            .append("operator", getOperator())
            .append("ext1", getExt1())
            .append("ext2", getExt2())
            .append("ext3", getExt3())
            .toString();
    }
}
