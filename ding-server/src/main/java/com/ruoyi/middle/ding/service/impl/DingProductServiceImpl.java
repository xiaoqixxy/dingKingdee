package com.ruoyi.middle.ding.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.middle.ding.domain.DingProduct;
import com.ruoyi.middle.ding.domain.DingProductVo;
import com.ruoyi.middle.ding.domain.DingSubscription;
import com.ruoyi.middle.ding.mapper.DingProductMapper;
import com.ruoyi.middle.ding.service.IDingProductService;
import com.ruoyi.middle.ding.service.IDingSubscriptionService;

@Service
public class DingProductServiceImpl implements IDingProductService
{
    @Autowired
    private DingProductMapper dingProductMapper;

    @Autowired
    private IDingSubscriptionService dingSubscriptionService;

    @Override
    public DingProduct selectDingProductById(Long id)
    {
        return dingProductMapper.selectDingProductById(id);
    }

    @Override
    public List<DingProduct> selectDingProductList(DingProduct dingProduct)
    {
        return dingProductMapper.selectDingProductList(dingProduct);
    }

    @Override
    public List<DingProduct> selectDingProductAll()
    {
        DingProduct product = new DingProduct();
        product.setStatus(1);
        return dingProductMapper.selectDingProductList(product);
    }

    @Override
    public int insertDingProduct(DingProduct dingProduct)
    {
        dingProduct.setCreateTime(DateUtils.getNowDate());
        return dingProductMapper.insertDingProduct(dingProduct);
    }

    @Override
    public int updateDingProduct(DingProduct dingProduct)
    {
        dingProduct.setUpdateTime(DateUtils.getNowDate());
        return dingProductMapper.updateDingProduct(dingProduct);
    }

    @Override
    public int deleteDingProductById(Long id)
    {
        return dingProductMapper.deleteDingProductById(id);
    }

    @Override
    public int deleteDingProductByIds(Long[] ids)
    {
        return dingProductMapper.deleteDingProductByIds(ids);
    }

    @Override
    public boolean checkProductNameUnique(String productName)
    {
        DingProduct product = new DingProduct();
        product.setProductName(productName);
        List<DingProduct> list = dingProductMapper.selectDingProductList(product);
        return list == null || list.size() == 0;
    }

    @Override
    public DingProduct selectDefaultProduct()
    {
        return dingProductMapper.selectDefaultProduct();
    }

    @Override
    public DingProduct getProductByCorpId(String dingCorpId)
    {
        return dingSubscriptionService.getOrRegisterDefaultProduct(dingCorpId);
    }

    @Override
    public DingProductVo getProductVoByCorpId(String dingCorpId)
    {
        DingProduct product = getProductByCorpId(dingCorpId);
        if (product == null)
        {
            return null;
        }
        DingSubscription subscription = dingSubscriptionService.selectDingSubscriptionByCorpId(dingCorpId);
        
        DingProductVo vo = new DingProductVo();
        vo.setId(product.getId());
        vo.setProductName(product.getProductName());
        vo.setProductType(product.getProductType());
        vo.setProductCategory(product.getProductCategory());
        vo.setPrice(product.getPrice());
        vo.setSyncFormLimit(product.getSyncFormLimit());
        vo.setSingleSyncLimit(product.getSingleSyncLimit());
        vo.setMonthSyncLimit(product.getMonthSyncLimit());
        vo.setStatus(product.getStatus());
        
        if (subscription != null && subscription.getEndTime() != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            vo.setEndTime(sdf.format(subscription.getEndTime()));
        }
        
        return vo;
    }
}
