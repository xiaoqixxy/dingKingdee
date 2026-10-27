package com.dj.middle.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 钉钉支付商户与网关配置。 */
@Component
@ConfigurationProperties(prefix = "payment.dingpay")
public class DingPayConfig
{
    /** 应用标识。 */
    private String appId;
    /** 应用密钥。 */
    private String appSecret;
    /** 支付结果通知地址。 */
    private String notifyUrl;

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

    /** 读取应用密钥。 */
    public String getAppSecret()
    {
        return appSecret;
    }

    /** 设置应用密钥。 */
    public void setAppSecret(String appSecret)
    {
        this.appSecret = appSecret;
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
}
