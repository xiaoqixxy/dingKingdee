package com.dj.middle.kingdee.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.dj.common.annotation.Excel;
import com.dj.common.core.domain.BaseEntity;

/**
 * 金蝶凭证配置对象 kingdee_voucher_config
 * 
 * @author ruoyi
 * @date 2026-07-04
 */
public class KingdeeVoucherConfig extends BaseEntity
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

    /** 应用密钥。 */
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

    /** 设置记录主键。 */
    public void setId(String id) 
    {
        this.id = id;
    }

    /** 读取记录主键。 */
    public String getId() 
    {
        return id;
    }

    /** 设置配置名称。 */
    public void setName(String name) 
    {
        this.name = name;
    }

    /** 读取配置名称。 */
    public String getName() 
    {
        return name;
    }

    /** 设置钉钉企业ID。 */
    public void setDingCorpId(String dingCorpId) 
    {
        this.dingCorpId = dingCorpId;
    }

    /** 读取钉钉企业ID。 */
    public String getDingCorpId() 
    {
        return dingCorpId;
    }

    /** 设置金蝶服务地址。 */
    public void setServerUrl(String serverUrl) 
    {
        this.serverUrl = serverUrl;
    }

    /** 读取金蝶服务地址。 */
    public String getServerUrl() 
    {
        return serverUrl;
    }

    /** 设置账套ID。 */
    public void setCId(String cId) 
    {
        this.cId = cId;
    }

    /** 读取账套ID。 */
    public String getCId() 
    {
        return cId;
    }

    /** 设置用户名。 */
    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    /** 读取用户名。 */
    public String getUserName() 
    {
        return userName;
    }

    /** 设置应用ID。 */
    public void setAppId(String appId) 
    {
        this.appId = appId;
    }

    /** 读取应用ID。 */
    public String getAppId() 
    {
        return appId;
    }

    /** 设置应用密钥。 */
    public void setAppSecret(String appSecret) 
    {
        this.appSecret = appSecret;
    }

    /** 读取应用密钥。 */
    public String getAppSecret() 
    {
        return appSecret;
    }

    /** 设置扩展字段1。 */
    public void setExt1(String ext1) 
    {
        this.ext1 = ext1;
    }

    /** 读取扩展字段1。 */
    public String getExt1() 
    {
        return ext1;
    }

    /** 设置扩展字段2。 */
    public void setExt2(String ext2) 
    {
        this.ext2 = ext2;
    }

    /** 读取扩展字段2。 */
    public String getExt2() 
    {
        return ext2;
    }

    /** 设置扩展字段3。 */
    public void setExt3(String ext3) 
    {
        this.ext3 = ext3;
    }

    /** 读取扩展字段3。 */
    public String getExt3() 
    {
        return ext3;
    }

    /** 设置排序号。 */
    public void setOrderNo(Integer orderNo) 
    {
        this.orderNo = orderNo;
    }

    /** 读取排序号。 */
    public Integer getOrderNo() 
    {
        return orderNo;
    }

    /** 按现有格式生成对象字段的调试文本。 */
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
