package com.dj.middle.ding.service.impl;

import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingOrder;
import com.dj.middle.ding.domain.DingSubscription;
import com.dj.middle.ding.domain.DingProduct;
import com.dj.middle.ding.mapper.DingOrderMapper;
import com.dj.middle.ding.mapper.DingSubscriptionMapper;
import com.dj.middle.ding.mapper.DingProductMapper;
import com.dj.middle.ding.service.IDingOrderService;

/** 订单业务实现。 */
@Service
public class DingOrderServiceImpl implements IDingOrderService
{
    /** 提供订单数据访问能力的映射接口。 */
    @Autowired
    private DingOrderMapper dingOrderMapper;

    /** 提供企业订阅数据访问能力的映射接口。 */
    @Autowired
    private DingSubscriptionMapper dingSubscriptionMapper;

    /** 提供产品套餐数据访问能力的映射接口。 */
    @Autowired
    private DingProductMapper dingProductMapper;

    /** 查询指定主键的订单记录。 */
    @Override
    public DingOrder selectDingOrderById(Long id)
    {
        return dingOrderMapper.selectDingOrderById(id);
    }

    /** 查询指定订单号的订单记录。 */
    @Override
    public DingOrder selectDingOrderByOrderNo(String orderNo)
    {
        return dingOrderMapper.selectDingOrderByOrderNo(orderNo);
    }

    /** 查询符合筛选条件的订单列表。 */
    @Override
    public List<DingOrder> selectDingOrderList(DingOrder dingOrder)
    {
        return dingOrderMapper.selectDingOrderList(dingOrder);
    }

    /** 新增订单记录。 */
    @Override
    @Transactional
    public int insertDingOrder(DingOrder dingOrder)
    {
        dingOrder.setCreateTime(DateUtils.getNowDate());
        int result = dingOrderMapper.insertDingOrder(dingOrder);
        
        // 后台直接录入已支付订单时也需要同步生成企业订阅。
        if (dingOrder.getPayStatus() != null && dingOrder.getPayStatus() == 1) {
            // 根据订单套餐创建或更新企业订阅，计算生效及到期时间。
            createSubscription(dingOrder);
        }
        
        return result;
    }

    /** 更新订单记录。 */
    @Override
    @Transactional
    public int updateDingOrder(DingOrder dingOrder)
    {
        // 比较更新前后的支付状态，只有从非已支付转为已支付才触发生效。
        DingOrder oldOrder = dingOrderMapper.selectDingOrderById(dingOrder.getId());
        dingOrder.setUpdateTime(DateUtils.getNowDate());
        int result = dingOrderMapper.updateDingOrder(dingOrder);
        
        if (oldOrder != null && dingOrder.getPayStatus() != null 
            && dingOrder.getPayStatus() == 1 && oldOrder.getPayStatus() != 1) {
            // 根据订单套餐创建或更新企业订阅，计算生效及到期时间。
            createSubscription(dingOrder);
        }
        
        return result;
    }

    /** 根据订单套餐创建或更新企业订阅，计算生效及到期时间。 */
    @Override
    public void createSubscription(DingOrder order) {
        // 按企业查找当前订阅，产品不存在时明确终止，不能生成缺少权益定义的订阅。
        DingSubscription existing = dingSubscriptionMapper.selectDingSubscriptionByCorpId(order.getDingCorpId());
        DingProduct product = dingProductMapper.selectDingProductById(order.getProductId());
        if (product == null) {
            throw new com.dj.common.exception.ServiceException("订单关联产品不存在，无法创建订阅");
        }
        
        // 已有订阅就更新套餐与有效期；新订阅另外初始化用量和自动续费状态。
        if (existing != null) {
            existing.setProductId(order.getProductId());
            existing.setProductType(product.getProductType());
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
            subscription.setProductType(product.getProductType());
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

    /** 删除指定主键的订单记录。 */
    @Override
    public int deleteDingOrderById(Long id)
    {
        return dingOrderMapper.deleteDingOrderById(id);
    }

    /** 删除指定主键集合的订单记录。 */
    @Override
    public int deleteDingOrderByIds(Long[] ids)
    {
        return dingOrderMapper.deleteDingOrderByIds(ids);
    }

    /** 校验订单号是否唯一。 */
    @Override
    public boolean checkOrderNoUnique(String orderNo)
    {
        DingOrder order = dingOrderMapper.checkOrderNoUnique(orderNo);
        return order == null;
    }

    /** 扫描过期未支付订单并更新为取消状态。 */
    @Override
    @Transactional
    public void cancelExpiredOrders()
    {
        // 只关闭已经过期且未支付的订单，不更改已完成支付的历史订单。
        List<DingOrder> expiredOrders = dingOrderMapper.selectExpiredUnpaidOrders();
        for (DingOrder order : expiredOrders) {
            order.setPayStatus(2);
            dingOrderMapper.updateDingOrder(order);
        }
    }
}
