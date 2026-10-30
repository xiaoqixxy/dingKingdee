package com.dj.middle.ding.service;

import java.util.List;
import com.dj.middle.ding.domain.DingProduct;
import com.dj.middle.ding.domain.DingSubscription;

/** 企业订阅服务契约。 */
public interface IDingSubscriptionService
{
    /** 查询指定主键的企业订阅记录。 */
    public DingSubscription selectDingSubscriptionById(Long id);
    /** 查询指定企业的企业订阅记录。 */
    public DingSubscription selectDingSubscriptionByCorpId(String dingCorpId);
    /** 查询符合筛选条件的企业订阅列表。 */
    public List<DingSubscription> selectDingSubscriptionList(DingSubscription dingSubscription);
    /** 新增企业订阅记录。 */
    public int insertDingSubscription(DingSubscription dingSubscription);
    /** 更新企业订阅记录。 */
    public int updateDingSubscription(DingSubscription dingSubscription);
    /** 删除指定主键的企业订阅记录。 */
    public int deleteDingSubscriptionById(Long id);
    /** 删除指定主键集合的企业订阅记录。 */
    public int deleteDingSubscriptionByIds(Long[] ids);
    /** 读取企业当前订阅，缺失时按既有规则登记默认产品权益。 */
    public DingProduct getOrRegisterDefaultProduct(String dingCorpId);
    /** 同步入口必须使用此方法：实时校验企业、状态及完整有效期。 */
    public DingProduct requireActiveProduct(String dingCorpId);
    /** 仅把仍生效且到期时间已过的订阅标记到期。 */
    public int expireDueSubscriptions();
}
