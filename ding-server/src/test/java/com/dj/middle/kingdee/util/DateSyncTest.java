package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONObject;
import com.dj.middle.kingdee.constant.DateConverterUtil;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.TimeZone;
import static org.junit.jupiter.api.Assertions.*;

class DateSyncTest {
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

    @Test void invalidDatesStillFail() {
        assertThrows(IllegalArgumentException.class, () -> DateConverterUtil.stringToDate("invalid"));
        assertThrows(IllegalArgumentException.class, () -> DateConverterUtil.stringToDate(""));
    }
}
