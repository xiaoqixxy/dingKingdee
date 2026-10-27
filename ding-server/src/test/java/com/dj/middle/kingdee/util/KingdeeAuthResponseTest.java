package com.dj.middle.kingdee.util;
import com.kingdee.bos.webapi.entity.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/** KingdeeAuthResponseTest：使用隔离夹具验证业务边界与历史兼容，不连接真实业务环境。 */
class KingdeeAuthResponseTest {
    /** 验证：登录必须明确成功，不能仅凭无异常放行。 */
    @Test void requiresExplicitSuccess() {
        assertThrows(IllegalArgumentException.class, () -> KingdeeAuthResponse.requireSuccess(null));
        RepoResult<Object> result = new RepoResult<>();
        assertThrows(IllegalArgumentException.class, () -> KingdeeAuthResponse.requireSuccess(result));
        RepoStatus status = new RepoStatus(); result.setResponseStatus(status);
        status.setIsSuccess(false);
        assertThrows(IllegalArgumentException.class, () -> KingdeeAuthResponse.requireSuccess(result));
        status.setIsSuccess(true);
        assertDoesNotThrow(() -> KingdeeAuthResponse.requireSuccess(result));
    }
}
