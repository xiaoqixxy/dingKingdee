package com.ruoyi.middle.ding.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class DingInvoice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    @Excel(name = "订单号")
    private String orderNo;

    @Excel(name = "发票类型", readConverterExp = "1=普通发票,2=增值税专用发票")
    private Integer invoiceType;

    @Excel(name = "发票抬头")
    private String invoiceTitle;

    @Excel(name = "纳税人识别号")
    private String taxNo;

    @Excel(name = "开票金额")
    private BigDecimal amount;

    @Excel(name = "开票状态", readConverterExp = "0=待开票,1=已开票")
    private Integer status;

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

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Integer getInvoiceType()
    {
        return invoiceType;
    }

    public void setInvoiceType(Integer invoiceType)
    {
        this.invoiceType = invoiceType;
    }

    public String getInvoiceTitle()
    {
        return invoiceTitle;
    }

    public void setInvoiceTitle(String invoiceTitle)
    {
        this.invoiceTitle = invoiceTitle;
    }

    public String getTaxNo()
    {
        return taxNo;
    }

    public void setTaxNo(String taxNo)
    {
        this.taxNo = taxNo;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
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
