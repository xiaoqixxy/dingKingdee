package com.dj.middle.ding.service.impl;

import java.util.Date;
import java.util.List;

import com.dingtalk.api.response.OapiServiceGetAuthInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.common.utils.DateUtils;
import com.dj.middle.ding.domain.DingProduct;
import com.dj.middle.ding.domain.DingSubscription;
import com.dj.middle.ding.domain.DingTenant;
import com.dj.middle.ding.mapper.DingProductMapper;
import com.dj.middle.ding.mapper.DingSubscriptionMapper;
import com.dj.middle.ding.mapper.DingTenantMapper;
import com.dj.middle.ding.service.IDingSubscriptionService;
import com.dj.middle.ding.util.DingTalkApiUtil;

/** 企业订阅业务实现。 */
@Service
public class DingSubscriptionServiceImpl implements IDingSubscriptionService
{
    /** 记录当前业务处理过程及异常的日志对象。 */
    private static final Logger log = LoggerFactory.getLogger(DingSubscriptionServiceImpl.class);
    /** 提供企业订阅数据访问能力的映射接口。 */
    @Autowired
    private DingSubscriptionMapper dingSubscriptionMapper;

    /** 提供产品套餐数据访问能力的映射接口。 */
    @Autowired
    private DingProductMapper dingProductMapper;

    /** 提供钉钉企业数据访问能力的映射接口。 */
    @Autowired
    private DingTenantMapper dingTenantMapper;

    /** 访问钉钉企业与应用接口的工具。 */
    @Autowired
    private DingTalkApiUtil dingTalkApiUtil;

    /** 查询指定主键的企业订阅记录。 */
    @Override
    public DingSubscription selectDingSubscriptionById(Long id)
    {
        return dingSubscriptionMapper.selectDingSubscriptionById(id);
    }

    /** 查询指定企业的企业订阅记录。 */
    @Override
    public DingSubscription selectDingSubscriptionByCorpId(String dingCorpId)
    {
        return dingSubscriptionMapper.selectDingSubscriptionByCorpId(dingCorpId);
    }

    /** 查询符合筛选条件的企业订阅列表。 */
    @Override
    public List<DingSubscription> selectDingSubscriptionList(DingSubscription dingSubscription)
    {
        return dingSubscriptionMapper.selectDingSubscriptionList(dingSubscription);
    }

    /** 新增企业订阅记录。 */
    @Override
    public int insertDingSubscription(DingSubscription dingSubscription)
    {
        dingSubscription.setCreateTime(DateUtils.getNowDate());
        return dingSubscriptionMapper.insertDingSubscription(dingSubscription);
    }

    /** 更新企业订阅记录。 */
    @Override
    public int updateDingSubscription(DingSubscription dingSubscription)
    {
        dingSubscription.setUpdateTime(DateUtils.getNowDate());
        return dingSubscriptionMapper.updateDingSubscription(dingSubscription);
    }

    /** 删除指定主键的企业订阅记录。 */
    @Override
    public int deleteDingSubscriptionById(Long id)
    {
        return dingSubscriptionMapper.deleteDingSubscriptionById(id);
    }

    /** 删除指定主键集合的企业订阅记录。 */
    @Override
    public int deleteDingSubscriptionByIds(Long[] ids)
    {
        return dingSubscriptionMapper.deleteDingSubscriptionByIds(ids);
    }

    /** 读取企业当前订阅，缺失时按既有规则登记默认产品权益。 */
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

    /** 根据订阅关联企业更新钉钉企业信息。 */
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
