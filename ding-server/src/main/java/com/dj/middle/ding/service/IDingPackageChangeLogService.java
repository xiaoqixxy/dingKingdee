package com.dj.middle.ding.service;

import java.util.List;
import com.dj.middle.ding.domain.DingPackageChangeLog;

/** 套餐变更日志服务契约。 */
public interface IDingPackageChangeLogService
{
    /** 查询指定主键的套餐变更日志记录。 */
    public DingPackageChangeLog selectDingPackageChangeLogById(Long id);
    /** 查询符合筛选条件的套餐变更日志列表。 */
    public List<DingPackageChangeLog> selectDingPackageChangeLogList(DingPackageChangeLog dingPackageChangeLog);
    /** 查询指定企业的套餐变更日志记录。 */
    public List<DingPackageChangeLog> selectDingPackageChangeLogByCorpId(String dingCorpId);
    /** 新增套餐变更日志记录。 */
    public int insertDingPackageChangeLog(DingPackageChangeLog dingPackageChangeLog);
    /** 删除指定主键的套餐变更日志记录。 */
    public int deleteDingPackageChangeLogById(Long id);
    /** 删除指定主键集合的套餐变更日志记录。 */
    public int deleteDingPackageChangeLogByIds(Long[] ids);
}
