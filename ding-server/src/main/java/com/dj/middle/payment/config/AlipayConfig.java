package com.dj.middle.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 支付宝商户与签名配置。 */
@Component
@ConfigurationProperties(prefix = "payment.alipay")
public class AlipayConfig
{
    /** 应用标识。 */
    private String appId;
    /** 支付签名私钥。 */
    private String privateKey;
    /** 支付宝验签公钥。 */
    private String alipayPublicKey;
    /** 支付网关地址。 */
    private String gatewayUrl;
    /** 支付结果通知地址。 */
    private String notifyUrl;
    /** 支付完成后的返回地址。 */
    private String returnUrl;

    /** 读取应用标识。 */
    public String getAppId()
    {
        return appId;
    }

    /** 设置应用标识。 */
    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    /** 读取支付签名私钥。 */
    public String getPrivateKey()
    {
        return privateKey;
    }

    /** 设置支付签名私钥。 */
    public void setPrivateKey(String privateKey)
    {
        this.privateKey = privateKey;
    }

    /** 读取支付宝验签公钥。 */
    public String getAlipayPublicKey()
    {
        return alipayPublicKey;
    }

    /** 设置支付宝验签公钥。 */
    public void setAlipayPublicKey(String alipayPublicKey)
    {
        this.alipayPublicKey = alipayPublicKey;
    }

    /** 读取支付网关地址。 */
    public String getGatewayUrl()
    {
        return gatewayUrl;
    }

    /** 设置支付网关地址。 */
    public void setGatewayUrl(String gatewayUrl)
    {
        this.gatewayUrl = gatewayUrl;
    }

    /** 读取支付结果通知地址。 */
    public String getNotifyUrl()
    {
        return notifyUrl;
    }

    /** 设置支付结果通知地址。 */
    public void setNotifyUrl(String notifyUrl)
    {
        this.notifyUrl = notifyUrl;
    }

    /** 读取支付完成后的返回地址。 */
    public String getReturnUrl()
    {
        return returnUrl;
    }

    /** 设置支付完成后的返回地址。 */
    public void setReturnUrl(String returnUrl)
    {
        this.returnUrl = returnUrl;
    }
}
