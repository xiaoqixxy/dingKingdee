package com.ruoyi.middle.ding.service;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingOrder;

public interface IDingOrderService
{
    public DingOrder selectDingOrderById(Long id);
    public DingOrder selectDingOrderByOrderNo(String orderNo);
    public List<DingOrder> selectDingOrderList(DingOrder dingOrder);
    public int insertDingOrder(DingOrder dingOrder);
    public int updateDingOrder(DingOrder dingOrder);
    public int deleteDingOrderById(Long id);
    public int deleteDingOrderByIds(Long[] ids);
    public boolean checkOrderNoUnique(String orderNo);

    public void cancelExpiredOrders();

    public void createSubscription(DingOrder order);
}
