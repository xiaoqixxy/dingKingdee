package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** GroupReferenceConfigurationTest：使用隔离夹具验证业务边界与历史兼容，不连接真实业务环境。 */
class GroupReferenceConfigurationTest {
    /** 构造不含业务凭证的最小元数据测试夹具。 */
    private JSONObject metadata(String form, String pk, String fields) {
        return JSONObject.parseObject("{\"Id\":\"" + form + "\",\"PkFieldName\":\"" + pk
                + "\",\"Name\":[{\"Key\":2052,\"Value\":\"Test\"}],\"Entrys\":[{\"Key\":\"Head\",\"EntityType\":\"HeadEntity\",\"Fields\":" + fields + "}]}");
    }
    // 构造包含物料分组与销售分组的元数据，验证相同通用对象保留不同所属字段。
    /** 构造物料引用元数据测试夹具。 */
    private JSONObject material() {
        return metadata("BD_MATERIAL", "FMATERIALID", """
            [{"Key":"FMaterialGroup","Name":[{"Key":2052,"Value":"物料分组"}],"FieldType":127,"ElementType":33,"LookUpObjectFormId":"BOS_FORMGROUP","GroupFieldTableName":"T_BD_MATERIALGROUP"},
             {"Key":"FSalGroup","Name":[{"Key":2052,"Value":"销售分组"}],"FieldType":127,"ElementType":33,"LookUpObjectFormId":"BOS_FORMGROUP","GroupFieldTableName":"T_BD_MATERIALSALGROUP"}]
            """);
    }
    /** 构造具有引用字段的来源对象。 */
    private JSONObject source() {
        return ContoryUtil.kingdeeToDing(metadata("STK_InStock", "FID", """
            [{"Key":"FMaterialId","Name":[{"Key":2052,"Value":"物料"}],"FieldType":127,"ElementType":13,"LookUpObjectFormId":"BD_MATERIAL"}]
            """));
    }
    /** 构造当前场景需要的同步配置参数。 */
    private JSONObject params(boolean legacy) {
        JSONArray configs = new JSONArray();
        for (String group : List.of("FMaterialGroup", "FSalGroup")) {
            JSONArray path = JSONArray.of(group);
            if (legacy) path.add("FID");
            configs.add(JSONObject.of("fieldId", "FMaterialId.FName", "outputFieldId", "FMaterialId.FName__ref_" + group,
                    "alias", group, "referencePath", path));
        }
        return JSONObject.of("fieldConfigs", configs);
    }
    /** 将测试字段配置应用到来源结构。 */
    private JSONObject configured(boolean legacy) {
        return FieldConfigurationSupport.apply(source(), params(legacy), form -> {
            assertEquals("BD_MATERIAL", form, "must not load generic BOS_FORMGROUP");
            return material();
        });
    }
    /** 验证：占位分组对象使用维护字典且携带所属字段上下文。 */
    @Test void placeholderUsesTheMaintainedDictionaryAndGroupChoicesCarryOwnerContext() {
        JSONObject placeholder = JSONObject.of("Id", "BOS_FORMGROUP", "PkFieldName", "FID", "Entrys", new JSONArray());
        assertEquals("FNumber", FieldConfigurationSupport.referenceSchema(placeholder).getJSONArray("fields").getJSONObject(0).getString("key"));
        JSONArray fields = FieldConfigurationSupport.referenceSchema(material()).getJSONArray("fields");
        for (int i = 1; i < fields.size(); i++) {
            JSONObject field = fields.getJSONObject(i);
            assertEquals("BOS_FORMGROUP", field.getString("referenceFormId"));
            assertEquals("BD_MATERIAL", field.getJSONObject("referenceContext").getString("formId"));
            assertEquals(field.getString("key"), field.getJSONObject("referenceContext").getString("groupFieldKey"));
            assertEquals(field.getString("key") + ".FNumber", field.getString("id"));
            assertEquals(i == 1 ? "物料分组" : "销售分组", field.getString("name"));
        }
    }
    /** 验证：旧合成FID路径兼容为原编码并保持输出列稳定。 */
    @Test void legacyFidChildrenUseSameQueriesAndStableOutputColumnsAsNewChoices() {
        assertEquals(configured(false), configured(true));
        for (Object item : configured(true).getJSONArray("fields")) {
            JSONObject field = (JSONObject) item;
            if (!field.containsKey("referencePlan")) continue;
            JSONArray plan = field.getJSONArray("referencePlan");
            assertEquals(2, plan.size());
            assertEquals("BD_MATERIAL", plan.getJSONObject(0).getString("formId"));
            assertEquals("FMATERIALID", plan.getJSONObject(0).getString("primaryKey"));
            assertTrue(Set.of("FMaterialGroup", "FSalGroup").contains(plan.getJSONObject(0).getString("fieldKey")));
            assertEquals("BOS_FORMGROUP", plan.getJSONObject(1).getString("dictionaryId"));
            assertEquals("FNumber", plan.getJSONObject(1).getString("fieldKey"));
        }
    }
    /** 验证：物料分组通过所属对象与专用分组接口解析。 */
    @Test void groupValuesAreResolvedThroughMaterialAndNeverThroughGenericGroupQueries() throws Exception {
        JSONObject schema = configured(true);
        JSONObject result = ReferenceQuerySupport.streamPage(schema, params(true), 0, 10, 100, request -> {
            if ("STK_InStock".equals(request.getString("FormId"))) {
                assertEquals("FID,FMaterialId.FName,FMaterialId,FMaterialId", request.getString("FieldKeys"));
                return List.of(List.of(100, "物料名称", 10, 10));
            }
            assertEquals("BD_MATERIAL", request.getString("FormId"));
            if ("QueryGroupInfo".equals(request.getString("queryType"))) {
                boolean material = "FMaterialGroup".equals(request.getString("GroupFieldKey"));
                assertEquals(material ? "101" : "202", request.getString("GroupPkIds"));
                return List.of(List.of(material ? 101 : 202, JSONObject.of("FID", material ? 101 : 202, "FNUMBER", material ? "M001" : "S002")));
            }
            String fields = request.getString("FieldKeys");
            if (fields.equals("FMATERIALID,FMaterialGroup")) return List.of(List.of(10, 101));
            assertEquals("FMATERIALID,FSalGroup", fields);
            return List.of(List.of(10, 202));
        }, () -> fail("unexpected quota"));
        JSONObject values = result.getJSONArray("records").getJSONObject(0).getJSONObject("fields");
        assertEquals("M001", values.getString("FMaterialId.FName__ref_FMaterialGroup"));
        assertEquals("S002", values.getString("FMaterialId.FName__ref_FSalGroup"));
    }
    /** 验证：已有直接分组列保留标识和别名。 */
    @Test void existingDirectGroupColumnRetainsIdAliasAndOwnerQuery() {
        JSONObject schema = ContoryUtil.kingdeeToDing(material());
        JSONObject config = JSONObject.of("fieldConfigs", JSONArray.of(JSONObject.of("fieldId", "FMaterialGroup.FNumber",
                "alias", "分组编码", "referencePath", JSONArray.of("FID"))));
        JSONObject fixed = FieldConfigurationSupport.apply(schema, config, form -> { throw new AssertionError(); });
        assertTrue(ReferenceQuerySupport.required(fixed));
        assertEquals("分组编码", fixed.getJSONArray("fields").getJSONObject(1).getString("name"));
        assertEquals("FMATERIALID,FMaterialGroup.FNumber,FSalGroup.FNumber",
                ContoryUtil.kingdeeToKingdeeQuery(fixed, 0, 10, null, null).getString("FieldKeys"));
    }
    /** 验证：不支持的分组属性明确拒绝而非静默换义。 */
    @Test void arbitraryGroupChildrenAreNotSilentlyReinterpreted() {
        JSONObject params = params(false);
        params.getJSONArray("fieldConfigs").getJSONObject(0).getJSONArray("referencePath").add("FNotSupported");
        assertThrows(IllegalArgumentException.class, () -> FieldConfigurationSupport.apply(source(), params, form -> material()));
    }

