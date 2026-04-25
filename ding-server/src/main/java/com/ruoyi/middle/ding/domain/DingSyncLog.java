package com.ruoyi.middle.ding.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class DingSyncLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    @Excel(name = "钉钉多维表ID")
    private String dingFormId;

    @Excel(name = "本次同步数据条数")
    private Integer syncCount;

    @Excel(name = "同步状态", readConverterExp = "1=成功,2=失败,3=超量拦截")
    private Integer status;

    @Excel(name = "异常信息")
    private String errorMsg;

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

    public String getDingFormId()
    {
        return dingFormId;
    }

    public void setDingFormId(String dingFormId)
    {
        this.dingFormId = dingFormId;
    }

    public Integer getSyncCount()
    {
        return syncCount;
    }

    public void setSyncCount(Integer syncCount)
    {
        this.syncCount = syncCount;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
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
            .append("dingFormId", getDingFormId())
            .append("syncCount", getSyncCount())
            .append("status", getStatus())
            .append("errorMsg", getErrorMsg())
            .append("ext1", getExt1())
            .append("ext2", getExt2())
            .append("ext3", getExt3())
            .toString();
    }
}
