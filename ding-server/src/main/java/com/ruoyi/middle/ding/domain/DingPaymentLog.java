package com.ruoyi.middle.ding.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class DingPaymentLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "订单号")
    private String orderNo;

    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    @Excel(name = "支付渠道")
    private String payChannel;

    @Excel(name = "第三方支付平台流水号")
    private String transactionId;

    @Excel(name = "支付金额")
    private BigDecimal payAmount;

    @Excel(name = "支付状态", readConverterExp = "1=成功,2=失败")
    private Integer status;

    @Excel(name = "第三方支付回调原始数据")
    private String rawData;

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

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getDingCorpId()
    {
        return dingCorpId;
    }

    public void setDingCorpId(String dingCorpId)
    {
        this.dingCorpId = dingCorpId;
    }

    public String getPayChannel()
    {
        return payChannel;
    }

    public void setPayChannel(String payChannel)
    {
        this.payChannel = payChannel;
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }

    public BigDecimal getPayAmount()
    {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount)
    {
        this.payAmount = payAmount;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getRawData()
    {
        return rawData;
    }

    public void setRawData(String rawData)
    {
        this.rawData = rawData;
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
            .append("orderNo", getOrderNo())
            .append("dingCorpId", getDingCorpId())
            .append("payChannel", getPayChannel())
            .append("transactionId", getTransactionId())
            .append("payAmount", getPayAmount())
            .append("status", getStatus())
            .append("rawData", getRawData())
            .append("ext1", getExt1())
            .append("ext2", getExt2())
            .append("ext3", getExt3())
            .toString();
    }
}
