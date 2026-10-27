package com.dj.middle.ding.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.dj.common.annotation.Excel;
import com.dj.common.core.domain.BaseEntity;

/** 同步日志数据对象。 */
public class DingSyncLog extends BaseEntity
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

    /** 记录主键。 */
    private Long id;

 /** 钉钉企业ID。 */
    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    /** 钉钉多维表ID。 */
    @Excel(name = "钉钉多维表ID")
    private String dingFormId;

    /** 本次同步数据条数。 */
    @Excel(name = "本次同步数据条数")
    private Integer syncCount;

    /** 同步状态。 */
    @Excel(name = "同步状态", readConverterExp = "1=成功,2=失败,3=超量拦截")
    private Integer status;

    /** 异常信息。 */
    @Excel(name = "异常信息")
    private String errorMsg;

    /** 异常信息。 */
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

    /** 读取钉钉多维表ID。 */
    public String getDingFormId()
    {
        return dingFormId;
    }

    /** 设置钉钉多维表ID。 */
    public void setDingFormId(String dingFormId)
    {
        this.dingFormId = dingFormId;
    }

    /** 读取本次同步数据条数。 */
    public Integer getSyncCount()
    {
        return syncCount;
    }

    /** 设置本次同步数据条数。 */
    public void setSyncCount(Integer syncCount)
    {
        this.syncCount = syncCount;
    }

    /** 读取同步状态。 */
    public Integer getStatus()
    {
        return status;
    }

    /** 设置同步状态。 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /** 读取异常信息。 */
    public String getErrorMsg()
    {
        return errorMsg;
    }

    /** 设置异常信息。 */
    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    /** 读取异常信息。 */
    public String getExt1()
    {
        return ext1;
    }

    /** 设置异常信息。 */
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
