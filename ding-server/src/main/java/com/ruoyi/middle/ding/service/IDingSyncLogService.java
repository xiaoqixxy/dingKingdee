package com.ruoyi.middle.ding.service;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingSyncLog;

public interface IDingSyncLogService
{
    public DingSyncLog selectDingSyncLogById(Long id);
    public List<DingSyncLog> selectDingSyncLogList(DingSyncLog dingSyncLog);
    public List<DingSyncLog> selectDingSyncLogByCorpId(String dingCorpId);
    public int insertDingSyncLog(DingSyncLog dingSyncLog);
    public int deleteDingSyncLogById(Long id);
    public int deleteDingSyncLogByIds(Long[] ids);
    public int sumSyncCountByCorpIdAndMonth(String dingCorpId, String month);
}
