package com.dj.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingPackageChangeLog;
import com.dj.middle.ding.mapper.DingPackageChangeLogMapper;
import com.dj.middle.ding.service.IDingPackageChangeLogService;

@Service
public class DingPackageChangeLogServiceImpl implements IDingPackageChangeLogService
{
    @Autowired
    private DingPackageChangeLogMapper dingPackageChangeLogMapper;

    @Override
    public DingPackageChangeLog selectDingPackageChangeLogById(Long id)
    {
        return dingPackageChangeLogMapper.selectDingPackageChangeLogById(id);
    }

    @Override
    public List<DingPackageChangeLog> selectDingPackageChangeLogList(DingPackageChangeLog dingPackageChangeLog)
    {
        return dingPackageChangeLogMapper.selectDingPackageChangeLogList(dingPackageChangeLog);
    }

    @Override
    public List<DingPackageChangeLog> selectDingPackageChangeLogByCorpId(String dingCorpId)
    {
        return dingPackageChangeLogMapper.selectDingPackageChangeLogByCorpId(dingCorpId);
    }

    @Override
    public int insertDingPackageChangeLog(DingPackageChangeLog dingPackageChangeLog)
    {
        dingPackageChangeLog.setCreateTime(DateUtils.getNowDate());
        return dingPackageChangeLogMapper.insertDingPackageChangeLog(dingPackageChangeLog);
    }

    @Override
    public int deleteDingPackageChangeLogById(Long id)
    {
        return dingPackageChangeLogMapper.deleteDingPackageChangeLogById(id);
    }

    @Override
    public int deleteDingPackageChangeLogByIds(Long[] ids)
    {
        return dingPackageChangeLogMapper.deleteDingPackageChangeLogByIds(ids);
    }
}
