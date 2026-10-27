package com.dj.middle.kingdee.util;

import com.kingdee.bos.webapi.entity.RepoResult;

/** 只有明确返回登录成功的响应才允许连接校验通过。 识别金蝶登录响应的业务成功状态。 */
public final class KingdeeAuthResponse {
    /** 工具类仅提供静态方法，禁止外部实例化。 */
    private KingdeeAuthResponse() {}
    /** 校验上游响应中的业务成功状态，失败时保留可识别的错误原因。 */
    public static void requireSuccess(RepoResult<?> result) {
        if (result == null || result.getResponseStatus() == null || !result.getResponseStatus().isIsSuccess()) {
            throw new IllegalArgumentException("身份认证或授权校验未通过，请检查账套 ID、用户名、APP_ID、APP_SECRET，以及应用的币别查询权限");
        }
    }
}
