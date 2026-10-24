package com.dj.middle.ding.domain;

/** Derived from the current subscription; never stored on the enterprise profile. */
public enum TenantType {
    UNSUBSCRIBED, NEW, ACTIVE, RENEWAL_DUE, CANCELLED, PENDING
}
