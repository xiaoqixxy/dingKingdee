package com.dj.middle.ding.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingProduct;
import com.dj.middle.ding.domain.DingProductVo;
import com.dj.middle.ding.domain.DingSubscription;
import com.dj.middle.ding.mapper.DingProductMapper;
import com.dj.middle.ding.service.IDingProductService;
import com.dj.middle.ding.service.IDingSubscriptionService;

/** 产品套餐业务实现。 */
@Service
public class DingProductServiceImpl implements IDingProductService
{
    /** 提供产品套餐数据访问能力的映射接口。 */
    @Autowired
    private DingProductMapper dingProductMapper;

    /** 处理企业订阅业务的服务组件。 */
    @Autowired
    private IDingSubscriptionService dingSubscriptionService;

    /** 查询指定主键的产品套餐记录。 */
    @Override
    public DingProduct selectDingProductById(Long id)
    {
        return dingProductMapper.selectDingProductById(id);
    }

    /** 查询符合筛选条件的产品套餐列表。 */
    @Override
    public List<DingProduct> selectDingProductList(DingProduct dingProduct)
    {
        return dingProductMapper.selectDingProductList(dingProduct);
    }

    /** 查询所有可选的产品套餐列表。 */
    @Override
    public List<DingProduct> selectDingProductAll()
    {
        DingProduct product = new DingProduct();
        product.setStatus(1);
        return dingProductMapper.selectDingProductList(product);
    }

    /** 新增产品套餐记录。 */
    @Override
    public int insertDingProduct(DingProduct dingProduct)
    {
        dingProduct.setCreateTime(DateUtils.getNowDate());
        return dingProductMapper.insertDingProduct(dingProduct);
    }

    /** 更新产品套餐记录。 */
    @Override
    public int updateDingProduct(DingProduct dingProduct)
    {
        dingProduct.setUpdateTime(DateUtils.getNowDate());
        return dingProductMapper.updateDingProduct(dingProduct);
    }

    /** 删除指定主键的产品套餐记录。 */
    @Override
    public int deleteDingProductById(Long id)
    {
        return dingProductMapper.deleteDingProductById(id);
    }

    /** 删除指定主键集合的产品套餐记录。 */
    @Override
    public int deleteDingProductByIds(Long[] ids)
    {
        return dingProductMapper.deleteDingProductByIds(ids);
    }

    /** 校验产品套餐名称是否唯一。 */
    @Override
    public boolean checkProductNameUnique(String productName)
    {
        DingProduct product = new DingProduct();
        product.setProductName(productName);
        List<DingProduct> list = dingProductMapper.selectDingProductList(product);
        return list == null || list.size() == 0;
    }

    /** 读取当前默认产品套餐。 */
    @Override
    public DingProduct selectDefaultProduct()
    {
        return dingProductMapper.selectDefaultProduct();
    }

    /** 根据企业有效订阅读取可用产品，无订阅时沿用默认产品逻辑。 */
    @Override
    public DingProduct getProductByCorpId(String dingCorpId)
    {
        return dingSubscriptionService.getOrRegisterDefaultProduct(dingCorpId);
    }

    /** 把企业当前产品和订阅信息转换为页面所需的权益视图。 */
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
