package com.ruoyi.middle.ding.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class DingOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "订单号")
    private String orderNo;

    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    private Long productId;

    @Excel(name = "订单类型", readConverterExp = "1=新购,2=升级,3=降级,4=续费")
    private Integer orderType;

    @Excel(name = "订单总金额")
    private BigDecimal totalAmount;

    @Excel(name = "实付金额")
    private BigDecimal payAmount;

    @Excel(name = "支付状态", readConverterExp = "0=待支付,1=已支付,2=已取消,3=退款")
    private Integer payStatus;

    @Excel(name = "支付时间")
    private Date payTime;

    private Date expireTime;

    @Excel(name = "支付渠道")
    private String payChannel;

    @Excel(name = "第三方支付流水号")
    private String transactionId;

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

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    public Integer getOrderType()
    {
        return orderType;
    }

    public void setOrderType(Integer orderType)
    {
        this.orderType = orderType;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPayAmount()
    {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount)
    {
        this.payAmount = payAmount;
    }

    public Integer getPayStatus()
    {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus)
    {
        this.payStatus = payStatus;
    }

    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
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
            .append("productId", getProductId())
            .append("orderType", getOrderType())
            .append("totalAmount", getTotalAmount())
            .append("payAmount", getPayAmount())
            .append("payStatus", getPayStatus())
            .append("payTime", getPayTime())
            .append("expireTime", getExpireTime())
            .append("payChannel", getPayChannel())
            .append("transactionId", getTransactionId())
            .append("remark", getRemark())
            .append("ext1", getExt1())
            .append("ext2", getExt2())
            .append("ext3", getExt3())
            .toString();
    }
}
