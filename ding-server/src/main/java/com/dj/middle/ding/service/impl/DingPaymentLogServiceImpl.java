package com.dj.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingPaymentLog;
import com.dj.middle.ding.mapper.DingPaymentLogMapper;
import com.dj.middle.ding.service.IDingPaymentLogService;

@Service
public class DingPaymentLogServiceImpl implements IDingPaymentLogService
{
    @Autowired
    private DingPaymentLogMapper dingPaymentLogMapper;

    @Override
    public DingPaymentLog selectDingPaymentLogById(Long id)
    {
        return dingPaymentLogMapper.selectDingPaymentLogById(id);
    }

    @Override
    public List<DingPaymentLog> selectDingPaymentLogList(DingPaymentLog dingPaymentLog)
    {
        return dingPaymentLogMapper.selectDingPaymentLogList(dingPaymentLog);
    }

    @Override
    public List<DingPaymentLog> selectDingPaymentLogByOrderNo(String orderNo)
    {
        return dingPaymentLogMapper.selectDingPaymentLogByOrderNo(orderNo);
    }

    @Override
    public int insertDingPaymentLog(DingPaymentLog dingPaymentLog)
    {
        dingPaymentLog.setCreateTime(DateUtils.getNowDate());
        return dingPaymentLogMapper.insertDingPaymentLog(dingPaymentLog);
    }

    @Override
    public int updateDingPaymentLog(DingPaymentLog dingPaymentLog)
    {
        dingPaymentLog.setUpdateTime(DateUtils.getNowDate());
        return dingPaymentLogMapper.updateDingPaymentLog(dingPaymentLog);
    }

    @Override
    public int deleteDingPaymentLogById(Long id)
    {
        return dingPaymentLogMapper.deleteDingPaymentLogById(id);
    }

    @Override
    public int deleteDingPaymentLogByIds(Long[] ids)
    {
        return dingPaymentLogMapper.deleteDingPaymentLogByIds(ids);
    }
}
