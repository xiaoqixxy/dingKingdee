package com.ruoyi.middle.ding.service.impl;

import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.middle.ding.domain.DingOrder;
import com.ruoyi.middle.ding.domain.DingSubscription;
import com.ruoyi.middle.ding.domain.DingProduct;
import com.ruoyi.middle.ding.mapper.DingOrderMapper;
import com.ruoyi.middle.ding.mapper.DingSubscriptionMapper;
import com.ruoyi.middle.ding.mapper.DingProductMapper;
import com.ruoyi.middle.ding.service.IDingOrderService;

@Service
public class DingOrderServiceImpl implements IDingOrderService
{
    @Autowired
    private DingOrderMapper dingOrderMapper;

    @Autowired
    private DingSubscriptionMapper dingSubscriptionMapper;

    @Autowired
    private DingProductMapper dingProductMapper;

    @Override
    public DingOrder selectDingOrderById(Long id)
    {
        return dingOrderMapper.selectDingOrderById(id);
    }

    @Override
    public DingOrder selectDingOrderByOrderNo(String orderNo)
    {
        return dingOrderMapper.selectDingOrderByOrderNo(orderNo);
    }

    @Override
    public List<DingOrder> selectDingOrderList(DingOrder dingOrder)
    {
        return dingOrderMapper.selectDingOrderList(dingOrder);
    }

    @Override
    @Transactional
    public int insertDingOrder(DingOrder dingOrder)
    {
        dingOrder.setCreateTime(DateUtils.getNowDate());
        int result = dingOrderMapper.insertDingOrder(dingOrder);
        
        if (dingOrder.getPayStatus() != null && dingOrder.getPayStatus() == 1) {
            createSubscription(dingOrder);
        }
        
        return result;
    }

    @Override
    @Transactional
    public int updateDingOrder(DingOrder dingOrder)
    {
        DingOrder oldOrder = dingOrderMapper.selectDingOrderById(dingOrder.getId());
        dingOrder.setUpdateTime(DateUtils.getNowDate());
        int result = dingOrderMapper.updateDingOrder(dingOrder);
        
        if (oldOrder != null && dingOrder.getPayStatus() != null 
            && dingOrder.getPayStatus() == 1 && oldOrder.getPayStatus() != 1) {
            createSubscription(dingOrder);
        }
        
        return result;
    }

    @Override
    public void createSubscription(DingOrder order) {
        DingSubscription existing = dingSubscriptionMapper.selectDingSubscriptionByCorpId(order.getDingCorpId());
        DingProduct product = dingProductMapper.selectDingProductById(order.getProductId());
        
        if (existing != null) {
            existing.setProductId(order.getProductId());
            existing.setProductType(order.getOrderType() == 1 ? 1 : 2);
            existing.setStartTime(order.getPayTime() != null ? order.getPayTime() : new Date());
            if (product != null) {
                existing.setEndTime(new Date(existing.getStartTime().getTime() + (product.getProductType() == 2 ? 365L : 30L) * 24 * 60 * 60 * 1000));
            }
            existing.setStatus(1);
            dingSubscriptionMapper.updateDingSubscription(existing);
        } else {
            DingSubscription subscription = new DingSubscription();
            subscription.setDingCorpId(order.getDingCorpId());
            subscription.setProductId(order.getProductId());
            subscription.setProductType(order.getOrderType() == 1 ? 1 : 2);
            subscription.setStartTime(order.getPayTime() != null ? order.getPayTime() : new Date());
            if (product != null) {
                long days = product.getProductType() == 2 ? 365 : 30;
                subscription.setEndTime(new Date(subscription.getStartTime().getTime() + days * 24 * 60 * 60 * 1000));
            } else {
                subscription.setEndTime(new Date(subscription.getStartTime().getTime() + 30L * 24 * 60 * 60 * 1000));
            }
            subscription.setAutoRenew(0);
            subscription.setUsedFormCount(0);
            subscription.setMonthUsedSync(0);
            subscription.setStatus(1);
            subscription.setCreateTime(DateUtils.getNowDate());
            dingSubscriptionMapper.insertDingSubscription(subscription);
        }
    }

    @Override
    public int deleteDingOrderById(Long id)
    {
        return dingOrderMapper.deleteDingOrderById(id);
    }

    @Override
    public int deleteDingOrderByIds(Long[] ids)
    {
        return dingOrderMapper.deleteDingOrderByIds(ids);
    }

    @Override
    public boolean checkOrderNoUnique(String orderNo)
    {
        DingOrder order = dingOrderMapper.checkOrderNoUnique(orderNo);
        return order == null;
    }

    @Override
    @Transactional
    public void cancelExpiredOrders()
    {
        List<DingOrder> expiredOrders = dingOrderMapper.selectExpiredUnpaidOrders();
        for (DingOrder order : expiredOrders) {
            order.setPayStatus(2);
            dingOrderMapper.updateDingOrder(order);
        }
    }
}
