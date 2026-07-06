package com.ruoyi.middle.kingdee.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 金蝶凭证配置对象 kingdee_voucher_config
 * 
 * @author ruoyi
 * @date 2026-07-04
 */
public class KingdeeVoucherConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private String id;

    /** 配置名称 */
    @Excel(name = "配置名称")
    private String name;

    /** 钉钉企业ID */
    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    /** 金蝶服务地址 */
    @Excel(name = "金蝶服务地址")
    private String serverUrl;

    /** 账套ID */
    @Excel(name = "账套ID")
    private String cId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 应用ID */
    @Excel(name = "应用ID")
    private String appId;

    /** 应用密钥 */
    @Excel(name = "应用密钥")
    private String appSecret;

    /** 扩展字段1 */
    @Excel(name = "扩展字段1")
    private String ext1;

    /** 扩展字段2 */
    @Excel(name = "扩展字段2")
    private String ext2;

    /** 扩展字段3 */
    @Excel(name = "扩展字段3")
    private String ext3;

    /** 排序号 */
    @Excel(name = "排序号")
    private Integer orderNo;

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getId() 
    {
        return id;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public void setDingCorpId(String dingCorpId) 
    {
        this.dingCorpId = dingCorpId;
    }

    public String getDingCorpId() 
    {
        return dingCorpId;
    }

    public void setServerUrl(String serverUrl) 
    {
        this.serverUrl = serverUrl;
    }

    public String getServerUrl() 
    {
        return serverUrl;
    }

    public void setCId(String cId) 
    {
        this.cId = cId;
    }

    public String getCId() 
    {
        return cId;
    }

    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getUserName() 
    {
        return userName;
    }

    public void setAppId(String appId) 
    {
        this.appId = appId;
    }

    public String getAppId() 
    {
        return appId;
    }

    public void setAppSecret(String appSecret) 
    {
        this.appSecret = appSecret;
    }

    public String getAppSecret() 
    {
        return appSecret;
    }

    public void setExt1(String ext1) 
    {
        this.ext1 = ext1;
    }

    public String getExt1() 
    {
        return ext1;
    }

    public void setExt2(String ext2) 
    {
        this.ext2 = ext2;
    }

    public String getExt2() 
    {
        return ext2;
    }

    public void setExt3(String ext3) 
    {
        this.ext3 = ext3;
    }

    public String getExt3() 
    {
        return ext3;
    }

    public void setOrderNo(Integer orderNo) 
    {
        this.orderNo = orderNo;
    }

    public Integer getOrderNo() 
    {
        return orderNo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("dingCorpId", getDingCorpId())
            .append("serverUrl", getServerUrl())
            .append("cId", getCId())
            .append("userName", getUserName())
            .append("appId", getAppId())
            .append("appSecret", getAppSecret())
            .append("ext1", getExt1())
            .append("ext2", getExt2())
            .append("ext3", getExt3())
            .append("orderNo", getOrderNo())
            .toString();
    }
}
