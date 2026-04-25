package com.ruoyi.middle.ding.mapper;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingSyncLog;

public interface DingSyncLogMapper
{
    public DingSyncLog selectDingSyncLogById(Long id);

    public List<DingSyncLog> selectDingSyncLogList(DingSyncLog dingSyncLog);

    public List<DingSyncLog> selectDingSyncLogByCorpId(String dingCorpId);

    public int insertDingSyncLog(DingSyncLog dingSyncLog);

    public int deleteDingSyncLogById(Long id);

    public int deleteDingSyncLogByIds(Long[] ids);

    public int sumSyncCountByCorpIdAndMonth(String dingCorpId, String month);
}
