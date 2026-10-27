package com.dj.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingPaymentLog;
import com.dj.middle.ding.mapper.DingPaymentLogMapper;
import com.dj.middle.ding.service.IDingPaymentLogService;

/** 支付日志业务实现。 */
@Service
public class DingPaymentLogServiceImpl implements IDingPaymentLogService
{
    /** 提供支付日志数据访问能力的映射接口。 */
    @Autowired
   private DingPaymentLogMapper dingPaymentLogMapper;

    /** 查询指定主键的支付日志记录。 */
    @Override
    public DingPaymentLog selectDingPaymentLogById(Long id)
    {
        return dingPaymentLogMapper.selectDingPaymentLogById(id);
    }

    /** 查询符合筛选条件的支付日志列表。 */
    @Override
    public List<DingPaymentLog> selectDingPaymentLogList(DingPaymentLog dingPaymentLog)
    {
        return dingPaymentLogMapper.selectDingPaymentLogList(dingPaymentLog);
    }

    /** 查询指定订单号的支付日志记录。 */
    @Override
    public List<DingPaymentLog> selectDingPaymentLogByOrderNo(String orderNo)
    {
        return dingPaymentLogMapper.selectDingPaymentLogByOrderNo(orderNo);
    }

    /** 新增支付日志记录。 */
    @Override
    public int insertDingPaymentLog(DingPaymentLog dingPaymentLog)
    {
        dingPaymentLog.setCreateTime(DateUtils.getNowDate());
        return dingPaymentLogMapper.insertDingPaymentLog(dingPaymentLog);
    }

    /** 更新支付日志记录。 */
    @Override
    public int updateDingPaymentLog(DingPaymentLog dingPaymentLog)
    {
        dingPaymentLog.setUpdateTime(DateUtils.getNowDate());
        return dingPaymentLogMapper.updateDingPaymentLog(dingPaymentLog);
    }

    /** 删除指定主键的支付日志记录。 */
    @Override
    public int deleteDingPaymentLogById(Long id)
    {
        return dingPaymentLogMapper.deleteDingPaymentLogById(id);
    }

    /** 删除指定主键集合的支付日志记录。 */
    @Override
    public int deleteDingPaymentLogByIds(Long[] ids)
    {
        return dingPaymentLogMapper.deleteDingPaymentLogByIds(ids);
    }
}
