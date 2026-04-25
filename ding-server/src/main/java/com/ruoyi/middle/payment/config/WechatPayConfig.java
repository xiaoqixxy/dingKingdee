package com.ruoyi.middle.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.wechat")
public class WechatPayConfig
{
    private String appId;
    private String mchId;
    private String apiV3Key;
    private String privateKeyPath;
    private String certPath;
    private String certSerialNo;
    private String notifyUrl;

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public String getMchId()
    {
        return mchId;
    }

    public void setMchId(String mchId)
    {
        this.mchId = mchId;
    }

    public String getApiV3Key()
    {
        return apiV3Key;
    }

    public void setApiV3Key(String apiV3Key)
    {
        this.apiV3Key = apiV3Key;
    }

    public String getPrivateKeyPath()
    {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath)
    {
        this.privateKeyPath = privateKeyPath;
    }

    public String getCertPath()
    {
        return certPath;
    }

    public void setCertPath(String certPath)
    {
        this.certPath = certPath;
    }

    public String getCertSerialNo()
    {
        return certSerialNo;
    }

    public void setCertSerialNo(String certSerialNo)
    {
        this.certSerialNo = certSerialNo;
    }

    public String getNotifyUrl()
    {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl)
    {
        this.notifyUrl = notifyUrl;
    }
}
