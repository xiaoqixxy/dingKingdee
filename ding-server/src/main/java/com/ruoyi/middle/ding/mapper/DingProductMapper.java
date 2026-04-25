package com.ruoyi.middle.ding.mapper;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingProduct;

public interface DingProductMapper
{
    public DingProduct selectDingProductById(Long id);

    public List<DingProduct> selectDingProductList(DingProduct dingProduct);

    public int insertDingProduct(DingProduct dingProduct);

    public int updateDingProduct(DingProduct dingProduct);

    public int deleteDingProductById(Long id);

    public int deleteDingProductByIds(Long[] ids);

    public DingProduct checkProductNameUnique(String productName);

    public DingProduct selectDefaultProduct();
}