    /** 验证：带版本的新路径支持真实内码名称描述及父级名称。 */
    @Test void versionedChoicesExposeRealGroupIdNamesDescriptionsAndParentNames() {
        for (String attribute : List.of("FID", "FName", "FDescription")) {
            JSONObject params = JSONObject.of("fieldConfigs", JSONArray.of(JSONObject.of("fieldId", "FMaterialId.FName",
                    "dictionaryVersion", 1, "referencePath", JSONArray.of("FMaterialGroup", attribute))));
            JSONObject configured = FieldConfigurationSupport.apply(source(), params, form -> material());
            JSONArray plan = configured.getJSONArray("fields").getJSONObject(1).getJSONArray("referencePlan");
            assertEquals(attribute, plan.getJSONObject(1).getString("fieldKey"));
            assertEquals("FMaterialGroup", plan.getJSONObject(1).getJSONObject("context").getString("groupFieldKey"));
        }
        JSONObject params = JSONObject.of("fieldConfigs", JSONArray.of(JSONObject.of("fieldId", "FMaterialId.FName",
                "dictionaryVersion", 1, "referencePath", JSONArray.of("FSalGroup", "FParentId", "FName"))));
        JSONArray plan = FieldConfigurationSupport.apply(source(), params, form -> material()).getJSONArray("fields").getJSONObject(1).getJSONArray("referencePlan");
        assertEquals(3, plan.size());
        assertEquals("FParentId", plan.getJSONObject(1).getString("fieldKey"));
        assertEquals("FName", plan.getJSONObject(2).getString("fieldKey"));
        assertEquals(plan.getJSONObject(1).getJSONObject("context"), plan.getJSONObject(2).getJSONObject("context"));
        assertEquals("FSalGroup", plan.getJSONObject(2).getJSONObject("context").getString("groupFieldKey"));
    }
}
