package com.dj.middle.kingdee.util;
import com.kingdee.bos.webapi.entity.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class KingdeeAuthResponseTest {
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
