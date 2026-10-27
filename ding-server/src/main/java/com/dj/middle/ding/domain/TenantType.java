package com.dj.middle.ding.domain;

/** 企业类型由当前订阅推导，不单独持久化到企业档案。 企业类型数据对象。 */
public enum TenantType {
    /** 尚未订购套餐的企业；新增企业；订阅处于有效期内的企业；临近续费的企业；已经取消订阅的企业；待确认订阅状态的企业。 新增企业；订阅处于有效期内的企业；临近续费的企业；已经取消订阅的企业；待确认订阅状态的企业。 */
    UNSUBSCRIBED, NEW, ACTIVE, RENEWAL_DUE, CANCELLED, PENDING
}
