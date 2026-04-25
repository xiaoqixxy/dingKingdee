package com.ruoyi.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.middle.ding.domain.DingTenant;
import com.ruoyi.middle.ding.mapper.DingTenantMapper;
import com.ruoyi.middle.ding.service.IDingTenantService;

@Service
public class DingTenantServiceImpl implements IDingTenantService
{
    @Autowired
    private DingTenantMapper dingTenantMapper;

    @Override
    public DingTenant selectDingTenantById(Long id)
    {
        return dingTenantMapper.selectDingTenantById(id);
    }

    @Override
    public DingTenant selectDingTenantByCorpId(String dingCorpId)
    {
        return dingTenantMapper.selectDingTenantByCorpId(dingCorpId);
    }

    @Override
    public List<DingTenant> selectDingTenantList(DingTenant dingTenant)
    {
        return dingTenantMapper.selectDingTenantList(dingTenant);
    }

    @Override
    public int insertDingTenant(DingTenant dingTenant)
    {
        dingTenant.setCreateTime(DateUtils.getNowDate());
        return dingTenantMapper.insertDingTenant(dingTenant);
    }

    @Override
    public int updateDingTenant(DingTenant dingTenant)
    {
        dingTenant.setUpdateTime(DateUtils.getNowDate());
        return dingTenantMapper.updateDingTenant(dingTenant);
    }

    @Override
    public int deleteDingTenantById(Long id)
    {
        return dingTenantMapper.deleteDingTenantById(id);
    }

    @Override
    public int deleteDingTenantByIds(Long[] ids)
    {
        return dingTenantMapper.deleteDingTenantByIds(ids);
    }

    @Override
    public boolean checkDingCorpIdUnique(String dingCorpId)
    {
        DingTenant tenant = dingTenantMapper.checkDingCorpIdUnique(dingCorpId);
        return tenant == null;
    }
}
