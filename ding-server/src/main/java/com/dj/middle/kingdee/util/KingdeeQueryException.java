package com.dj.middle.kingdee.util;

/** Upstream business rejection, distinct from local validation and transport errors. */
public class KingdeeQueryException extends IllegalArgumentException {
    private final String businessCode;

    public KingdeeQueryException(String businessCode, String message) {
        super(message);
        this.businessCode = businessCode;
    }

    public String getBusinessCode() { return businessCode; }
}
