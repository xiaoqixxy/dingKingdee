package com.dj.middle.ding.mapper;

import java.util.List;
import com.dj.middle.ding.domain.DingOrder;

/** 订单数据访问契约。 */
public interface DingOrderMapper
{
    /** 查询指定主键的订单记录。 */
    public DingOrder selectDingOrderById(Long id);

    /** 查询指定订单号的订单记录。 */
    public DingOrder selectDingOrderByOrderNo(String orderNo);

    /** 查询符合筛选条件的订单列表。 */
    public List<DingOrder> selectDingOrderList(DingOrder dingOrder);

    /** 新增订单记录。 */
    public int insertDingOrder(DingOrder dingOrder);

    /** 更新订单记录。 */
    public int updateDingOrder(DingOrder dingOrder);

    /** 删除指定主键的订单记录。 */
    public int deleteDingOrderById(Long id);

    /** 删除指定主键集合的订单记录。 */
    public int deleteDingOrderByIds(Long[] ids);

    /** 校验订单号是否唯一。 */
    public DingOrder checkOrderNoUnique(String orderNo);

    /** 查询已过期且仍未支付的订单。 */
    public List<DingOrder> selectExpiredUnpaidOrders();
}
