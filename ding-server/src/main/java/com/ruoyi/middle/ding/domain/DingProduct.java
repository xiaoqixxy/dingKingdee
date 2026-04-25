package com.ruoyi.middle.ding.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class DingProduct extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "产品类型", readConverterExp = "1=包月,2=包年,3=按量付费")
    private Integer productType;

    @Excel(name = "产品分类")
    private String productCategory;

    @Excel(name = "是否默认产品", readConverterExp = "0=否,1=是")
    private Integer isDefault;

    @Excel(name = "产品价格")
    private BigDecimal price;

    @Excel(name = "可绑定同步表单数量")
    private Integer syncFormLimit;

    @Excel(name = "单次同步数据上限")
    private Integer singleSyncLimit;

    @Excel(name = "每月总同步数据上限")
    private Integer monthSyncLimit;

    @Excel(name = "产品状态", readConverterExp = "1=上架,2=下架")
    private Integer status;

    @Excel(name = "排序权重")
    private Integer sort;

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

    public Integer getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault)
    {
        this.isDefault = isDefault;
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

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
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
            .append("productName", getProductName())
            .append("productType", getProductType())
            .append("productCategory", getProductCategory())
            .append("isDefault", getIsDefault())
            .append("price", getPrice())
            .append("syncFormLimit", getSyncFormLimit())
            .append("singleSyncLimit", getSingleSyncLimit())
            .append("monthSyncLimit", getMonthSyncLimit())
            .append("status", getStatus())
            .append("sort", getSort())
            .append("remark", getRemark())
            .append("ext1", getExt1())
            .append("ext2", getExt2())
            .append("ext3", getExt3())
            .toString();
    }
}
