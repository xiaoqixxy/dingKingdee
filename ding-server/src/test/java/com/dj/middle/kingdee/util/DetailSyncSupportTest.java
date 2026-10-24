package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DetailSyncSupportTest {
    private JSONObject metadata() throws Exception {
        try (var input = getClass().getResourceAsStream("/expense-request-metadata.json")) {
            return JSONObject.parseObject(new String(Objects.requireNonNull(input).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8))
                    .getJSONObject("Result").getJSONObject("NeedReturnData");
        }
    }

    private JSONObject params(String mode) {
        return JSONObject.parseObject("{\"CID\":\"test-account\",\"selectedEntryKey\":\"FEntity\",\"headerFillMode\":\"" + mode + "\"}");
    }

    private List<Object> row(JSONObject schema, long bill, long entry) {
        List<Object> row = new ArrayList<>(Collections.nCopies(schema.getJSONArray("fields").size(), null));
        row.set(0, bill);
        row.set(1, "FY" + bill);
        row.set(schema.getIntValue("headerFieldCount"), entry);
        return row;
    }

    @Test void realMetadataAndLegacySchema() throws Exception {
        JSONObject meta = metadata();
        JSONObject legacy = DetailSyncSupport.schema(meta, new JSONObject());
        assertEquals(ContoryUtil.kingdeeToDing(meta).getJSONArray("fields"), legacy.getJSONArray("fields"));
        assertEquals(1, legacy.getJSONArray("entryOptions").size());
        assertEquals("明细信息", legacy.getJSONArray("entryOptions").getJSONObject(0).getString("name"));
        JSONObject selected = DetailSyncSupport.schema(meta, params("FIRST_ROW"));
        assertEquals(59, selected.getIntValue("headerFieldCount"));
        assertEquals(82, selected.getJSONArray("fields").size());
        assertEquals("FEntity_FEntryID", selected.getJSONArray("fields").getJSONObject(59).getString("id"));
        assertEquals("FEntity_FSEQ", selected.getString("sequenceKey"));
        assertEquals("FEntity", meta.getJSONArray("Entrys").getJSONObject(1).getString("Key"));
        // Export server-generated discovery response for local browser debugging.
        Files.createDirectories(Path.of("target/detail-debug"));
        Files.writeString(Path.of("target/detail-debug/schema.json"), legacy.toJSONString());
    }

    @Test void scopedOutputIdsUseActualBusinessKeysForQueries() throws Exception {
        JSONObject params = params("ALL_ROWS");
        JSONObject schema = DetailSyncSupport.schema(metadata(), params);
        JSONObject query = DetailSyncSupport.query(schema, params, 0, 10);
        List<String> keys = Arrays.asList(query.getString("FieldKeys").split(","));
        assertTrue(keys.contains("FBillNo"));
        assertTrue(keys.contains("FOrgAmount"));
        assertTrue(keys.contains("FExpenseItemID.FName"));
        assertFalse(keys.contains("FEntity_FOrgAmount"));
        assertFalse(keys.contains("FEntity_FExpenseItemID.FName"));
        assertTrue(schema.getJSONArray("fields").toJSONString().contains("FEntity_FOrgAmount"));
        assertFalse(keys.contains("FEntity_FEntity_FEntryID"));
    }

    @Test void firstRowAcrossPagesAndRetry() throws Exception {
        JSONObject params = params("FIRST_ROW");
        JSONObject schema = DetailSyncSupport.schema(metadata(), params);
        List<List<Object>> rows = List.of(row(schema, 1, 11), row(schema, 1, 12), row(schema, 2, 21));
        JSONObject page1 = DetailSyncSupport.result(schema, params, rows, 0, 1, 100);
        assertEquals("FY1", page1.getJSONArray("records").getJSONObject(0).getJSONObject("fields").getString("FBillNo"));
        assertTrue(page1.getBooleanValue("hasMore"));
        JSONObject page2 = DetailSyncSupport.result(schema, params, rows, 1, 2, 100);
        JSONArray records = page2.getJSONArray("records");
        assertEquals("", records.getJSONObject(0).getJSONObject("fields").getString("FBillNo"));
        assertEquals("FY2", records.getJSONObject(1).getJSONObject("fields").getString("FBillNo"));
        assertFalse(page2.getBooleanValue("hasMore"));
        assertEquals(page2, DetailSyncSupport.result(schema, params, rows, 1, 2, 100));
        assertNotEquals(records.getJSONObject(0).getString("id"), records.getJSONObject(1).getString("id"));
        assertTrue(records.getJSONObject(0).getJSONObject("fields").toJSONString().contains("\"FBillNo\":\"\""));
    }

    @Test void modeDoesNotChangeIdentityAndLimitDoesNotInsertFakeRows() throws Exception {
        JSONObject first = params("FIRST_ROW"), all = params("ALL_ROWS");
        JSONObject schema = DetailSyncSupport.schema(metadata(), first);
        List<List<Object>> rows = List.of(row(schema, 1, 11), row(schema, 1, 12), row(schema, 2, 21));
        JSONObject a = DetailSyncSupport.result(schema, first, rows, 0, 2, 2);
        JSONObject b = DetailSyncSupport.result(schema, all, rows, 0, 2, 2);
        assertEquals(2, a.getJSONArray("records").size());
        assertFalse(a.getBooleanValue("hasMore"));
        assertEquals("FY1", b.getJSONArray("records").getJSONObject(1).getJSONObject("fields").getString("FBillNo"));
        assertEquals(a.getJSONArray("records").getJSONObject(1).getString("id"), b.getJSONArray("records").getJSONObject(1).getString("id"));
        assertEquals(0, DetailSyncSupport.result(schema, first, List.of(), 2, 0, 2).getJSONArray("records").size());
    }

    @Test void filteredFirstRowAndStableGroupedQuery() throws Exception {
        JSONObject params = params("FIRST_ROW");
        params.put("sortConfigs", JSONArray.parseArray("[{\"fieldId\":\"FEntity_FOrgAmount\",\"order\":\"desc\"}]"));
        params.put("filterConditions", JSONArray.parseArray("[{\"conditions\":[{\"fieldId\":\"FEntity_FOrgAmount\",\"operator\":\">\",\"value\":\"100\"}]}]"));
        JSONObject schema = DetailSyncSupport.schema(metadata(), params);
        JSONObject query = DetailSyncSupport.query(schema, params, 0, 3);
        assertEquals("FID asc , FOrgAmount desc , FEntity_FSEQ asc , FEntity_FEntryID asc", query.getString("OrderString"));
        assertTrue(query.getString("FilterString").contains("FEntity_FEntryID > 0"));
        assertTrue(query.getString("FilterString").contains("FOrgAmount > '100'"));
        JSONObject result = DetailSyncSupport.result(schema, params, List.of(row(schema, 1, 12)), 0, 2, 100);
        assertEquals("FY1", result.getJSONArray("records").getJSONObject(0).getJSONObject("fields").getString("FBillNo"));
    }

    @Test void newOperatorsPassDetailValidationAndTranslateFieldKeys() throws Exception {
        JSONObject params = params("FIRST_ROW");
        JSONObject schema = DetailSyncSupport.schema(metadata(), params);
        for (String op : List.of("包含", "不包含", "为空", "不为空")) {
            JSONObject condition = new JSONObject(); condition.put("fieldId", "FBillNo"); condition.put("operator", op); condition.put("value", "FY");
            JSONArray conditions = new JSONArray(); conditions.add(condition);
            JSONObject group = new JSONObject(); group.put("conditions", conditions);
            JSONArray groups = new JSONArray(); groups.add(group); params.put("filterConditions", groups);
            String filter = DetailSyncSupport.query(schema, params, 0, 3).getString("FilterString");
            assertTrue(filter.contains("FBillNo")); assertTrue(filter.contains("FEntity_FEntryID > 0"));
        }
        params.put("filterConditions", JSONArray.parseArray("[{\"conditions\":[{\"fieldId\":\"FEntity_FOrgAmount\",\"operator\":\"为空\"}]}]"));
        assertTrue(DetailSyncSupport.query(schema, params, 0, 3).getString("FilterString").contains("FOrgAmount is null"));
    }

    @Test void rejectStaleEntityAndFieldsAndUnsupportedModes() throws Exception {
        JSONObject params = params("FIRST_ROW");
        JSONObject meta = metadata();
        params.put("selectedEntryKey", "missing");
        assertThrows(IllegalArgumentException.class, () -> DetailSyncSupport.schema(meta, params));
        params.put("selectedEntryKey", "FEntity");
        params.put("headerFillMode", "invalid");
        assertThrows(IllegalArgumentException.class, () -> DetailSyncSupport.schema(meta, params));
        params.put("headerFillMode", "FIRST_ROW");
        JSONObject schema = DetailSyncSupport.schema(meta, params);
        params.put("sortConfigs", JSONArray.parseArray("[{\"fieldId\":\"OtherEntryField\",\"order\":\"asc\"}]"));
        assertThrows(IllegalArgumentException.class, () -> DetailSyncSupport.query(schema, params, 0, 10));
    }

    @Test void flexContainersAreExpandedIntoQueryableSubfields() throws Exception {
        JSONObject meta = metadata();
        JSONObject flex = JSONObject.parseObject("{\"Key\":\"FAuxPropId\",\"Name\":[{\"Key\":2052,\"Value\":\"辅助属性\"}],\"FlexFields\":[{\"Key\":\"FAUXPROPID__FF100001\",\"FieldName\":\"FF100001\",\"FieldType\":231,\"ElementType\":30,\"Name\":[{\"Key\":2052,\"Value\":\"版本号\"}]}]}");
        meta.getJSONArray("Entrys").getJSONObject(1).getJSONArray("Fields").add(flex);
        JSONObject params = params("FIRST_ROW");
        JSONObject schema = DetailSyncSupport.schema(meta, params);
        String keys = DetailSyncSupport.query(schema, params, 0, 2).getString("FieldKeys");
        assertTrue(keys.contains("FAuxPropId.FF100001.FNumber"));
        assertFalse(keys.contains("FAuxPropId.FName"));
        assertEquals("FAuxPropId", flex.getString("Key"));
    }

    @Test void businessErrorsAreNotTreatedAsRecordsEvenAtPageBoundary() throws Exception {
        JSONObject error = JSONObject.parseObject("{\"Result\":{\"ResponseStatus\":{\"IsSuccess\":false,\"Errors\":[{\"Message\":\"元数据中标识为FAuxPropId的字段不存在\"}]}}}");
        List<List<Object>> rows = List.of(List.of(error));
        var failure = assertThrows(IllegalArgumentException.class, () -> KingdeeQueryResponse.requireSuccess(rows));
        assertTrue(failure.getMessage().contains("FAuxPropId"));
        JSONObject params = params("FIRST_ROW");
        JSONObject schema = DetailSyncSupport.schema(metadata(), params);
        assertThrows(IllegalArgumentException.class, () -> DetailSyncSupport.result(schema, params, rows, 300, 300, 1000));
    }

    @Test void customEntityAndFieldKeysComeEntirelyFromMetadata() throws Exception {
        JSONObject meta = metadata();
        meta.put("Id", "CustomForm");
        meta.put("PkFieldName", "CustomHeadPk");
        JSONObject head = meta.getJSONArray("Entrys").getJSONObject(0);
        head.put("Key", "CustomHead");
        JSONObject entry = meta.getJSONArray("Entrys").getJSONObject(1);
        entry.put("Key", "CustomEntry");
        entry.put("ParentKey", "CustomHead");
        entry.put("EntryPkFieldName", "CustomEntryPk");
        entry.put("SeqFieldKey", "CustomSequence");
        entry.getJSONArray("Fields").getJSONObject(1).put("Key", "CustomAmount");
        JSONObject params = params("FIRST_ROW");
        params.put("selectedEntryKey", "CustomEntry");
        JSONObject schema = DetailSyncSupport.schema(meta, params);
        JSONObject query = DetailSyncSupport.query(schema, params, 0, 2);
        assertEquals("CustomForm", query.getString("FormId"));
        assertTrue(query.getString("FieldKeys").startsWith("CustomHeadPk,"));
        assertTrue(query.getString("FieldKeys").contains("CustomEntry_CustomEntryPk"));
        assertTrue(query.getString("FieldKeys").contains("CustomAmount"));
        assertTrue(query.getString("OrderString").contains("CustomEntry_CustomSequence"));
        assertEquals(59, schema.getIntValue("headerFieldCount"));
    }

    @Test void detailAndHeadShareAllDisplayFormats() throws Exception {
        JSONObject meta = metadata();
        JSONObject head = meta.getJSONArray("Entrys").getJSONObject(0);
        JSONObject entry = meta.getJSONArray("Entrys").getJSONObject(1);
        entry.put("Fields", JSONArray.parseArray(head.getJSONArray("Fields").toJSONString()));
        JSONObject schema = DetailSyncSupport.schema(meta, params("ALL_ROWS"));
        JSONArray fields = schema.getJSONArray("fields");
        int count = schema.getIntValue("headerFieldCount");
        Set<String> types = new HashSet<>();
        for (int i = 1; i < count; i++) {
            JSONObject a = fields.getJSONObject(i), b = fields.getJSONObject(count + i);
            assertEquals(a.getString("type"), b.getString("type"));
            assertEquals(a.getJSONObject("property"), b.getJSONObject("property"));
            types.add(b.getString("type"));
        }
        assertTrue(types.containsAll(Set.of("text", "date", "number", "singleSelect")));
        JSONObject booleanField = JSONObject.parseObject("{\"type\":\"singleSelect\",\"property\":{\"choices\":[{\"value\":\"true\",\"name\":\"是\"},{\"value\":\"false\",\"name\":\"否\"}]}}");
        assertEquals("是", ContoryUtil.getKingdeeFieldValue(booleanField, true));
        assertEquals("否", ContoryUtil.getKingdeeFieldValue(booleanField, false));
        JSONObject numericEnum = JSONObject.parseObject("{\"type\":\"singleSelect\",\"property\":{\"choices\":[{\"value\":\"1\",\"name\":\"选项一\"}]}}");
        assertEquals("选项一", ContoryUtil.getKingdeeFieldValue(numericEnum, 1));
    }

    @Test void onlySelectedEntityAndNoEntryOption() throws Exception {
        JSONObject meta = metadata();
        JSONObject another = JSONObject.parseObject(meta.getJSONArray("Entrys").getJSONObject(1).toJSONString());
        another.put("Key", "FSecondEntry");
        another.put("Fields", new JSONArray());
        meta.getJSONArray("Entrys").add(another);
        JSONObject schema = DetailSyncSupport.schema(meta, params("FIRST_ROW"));
        assertEquals(2, schema.getJSONArray("entryOptions").size());
        assertFalse(schema.getJSONArray("fields").toJSONString().contains("FSecondEntry"));
        meta.getJSONArray("Entrys").remove(2);
        meta.getJSONArray("Entrys").remove(1);
        assertTrue(DetailSyncSupport.schema(meta, new JSONObject()).getJSONArray("entryOptions").isEmpty());
    }

    @Test void multipleEntriesIncludeAllFieldsAndStableCombinationIds() throws Exception {
        JSONObject meta = metadata();
        JSONObject second = JSONObject.parseObject(meta.getJSONArray("Entrys").getJSONObject(1).toJSONString());
        second.put("Key", "FSecond");
        second.put("Fields", new JSONArray());
        meta.getJSONArray("Entrys").add(second);
        JSONObject params = params("FIRST_ROW");
        params.put("selectedEntryKeys", JSONArray.of("FSecond", "FEntity"));
        JSONObject schema = DetailSyncSupport.schema(meta, params);
        JSONArray fields = schema.getJSONArray("fields");
        int last = fields.size() - 1;
        assertEquals("FSecond_FEntryID", fields.getJSONObject(last).getString("id"));
        JSONObject query = DetailSyncSupport.query(schema, params, 0, 3);
        assertTrue(query.getString("FieldKeys").contains("FSecond_FEntryID"));
        assertTrue(query.getString("FilterString").contains("FEntity_FEntryID > 0 or FSecond_FEntryID > 0"));
        List<Object> a = row(schema, 1, 11), b = row(schema, 1, 11);
        a.set(last, 21L); b.set(last, 22L);
        JSONArray records = DetailSyncSupport.result(schema, params, List.of(a, b), 0, 2, 100).getJSONArray("records");
        assertNotEquals(records.getJSONObject(0).getString("id"), records.getJSONObject(1).getString("id"));
        assertEquals("", records.getJSONObject(1).getJSONObject("fields").getString("FBillNo"));
        params.put("selectedEntryKeys", JSONArray.of("FEntity", "FSecond"));
        assertEquals(schema, DetailSyncSupport.schema(meta, params));
        params.put("selectedEntryKeys", new JSONArray());
        assertFalse(DetailSyncSupport.schema(meta, params).containsKey("selectedEntryKey"));
    }

    @Test void unitLookupWithEmptyEnumMetadataUsesDisplayName() throws Exception {
        JSONObject meta = metadata();
        JSONObject unit = JSONObject.of("Key", "FUnitID", "LookUpObjectFormId", "BD_UNIT", "Extends", new JSONArray(),
                "Name", JSONArray.of(JSONObject.of("Key", 2052, "Value", "计量单位")));
        meta.getJSONArray("Entrys").getJSONObject(1).getJSONArray("Fields").add(unit);
        JSONObject schema = DetailSyncSupport.schema(meta, params("ALL_ROWS"));
        JSONObject converted = schema.getJSONArray("fields").getJSONObject(schema.getJSONArray("fields").size() - 1);
        assertEquals("FEntity_FUnitID.FName", converted.getString("id"));
        assertEquals("text", converted.getString("type"));
        assertEquals("箱", ContoryUtil.getKingdeeFieldValue(converted, "箱"));
        assertTrue(DetailSyncSupport.query(schema, params("ALL_ROWS"), 0, 10).getString("FieldKeys").contains("FUnitID.FName"));
    }
}
