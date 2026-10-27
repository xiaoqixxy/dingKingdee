package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONObject;
import com.dj.middle.kingdee.constant.DateConverterUtil;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.TimeZone;
import static org.junit.jupiter.api.Assertions.*;

/** DateSyncTest：使用隔离夹具验证业务边界与历史兼容，不连接真实业务环境。 */
class DateSyncTest {
    /** 验证：日期输出保留时分秒和毫秒。 */
    @Test void dateFieldPreservesTimeAndMilliseconds() throws Exception {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
            JSONObject field = new JSONObject();
            field.put("type", "date");
            assertEquals(Instant.parse("2026-09-16T06:35:47Z").toEpochMilli(),
                    ContoryUtil.getKingdeeFieldValue(field, "2026-09-16T14:35:47"));
            assertEquals(Instant.parse("2026-09-16T06:35:47.123Z").toEpochMilli(),
                    ContoryUtil.getKingdeeFieldValue(field, "2026-09-16T14:35:47.123"));
            assertEquals(Instant.parse("2026-09-15T16:00:00Z").toEpochMilli(),
                    ContoryUtil.getKingdeeFieldValue(field, "2026-09-16T00:00:00"));
            assertEquals("", ContoryUtil.getKingdeeFieldValue(field, null));
        } finally {
            TimeZone.setDefault(original);
        }
    }

    /** 验证：非法日期仍然明确报错。 */
    @Test void invalidDatesStillFail() {
        assertThrows(IllegalArgumentException.class, () -> DateConverterUtil.stringToDate("invalid"));
        assertThrows(IllegalArgumentException.class, () -> DateConverterUtil.stringToDate(""));
    }
}
