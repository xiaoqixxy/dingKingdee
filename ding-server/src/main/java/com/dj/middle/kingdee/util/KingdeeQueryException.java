package com.dj.middle.kingdee.util;

/** 上游业务拒绝异常，与本地参数校验和网络传输错误区分。 携带金蝶真实业务错误码的查询异常。 */
public class KingdeeQueryException extends IllegalArgumentException {
    /** 金蝶返回的业务错误码。 */
    private final String businessCode;

    /** 初始化当前组件并保存构造参数。 */
    public KingdeeQueryException(String businessCode, String message) {
        super(message);
        this.businessCode = businessCode;
    }

    /** 读取上游业务错误码。 */
    public String getBusinessCode() { return businessCode; }
}
