package com.ruoyi.middle.ding.service.impl;

import java.util.Date;
import java.util.List;

import com.dingtalk.api.response.OapiServiceGetAuthInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.middle.ding.domain.DingProduct;
import com.ruoyi.middle.ding.domain.DingSubscription;
import com.ruoyi.middle.ding.domain.DingTenant;
import com.ruoyi.middle.ding.mapper.DingProductMapper;
import com.ruoyi.middle.ding.mapper.DingSubscriptionMapper;
import com.ruoyi.middle.ding.mapper.DingTenantMapper;
import com.ruoyi.middle.ding.service.IDingSubscriptionService;
import com.ruoyi.middle.ding.util.DingTalkApiUtil;

@Service
public class DingSubscriptionServiceImpl implements IDingSubscriptionService
{
    private static final Logger log = LoggerFactory.getLogger(DingSubscriptionServiceImpl.class);
    @Autowired
    private DingSubscriptionMapper dingSubscriptionMapper;

    @Autowired
    private DingProductMapper dingProductMapper;

    @Autowired
    private DingTenantMapper dingTenantMapper;

    @Autowired
    private DingTalkApiUtil dingTalkApiUtil;

    @Override
    public DingSubscription selectDingSubscriptionById(Long id)
    {
        return dingSubscriptionMapper.selectDingSubscriptionById(id);
    }

    @Override
    public DingSubscription selectDingSubscriptionByCorpId(String dingCorpId)
    {
        return dingSubscriptionMapper.selectDingSubscriptionByCorpId(dingCorpId);
    }

    @Override
    public List<DingSubscription> selectDingSubscriptionList(DingSubscription dingSubscription)
    {
        return dingSubscriptionMapper.selectDingSubscriptionList(dingSubscription);
    }

    @Override
    public int insertDingSubscription(DingSubscription dingSubscription)
    {
        dingSubscription.setCreateTime(DateUtils.getNowDate());
        return dingSubscriptionMapper.insertDingSubscription(dingSubscription);
    }

    @Override
    public int updateDingSubscription(DingSubscription dingSubscription)
    {
        dingSubscription.setUpdateTime(DateUtils.getNowDate());
        return dingSubscriptionMapper.updateDingSubscription(dingSubscription);
    }

    @Override
    public int deleteDingSubscriptionById(Long id)
    {
        return dingSubscriptionMapper.deleteDingSubscriptionById(id);
    }

    @Override
    public int deleteDingSubscriptionByIds(Long[] ids)
    {
        return dingSubscriptionMapper.deleteDingSubscriptionByIds(ids);
    }

    @Override
    public DingProduct getOrRegisterDefaultProduct(String dingCorpId)
    {

        //todo 需要三方企业应用权限调用接口
//        syncDingTenantInfo(dingCorpId);

        DingSubscription subscription = dingSubscriptionMapper.selectDingSubscriptionByCorpId(dingCorpId);
        if (subscription != null)
        {
            return dingProductMapper.selectDingProductById(subscription.getProductId());
        }

        DingProduct defaultProduct = dingProductMapper.selectDefaultProduct();
        if (defaultProduct == null)
        {
            return null;
        }
        DingSubscription newSubscription = new DingSubscription();
        newSubscription.setDingCorpId(dingCorpId);
        newSubscription.setProductId(defaultProduct.getId());
        newSubscription.setProductType(defaultProduct.getProductType());
        newSubscription.setStartTime(new Date());
        newSubscription.setEndTime(new Date(System.currentTimeMillis() + (defaultProduct.getProductType() == 2 ? 365L : 30L) * 24 * 60 * 60 * 1000));
        newSubscription.setStatus(1);
        newSubscription.setAutoRenew(0);
        newSubscription.setUsedFormCount(0);
        newSubscription.setMonthUsedSync(0);
        dingSubscriptionMapper.insertDingSubscription(newSubscription);

        return defaultProduct;
    }

    private void syncDingTenantInfo(String dingCorpId)
    {
        DingTenant existingTenant = dingTenantMapper.selectDingTenantByCorpId(dingCorpId);
        if (existingTenant != null)
        {
            return;
        }

        try
        {
            OapiServiceGetAuthInfoResponse corpInfo = dingTalkApiUtil.getCorpByCorpId(dingCorpId);
            if (corpInfo == null)
            {
                return;
            }
            DingTenant tenant = new DingTenant();
            tenant.setDingCorpId(dingCorpId);
            tenant.setCorpName(corpInfo.getAuthCorpInfo().getCorpName());
            tenant.setIndustry(corpInfo.getAuthCorpInfo().getIndustry());
            tenant.setCorpLogoUrl(corpInfo.getAuthCorpInfo().getCorpLogoUrl());
            tenant.setLicenseCode(corpInfo.getAuthCorpInfo().getLicenseCode());
            tenant.setAuthChannel(corpInfo.getAuthCorpInfo().getAuthChannel());
            tenant.setAuthChannelType(corpInfo.getAuthCorpInfo().getAuthChannelType());
            tenant.setIsAuthenticated(corpInfo.getAuthCorpInfo().getIsAuthenticated());
            tenant.setAuthLevel(corpInfo.getAuthCorpInfo().getAuthLevel());
            tenant.setInviteUrl(corpInfo.getAuthCorpInfo().getInviteUrl());
            tenant.setStatus(1);
            tenant.setCreateTime(DateUtils.getNowDate());
            dingTenantMapper.insertDingTenant(tenant);
        }
        catch (Exception e)
        {
            log.error("同步企业信息失败: {}", e.getMessage());
        }
    }
}
