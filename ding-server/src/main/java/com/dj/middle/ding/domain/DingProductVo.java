package com.dj.middle.ding.domain;

import java.math.BigDecimal;

/** 产品权益视图数据对象。 */
public class DingProductVo
{
    /** 记录主键。 */
    private Long id;
    /** 产品套餐名称。 */
    private String productName;
    /** 产品计费类型。 */
    private Integer productType;
    /** 产品分类。 */
    private String productCategory;
    /** 套餐价格。 */
    private BigDecimal price;
    /** 允许绑定的表单数量。 */
    private Integer syncFormLimit;
    /** 单次同步数量上限。 */
    private Integer singleSyncLimit;
    /** 每月同步数量上限。 */
    private Integer monthSyncLimit;
    /** 业务状态。 */
    private Integer status;
    /** 到期时间。 */
    private String endTime;
    private boolean active;
    private String accessMessage;
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active=active; }
    public String getAccessMessage() { return accessMessage; }
    public void setAccessMessage(String message) { this.accessMessage=message; }

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

    /** 读取产品套餐名称。 */
    public String getProductName()
    {
        return productName;
    }

    /** 设置产品套餐名称。 */
    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    /** 读取产品计费类型。 */
    public Integer getProductType()
    {
        return productType;
    }

    /** 设置产品计费类型。 */
    public void setProductType(Integer productType)
    {
        this.productType = productType;
    }

    /** 读取产品分类。 */
    public String getProductCategory()
    {
        return productCategory;
    }

    /** 设置产品分类。 */
    public void setProductCategory(String productCategory)
    {
        this.productCategory = productCategory;
    }

    /** 读取套餐价格。 */
    public BigDecimal getPrice()
    {
        return price;
    }

    /** 设置套餐价格。 */
    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    /** 读取允许绑定的表单数量。 */
    public Integer getSyncFormLimit()
    {
        return syncFormLimit;
    }

    /** 设置允许绑定的表单数量。 */
    public void setSyncFormLimit(Integer syncFormLimit)
    {
        this.syncFormLimit = syncFormLimit;
    }

    /** 读取单次同步数量上限。 */
    public Integer getSingleSyncLimit()
    {
        return singleSyncLimit;
    }

    /** 设置单次同步数量上限。 */
    public void setSingleSyncLimit(Integer singleSyncLimit)
    {
        this.singleSyncLimit = singleSyncLimit;
    }

    /** 读取每月同步数量上限。 */
    public Integer getMonthSyncLimit()
    {
        return monthSyncLimit;
    }

    /** 设置每月同步数量上限。 */
    public void setMonthSyncLimit(Integer monthSyncLimit)
    {
        this.monthSyncLimit = monthSyncLimit;
    }

    /** 读取业务状态。 */
    public Integer getStatus()
    {
        return status;
    }

    /** 设置业务状态。 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /** 读取到期时间。 */
    public String getEndTime()
    {
        return endTime;
    }

    /** 设置到期时间。 */
    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }
}