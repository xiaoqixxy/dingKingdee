package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MaterialGroupSyncTest {
    // Minimal fixture of the failing tenant's BD_MATERIAL group metadata (no business rows).
    private JSONObject metadata() {
        return JSONObject.parseObject("""
            {"Id":"BD_MATERIAL","Name":[{"Key":2052,"Value":"物料"}],"PkFieldName":"FMATERIALID","Entrys":[
              {"Key":"FBillHead","EntityType":"HeadEntity","Fields":[
                {"Key":"FMaterialGroup","Name":[{"Key":2052,"Value":"物料分组"}],"FieldType":127,"ElementType":33,"LookUpObjectFormId":"BOS_FORMGROUP","GroupFieldTableName":"T_BD_MATERIALGROUP"},
                {"Key":"FCreateOrgId","Name":[{"Key":2052,"Value":"创建组织"}],"FieldType":127,"ElementType":7,"LookUpObjectFormId":"ORG_Organizations"}]},
              {"Key":"FEntity","Name":[{"Key":2052,"Value":"明细"}],"EntityType":"EntryEntity","EntryPkFieldName":"FEntryID","ParentKey":"FBillHead","Fields":[]}
            ]}
            """);
    }

    @Test void groupUsesNumberWhileOrdinaryReferencesKeepNames() throws Exception {
        JSONObject schema = DetailSyncSupport.schema(metadata(), new JSONObject());
        JSONObject query = ContoryUtil.kingdeeToKingdeeQuery(schema, 0, 300, null, null);
        assertEquals("FMATERIALID,FMaterialGroup.FNumber,FCreateOrgId.FName", query.getString("FieldKeys"));
        assertEquals("物料分组（编码）", schema.getJSONArray("fields").getJSONObject(1).getString("name"));
        JSONObject result = ContoryUtil.kingdeeToDingResult(schema, List.of(List.of(1, "G001", "组织")), 0, 300, 1000);
        assertEquals("G001", result.getJSONArray("records").getJSONObject(0).getJSONObject("fields").getString("FMaterialGroup.FNumber"));
    }

    @Test void detailModeAlsoAvoidsGenericGroupNameJoin() {
        JSONObject params = JSONObject.of("selectedEntryKey", "FEntity");
        JSONObject query = DetailSyncSupport.query(DetailSyncSupport.schema(metadata(), params), params, 0, 2);
        assertTrue(query.getString("FieldKeys").contains("FMaterialGroup.FNumber"));
        assertFalse(query.getString("FieldKeys").contains("FMaterialGroup.FName"));
    }

    @Test void businessErrorKeepsTypedCauseAndRealCode() {
        var row = List.<Object>of(Map.of("Result", Map.of("ResponseStatus", Map.of(
            "IsSuccess", false, "ErrorCode", "500", "Errors", List.of(Map.of("Message", "对象名 'FormGroup_L' 无效。"))))));
        KingdeeQueryException error = assertThrows(KingdeeQueryException.class,
            () -> KingdeeQueryResponse.requireSuccess(List.of(row)));
        assertEquals("500", error.getBusinessCode());
        assertTrue(error.getMessage().contains("FormGroup_L"));
    }
}
