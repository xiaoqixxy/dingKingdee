package com.dj.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingPackageChangeLog;
import com.dj.middle.ding.mapper.DingPackageChangeLogMapper;
import com.dj.middle.ding.service.IDingPackageChangeLogService;

/** 套餐变更日志业务实现。 */
@Service
public class DingPackageChangeLogServiceImpl implements IDingPackageChangeLogService
{
    /** 提供套餐变更日志数据访问能力的映射接口。 */
    @Autowired
   private DingPackageChangeLogMapper dingPackageChangeLogMapper;

    /** 查询指定主键的套餐变更日志记录。 */
    @Override
    public DingPackageChangeLog selectDingPackageChangeLogById(Long id)
    {
        return dingPackageChangeLogMapper.selectDingPackageChangeLogById(id);
    }

    /** 查询符合筛选条件的套餐变更日志列表。 */
    @Override
    public List<DingPackageChangeLog> selectDingPackageChangeLogList(DingPackageChangeLog dingPackageChangeLog)
    {
        return dingPackageChangeLogMapper.selectDingPackageChangeLogList(dingPackageChangeLog);
    }

    /** 查询指定企业的套餐变更日志记录。 */
    @Override
    public List<DingPackageChangeLog> selectDingPackageChangeLogByCorpId(String dingCorpId)
    {
        return dingPackageChangeLogMapper.selectDingPackageChangeLogByCorpId(dingCorpId);
    }

    /** 新增套餐变更日志记录。 */
    @Override
    public int insertDingPackageChangeLog(DingPackageChangeLog dingPackageChangeLog)
    {
        dingPackageChangeLog.setCreateTime(DateUtils.getNowDate());
        return dingPackageChangeLogMapper.insertDingPackageChangeLog(dingPackageChangeLog);
    }

    /** 删除指定主键的套餐变更日志记录。 */
    @Override
    public int deleteDingPackageChangeLogById(Long id)
    {
        return dingPackageChangeLogMapper.deleteDingPackageChangeLogById(id);
    }

    /** 删除指定主键集合的套餐变更日志记录。 */
    @Override
    public int deleteDingPackageChangeLogByIds(Long[] ids)
    {
        return dingPackageChangeLogMapper.deleteDingPackageChangeLogByIds(ids);
    }
}
