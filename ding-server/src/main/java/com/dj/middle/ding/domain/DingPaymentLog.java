package com.dj.middle.ding.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.dj.common.annotation.Excel;
import com.dj.common.core.domain.BaseEntity;

/** 支付日志数据对象。 */
public class DingPaymentLog extends BaseEntity
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

  /** 订单号。 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 钉钉企业ID。 */
    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    /** 支付渠道。 */
    @Excel(name = "支付渠道")
    private String payChannel;

    /** 第三方支付平台流水号。 */
    @Excel(name = "第三方支付平台流水号")
    private String transactionId;

    /** 支付金额。 */
    @Excel(name = "支付金额")
    private BigDecimal payAmount;

    /** 业务状态。 */
    @Excel(name = "支付状态", readConverterExp = "1=成功,2=失败")
    private Integer status;

    /** 第三方支付回调原始数据。 */
    @Excel(name = "第三方支付回调原始数据")
    private String rawData;

    /** 第三方支付回调原始数据。 */
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

    /** 读取支付渠道。 */
    public String getPayChannel()
    {
        return payChannel;
    }

    /** 设置支付渠道。 */
    public void setPayChannel(String payChannel)
    {
        this.payChannel = payChannel;
    }

    /** 读取第三方支付平台流水号。 */
    public String getTransactionId()
    {
        return transactionId;
    }

    /** 设置第三方支付平台流水号。 */
    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }

    /** 读取支付金额。 */
    public BigDecimal getPayAmount()
    {
        return payAmount;
    }

    /** 设置支付金额。 */
    public void setPayAmount(BigDecimal payAmount)
    {
        this.payAmount = payAmount;
    }

    /** 读取支付状态。 */
    public Integer getStatus()
    {
        return status;
    }

    /** 设置支付状态。 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /** 读取第三方支付回调原始数据。 */
    public String getRawData()
    {
        return rawData;
    }

    /** 设置第三方支付回调原始数据。 */
    public void setRawData(String rawData)
    {
        this.rawData = rawData;
    }

    /** 读取第三方支付回调原始数据。 */
    public String getExt1()
    {
        return ext1;
    }

    /** 设置第三方支付回调原始数据。 */
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
