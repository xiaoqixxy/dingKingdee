package com.dj.middle.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 微信支付商户、证书与回调配置。 */
@Component
@ConfigurationProperties(prefix = "payment.wechat")
public class WechatPayConfig
{
    /** 应用标识。 */
    private String appId;
    /** 微信支付商户号。 */
    private String mchId;
    /** 微信支付回调解密密钥。 */
    private String apiV3Key;
    /** 商户私钥文件路径。 */
    private String privateKeyPath;
    /** 证书文件路径。 */
    private String certPath;
    /** 商户证书序列号。 */
    private String certSerialNo;
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

    /** 读取微信支付商户号。 */
    public String getMchId()
    {
        return mchId;
    }

    /** 设置微信支付商户号。 */
    public void setMchId(String mchId)
    {
        this.mchId = mchId;
    }

    /** 读取微信支付回调解密密钥。 */
    public String getApiV3Key()
    {
        return apiV3Key;
    }

    /** 设置微信支付回调解密密钥。 */
    public void setApiV3Key(String apiV3Key)
    {
        this.apiV3Key = apiV3Key;
    }

    /** 读取商户私钥文件路径。 */
    public String getPrivateKeyPath()
    {
        return privateKeyPath;
    }

    /** 设置商户私钥文件路径。 */
    public void setPrivateKeyPath(String privateKeyPath)
    {
        this.privateKeyPath = privateKeyPath;
    }

    /** 读取证书文件路径。 */
    public String getCertPath()
    {
        return certPath;
    }

    /** 设置证书文件路径。 */
    public void setCertPath(String certPath)
    {
        this.certPath = certPath;
    }

    /** 读取商户证书序列号。 */
    public String getCertSerialNo()
    {
        return certSerialNo;
    }

    /** 设置商户证书序列号。 */
    public void setCertSerialNo(String certSerialNo)
    {
        this.certSerialNo = certSerialNo;
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
