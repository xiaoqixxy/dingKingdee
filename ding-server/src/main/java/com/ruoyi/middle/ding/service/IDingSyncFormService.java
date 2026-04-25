package com.ruoyi.middle.ding.service;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingSyncForm;

public interface IDingSyncFormService
{
    public DingSyncForm selectDingSyncFormById(Long id);
    public DingSyncForm selectDingSyncFormByCorpIdAndFormId(String dingCorpId, String dingFormId);
    public List<DingSyncForm> selectDingSyncFormList(DingSyncForm dingSyncForm);
    public List<DingSyncForm> selectDingSyncFormByCorpId(String dingCorpId);
    public int insertDingSyncForm(DingSyncForm dingSyncForm);
    public int updateDingSyncForm(DingSyncForm dingSyncForm);
    public int deleteDingSyncFormById(Long id);
    public int deleteDingSyncFormByIds(Long[] ids);
    public int countByCorpId(String dingCorpId);
}
