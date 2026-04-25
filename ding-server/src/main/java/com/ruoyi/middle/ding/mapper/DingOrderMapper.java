package com.ruoyi.middle.ding.mapper;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingOrder;

public interface DingOrderMapper
{
    public DingOrder selectDingOrderById(Long id);

    public DingOrder selectDingOrderByOrderNo(String orderNo);

    public List<DingOrder> selectDingOrderList(DingOrder dingOrder);

    public int insertDingOrder(DingOrder dingOrder);

    public int updateDingOrder(DingOrder dingOrder);

    public int deleteDingOrderById(Long id);

    public int deleteDingOrderByIds(Long[] ids);

    public DingOrder checkOrderNoUnique(String orderNo);

    public List<DingOrder> selectExpiredUnpaidOrders();
}
