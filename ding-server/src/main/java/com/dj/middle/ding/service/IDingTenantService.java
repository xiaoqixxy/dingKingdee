package com.dj.middle.ding.service;

import java.util.List;
import com.dj.middle.ding.domain.DingTenant;

/** 钉钉企业服务契约。 */
public interface IDingTenantService
{
    /** 查询指定主键的钉钉企业记录。 */
    public DingTenant selectDingTenantById(Long id);
    /** 查询指定企业的钉钉企业记录。 */
    public DingTenant selectDingTenantByCorpId(String dingCorpId);
    /** 查询符合筛选条件的钉钉企业列表。 */
    public List<DingTenant> selectDingTenantList(DingTenant dingTenant);
    /** 新增钉钉企业记录。 */
    public int insertDingTenant(DingTenant dingTenant);
    /** 更新钉钉企业记录。 */
    public int updateDingTenant(DingTenant dingTenant);
    /** 删除指定主键的钉钉企业记录。 */
    public int deleteDingTenantById(Long id);
    /** 删除指定主键集合的钉钉企业记录。 */
    public int deleteDingTenantByIds(Long[] ids);
    /** 校验企业标识是否已登记。 */
    public boolean checkDingCorpIdUnique(String dingCorpId);
}
