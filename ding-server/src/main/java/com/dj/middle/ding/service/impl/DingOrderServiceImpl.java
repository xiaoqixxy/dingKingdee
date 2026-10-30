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
        
        if (result > 0 && oldOrder != null && Integer.valueOf(1).equals(dingOrder.getPayStatus())
            && !Integer.valueOf(1).equals(oldOrder.getPayStatus())) {
            // 根据订单套餐创建或更新企业订阅，计算生效及到期时间。
            createSubscription(dingOrderMapper.selectDingOrderById(dingOrder.getId()));
        }
        
        return result;
    }

    /** 根据订单套餐创建或更新企业订阅，计算生效及到期时间。 */
    @Override
    @Transactional
    public void createSubscription(DingOrder order) {
        // 按企业查找当前订阅，产品不存在时明确终止，不能生成缺少权益定义的订阅。
        DingSubscription existing = dingSubscriptionMapper.selectDingSubscriptionByCorpIdForUpdate(order.getDingCorpId());
        DingProduct product = dingProductMapper.selectDingProductById(order.getProductId());
        if (product == null) {
            throw new com.dj.common.exception.ServiceException("订单关联产品不存在，无法创建订阅");
        }
        
        Date paidAt = order.getPayTime() != null ? order.getPayTime() : new Date();
        boolean continuing = existing != null && java.util.Objects.equals(existing.getProductId(), product.getId())
                && Integer.valueOf(1).equals(existing.getStatus()) && existing.getEndTime() != null
                && existing.getEndTime().after(paidAt);
        // 同套餐提前续费从原到期日延长，已到期或切换套餐从付款日生效。
        Date base = continuing ? existing.getEndTime() : paidAt;
        java.time.ZonedDateTime expiry = base.toInstant().atZone(java.time.ZoneId.of("Asia/Shanghai"));
        expiry = Integer.valueOf(2).equals(product.getProductType()) ? expiry.plusYears(1) : expiry.plusMonths(1);
        DingSubscription subscription = new DingSubscription();
        subscription.setProductId(product.getId());
        subscription.setProductType(product.getProductType());
        subscription.setStartTime(continuing ? existing.getStartTime() : paidAt);
        subscription.setEndTime(Date.from(expiry.toInstant()));
        subscription.setStatus(1);
        if (existing != null) {
            // 只更新权益字段，不拿读取时的旧用量覆盖同步过程中新增的用量。
            subscription.setId(existing.getId());
            dingSubscriptionMapper.updateDingSubscription(subscription);
        } else {
            subscription.setDingCorpId(order.getDingCorpId());
            subscription.setAutoRenew(0);
            subscription.setUsedFormCount(0);
            subscription.setMonthUsedSync(0);
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
