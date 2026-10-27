package com.dj.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingSyncForm;
import com.dj.middle.ding.mapper.DingSyncFormMapper;
import com.dj.middle.ding.service.IDingSyncFormService;

/** 同步表单绑定业务实现。 */
@Service
public class DingSyncFormServiceImpl implements IDingSyncFormService
{
    /** 提供同步表单绑定数据访问能力的映射接口。 */
    @Autowired
   private DingSyncFormMapper dingSyncFormMapper;

    /** 查询指定主键的同步表单绑定记录。 */
    @Override
    public DingSyncForm selectDingSyncFormById(Long id)
    {
        return dingSyncFormMapper.selectDingSyncFormById(id);
    }

    /** 查询指定企业及表单的同步表单绑定记录。 */
    @Override
    public DingSyncForm selectDingSyncFormByCorpIdAndFormId(String dingCorpId, String dingFormId)
    {
        return dingSyncFormMapper.selectDingSyncFormByCorpIdAndFormId(dingCorpId, dingFormId);
    }

    /** 查询符合筛选条件的同步表单绑定列表。 */
    @Override
    public List<DingSyncForm> selectDingSyncFormList(DingSyncForm dingSyncForm)
    {
        return dingSyncFormMapper.selectDingSyncFormList(dingSyncForm);
    }

    /** 查询指定企业的同步表单绑定记录。 */
    @Override
    public List<DingSyncForm> selectDingSyncFormByCorpId(String dingCorpId)
    {
        return dingSyncFormMapper.selectDingSyncFormByCorpId(dingCorpId);
    }

    /** 新增同步表单绑定记录。 */
    @Override
    public int insertDingSyncForm(DingSyncForm dingSyncForm)
    {
        dingSyncForm.setCreateTime(DateUtils.getNowDate());
        return dingSyncFormMapper.insertDingSyncForm(dingSyncForm);
    }

    /** 更新同步表单绑定记录。 */
    @Override
    public int updateDingSyncForm(DingSyncForm dingSyncForm)
    {
        dingSyncForm.setUpdateTime(DateUtils.getNowDate());
        return dingSyncFormMapper.updateDingSyncForm(dingSyncForm);
    }

    /** 删除指定主键的同步表单绑定记录。 */
    @Override
    public int deleteDingSyncFormById(Long id)
    {
        return dingSyncFormMapper.deleteDingSyncFormById(id);
    }

    /** 删除指定主键集合的同步表单绑定记录。 */
    @Override
    public int deleteDingSyncFormByIds(Long[] ids)
    {
        return dingSyncFormMapper.deleteDingSyncFormByIds(ids);
    }

    /** 统计指定企业已绑定的同步表单数量。 */
    @Override
    public int countByCorpId(String dingCorpId)
    {
        return dingSyncFormMapper.countByCorpId(dingCorpId);
    }
}
