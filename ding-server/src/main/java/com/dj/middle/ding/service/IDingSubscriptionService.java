package com.dj.middle.ding.service;

import java.util.List;
import com.dj.middle.ding.domain.DingProduct;
import com.dj.middle.ding.domain.DingSubscription;

public interface IDingSubscriptionService
{
    public DingSubscription selectDingSubscriptionById(Long id);
    public DingSubscription selectDingSubscriptionByCorpId(String dingCorpId);
    public List<DingSubscription> selectDingSubscriptionList(DingSubscription dingSubscription);
    public int insertDingSubscription(DingSubscription dingSubscription);
    public int updateDingSubscription(DingSubscription dingSubscription);
    public int deleteDingSubscriptionById(Long id);
    public int deleteDingSubscriptionByIds(Long[] ids);
    public DingProduct getOrRegisterDefaultProduct(String dingCorpId);
}
