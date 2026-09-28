package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeSyncTest {
    private JSONObject metadata() throws Exception {
        try (var input = getClass().getResourceAsStream("/employee-metadata.json")) {
            return JSONObject.parseObject(new String(Objects.requireNonNull(input).readAllBytes(),
                    StandardCharsets.UTF_8)).getJSONObject("Result").getJSONObject("NeedReturnData");
        }
    }

    @Test void employeeHeadQueriesCardIdAndKeepsOtherReferenceNames() throws Exception {
        JSONObject metadata = metadata();
        String before = metadata.toJSONString();
        JSONObject schema = DetailSyncSupport.schema(metadata, new JSONObject());
        JSONObject query = ContoryUtil.kingdeeToKingdeeQuery(schema, 0, 10, null, null);
        List<String> keys = Arrays.asList(query.getString("FieldKeys").split(","));
        assertEquals("BD_Empinfo", query.getString("FormId"));
        assertTrue(keys.contains("FFirstCardID"));
        assertFalse(keys.contains("FFirstCardID.FName"));
        assertTrue(keys.contains("FCreateOrgId.FName"));
        assertTrue(keys.contains("FStaffId.FName"));
        assertEquals(before, metadata.toJSONString());
        JSONObject card = schema.getJSONArray("fields").stream().map(v -> (JSONObject) v)
                .filter(f -> "FFirstCardID".equals(f.getString("id"))).findFirst().orElseThrow();
        assertEquals("主证件内码", card.getString("name"));
        assertEquals("123456", ContoryUtil.getKingdeeFieldValue(card, 123456L));
        assertEquals("", ContoryUtil.getKingdeeFieldValue(card, null));
    }

    @Test void employeeDetailQueriesAlsoKeepCardId() throws Exception {
        JSONObject params = new JSONObject();
        params.put("selectedEntryKey", "FPostEntity");
        params.put("headerFillMode", "ALL_ROWS");
        JSONObject schema = DetailSyncSupport.schema(metadata(), params);
        List<String> keys = Arrays.asList(DetailSyncSupport.query(schema, params, 0, 10)
                .getString("FieldKeys").split(","));
        assertTrue(keys.contains("FFirstCardID"));
        assertFalse(keys.contains("FFirstCardID.FName"));
        assertTrue(keys.contains("FPost.FName"));
        assertTrue(keys.contains("FPostDept.FName"));
    }
}
