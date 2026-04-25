package com.ruoyi.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.middle.ding.domain.DingSyncForm;
import com.ruoyi.middle.ding.mapper.DingSyncFormMapper;
import com.ruoyi.middle.ding.service.IDingSyncFormService;

@Service
public class DingSyncFormServiceImpl implements IDingSyncFormService
{
    @Autowired
    private DingSyncFormMapper dingSyncFormMapper;

    @Override
    public DingSyncForm selectDingSyncFormById(Long id)
    {
        return dingSyncFormMapper.selectDingSyncFormById(id);
    }

    @Override
    public DingSyncForm selectDingSyncFormByCorpIdAndFormId(String dingCorpId, String dingFormId)
    {
        return dingSyncFormMapper.selectDingSyncFormByCorpIdAndFormId(dingCorpId, dingFormId);
    }

    @Override
    public List<DingSyncForm> selectDingSyncFormList(DingSyncForm dingSyncForm)
    {
        return dingSyncFormMapper.selectDingSyncFormList(dingSyncForm);
    }

    @Override
    public List<DingSyncForm> selectDingSyncFormByCorpId(String dingCorpId)
    {
        return dingSyncFormMapper.selectDingSyncFormByCorpId(dingCorpId);
    }

    @Override
    public int insertDingSyncForm(DingSyncForm dingSyncForm)
    {
        dingSyncForm.setCreateTime(DateUtils.getNowDate());
        return dingSyncFormMapper.insertDingSyncForm(dingSyncForm);
    }

    @Override
    public int updateDingSyncForm(DingSyncForm dingSyncForm)
    {
        dingSyncForm.setUpdateTime(DateUtils.getNowDate());
        return dingSyncFormMapper.updateDingSyncForm(dingSyncForm);
    }

    @Override
    public int deleteDingSyncFormById(Long id)
    {
        return dingSyncFormMapper.deleteDingSyncFormById(id);
    }

    @Override
    public int deleteDingSyncFormByIds(Long[] ids)
    {
        return dingSyncFormMapper.deleteDingSyncFormByIds(ids);
    }

    @Override
    public int countByCorpId(String dingCorpId)
    {
        return dingSyncFormMapper.countByCorpId(dingCorpId);
    }
}
