package com.dj.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingSyncLog;
import com.dj.middle.ding.mapper.DingSyncLogMapper;
import com.dj.middle.ding.service.IDingSyncLogService;

/** 同步日志业务实现。 */
@Service
public class DingSyncLogServiceImpl implements IDingSyncLogService
{
    /** 提供同步日志数据访问能力的映射接口。 */
    @Autowired
   private DingSyncLogMapper dingSyncLogMapper;

    /** 查询指定主键的同步日志记录。 */
    @Override
    public DingSyncLog selectDingSyncLogById(Long id)
    {
        return dingSyncLogMapper.selectDingSyncLogById(id);
    }

    /** 查询符合筛选条件的同步日志列表。 */
    @Override
    public List<DingSyncLog> selectDingSyncLogList(DingSyncLog dingSyncLog)
    {
        return dingSyncLogMapper.selectDingSyncLogList(dingSyncLog);
    }

    /** 查询指定企业的同步日志记录。 */
    @Override
    public List<DingSyncLog> selectDingSyncLogByCorpId(String dingCorpId)
    {
        return dingSyncLogMapper.selectDingSyncLogByCorpId(dingCorpId);
    }

    /** 新增同步日志记录。 */
    @Override
    public int insertDingSyncLog(DingSyncLog dingSyncLog)
    {
        dingSyncLog.setCreateTime(DateUtils.getNowDate());
        return dingSyncLogMapper.insertDingSyncLog(dingSyncLog);
    }

    /** 删除指定主键的同步日志记录。 */
    @Override
    public int deleteDingSyncLogById(Long id)
    {
        return dingSyncLogMapper.deleteDingSyncLogById(id);
    }

    /** 删除指定主键集合的同步日志记录。 */
    @Override
    public int deleteDingSyncLogByIds(Long[] ids)
    {
        return dingSyncLogMapper.deleteDingSyncLogByIds(ids);
    }

    /** 汇总指定企业在目标月份的同步记录数。 */
    @Override
    public int sumSyncCountByCorpIdAndMonth(String dingCorpId, String month)
    {
        return dingSyncLogMapper.sumSyncCountByCorpIdAndMonth(dingCorpId, month);
    }
}
