package com.dj.middle.kingdee.util;

import com.kingdee.bos.webapi.entity.RepoResult;

/** Only an explicit authenticated success can pass connection verification. */
public final class KingdeeAuthResponse {
    private KingdeeAuthResponse() {}
    public static void requireSuccess(RepoResult<?> result) {
        if (result == null || result.getResponseStatus() == null || !result.getResponseStatus().isIsSuccess()) {
            throw new IllegalArgumentException("身份认证或授权校验未通过，请检查账套 ID、用户名、APP_ID、APP_SECRET，以及应用的币别查询权限");
        }
    }
}
