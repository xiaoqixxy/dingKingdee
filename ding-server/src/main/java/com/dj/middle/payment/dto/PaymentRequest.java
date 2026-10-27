package com.dj.middle.payment.dto;

import java.math.BigDecimal;

/** 支付请求数据对象。 */
public class PaymentRequest
{
    /** 业务订单号。 */
    private String orderNo;
    /** 钉钉企业标识。 */
    private String dingCorpId;
    /** 金额。 */
    private BigDecimal amount;
    /** 支付订单标题。 */
    private String subject;
    /** 支付订单说明。 */
    private String body;

    /** 读取业务订单号。 */
    public String getOrderNo()
    {
        return orderNo;
    }

    /** 设置业务订单号。 */
    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    /** 读取钉钉企业标识。 */
    public String getDingCorpId()
    {
        return dingCorpId;
    }

    /** 设置钉钉企业标识。 */
    public void setDingCorpId(String dingCorpId)
    {
        this.dingCorpId = dingCorpId;
    }

    /** 读取金额。 */
    public BigDecimal getAmount()
    {
        return amount;
    }

    /** 设置金额。 */
    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    /** 读取支付订单标题。 */
    public String getSubject()
    {
        return subject;
    }

    /** 设置支付订单标题。 */
    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    /** 读取支付订单说明。 */
    public String getBody()
    {
        return body;
    }

    /** 设置支付订单说明。 */
    public void setBody(String body)
    {
        this.body = body;
    }
}
