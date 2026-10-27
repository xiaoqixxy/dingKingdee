package com.dj.middle.ding.service;

import java.util.List;
import com.dj.middle.ding.domain.DingSyncLog;

/** 同步日志服务契约。 */
public interface IDingSyncLogService
{
    /** 查询指定主键的同步日志记录。 */
    public DingSyncLog selectDingSyncLogById(Long id);
    /** 查询符合筛选条件的同步日志列表。 */
    public List<DingSyncLog> selectDingSyncLogList(DingSyncLog dingSyncLog);
    /** 查询指定企业的同步日志记录。 */
    public List<DingSyncLog> selectDingSyncLogByCorpId(String dingCorpId);
    /** 新增同步日志记录。 */
    public int insertDingSyncLog(DingSyncLog dingSyncLog);
    /** 删除指定主键的同步日志记录。 */
    public int deleteDingSyncLogById(Long id);
    /** 删除指定主键集合的同步日志记录。 */
    public int deleteDingSyncLogByIds(Long[] ids);
    /** 汇总指定企业在目标月份的同步记录数。 */
    public int sumSyncCountByCorpIdAndMonth(String dingCorpId, String month);
}
