package com.dj.middle.ding.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingTenant;
import com.dj.middle.ding.mapper.DingTenantMapper;
import com.dj.middle.ding.service.IDingTenantService;

/** 钉钉企业业务实现。 */
@Service
public class DingTenantServiceImpl implements IDingTenantService
{
    /** 提供钉钉企业数据访问能力的映射接口。 */
    @Autowired
   private DingTenantMapper dingTenantMapper;

    /** 查询指定主键的钉钉企业记录。 */
    @Override
    public DingTenant selectDingTenantById(Long id)
    {
        return dingTenantMapper.selectDingTenantById(id);
    }

    /** 查询指定企业的钉钉企业记录。 */
    @Override
    public DingTenant selectDingTenantByCorpId(String dingCorpId)
    {
        return dingTenantMapper.selectDingTenantByCorpId(dingCorpId);
    }

    /** 查询符合筛选条件的钉钉企业列表。 */
    @Override
    public List<DingTenant> selectDingTenantList(DingTenant dingTenant)
    {
        return dingTenantMapper.selectDingTenantList(dingTenant);
    }

    /** 新增钉钉企业记录。 */
    @Override
    public int insertDingTenant(DingTenant dingTenant)
    {
        dingTenant.setCreateTime(DateUtils.getNowDate());
        return dingTenantMapper.insertDingTenant(dingTenant);
    }

    /** 更新钉钉企业记录。 */
    @Override
    public int updateDingTenant(DingTenant dingTenant)
    {
        dingTenant.setUpdateTime(DateUtils.getNowDate());
        return dingTenantMapper.updateDingTenant(dingTenant);
    }

    /** 删除指定主键的钉钉企业记录。 */
    @Override
    public int deleteDingTenantById(Long id)
    {
        return dingTenantMapper.deleteDingTenantById(id);
    }

    /** 删除指定主键集合的钉钉企业记录。 */
    @Override
    public int deleteDingTenantByIds(Long[] ids)
    {
        return dingTenantMapper.deleteDingTenantByIds(ids);
    }

    /** 校验企业标识是否已登记。 */
    @Override
    public boolean checkDingCorpIdUnique(String dingCorpId)
    {
        DingTenant tenant = dingTenantMapper.checkDingCorpIdUnique(dingCorpId);
        return tenant == null;
    }
}
