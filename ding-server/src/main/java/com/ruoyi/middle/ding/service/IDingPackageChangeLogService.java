package com.ruoyi.middle.ding.service;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingPackageChangeLog;

public interface IDingPackageChangeLogService
{
    public DingPackageChangeLog selectDingPackageChangeLogById(Long id);
    public List<DingPackageChangeLog> selectDingPackageChangeLogList(DingPackageChangeLog dingPackageChangeLog);
    public List<DingPackageChangeLog> selectDingPackageChangeLogByCorpId(String dingCorpId);
    public int insertDingPackageChangeLog(DingPackageChangeLog dingPackageChangeLog);
    public int deleteDingPackageChangeLogById(Long id);
    public int deleteDingPackageChangeLogByIds(Long[] ids);
}
