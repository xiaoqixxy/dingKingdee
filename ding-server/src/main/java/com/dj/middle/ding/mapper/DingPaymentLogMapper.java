package com.dj.middle.ding.mapper;

import java.util.List;
import com.dj.middle.ding.domain.DingPaymentLog;

/** 支付日志数据访问契约。 */
public interface DingPaymentLogMapper
{
    /** 查询指定主键的支付日志记录。 */
    public DingPaymentLog selectDingPaymentLogById(Long id);

    /** 查询符合筛选条件的支付日志列表。 */
    public List<DingPaymentLog> selectDingPaymentLogList(DingPaymentLog dingPaymentLog);

    /** 查询指定订单号的支付日志记录。 */
    public List<DingPaymentLog> selectDingPaymentLogByOrderNo(String orderNo);

    /** 新增支付日志记录。 */
    public int insertDingPaymentLog(DingPaymentLog dingPaymentLog);

    /** 更新支付日志记录。 */
    public int updateDingPaymentLog(DingPaymentLog dingPaymentLog);

    /** 删除指定主键的支付日志记录。 */
    public int deleteDingPaymentLogById(Long id);

    /** 删除指定主键集合的支付日志记录。 */
    public int deleteDingPaymentLogByIds(Long[] ids);
}
