package com.ruoyi.middle.ding.service;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingProduct;
import com.ruoyi.middle.ding.domain.DingProductVo;

public interface IDingProductService
{
    public DingProduct selectDingProductById(Long id);

    public List<DingProduct> selectDingProductList(DingProduct dingProduct);

    public List<DingProduct> selectDingProductAll();

    public int insertDingProduct(DingProduct dingProduct);

    public int updateDingProduct(DingProduct dingProduct);

    public int deleteDingProductById(Long id);

    public int deleteDingProductByIds(Long[] ids);

    public boolean checkProductNameUnique(String productName);

    public DingProduct selectDefaultProduct();

    public DingProduct getProductByCorpId(String dingCorpId);

    public DingProductVo getProductVoByCorpId(String dingCorpId);
}
