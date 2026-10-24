package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;

class ReferenceQuerySupportTest {
    private JSONObject schema() {
        return JSONObject.of("Id", "ORDER", "fields", JSONArray.of(
                JSONObject.of("id", "FID", "type", "text", "filterType", "number", "isPrimary", true),
                JSONObject.of("id", "Ref.FName", "type", "text", "name", "单位名称", "queryKey", "Ref.FUnit.FName", "referenceKey", "Ref", "referencePlan", JSONArray.of(
                        JSONObject.of("formId", "MATERIAL", "primaryKey", "MID", "fieldKey", "FUnit"),
                        JSONObject.of("formId", "UNIT", "primaryKey", "UID", "fieldKey", "FName")))));
    }
    private JSONObject condition(String op, Object value) { return JSONObject.of("fieldId", "Ref.FName", "operator", op, "value", value); }
    private JSONObject params(JSONObject... conditions) { return JSONObject.of("filterConditions", JSONArray.of(JSONObject.of("conditions", JSONArray.of((Object[]) conditions)))); }

    private Function<JSONObject, List<List<Object>>> query(List<List<Object>> source, AtomicInteger sourceCalls) {
        return request -> {
            String form = request.getString("FormId");
            if ("ORDER".equals(form)) {
                sourceCalls.incrementAndGet();
                assertEquals("FID,Ref", request.getString("FieldKeys"));
                assertFalse(request.getString("FilterString").contains("Ref.FUnit.FName"));
                int begin = Math.min(source.size(), request.getIntValue("StartRow"));
                return source.subList(begin, Math.min(source.size(), begin + request.getIntValue("Limit")));
            }
            String filter = request.getString("FilterString");
            List<List<Object>> rows = new ArrayList<>();
            if ("MATERIAL".equals(form)) {
                assertEquals("MID,FUnit", request.getString("FieldKeys"));
                if (filter.contains("'1'")) rows.add(List.of(1, 10));
                if (filter.contains("'2'")) rows.add(List.of(2, 20));
            } else if ("UNIT".equals(form)) {
                assertEquals("UID,FName", request.getString("FieldKeys"));
                if (filter.contains("'10'")) rows.add(List.of(10, "alpha"));
                if (filter.contains("'20'")) rows.add(List.of(20, "zeta"));
            } else fail("Unexpected form");
            return rows;
        };
    }

    @Test void resolvesHopsFiltersBeforePaginationAndPreservesAliasesAndColumnIds() throws Exception {
        List<List<Object>> source = new ArrayList<>();
        for (int i = 1; i <= 305; i++) source.add(List.of(i, i <= 300 ? 1 : 2));
        AtomicInteger calls = new AtomicInteger();
        JSONObject params = params(condition("包含", "zet"));
        params.put("sortConfigs", JSONArray.of(JSONObject.of("fieldId", "FID", "order", "desc")));
        List<List<Object>> rows = ReferenceQuerySupport.collect(schema(), params, query(source, calls));
        assertEquals(2, calls.get()); assertEquals(5, rows.size());
        assertEquals(List.of(305, "zeta"), rows.get(0));
        JSONObject page = ReferenceQuerySupport.page(schema(), params, rows, 0, 2, 4);
        assertTrue(page.getBooleanValue("hasMore"));
        assertEquals("zeta", page.getJSONArray("records").getJSONObject(0).getJSONObject("fields").getString("Ref.FName"));
        JSONObject last = ReferenceQuerySupport.page(schema(), params, rows, 2, 2, 4);
        assertEquals(2, last.getJSONArray("records").size()); assertFalse(last.getBooleanValue("hasMore"));
    }

    @Test void orGroupsDeduplicateRowsAndNullReferencesRemainEmpty() {
        List<List<Object>> source = List.of(List.of(1, 1), List.of(2, 2), Arrays.asList(3, null));
        JSONObject params = params(condition("包含", "alpha"));
        params.getJSONArray("filterConditions").add(JSONObject.of("conditions", JSONArray.of(condition("不为空", ""))));
        List<List<Object>> rows = ReferenceQuerySupport.collect(schema(), params, query(source, new AtomicInteger()));
        assertEquals(2, rows.size());
        List<List<Object>> empty = ReferenceQuerySupport.collect(schema(), params(condition("为空", "")), query(source, new AtomicInteger()));
        assertEquals(1, empty.size()); assertNull(empty.get(0).get(1));
    }

