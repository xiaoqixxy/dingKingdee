package com.dj.middle.ding.mapper;

import java.util.List;
import com.dj.middle.ding.domain.DingSubscription;

/** 企业订阅数据访问契约。 */
public interface DingSubscriptionMapper
{
    /** 查询指定主键的企业订阅记录。 */
    public DingSubscription selectDingSubscriptionById(Long id);

    /** 查询指定企业的企业订阅记录。 */
    public DingSubscription selectDingSubscriptionByCorpId(String dingCorpId);
    /** 续费事务锁定企业订阅，防止同时续费丢失有效期。 */
    public DingSubscription selectDingSubscriptionByCorpIdForUpdate(String dingCorpId);

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

    /** 只改到期状态，不覆盖有效期或并发累加的用量。 */
    public int expireDueSubscriptions();
}
