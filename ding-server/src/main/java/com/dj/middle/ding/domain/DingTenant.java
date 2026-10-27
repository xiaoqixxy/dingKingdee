package com.dj.middle.ding.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.dj.common.annotation.Excel;
import com.dj.common.core.domain.BaseEntity;

/** 钉钉企业数据对象。 */
public class DingTenant extends BaseEntity
{
    /** Java对象序列化版本。 */
    private static final long serialVersionUID = 1L;

    /** 企业套餐。 */
    @Excel(name = "企业套餐")
    private String packageName;
    /** 企业套餐。 */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date lastUsedTime;
    /** 企业类型。 */
    private TenantType tenantType;
    /** 读取企业套餐。 */
    public String getPackageName() { return packageName; }
    /** 设置企业套餐。 */
    public void setPackageName(String value) { packageName = value; }
    /** 读取企业套餐。 */
    public java.util.Date getLastUsedTime() { return lastUsedTime; }
    /** 设置企业套餐。 */
    public void setLastUsedTime(java.util.Date value) { lastUsedTime = value; }
    /** 读取企业类型。 */
    public TenantType getTenantType() { return tenantType; }
    /** 设置企业类型。 */
    public void setTenantType(TenantType value) { tenantType = value; }

    /** 记录主键。 */
    private Long id;

    /** 钉钉企业ID。 */
    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    /** 企业名称。 */
    @Excel(name = "企业名称")
    private String corpName;

    /** 企业所属行业。 */
    @Excel(name = "企业所属行业")
    private String industry;

    /** 企业logo。 */
    @Excel(name = "企业logo")
    private String corpLogoUrl;

    /** 序列号。 */
    @Excel(name = "序列号")
    private String licenseCode;

    /** 渠道码。 */
    @Excel(name = "渠道码")
    private String authChannel;

    /** 渠道类型。 */
    @Excel(name = "渠道类型")
    private String authChannelType;

    /** 是否认证。 */
    @Excel(name = "是否认证")
    private Boolean isAuthenticated;

    /** 认证等级。 */
    @Excel(name = "认证等级")
    private Long authLevel;

    /** 邀请链接。 */
    @Excel(name = "邀请链接")
    private String inviteUrl;

    /** 企业联系人。 */
    @Excel(name = "企业联系人")
    private String contactUser;

    /** 联系人电话。 */
    @Excel(name = "联系人电话")
    private String contactPhone;

    /** 企业状态。 */
    @Excel(name = "企业状态", readConverterExp = "1=正常,2=停用")
    private Integer status;

    /** 企业状态。 */
    private String ext1;
    /** 预留扩展字段2。 */
    private String ext2;
    /** 预留扩展字段3。 */
    private String ext3;

    /** 读取记录主键。 */
    public Long getId()
    {
        return id;
    }

    /** 设置记录主键。 */
    public void setId(Long id)
    {
        this.id = id;
    }

    /** 读取钉钉企业ID。 */
    public String getDingCorpId()
    {
        return dingCorpId;
    }

    /** 设置钉钉企业ID。 */
    public void setDingCorpId(String dingCorpId)
    {
        this.dingCorpId = dingCorpId;
    }

    /** 读取企业名称。 */
    public String getCorpName()
    {
        return corpName;
    }

    /** 设置企业名称。 */
    public void setCorpName(String corpName)
    {
        this.corpName = corpName;
    }

    /** 读取企业所属行业。 */
    public String getIndustry()
    {
        return industry;
    }

    /** 设置企业所属行业。 */
    public void setIndustry(String industry)
    {
        this.industry = industry;
    }

    /** 读取企业logo。 */
    public String getCorpLogoUrl()
    {
        return corpLogoUrl;
    }

    /** 设置企业logo。 */
    public void setCorpLogoUrl(String corpLogoUrl)
    {
        this.corpLogoUrl = corpLogoUrl;
    }

    /** 读取序列号。 */
    public String getLicenseCode()
    {
        return licenseCode;
    }

    /** 设置序列号。 */
    public void setLicenseCode(String licenseCode)
    {
        this.licenseCode = licenseCode;
    }

    /** 读取渠道码。 */
    public String getAuthChannel()
    {
        return authChannel;
    }

    /** 设置渠道码。 */
    public void setAuthChannel(String authChannel)
    {
        this.authChannel = authChannel;
    }

    /** 读取渠道类型。 */
    public String getAuthChannelType()
    {
        return authChannelType;
    }

    /** 设置渠道类型。 */
    public void setAuthChannelType(String authChannelType)
    {
        this.authChannelType = authChannelType;
    }

    /** 读取是否认证。 */
    public Boolean getIsAuthenticated()
    {
        return isAuthenticated;
    }

    /** 设置是否认证。 */
    public void setIsAuthenticated(Boolean isAuthenticated)
    {
        this.isAuthenticated = isAuthenticated;
    }

    /** 读取认证等级。 */
    public Long getAuthLevel()
    {
        return authLevel;
    }

    /** 设置认证等级。 */
    public void setAuthLevel(Long authLevel)
    {
        this.authLevel = authLevel;
    }

    /** 读取邀请链接。 */
    public String getInviteUrl()
    {
        return inviteUrl;
    }

    /** 设置邀请链接。 */
    public void setInviteUrl(String inviteUrl)
    {
        this.inviteUrl = inviteUrl;
    }

    /** 读取企业联系人。 */
    public String getContactUser()
    {
        return contactUser;
    }

    /** 设置企业联系人。 */
    public void setContactUser(String contactUser)
    {
        this.contactUser = contactUser;
    }

    /** 读取联系人电话。 */
    public String getContactPhone()
    {
        return contactPhone;
    }

    /** 设置联系人电话。 */
    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    /** 读取企业状态。 */
    public Integer getStatus()
    {
        return status;
    }

    /** 设置企业状态。 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /** 读取企业状态。 */
    public String getExt1()
    {
        return ext1;
    }

    /** 设置企业状态。 */
    public void setExt1(String ext1)
    {
        this.ext1 = ext1;
    }

    /** 读取预留扩展字段2。 */
    public String getExt2()
    {
        return ext2;
    }

    /** 设置预留扩展字段2。 */
    public void setExt2(String ext2)
    {
        this.ext2 = ext2;
    }

    /** 读取预留扩展字段3。 */
    public String getExt3()
    {
        return ext3;
    }

    /** 设置预留扩展字段3。 */
    public void setExt3(String ext3)
    {
        this.ext3 = ext3;
    }

    /** 按现有格式生成对象字段的调试文本。 */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("dingCorpId", getDingCorpId())
            .append("corpName", getCorpName())
            .append("industry", getIndustry())
            .append("corpLogoUrl", getCorpLogoUrl())
            .append("licenseCode", getLicenseCode())
            .append("authChannel", getAuthChannel())
            .append("authChannelType", getAuthChannelType())
            .append("isAuthenticated", getIsAuthenticated())
            .append("authLevel", getAuthLevel())
            .append("inviteUrl", getInviteUrl())
            .append("contactUser", getContactUser())
            .append("contactPhone", getContactPhone())
            .append("status", getStatus())
            .append("ext1", getExt1())
            .append("ext2", getExt2())
            .append("ext3", getExt3())
            .toString();
    }
}