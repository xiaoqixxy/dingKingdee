package com.dj.middle.ding.service;

import java.util.List;
import com.dj.middle.ding.domain.DingSyncForm;

/** 同步表单绑定服务契约。 */
public interface IDingSyncFormService
{
    /** 查询指定主键的同步表单绑定记录。 */
    public DingSyncForm selectDingSyncFormById(Long id);
    /** 查询指定企业及表单的同步表单绑定记录。 */
    public DingSyncForm selectDingSyncFormByCorpIdAndFormId(String dingCorpId, String dingFormId);
    /** 查询符合筛选条件的同步表单绑定列表。 */
    public List<DingSyncForm> selectDingSyncFormList(DingSyncForm dingSyncForm);
    /** 查询指定企业的同步表单绑定记录。 */
    public List<DingSyncForm> selectDingSyncFormByCorpId(String dingCorpId);
    /** 新增同步表单绑定记录。 */
    public int insertDingSyncForm(DingSyncForm dingSyncForm);
    /** 更新同步表单绑定记录。 */
    public int updateDingSyncForm(DingSyncForm dingSyncForm);
    /** 删除指定主键的同步表单绑定记录。 */
    public int deleteDingSyncFormById(Long id);
    /** 删除指定主键集合的同步表单绑定记录。 */
    public int deleteDingSyncFormByIds(Long[] ids);
    /** 统计指定企业已绑定的同步表单数量。 */
    public int countByCorpId(String dingCorpId);
}
