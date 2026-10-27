package com.dj.middle.ding.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.dj.common.annotation.Excel;
import com.dj.common.core.domain.BaseEntity;

/** 企业订阅数据对象。 */
public class DingSubscription extends BaseEntity
{
    /** Java对象序列化版本。 */
    private static final long serialVersionUID = 1L;

    /** 企业名称。 */
    @Excel(name = "企业名称")
    private String corpName;
    /** 读取企业名称。 */
    public String getCorpName() { return corpName; }
    /** 设置企业名称。 */
    public void setCorpName(String corpName) { this.corpName = corpName; }

    /** 记录主键。 */
    private Long id;

    /** 钉钉企业ID。 */
    @Excel(name = "钉钉企业ID")
    private String dingCorpId;

    /** 钉钉企业ID。 */
    private Long productId;

    /** 产品类型。 */
    @Excel(name = "产品类型", readConverterExp = "1=包月,2=包年")
    private Integer productType;

    /** 套餐生效时间。 */
    @Excel(name = "套餐生效时间")
    private Date startTime;

    /** 套餐到期时间。 */
    @Excel(name = "套餐到期时间")
    private Date endTime;

    /** 是否自动续费。 */
    @Excel(name = "是否自动续费", readConverterExp = "0=否,1=是")
    private Integer autoRenew;

    /** 已使用绑定表单数量。 */
    @Excel(name = "已使用绑定表单数量")
    private Integer usedFormCount;

    /** 本月已同步数据总量。 */
    @Excel(name = "本月已同步数据总量")
    private Integer monthUsedSync;

    /** 订阅状态。 */
    @Excel(name = "订阅状态", readConverterExp = "1=生效中,2=已过期,3=已取消")
    private Integer status;

    /** 订阅状态。 */
    private String productName;

    /** 预留扩展字段1。 */
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

    /** 读取钉钉企业ID。 */
    public Long getProductId()
    {
        return productId;
    }

    /** 设置钉钉企业ID。 */
    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    /** 读取产品类型。 */
    public Integer getProductType()
    {
        return productType;
    }

    /** 设置产品类型。 */
    public void setProductType(Integer productType)
    {
        this.productType = productType;
    }

    /** 读取套餐生效时间。 */
    public Date getStartTime()
    {
        return startTime;
    }

    /** 设置套餐生效时间。 */
    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    /** 读取套餐到期时间。 */
    public Date getEndTime()
    {
        return endTime;
    }

    /** 设置套餐到期时间。 */
    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    /** 读取是否自动续费。 */
    public Integer getAutoRenew()
    {
        return autoRenew;
    }

    /** 设置是否自动续费。 */
    public void setAutoRenew(Integer autoRenew)
    {
        this.autoRenew = autoRenew;
    }

    /** 读取已使用绑定表单数量。 */
    public Integer getUsedFormCount()
    {
        return usedFormCount;
    }

    /** 设置已使用绑定表单数量。 */
    public void setUsedFormCount(Integer usedFormCount)
    {
        this.usedFormCount = usedFormCount;
    }

    /** 读取本月已同步数据总量。 */
    public Integer getMonthUsedSync()
    {
        return monthUsedSync;
    }

    /** 设置本月已同步数据总量。 */
    public void setMonthUsedSync(Integer monthUsedSync)
    {
        this.monthUsedSync = monthUsedSync;
    }

    /** 读取订阅状态。 */
    public Integer getStatus()
    {
        return status;
    }

    /** 设置订阅状态。 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /** 读取订阅状态。 */
    public String getProductName()
    {
        return productName;
    }

    /** 设置订阅状态。 */
    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    /** 读取预留扩展字段1。 */
    public String getExt1()
    {
        return ext1;
    }

    /** 设置预留扩展字段1。 */
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
