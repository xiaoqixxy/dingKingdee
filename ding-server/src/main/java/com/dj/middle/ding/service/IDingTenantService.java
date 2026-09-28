package com.dj.middle.ding.service;

import java.util.List;
import com.dj.middle.ding.domain.DingTenant;

public interface IDingTenantService
{
    public DingTenant selectDingTenantById(Long id);
    public DingTenant selectDingTenantByCorpId(String dingCorpId);
    public List<DingTenant> selectDingTenantList(DingTenant dingTenant);
    public int insertDingTenant(DingTenant dingTenant);
    public int updateDingTenant(DingTenant dingTenant);
    public int deleteDingTenantById(Long id);
    public int deleteDingTenantByIds(Long[] ids);
    public boolean checkDingCorpIdUnique(String dingCorpId);
}
