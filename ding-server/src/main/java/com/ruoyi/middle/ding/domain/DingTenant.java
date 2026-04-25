package com.ruoyi.middle.ding.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class DingTenant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    @Excel(name = "企业名称")
    private String corpName;

    @Excel(name = "企业所属行业")
    private String industry;

    @Excel(name = "企业logo")
    private String corpLogoUrl;

    @Excel(name = "序列号")
    private String licenseCode;

    @Excel(name = "渠道码")
    private String authChannel;

    @Excel(name = "渠道类型")
    private String authChannelType;

    @Excel(name = "是否认证")
    private Boolean isAuthenticated;

    @Excel(name = "认证等级")
    private Long authLevel;

    @Excel(name = "邀请链接")
    private String inviteUrl;

    @Excel(name = "企业联系人")
    private String contactUser;

    @Excel(name = "联系人电话")
    private String contactPhone;

    @Excel(name = "企业状态", readConverterExp = "1=正常,2=停用")
    private Integer status;

    private String ext1;
    private String ext2;
    private String ext3;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getDingCorpId()
    {
        return dingCorpId;
    }

    public void setDingCorpId(String dingCorpId)
    {
        this.dingCorpId = dingCorpId;
    }

    public String getCorpName()
    {
        return corpName;
    }

    public void setCorpName(String corpName)
    {
        this.corpName = corpName;
    }

    public String getIndustry()
    {
        return industry;
    }

    public void setIndustry(String industry)
    {
        this.industry = industry;
    }

    public String getCorpLogoUrl()
    {
        return corpLogoUrl;
    }

    public void setCorpLogoUrl(String corpLogoUrl)
    {
        this.corpLogoUrl = corpLogoUrl;
    }

    public String getLicenseCode()
    {
        return licenseCode;
    }

    public void setLicenseCode(String licenseCode)
    {
        this.licenseCode = licenseCode;
    }

    public String getAuthChannel()
    {
        return authChannel;
    }

    public void setAuthChannel(String authChannel)
    {
        this.authChannel = authChannel;
    }

    public String getAuthChannelType()
    {
        return authChannelType;
    }

    public void setAuthChannelType(String authChannelType)
    {
        this.authChannelType = authChannelType;
    }

    public Boolean getIsAuthenticated()
    {
        return isAuthenticated;
    }

    public void setIsAuthenticated(Boolean isAuthenticated)
    {
        this.isAuthenticated = isAuthenticated;
    }

    public Long getAuthLevel()
    {
        return authLevel;
    }

    public void setAuthLevel(Long authLevel)
    {
        this.authLevel = authLevel;
    }

    public String getInviteUrl()
    {
        return inviteUrl;
    }

    public void setInviteUrl(String inviteUrl)
    {
        this.inviteUrl = inviteUrl;
    }

    public String getContactUser()
    {
        return contactUser;
    }

    public void setContactUser(String contactUser)
    {
        this.contactUser = contactUser;
    }

    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getExt1()
    {
        return ext1;
    }

    public void setExt1(String ext1)
    {
        this.ext1 = ext1;
    }

    public String getExt2()
    {
        return ext2;
    }

    public void setExt2(String ext2)
    {
        this.ext2 = ext2;
    }

    public String getExt3()
    {
        return ext3;
    }

    public void setExt3(String ext3)
    {
        this.ext3 = ext3;
    }

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