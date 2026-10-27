package com.dj.middle.ding.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.dj.common.annotation.Excel;
import com.dj.common.core.domain.BaseEntity;

/** 套餐变更日志数据对象。 */
public class DingPackageChangeLog extends BaseEntity
{
    /** Java对象序列化版本。 */
    private static final long serialVersionUID = 1L;

    /** 记录主键。 */
    private Long id;

    /** 钉钉企业ID。 */
    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    /** 原套餐ID。 */
    @Excel(name = "原套餐ID")
    private Long oldPackageId;

    /** 新套餐ID。 */
    @Excel(name = "新套餐ID")
    private Long newPackageId;

    /** 变更类型。 */
    @Excel(name = "变更类型", readConverterExp = "1=升级,2=降级")
    private Integer changeType;

    /** 关联订单号。 */
    @Excel(name = "关联订单号")
    private String orderNo;

    /** 操作人。 */
    @Excel(name = "操作人")
    private String operator;

    /** 操作人。 */
    private String ext1;
    /** 预留扩展字段2。 */
    private String ext2;
    /** 预留扩展字段3。 */
    private String ext3;

    /** 读取记录主键。 */
    public Long getId()
    {
        return id;
    }

    /** 设置记录主键。 */
    public void setId(Long id)
    {
        this.id = id;
    }

    /** 读取钉钉企业ID。 */
    public String getDingCorpId()
    {
        return dingCorpId;
    }

    /** 设置钉钉企业ID。 */
    public void setDingCorpId(String dingCorpId)
    {
        this.dingCorpId = dingCorpId;
    }

    /** 读取原套餐ID。 */
    public Long getOldPackageId()
    {
        return oldPackageId;
    }

    /** 设置原套餐ID。 */
    public void setOldPackageId(Long oldPackageId)
    {
        this.oldPackageId = oldPackageId;
    }

    /** 读取新套餐ID。 */
    public Long getNewPackageId()
    {
        return newPackageId;
    }

    /** 设置新套餐ID。 */
    public void setNewPackageId(Long newPackageId)
    {
        this.newPackageId = newPackageId;
    }

    /** 读取变更类型。 */
    public Integer getChangeType()
    {
        return changeType;
    }

    /** 设置变更类型。 */
    public void setChangeType(Integer changeType)
    {
        this.changeType = changeType;
    }

    /** 读取关联订单号。 */
    public String getOrderNo()
    {
        return orderNo;
    }

    /** 设置关联订单号。 */
    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    /** 读取操作人。 */
    public String getOperator()
    {
        return operator;
    }

    /** 设置操作人。 */
    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    /** 读取操作人。 */
    public String getExt1()
    {
        return ext1;
    }

    /** 设置操作人。 */
    public void setExt1(String ext1)
    {
        this.ext1 = ext1;
    }

    /** 读取预留扩展字段2。 */
    public String getExt2()
    {
        return ext2;
    }

    /** 设置预留扩展字段2。 */
    public void setExt2(String ext2)
    {
        this.ext2 = ext2;
    }

    /** 读取预留扩展字段3。 */
    public String getExt3()
    {
        return ext3;
    }

    /** 设置预留扩展字段3。 */
    public void setExt3(String ext3)
    {
        this.ext3 = ext3;
    }

    /** 按现有格式生成对象字段的调试文本。 */
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
