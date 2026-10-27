package com.dj.middle.ding.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.dj.common.annotation.Excel;
import com.dj.common.core.domain.BaseEntity;

/** 产品套餐数据对象。 */
public class DingProduct extends BaseEntity
{
    /** Java对象序列化版本。 */
    private static final long serialVersionUID = 1L;

    /** 记录主键。 */
    private Long id;

    /** 产品名称。 */
    @Excel(name = "产品名称")
    private String productName;

    /** 产品类型。 */
    @Excel(name = "产品类型", readConverterExp = "1=包月,2=包年,3=按量付费")
    private Integer productType;

    /** 产品分类。 */
    @Excel(name = "产品分类")
    private String productCategory;

    /** 是否默认产品。 */
    @Excel(name = "是否默认产品", readConverterExp = "0=否,1=是")
    private Integer isDefault;

    /** 产品价格。 */
    @Excel(name = "产品价格")
    private BigDecimal price;

    /** 可绑定同步表单数量。 */
    @Excel(name = "可绑定同步表单数量")
    private Integer syncFormLimit;

    /** 单次同步数据上限。 */
    @Excel(name = "单次同步数据上限")
    private Integer singleSyncLimit;

    /** 每月总同步数据上限。 */
    @Excel(name = "每月总同步数据上限")
    private Integer monthSyncLimit;

    /** 产品状态。 */
    @Excel(name = "产品状态", readConverterExp = "1=上架,2=下架")
    private Integer status;

    /** 排序权重。 */
    @Excel(name = "排序权重")
    private Integer sort;

    /** 排序权重。 */
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

    /** 读取产品名称。 */
    public String getProductName()
    {
        return productName;
    }

    /** 设置产品名称。 */
    public void setProductName(String productName)
    {
        this.productName = productName;
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

    /** 读取是否默认产品。 */
    public Integer getIsDefault()
    {
        return isDefault;
    }

    /** 设置是否默认产品。 */
    public void setIsDefault(Integer isDefault)
    {
        this.isDefault = isDefault;
    }

    /** 读取产品价格。 */
    public BigDecimal getPrice()
    {
        return price;
    }

    /** 设置产品价格。 */
    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    /** 读取可绑定同步表单数量。 */
    public Integer getSyncFormLimit()
    {
        return syncFormLimit;
    }

    /** 设置可绑定同步表单数量。 */
    public void setSyncFormLimit(Integer syncFormLimit)
    {
        this.syncFormLimit = syncFormLimit;
    }

    /** 读取单次同步数据上限。 */
    public Integer getSingleSyncLimit()
    {
        return singleSyncLimit;
    }

    /** 设置单次同步数据上限。 */
    public void setSingleSyncLimit(Integer singleSyncLimit)
    {
        this.singleSyncLimit = singleSyncLimit;
    }

    /** 读取每月总同步数据上限。 */
    public Integer getMonthSyncLimit()
    {
        return monthSyncLimit;
    }

    /** 设置每月总同步数据上限。 */
    public void setMonthSyncLimit(Integer monthSyncLimit)
    {
        this.monthSyncLimit = monthSyncLimit;
    }

    /** 读取产品状态。 */
    public Integer getStatus()
    {
        return status;
    }

    /** 设置产品状态。 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /** 读取排序权重。 */
    public Integer getSort()
    {
        return sort;
    }

    /** 设置排序权重。 */
    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

    /** 读取排序权重。 */
    public String getExt1()
    {
        return ext1;
    }

    /** 设置排序权重。 */
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