    @Test void configuredReferenceSortPrecedesPagination() {
        List<List<Object>> source = List.of(List.of(1, 1), List.of(2, 2));
        JSONObject params = new JSONObject();
        params.put("sortConfigs", JSONArray.of(JSONObject.of("fieldId", "Ref.FName", "order", "desc")));
        List<List<Object>> rows = ReferenceQuerySupport.collect(schema(), params, query(source, new AtomicInteger()));
        assertEquals(List.of(2, "zeta"), rows.get(0));
    }

    @Test void referenceBusinessErrorsAreNotTreatedAsMissingValues() {
        JSONObject schema = schema();
        assertThrows(KingdeeQueryException.class, () -> ReferenceQuerySupport.collect(schema, new JSONObject(), request -> {
            if ("ORDER".equals(request.getString("FormId"))) return List.of(List.of(1, 1));
            return List.of(List.of(Map.of("Result", Map.of("ResponseStatus", Map.of("IsSuccess", false, "ErrorCode", "500", "Errors", List.of(Map.of("Message", "query failed")))))));
        }));
    }

    @Test void defaultDisplayChangesResolveOnlyTheRequestedPageAndHonorLimits() throws Exception {
        JSONObject schema = schema();
        assertTrue(ReferenceQuerySupport.canStream(schema, new JSONObject()));
        assertFalse(ReferenceQuerySupport.canStream(schema, params(condition("包含", "a"))));
        AtomicInteger sourceCalls = new AtomicInteger(), limits = new AtomicInteger();
        Function<JSONObject, List<List<Object>>> base = query(List.of(), sourceCalls);
        JSONObject page = ReferenceQuerySupport.streamPage(schema, new JSONObject(), 1, 2, 3, request -> {
            if ("ORDER".equals(request.getString("FormId"))) {
                assertEquals(1, request.getIntValue("StartRow"));
                assertEquals(3, request.getIntValue("Limit"));
                assertEquals("FID,Ref", request.getString("FieldKeys"));
                return List.of(List.of(2, 2), List.of(3, 1), List.of(4, 2));
            }
            return base.apply(request);
        }, limits::incrementAndGet);
        assertEquals(2, page.getJSONArray("records").size());
        assertEquals("zeta", page.getJSONArray("records").getJSONObject(0).getJSONObject("fields").getString("Ref.FName"));
        assertFalse(page.getBooleanValue("hasMore"));
        assertEquals(1, limits.get());
    }

    @Test void materializedDetailGroupsKeepSequenceAndSuppressHeadersAcrossPages() throws Exception {
        JSONObject schema = schema();
        schema.getJSONArray("fields").add(1, JSONObject.of("id", "Entry_FEntryID", "type", "text", "filterType", "number"));
        schema.put("headerFieldCount", 1); schema.put("selectedEntryKey", "Entry");
        schema.put("entryPrimaryIds", JSONArray.of("Entry_FEntryID")); schema.put("sequenceKeys", JSONArray.of("Entry_FSeq"));
        JSONObject params = params(condition("不为空", ""));
        params.put("headerFillMode", "FIRST_ROW");
        Function<JSONObject, List<List<Object>>> base = query(List.of(), new AtomicInteger());
        List<List<Object>> rows = ReferenceQuerySupport.collect(schema, params, request -> {
            if ("ORDER".equals(request.getString("FormId"))) {
                assertEquals("FID,Entry_FEntryID,Ref,Entry_FSeq", request.getString("FieldKeys"));
                return List.of(List.of(1, 11, 2, 2), List.of(1, 12, 1, 1));
            }
            return base.apply(request);
        });
        assertEquals(List.of(1, 12, "alpha"), rows.get(0));
        assertEquals(3, rows.get(0).size());
        JSONObject page = ReferenceQuerySupport.page(schema, params, rows, 1, 1, 100);
        assertEquals("", page.getJSONArray("records").getJSONObject(0).getJSONObject("fields").getString("FID"));
        assertFalse(page.getBooleanValue("hasMore"));
    }
}
