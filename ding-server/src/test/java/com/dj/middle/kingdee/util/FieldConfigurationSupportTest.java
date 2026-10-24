package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class FieldConfigurationSupportTest {
    private JSONObject metadata(String id, String fields) {
        return JSONObject.parseObject("{\"Id\":\"" + id + "\",\"Name\":[{\"Key\":2052,\"Value\":\"Test\"}],\"PkFieldName\":\"FID\",\"Entrys\":[{\"Key\":\"Head\",\"EntityType\":\"HeadEntity\",\"Fields\":" + fields + "}]}");
    }
    private JSONObject source() {
        return metadata("ORDER", """
          [{"Key":"FBillNo","Name":[{"Key":2052,"Value":"单号"}],"FieldType":231,"ElementType":1},
           {"Key":"FMaterial","Name":[{"Key":2052,"Value":"物料"}],"FieldType":127,"ElementType":13,"LookUpObjectFormId":"MATERIAL"}]
          """);
    }
    private JSONObject referenced(String id) {
        return switch (id) {
            case "MATERIAL" -> metadata(id, """
              [{"Key":"FName","Name":[{"Key":2052,"Value":"名称"}],"FieldType":231,"ElementType":1},{"Key":"FNumber","Name":[{"Key":2052,"Value":"编码"}],"FieldType":231,"ElementType":1},{"Key":"FSpecification","Name":[{"Key":2052,"Value":"规格型号"}],"FieldType":231,"ElementType":1},
               {"Key":"FUnit","Name":[{"Key":2052,"Value":"单位"}],"FieldType":127,"ElementType":13,"LookUpObjectFormId":"UNIT"}]
              """);
            case "UNIT" -> metadata(id, """
              [{"Key":"FPrecision","Name":[{"Key":2052,"Value":"精度"}],"FieldType":106,"ElementType":21},
               {"Key":"FName","Name":[{"Key":2052,"Value":"名称"}],"FieldType":231,"ElementType":1}]
              """);
            default -> throw new IllegalArgumentException("Unexpected metadata access");
        };
    }
    private JSONObject schema() { return DetailSyncSupport.schema(source(), new JSONObject()); }
    private JSONObject params(String path) {
        return JSONObject.parseObject("{\"fieldConfigs\":[{\"fieldId\":\"FMaterial.FName\",\"alias\":\"规格\",\"referencePath\":" + path + "}]}");
    }

    @Test void emptyConfigAndAliasesDoNotLoadReferencesOrChangeQueries() {
        JSONObject original = schema();
        assertSame(original, FieldConfigurationSupport.apply(original, new JSONObject(), key -> { throw new AssertionError(); }));
        JSONObject configured = FieldConfigurationSupport.apply(original, params("[]"), key -> { throw new AssertionError(); });
        assertEquals("规格", configured.getJSONArray("fields").getJSONObject(2).getString("name"));
        assertEquals("物料", original.getJSONArray("fields").getJSONObject(2).getString("name"));
        assertEquals(ContoryUtil.kingdeeToKingdeeQuery(original, 0, 10, null, null),
                ContoryUtil.kingdeeToKingdeeQuery(configured, 0, 10, null, null));
    }

    @Test void specificationAndNestedNumericPathsKeepOutputIdsAndTranslateFiltersAndSorts() throws Exception {
        JSONObject configured = FieldConfigurationSupport.apply(schema(), params("[\"FSpecification\"]"), this::referenced);
        JSONObject field = configured.getJSONArray("fields").getJSONObject(2);
        assertEquals("FMaterial.FSpecification", field.getString("queryKey"));
        assertEquals("FMaterial.FName", field.getString("id"));
        JSONObject nested = FieldConfigurationSupport.apply(schema(), params("[\"FUnit\",\"FPrecision\"]"), this::referenced);
        assertEquals("number", nested.getJSONArray("fields").getJSONObject(2).getString("type"));
        JSONArray filters = JSONArray.parseArray("[{\"conditions\":[{\"fieldId\":\"FMaterial.FName\",\"operator\":\">\",\"value\":\"2\"}]}]");
        JSONArray sorts = JSONArray.parseArray("[{\"fieldId\":\"FMaterial.FName\",\"order\":\"desc\"}]");
        String unchanged = filters.toJSONString();
        JSONObject query = ContoryUtil.kingdeeToKingdeeQuery(nested, 0, 10, filters, sorts);
        assertEquals("FID,FBillNo,FMaterial.FUnit.FPrecision", query.getString("FieldKeys"));
        assertTrue(query.getString("FilterString").contains("FMaterial.FUnit.FPrecision > '2'"));
        assertEquals("FMaterial.FUnit.FPrecision desc", query.getString("OrderString"));
        assertEquals(unchanged, filters.toJSONString());
        JSONObject result = ContoryUtil.kingdeeToDingResult(nested, List.of(List.of(1, "B001", 3)), 0, 10, 100);
        assertEquals("3", result.getJSONArray("records").getJSONObject(0).getJSONObject("fields").getString("FMaterial.FName"));
    }

    @Test void referenceLeafCanUseItsDefaultNameAndMetadataIsCachedPerRequest() {
        JSONObject params = params("[\"FUnit\"]");
        JSONObject configured = FieldConfigurationSupport.apply(schema(), params, this::referenced);
        assertEquals("FMaterial.FUnit.FName", configured.getJSONArray("fields").getJSONObject(2).getString("queryKey"));
        JSONObject raw = source();
        JSONObject another = JSONObject.parseObject(raw.getJSONArray("Entrys").getJSONObject(0).getJSONArray("Fields").getJSONObject(1).toJSONString());
        another.put("Key", "FOther"); raw.getJSONArray("Entrys").getJSONObject(0).getJSONArray("Fields").add(another);
        params.getJSONArray("fieldConfigs").add(JSONObject.of("fieldId", "FOther.FName", "referencePath", JSONArray.of("FSpecification")));
        AtomicInteger calls = new AtomicInteger();
        FieldConfigurationSupport.apply(DetailSyncSupport.schema(raw, new JSONObject()), params,
                key -> { calls.incrementAndGet(); return referenced(key); });
        assertEquals(1, calls.get());
    }

    @Test void invalidPathsStaleFieldsDuplicatesAndScalarTraversalAreRejected() {
        for (String path : List.of("[\"bad;sql\"]", "[\"FSpecification\",\"FName\"]", "[\"FUnit\",\"Missing\"]"))
            assertThrows(IllegalArgumentException.class, () -> FieldConfigurationSupport.apply(schema(), params(path), this::referenced));
        JSONObject stale = params("[]"); stale.getJSONArray("fieldConfigs").getJSONObject(0).put("fieldId", "missing");
        assertThrows(IllegalArgumentException.class, () -> FieldConfigurationSupport.apply(schema(), stale, this::referenced));
        JSONObject duplicate = params("[]"); duplicate.getJSONArray("fieldConfigs").add(duplicate.getJSONArray("fieldConfigs").get(0));
        assertThrows(IllegalArgumentException.class, () -> FieldConfigurationSupport.apply(schema(), duplicate, this::referenced));
        JSONObject scalar = params("[\"FName\"]"); scalar.getJSONArray("fieldConfigs").getJSONObject(0).put("fieldId", "FID");
        assertThrows(IllegalArgumentException.class, () -> FieldConfigurationSupport.apply(schema(), scalar, this::referenced));
    }

    @Test void detailQueryUsesConfiguredPathAndKeepsRecordIdentity() throws Exception {
        JSONObject raw = source();
        JSONObject entity = raw.getJSONArray("Entrys").getJSONObject(0);
        entity.put("Key", "Entry"); entity.put("EntityType", "EntryEntity"); entity.put("EntryPkFieldName", "FEntryID");
        raw.getJSONArray("Entrys").add(0, JSONObject.of("Key", "Head", "EntityType", "HeadEntity", "Fields", new JSONArray()));
        JSONObject params = params("[\"FUnit\",\"FPrecision\"]");
        params.put("selectedEntryKey", "Entry"); params.put("headerFillMode", "ALL_ROWS");
        params.getJSONArray("fieldConfigs").getJSONObject(0).put("fieldId", "Entry_FMaterial.FName");
        JSONObject original = DetailSyncSupport.schema(raw, params);
        JSONObject configured = FieldConfigurationSupport.apply(original, params, this::referenced);
        JSONObject query = DetailSyncSupport.query(configured, params, 0, 10);
        assertTrue(query.getString("FieldKeys").contains("FMaterial.FUnit.FPrecision"));
        List<List<Object>> rows = List.of(List.of(1, 10, "B001", 3));
        JSONObject a = DetailSyncSupport.result(original, params, rows, 0, 10, 100);
        JSONObject b = DetailSyncSupport.result(configured, params, rows, 0, 10, 100);
        assertEquals(a.getJSONArray("records").getJSONObject(0).getString("id"), b.getJSONArray("records").getJSONObject(0).getString("id"));
    }

    @Test void additionalReferenceColumnPreservesOriginalNameAndOutputsNumberIndependently() throws Exception {
        String outputId = "FMaterial.FName__ref_number";
        JSONObject params = JSONObject.of("fieldConfigs", JSONArray.of(JSONObject.of("fieldId", "FMaterial.FName",
                "outputFieldId", outputId, "alias", "物料编号", "referencePath", JSONArray.of("FNumber"))));
        JSONObject original = schema();
        JSONObject configured = FieldConfigurationSupport.apply(original, params, this::referenced);
        JSONArray fields = configured.getJSONArray("fields");
        assertEquals(4, fields.size());
        assertEquals("物料", fields.getJSONObject(2).getString("name"));
        assertEquals("FMaterial.FName", fields.getJSONObject(2).getString("id"));
        assertEquals(outputId, fields.getJSONObject(3).getString("id"));
        assertFalse(fields.getJSONObject(3).getBooleanValue("isPrimary"));
        JSONObject page = ReferenceQuerySupport.streamPage(configured, params, 0, 10, 100, request -> {
            if ("ORDER".equals(request.getString("FormId"))) {
                assertEquals("FID,FBillNo,FMaterial.FName,FMaterial", request.getString("FieldKeys"));
                return List.of(List.of(1, "B001", "物料A", 101));
            }
            assertEquals("MATERIAL", request.getString("FormId"));
            assertEquals("FID,FNumber", request.getString("FieldKeys"));
            return List.of(List.of(101, "WL001"));
        }, () -> {});
        JSONObject values = page.getJSONArray("records").getJSONObject(0).getJSONObject("fields");
        assertEquals("物料A", values.getString("FMaterial.FName"));
        assertEquals("WL001", values.getString(outputId));
        assertEquals(3, original.getJSONArray("fields").size());
        assertEquals(configured, FieldConfigurationSupport.apply(original, params, this::referenced));
    }

    @Test void additionalHeaderColumnsShiftDetailBoundaryWithoutChangingRecordIdentity() throws Exception {
        JSONObject metadata = source();
        metadata.getJSONArray("Entrys").add(JSONObject.of("Key", "Entry", "Name", JSONArray.of(JSONObject.of("Key",2052,"Value","明细")),
                "EntityType", "EntryEntity", "EntryPkFieldName", "FEntryID", "Fields", new JSONArray()));
        JSONObject params = JSONObject.of("selectedEntryKey", "Entry", "headerFillMode", "ALL_ROWS", "fieldConfigs",
                JSONArray.of(JSONObject.of("fieldId", "FMaterial.FName", "outputFieldId", "FMaterial.FName__ref_spec", "referencePath", JSONArray.of("FSpecification"))));
        JSONObject original = DetailSyncSupport.schema(metadata, params);
        JSONObject configured = FieldConfigurationSupport.apply(original, params, this::referenced);
        assertEquals(4, configured.getIntValue("headerFieldCount"));
        assertEquals("Entry_FEntryID", configured.getJSONArray("fields").getJSONObject(4).getString("id"));
        JSONObject before = DetailSyncSupport.result(original, params, List.of(List.of(1, "B001", "A", 10)), 0, 10, 100);
        JSONObject after = DetailSyncSupport.result(configured, params, List.of(List.of(1, "B001", "A", "M8", 10)), 0, 10, 100);
        assertEquals(before.getJSONArray("records").getJSONObject(0).getString("id"), after.getJSONArray("records").getJSONObject(0).getString("id"));
    }

    @Test void duplicateOrInvalidAdditionalColumnsAreRejected() {
        for (String outputId : List.of("FID", "FMaterial.FName__ref_bad;sql")) {
            JSONObject config = params("[]"); config.getJSONArray("fieldConfigs").getJSONObject(0).put("outputFieldId", outputId);
            assertThrows(IllegalArgumentException.class, () -> FieldConfigurationSupport.apply(schema(), config, this::referenced));
        }
        JSONObject duplicate = params("[]");
        JSONObject extra = duplicate.getJSONArray("fieldConfigs").getJSONObject(0); extra.put("outputFieldId", "FMaterial.FName__ref_copy");
        duplicate.getJSONArray("fieldConfigs").add(JSONObject.parseObject(extra.toJSONString()));
        assertThrows(IllegalArgumentException.class, () -> FieldConfigurationSupport.apply(schema(), duplicate, this::referenced));
    }
}
