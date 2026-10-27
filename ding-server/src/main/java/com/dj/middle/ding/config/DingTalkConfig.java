package com.dj.middle.ding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 钉钉应用与授权配置。 */
@Component
@ConfigurationProperties(prefix = "dingtalk")
public class DingTalkConfig
{
    /** 应用标识。 */
    private String appId;
    /** 应用密钥。 */
    private String appSecret;
    /** 语言配置。 */
    private String language;
    /** 套件票据。 */
    private String suiteTicket;
    /** 套件标识。 */
    private String suiteKey;

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

    /** 读取语言配置。 */
    public String getLanguage() {
        return language;
    }

    /** 设置语言配置。 */
    public void setLanguage(String language) {
        this.language = language;
    }

    /** 读取套件票据。 */
    public String getSuiteTicket() {
        return suiteTicket;
    }

    /** 设置套件票据。 */
    public void setSuiteTicket(String suiteTicket) {
        this.suiteTicket = suiteTicket;
    }

    /** 读取套件标识。 */
    public String getSuiteKey() {
        return suiteKey;
    }

    /** 设置套件标识。 */
    public void setSuiteKey(String suiteKey) {
        this.suiteKey = suiteKey;
    }
}