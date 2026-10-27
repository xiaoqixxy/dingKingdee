package com.dj.middle.ding.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.dj.common.annotation.Excel;
import com.dj.common.core.domain.BaseEntity;

/** 发票数据对象。 */
public class DingInvoice extends BaseEntity
{
    /** 企业名称。 */
    @Excel(name = "企业名称")
    private String corpName;
    /** 读取企业名称。 */
    public String getCorpName() { return corpName; }
    /** 设置企业名称。 */
    public void setCorpName(String corpName) { this.corpName = corpName; }

    /** Java对象序列化版本。 */
    private static final long serialVersionUID = 1L;

    /** 记录主键。 */
    private Long id;

  /** 钉钉企业ID。 */
    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    /** 订单号。 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 发票类型。 */
    @Excel(name = "发票类型", readConverterExp = "1=普通发票,2=增值税专用发票")
    private Integer invoiceType;

    /** 发票抬头。 */
    @Excel(name = "发票抬头")
    private String invoiceTitle;

    /** 纳税人识别号。 */
    @Excel(name = "纳税人识别号")
    private String taxNo;

    /** 开票金额。 */
    @Excel(name = "开票金额")
    private BigDecimal amount;

    /** 开票状态。 */
    @Excel(name = "开票状态", readConverterExp = "0=待开票,1=已开票")
    private Integer status;

    /** 开票状态。 */
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

    /** 读取订单号。 */
    public String getOrderNo()
    {
        return orderNo;
    }

    /** 设置订单号。 */
    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    /** 读取发票类型。 */
    public Integer getInvoiceType()
    {
        return invoiceType;
    }

    /** 设置发票类型。 */
    public void setInvoiceType(Integer invoiceType)
    {
        this.invoiceType = invoiceType;
    }

    /** 读取发票抬头。 */
    public String getInvoiceTitle()
    {
        return invoiceTitle;
    }

    /** 设置发票抬头。 */
    public void setInvoiceTitle(String invoiceTitle)
    {
        this.invoiceTitle = invoiceTitle;
    }

    /** 读取纳税人识别号。 */
    public String getTaxNo()
    {
        return taxNo;
    }

    /** 设置纳税人识别号。 */
    public void setTaxNo(String taxNo)
    {
        this.taxNo = taxNo;
    }

    /** 读取开票金额。 */
    public BigDecimal getAmount()
    {
        return amount;
    }

    /** 设置开票金额。 */
    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    /** 读取开票状态。 */
    public Integer getStatus()
    {
        return status;
    }

    /** 设置开票状态。 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /** 读取开票状态。 */
    public String getExt1()
    {
        return ext1;
    }

    /** 设置开票状态。 */
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
            .append("orderNo", getOrderNo())
            .append("invoiceType", getInvoiceType())
            .append("invoiceTitle", getInvoiceTitle())
            .append("taxNo", getTaxNo())
            .append("amount", getAmount())
            .append("status", getStatus())
            .append("ext1", getExt1())
            .append("ext2", getExt2())
            .append("ext3", getExt3())
            .toString();
    }
}
