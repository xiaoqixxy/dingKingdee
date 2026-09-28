package com.dj.middle.ding.mapper;

import java.util.List;
import com.dj.middle.ding.domain.DingSubscription;

public interface DingSubscriptionMapper
{
    public DingSubscription selectDingSubscriptionById(Long id);

    public DingSubscription selectDingSubscriptionByCorpId(String dingCorpId);

    public List<DingSubscription> selectDingSubscriptionList(DingSubscription dingSubscription);

    public int insertDingSubscription(DingSubscription dingSubscription);

    public int updateDingSubscription(DingSubscription dingSubscription);

    public int deleteDingSubscriptionById(Long id);

    public int deleteDingSubscriptionByIds(Long[] ids);
}
