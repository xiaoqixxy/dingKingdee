package com.ruoyi.middle.ding.service;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingPaymentLog;

public interface IDingPaymentLogService
{
    public DingPaymentLog selectDingPaymentLogById(Long id);
    public List<DingPaymentLog> selectDingPaymentLogList(DingPaymentLog dingPaymentLog);
    public List<DingPaymentLog> selectDingPaymentLogByOrderNo(String orderNo);
    public int insertDingPaymentLog(DingPaymentLog dingPaymentLog);
    public int updateDingPaymentLog(DingPaymentLog dingPaymentLog);
    public int deleteDingPaymentLogById(Long id);
    public int deleteDingPaymentLogByIds(Long[] ids);
}
