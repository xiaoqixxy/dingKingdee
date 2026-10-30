package com.dj.middle.ding.service.impl;

import java.util.Date;
import java.util.List;
import java.time.ZonedDateTime;
import org.springframework.dao.DuplicateKeyException;

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

        if (dingCorpId == null || dingCorpId.isBlank() || "jxdj".equals(dingCorpId))
            throw new IllegalArgumentException("缺少有效的钉钉企业ID，请从钉钉企业内打开插件");
        syncDingTenantInfo(dingCorpId);

        DingSubscription subscription = dingSubscriptionMapper.selectDingSubscriptionByCorpId(dingCorpId);
        if (subscription != null)
        {
            return dingProductMapper.selectDingProductById(subscription.getProductId());
        }

        DingProduct defaultProduct = dingProductMapper.selectTrialBasicProduct();
        if (defaultProduct == null)
        {
            throw new IllegalStateException("未配置上架的基础版产品，请联系管理员");
        }
        DingSubscription newSubscription = new DingSubscription();
        newSubscription.setDingCorpId(dingCorpId);
        newSubscription.setProductId(defaultProduct.getId());
        newSubscription.setProductType(defaultProduct.getProductType());
        ZonedDateTime start = ZonedDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        newSubscription.setStartTime(Date.from(start.toInstant()));
        newSubscription.setEndTime(Date.from(start.plusMonths(3).toInstant()));
        newSubscription.setExt1("FIRST_VISIT_BASIC_3_MONTHS");
        newSubscription.setStatus(1);
        newSubscription.setAutoRenew(0);
        newSubscription.setUsedFormCount(0);
        newSubscription.setMonthUsedSync(0);
        try {
            dingSubscriptionMapper.insertDingSubscription(newSubscription);
        } catch (DuplicateKeyException duplicate) {
            // 企业ID唯一约束解决并发首次访问，绝不重置已存在的订阅。
            DingSubscription existing = dingSubscriptionMapper.selectDingSubscriptionByCorpId(dingCorpId);
            if (existing == null) throw duplicate;
            return dingProductMapper.selectDingProductById(existing.getProductId());
        }

        return defaultProduct;
    }

    @Override
    public DingProduct requireActiveProduct(String dingCorpId) {
        DingProduct product = getOrRegisterDefaultProduct(dingCorpId);
        com.dj.middle.ding.service.SubscriptionAccessPolicy.requireActive(
            dingTenantMapper.selectDingTenantByCorpId(dingCorpId),
            dingSubscriptionMapper.selectDingSubscriptionByCorpId(dingCorpId), product, java.time.Instant.now());
        return product;
    }

    @Override
    public int expireDueSubscriptions() { return dingSubscriptionMapper.expireDueSubscriptions(); }

    /** 根据订阅关联企业更新钉钉企业信息。 */
    private void syncDingTenantInfo(String dingCorpId)
    {
        DingTenant tenant = dingTenantMapper.selectDingTenantByCorpId(dingCorpId);
        if (tenant == null) {
            tenant = new DingTenant();
            tenant.setDingCorpId(dingCorpId);
            tenant.setCorpName("企业名称待补全（" + dingCorpId + "）");
            tenant.setStatus(1);
            tenant.setCreateTime(DateUtils.getNowDate());
            try {
                dingTenantMapper.insertDingTenant(tenant);
            } catch (DuplicateKeyException duplicate) {
                tenant = dingTenantMapper.selectDingTenantByCorpId(dingCorpId);
                if (tenant == null) throw duplicate;
            }
        }
        String name = tenant.getCorpName();
        if (name != null && !name.isBlank() && !name.startsWith("企业名称待补全")
                && !"未登记企业".equals(name)) return;
        // 未授权时每天最多补全一次，避免每次读取套餐均请求钉钉。
        if (tenant.getUpdateTime() != null
                && System.currentTimeMillis() - tenant.getUpdateTime().getTime() < 86400000L) return;
        try {
            OapiServiceGetAuthInfoResponse info = dingTalkApiUtil.getCorpByCorpId(dingCorpId);
            if (info != null && info.isSuccess() && info.getAuthCorpInfo() != null
                    && info.getAuthCorpInfo().getCorpName() != null
                    && !info.getAuthCorpInfo().getCorpName().isBlank()) {
                tenant.setCorpName(info.getAuthCorpInfo().getCorpName());
            }
        } catch (Exception e) {
            log.warn("企业名称暂未获取，已按企业ID登记: {}", dingCorpId);
        }
        tenant.setUpdateTime(DateUtils.getNowDate());
        dingTenantMapper.updateDingTenant(tenant);
    }
}
