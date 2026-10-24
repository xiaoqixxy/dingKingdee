package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;

class ExtensionDictionarySupportTest {
    private JSONObject hop(String group, String key) {
        return JSONObject.of("dictionaryId", "BOS_FORMGROUP", "fieldKey", key,
                "context", JSONObject.of("formId", "BD_MATERIAL", "groupFieldKey", group));
    }
    private JSONObject response(Object data) {
        return JSONObject.of("Result", JSONObject.of("ResponseStatus", JSONObject.of("IsSuccess", true), "NeedReturnData", data));
    }
    @Test void maintainedSchemaExposesFieldsAndParentReferenceWithNoBusinessNameSuffix() {
        JSONObject schema = ExtensionDictionarySupport.schema("BOS_FORMGROUP");
        assertEquals("FID", schema.getString("primaryKey"));
        assertEquals("FNumber", schema.getJSONArray("fields").getJSONObject(0).getString("id"));
        JSONObject parent = schema.getJSONArray("fields").stream().map(item -> (JSONObject) item)
                .filter(item -> "FParentId".equals(item.getString("key"))).findFirst().orElseThrow();
        assertEquals("FParentId.FNumber", parent.getString("id"));
        assertEquals("BOS_FORMGROUP", parent.getString("referenceDictionaryId"));
        schema.getJSONArray("fields").clear();
        assertFalse(ExtensionDictionarySupport.schema("BOS_FORMGROUP").getJSONArray("fields").isEmpty());
    }
    @Test void responseAcceptsArrayStringAndNullButPreservesBusinessAndFormatFailures() {
        JSONArray rows = JSONArray.of(JSONObject.of("FID", 1, "FNAME", "组名"));
        assertEquals(1, ExtensionDictionarySupport.response(response(rows.toJSONString()), "BOS_FORMGROUP").size());
        assertTrue(ExtensionDictionarySupport.response(response(null), "BOS_FORMGROUP").isEmpty());
        JSONObject error = JSONObject.of("Result", JSONObject.of("ResponseStatus", JSONObject.of("IsSuccess", false,
                "ErrorCode", "403", "Errors", JSONArray.of(JSONObject.of("Message", "没有权限")))));
        assertTrue(assertThrows(KingdeeQueryException.class, () -> ExtensionDictionarySupport.response(error, "BOS_FORMGROUP")).getMessage().contains("没有权限"));
        assertThrows(IllegalArgumentException.class, () -> ExtensionDictionarySupport.response(response(new JSONObject()), "BOS_FORMGROUP"));
        assertThrows(IllegalArgumentException.class, () -> ExtensionDictionarySupport.response(response(JSONArray.of(JSONObject.of("FNUMBER", "A"))), "BOS_FORMGROUP"));
    }
    @Test void columnsShareCachedRecordsButDifferentGroupContextsDoNotCollide() {
        Map<String, Map<String, Object>> cache = new HashMap<>(); AtomicInteger calls = new AtomicInteger();
        Function<JSONObject, List<List<Object>>> query = request -> {
            calls.incrementAndGet(); assertEquals("QueryGroupInfo", request.getString("queryType"));
            assertTrue(Set.of("1", "1,99").contains(request.getString("GroupPkIds"))); assertEquals("", request.getString("Ids"));
            return List.of(List.of(1, JSONObject.of("FID", 1, "FNAME", request.getString("GroupFieldKey"), "FNUMBER", "G001")));
        };
        List<Object> ids = Arrays.asList(1, 1.0, null, 0, 99);
        assertEquals(Arrays.asList("FMaterialGroup", "FMaterialGroup", null, null, null),
                ExtensionDictionarySupport.resolve(hop("FMaterialGroup", "FName"), ids, query, cache));
        assertEquals(Arrays.asList("G001", "G001", null, null, null),
                ExtensionDictionarySupport.resolve(hop("FMaterialGroup", "FNumber"), ids, request -> { throw new AssertionError(); }, cache));
        assertEquals(List.of("FSalGroup"), ExtensionDictionarySupport.resolve(hop("FSalGroup", "FName"), List.of(1), query, cache));
        assertEquals(2, calls.get());
    }
    @Test void batchingUsesAtMost200IdsAndDoesNotSwallowMalformedOrOutOfRangeResults() {
        List<Object> ids = new ArrayList<>(); for (int i = 1; i <= 205; i++) ids.add(i);
        AtomicInteger calls = new AtomicInteger();
        List<Object> result = ExtensionDictionarySupport.resolve(hop("FMaterialGroup", "FName"), ids, request -> {
            calls.incrementAndGet(); String[] batch = request.getString("GroupPkIds").split(","); assertTrue(batch.length <= 200);
            List<List<Object>> rows = new ArrayList<>(); for (String id : batch) rows.add(List.of(id, JSONObject.of("FID", id, "FNAME", "组" + id)));
            return rows;
        }, new HashMap<>());
        assertEquals(205, result.size()); assertEquals(2, calls.get());
        assertThrows(IllegalArgumentException.class, () -> ExtensionDictionarySupport.resolve(hop("FMaterialGroup", "FName"), List.of(1),
                request -> List.of(List.of(2, JSONObject.of("FID", 2, "FNAME", "错组"))), new HashMap<>()));
        assertThrows(IllegalArgumentException.class, () -> ExtensionDictionarySupport.resolve(hop("FMaterialGroup", "FName"), List.of(1),
                request -> List.of(List.of(1, JSONObject.of("FID", 1))), new HashMap<>()));
    }
}
