package com.ruoyi.middle.ding.mapper;

import java.util.List;
import com.ruoyi.middle.ding.domain.DingTenant;

public interface DingTenantMapper
{
    public DingTenant selectDingTenantById(Long id);

    public DingTenant selectDingTenantByCorpId(String dingCorpId);

    public List<DingTenant> selectDingTenantList(DingTenant dingTenant);

    public int insertDingTenant(DingTenant dingTenant);

    public int updateDingTenant(DingTenant dingTenant);

    public int deleteDingTenantById(Long id);

    public int deleteDingTenantByIds(Long[] ids);

    public DingTenant checkDingCorpIdUnique(String dingCorpId);
}
