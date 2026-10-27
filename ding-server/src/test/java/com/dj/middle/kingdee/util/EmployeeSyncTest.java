package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

/** EmployeeSyncTest：使用隔离夹具验证业务边界与历史兼容，不连接真实业务环境。 */
class EmployeeSyncTest {
    /** 构造不含业务凭证的最小元数据测试夹具。 */
    private JSONObject metadata() throws Exception {
        try (var input = getClass().getResourceAsStream("/employee-metadata.json")) {
            return JSONObject.parseObject(new String(Objects.requireNonNull(input).readAllBytes(),
                    StandardCharsets.UTF_8)).getJSONObject("Result").getJSONObject("NeedReturnData");
        }
    }

    /** 验证：员工主证件读取关联内码，其他引用保持原显示值。 */
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

    /** 验证：员工明细查询也保留证件内码。 */
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
