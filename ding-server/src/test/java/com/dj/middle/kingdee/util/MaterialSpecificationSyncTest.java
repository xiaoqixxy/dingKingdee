package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** MaterialSpecificationSyncTest：使用隔离夹具验证业务边界与历史兼容，不连接真实业务环境。 */
class MaterialSpecificationSyncTest {
    /** 构造不含业务凭证的最小元数据测试夹具。 */
    private JSONObject metadata() {
        // Procurement metadata returns the material reference, but no separate specification field.
        return JSONObject.parseObject("""
          {"Id":"PUR_Requisition","Name":[{"Key":2052,"Value":"采购申请单"}],"PkFieldName":"FID","Entrys":[
            {"Key":"FBillHead","EntityType":"HeadEntity","Fields":[]},
            {"Key":"FEntity","Name":[{"Key":2052,"Value":"明细信息"}],"EntityType":"EntryEntity","EntryPkFieldName":"FEntryID","Fields":[
              {"Key":"FMaterialId","Name":[{"Key":2052,"Value":"物料编码"}],"FieldType":127,"ElementType":13,"LookUpObjectFormId":"BD_MATERIAL"},
              {"Key":"FParentMaterialId","Name":[{"Key":2052,"Value":"产品"}],"FieldType":127,"ElementType":13,"LookUpObjectFormId":"BD_MATERIAL"},
              {"Key":"FUnitId","Name":[{"Key":2052,"Value":"单位"}],"FieldType":127,"ElementType":13,"LookUpObjectFormId":"BD_UNIT"}]}]}
          """);
    }

    /** 验证：不自动补加元数据未声明的规格型号字段。 */
    @Test void undeclaredSpecificationsAreNeverAddedToDiscoveryOrQueries() {
        JSONObject meta = metadata();
        String original = meta.toJSONString();
        JSONObject discovery = DetailSyncSupport.schema(meta, new JSONObject());
        JSONArray discovered = discovery.getJSONArray("entryOptions").getJSONObject(0).getJSONArray("fields");
        assertEquals(4, discovered.size()); // Entry primary key plus the three declared fields.
        assertFalse(discovered.toJSONString().contains("FSpecification"));
        JSONObject params = JSONObject.of("selectedEntryKeys", JSONArray.of("FEntity"), "headerFillMode", "ALL_ROWS");
        JSONObject schema = DetailSyncSupport.schema(meta, params);
        JSONObject query = DetailSyncSupport.query(schema, params, 0, 10);
        assertTrue(query.getString("FieldKeys").contains("FMaterialId.FName"));
        assertFalse(query.getString("FieldKeys").contains("FSpecification"));
        assertEquals(original, meta.toJSONString());
    }

    /** 验证：保留元数据显式声明的规格型号。 */
    @Test void explicitlyDeclaredSpecificationIsPreserved() {
        JSONObject meta = metadata();
        JSONObject entry = meta.getJSONArray("Entrys").getJSONObject(1);
        entry.getJSONArray("Fields").add(JSONObject.of("Key", "FMaterialId.FSpecification", "FieldType", 231, "ElementType", 1,
                "Name", JSONArray.of(JSONObject.of("Key", 2052, "Value", "规格型号"))));
        meta.getJSONArray("Entrys").getJSONObject(0).put("Fields", entry.getJSONArray("Fields"));
        JSONArray fields = ContoryUtil.kingdeeToDing(meta).getJSONArray("fields");
        assertEquals(1, fields.stream().filter(item -> "FMaterialId.FSpecification".equals(((JSONObject)item).getString("id"))).count());
    }

    /** 验证：引用元数据同样不凭空新增字段。 */
    @Test void referenceMetadataDoesNotInventFieldsEither() {
        JSONObject meta = metadata();
        meta.getJSONArray("Entrys").getJSONObject(0).put("Fields", meta.getJSONArray("Entrys").getJSONObject(1).getJSONArray("Fields"));
        JSONArray fields = FieldConfigurationSupport.referenceSchema(meta).getJSONArray("fields");
        assertFalse(fields.toJSONString().contains("FSpecification"));
    }
}
