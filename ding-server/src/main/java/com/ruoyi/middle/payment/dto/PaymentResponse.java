package com.ruoyi.middle.payment.dto;

public class PaymentResponse
{
    private String orderNo;
    private String qrCode;
    private String payUrl;
    private String channel;
    private String expireTime;

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getQrCode()
    {
        return qrCode;
    }

    public void setQrCode(String qrCode)
    {
        this.qrCode = qrCode;
    }

    public String getPayUrl()
    {
        return payUrl;
    }

    public void setPayUrl(String payUrl)
    {
        this.payUrl = payUrl;
    }

    public String getChannel()
    {
        return channel;
    }

    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public String getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(String expireTime)
    {
        this.expireTime = expireTime;
    }
}
