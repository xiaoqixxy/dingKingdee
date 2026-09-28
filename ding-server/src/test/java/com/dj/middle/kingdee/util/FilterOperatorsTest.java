package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FilterOperatorsTest {
    static String filter(String operator, Object value, String type) {
        JSONObject field = new JSONObject(); field.put("id", "FName"); field.put("type", type);
        JSONArray fields = new JSONArray(); fields.add(field);
        JSONObject schema = new JSONObject(); schema.put("Id", "TEST"); schema.put("fields", fields);
        JSONObject condition = new JSONObject(); condition.put("fieldId", "FName"); condition.put("operator", operator); condition.put("value", value);
        JSONArray conditions = new JSONArray(); conditions.add(condition);
        return ContoryUtil.kingdeeToKingdeeQuery(schema, 0, 10, conditions, null).getString("FilterString");
    }
    @Test void substringAndLiteralEscaping() {
        assertEquals("FName like '%abc%'", filter("包含", "abc", "text"));
        assertEquals("FName not like '%O''Brien[%][_][[]%'", filter("不包含", "O'Brien%_[", "text"));
        assertEquals("FName = 'O''Brien'", filter("=", "O'Brien", "text"));
        assertThrows(IllegalArgumentException.class, () -> filter("包含", "", "text"));
        assertThrows(IllegalArgumentException.class, () -> filter("or 1=1", "", "text"));
    }
    @Test void emptyTextIncludesNullAndEmptyStringButNumbersDoNotMatchZero() {
        assertEquals("(FName is null or FName = '')", filter("为空", "ignored", "text"));
        assertEquals("(FName is not null and FName <> '')", filter("不为空", null, "text"));
        assertEquals("FName is null", filter("为空", null, "number"));
        assertEquals("FName is not null", filter("不为空", null, "date"));
    }
    @Test void datesUseWholeDaysAndNumericInputsAreValidated() {
        assertEquals("(FName >= '2026-09-18' and FName < '2026-09-19')", filter("=", "2026-09-18", "date"));
        assertEquals("(FName < '2026-09-18' or FName >= '2026-09-19')", filter("!=", "2026-09-18", "date"));
        assertEquals("FName < '2026-10-01'", filter("<=", "2026-09-30", "date"));
        assertEquals("FName >= '2027-01-01'", filter(">", "2026-12-31", "date"));
        assertTrue(filter("=", "this_month", "date").contains(" and FName < '"));
        assertEquals("FName = 'this_month'", filter("=", "this_month", "text"));
        assertThrows(IllegalArgumentException.class, () -> filter("=", "NaN", "number"));
        assertEquals("FName = '0'", filter("=", "0", "number"));
        assertEquals("FName = 'false'", filter("=", "false", "boolean"));
        assertEquals("FName is null", filter("为空", "", "boolean"));
        assertEquals("FName = 'one'", filter("=", java.util.List.of("one"), "text"));
        assertThrows(IllegalArgumentException.class, () -> filter("=", java.util.List.of("one", "two"), "text"));
    }

}
