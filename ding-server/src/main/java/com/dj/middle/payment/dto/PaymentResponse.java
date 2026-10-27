package com.dj.middle.payment.dto;

/** 支付响应数据对象。 */
public class PaymentResponse
{
    /** 业务订单号。 */
    private String orderNo;
    /** 支付二维码内容。 */
    private String qrCode;
    /** 收款页面地址。 */
    private String payUrl;
    /** 支付渠道编码。 */
    private String channel;
    /** 支付过期时间。 */
    private String expireTime;

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

    /** 读取支付二维码内容。 */
    public String getQrCode()
    {
        return qrCode;
    }

    /** 设置支付二维码内容。 */
    public void setQrCode(String qrCode)
    {
        this.qrCode = qrCode;
    }

    /** 读取收款页面地址。 */
    public String getPayUrl()
    {
        return payUrl;
    }

    /** 设置收款页面地址。 */
    public void setPayUrl(String payUrl)
    {
        this.payUrl = payUrl;
    }

    /** 读取支付渠道编码。 */
    public String getChannel()
    {
        return channel;
    }

    /** 设置支付渠道编码。 */
    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    /** 读取支付过期时间。 */
    public String getExpireTime()
    {
        return expireTime;
    }

    /** 设置支付过期时间。 */
    public void setExpireTime(String expireTime)
    {
        this.expireTime = expireTime;
    }
}
