package com.ruoyi.middle.ding.domain;

import java.math.BigDecimal;

public class DingProductVo
{
    private Long id;
    private String productName;
    private Integer productType;
    private String productCategory;
    private BigDecimal price;
    private Integer syncFormLimit;
    private Integer singleSyncLimit;
    private Integer monthSyncLimit;
    private Integer status;
    private String endTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public Integer getProductType()
    {
        return productType;
    }

    public void setProductType(Integer productType)
    {
        this.productType = productType;
    }

    public String getProductCategory()
    {
        return productCategory;
    }

    public void setProductCategory(String productCategory)
    {
        this.productCategory = productCategory;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public Integer getSyncFormLimit()
    {
        return syncFormLimit;
    }

    public void setSyncFormLimit(Integer syncFormLimit)
    {
        this.syncFormLimit = syncFormLimit;
    }

    public Integer getSingleSyncLimit()
    {
        return singleSyncLimit;
    }

    public void setSingleSyncLimit(Integer singleSyncLimit)
    {
        this.singleSyncLimit = singleSyncLimit;
    }

    public Integer getMonthSyncLimit()
    {
        return monthSyncLimit;
    }

    public void setMonthSyncLimit(Integer monthSyncLimit)
    {
        this.monthSyncLimit = monthSyncLimit;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }
}