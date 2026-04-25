package com.ruoyi.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.middle.ding.domain.DingSyncLog;
import com.ruoyi.middle.ding.mapper.DingSyncLogMapper;
import com.ruoyi.middle.ding.service.IDingSyncLogService;

@Service
public class DingSyncLogServiceImpl implements IDingSyncLogService
{
    @Autowired
    private DingSyncLogMapper dingSyncLogMapper;

    @Override
    public DingSyncLog selectDingSyncLogById(Long id)
    {
        return dingSyncLogMapper.selectDingSyncLogById(id);
    }

    @Override
    public List<DingSyncLog> selectDingSyncLogList(DingSyncLog dingSyncLog)
    {
        return dingSyncLogMapper.selectDingSyncLogList(dingSyncLog);
    }

    @Override
    public List<DingSyncLog> selectDingSyncLogByCorpId(String dingCorpId)
    {
        return dingSyncLogMapper.selectDingSyncLogByCorpId(dingCorpId);
    }

    @Override
    public int insertDingSyncLog(DingSyncLog dingSyncLog)
    {
        dingSyncLog.setCreateTime(DateUtils.getNowDate());
        return dingSyncLogMapper.insertDingSyncLog(dingSyncLog);
    }

    @Override
    public int deleteDingSyncLogById(Long id)
    {
        return dingSyncLogMapper.deleteDingSyncLogById(id);
    }

    @Override
    public int deleteDingSyncLogByIds(Long[] ids)
    {
        return dingSyncLogMapper.deleteDingSyncLogByIds(ids);
    }

    @Override
    public int sumSyncCountByCorpIdAndMonth(String dingCorpId, String month)
    {
        return dingSyncLogMapper.sumSyncCountByCorpIdAndMonth(dingCorpId, month);
    }
}
