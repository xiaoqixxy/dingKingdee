package com.ruoyi.middle.ding.mapper;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingPackageChangeLog;

public interface DingPackageChangeLogMapper
{
    public DingPackageChangeLog selectDingPackageChangeLogById(Long id);

    public List<DingPackageChangeLog> selectDingPackageChangeLogList(DingPackageChangeLog dingPackageChangeLog);

    public List<DingPackageChangeLog> selectDingPackageChangeLogByCorpId(String dingCorpId);

    public int insertDingPackageChangeLog(DingPackageChangeLog dingPackageChangeLog);

    public int deleteDingPackageChangeLogById(Long id);

    public int deleteDingPackageChangeLogByIds(Long[] ids);
}
