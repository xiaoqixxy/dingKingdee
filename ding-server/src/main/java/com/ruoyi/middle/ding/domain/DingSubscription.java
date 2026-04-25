package com.ruoyi.middle.ding.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class DingSubscription extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    private Long productId;

    @Excel(name = "产品类型", readConverterExp = "1=包月,2=包年")
    private Integer productType;

    @Excel(name = "套餐生效时间")
    private Date startTime;

    @Excel(name = "套餐到期时间")
    private Date endTime;

    @Excel(name = "是否自动续费", readConverterExp = "0=否,1=是")
    private Integer autoRenew;

    @Excel(name = "已使用绑定表单数量")
    private Integer usedFormCount;

    @Excel(name = "本月已同步数据总量")
    private Integer monthUsedSync;

    @Excel(name = "订阅状态", readConverterExp = "1=生效中,2=已过期,3=已取消")
    private Integer status;

    private String productName;

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

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    public Integer getProductType()
    {
        return productType;
    }

    public void setProductType(Integer productType)
    {
        this.productType = productType;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public Integer getAutoRenew()
    {
        return autoRenew;
    }

    public void setAutoRenew(Integer autoRenew)
    {
        this.autoRenew = autoRenew;
    }

    public Integer getUsedFormCount()
    {
        return usedFormCount;
    }

    public void setUsedFormCount(Integer usedFormCount)
    {
        this.usedFormCount = usedFormCount;
    }

    public Integer getMonthUsedSync()
    {
        return monthUsedSync;
    }

    public void setMonthUsedSync(Integer monthUsedSync)
    {
        this.monthUsedSync = monthUsedSync;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
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
            .append("productId", getProductId())
            .append("productType", getProductType())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("autoRenew", getAutoRenew())
            .append("usedFormCount", getUsedFormCount())
            .append("monthUsedSync", getMonthUsedSync())
            .append("status", getStatus())
            .append("productName", getProductName())
            .append("ext1", getExt1())
            .append("ext2", getExt2())
            .append("ext3", getExt3())
            .toString();
    }
}
