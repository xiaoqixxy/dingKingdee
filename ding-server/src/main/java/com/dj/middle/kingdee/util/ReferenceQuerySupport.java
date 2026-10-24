package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dj.middle.kingdee.constant.DateConverterUtil;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;

/** Executes reference hops separately: ExecuteBillQuery does not support arbitrary nested joins. */
public final class ReferenceQuerySupport {
    private ReferenceQuerySupport() {}
    public static boolean required(JSONObject schema) {
        return schema.getJSONArray("fields").stream().anyMatch(item -> ((JSONObject) item).containsKey("referencePlan"));
    }

    private static Set<String> configuredIds(JSONObject schema) {
        Set<String> ids = new HashSet<>();
        for (Object item : schema.getJSONArray("fields")) {
            JSONObject field = (JSONObject) item;
            if (field.containsKey("referencePlan")) ids.add(field.getString("id"));
        }
        return ids;
    }

    public static boolean canStream(JSONObject schema, JSONObject params) {
        Set<String> ids = configuredIds(schema);
        for (JSONArray group : groups(params.getJSONArray("filterConditions")))
            for (Object item : group) if (ids.contains(((JSONObject) item).getString("fieldId"))) return false;
        JSONArray sorts = params.getJSONArray("sortConfigs");
        if (sorts != null) for (Object item : sorts) if (ids.contains(((JSONObject) item).getString("fieldId"))) return false;
        return true;
    }

    private static JSONObject projection(JSONObject schema) {
        JSONObject result = JSONObject.parseObject(schema.toJSONString());
        for (Object item : result.getJSONArray("fields")) {
            JSONObject field = (JSONObject) item;
            if (field.containsKey("referencePlan")) field.put("queryKey", field.getString("referenceKey"));
        }
        return result;
    }

    public static JSONObject streamPage(JSONObject schema, JSONObject params, int offset, int limit, int syncLimit,
            Function<JSONObject, List<List<Object>>> query, Runnable onLimitReached) throws java.text.ParseException {
        int pageSize = Math.min(limit, Math.max(0, syncLimit - offset));
        boolean detail = schema.containsKey("selectedEntryKey");
        JSONObject projection = projection(schema);
        JSONObject request = detail ? DetailSyncSupport.query(projection, params, Math.max(0, offset - (offset > 0 ? 1 : 0)), pageSize + (offset > 0 ? 1 : 0) + 1)
                : ContoryUtil.kingdeeToKingdeeQuery(projection, offset, pageSize + 1, params.getJSONArray("filterConditions"), params.getJSONArray("sortConfigs"));
        List<List<Object>> rows = new ArrayList<>();
        for (List<Object> row : execute(query, request)) {
            if (row.size() != schema.getJSONArray("fields").size()) throw new IllegalArgumentException("金蝶结果字段数量不一致");
            rows.add(new ArrayList<>(row));
        }
        resolve(schema.getJSONArray("fields"), rows, query, new HashMap<>());
        int preceding = detail && offset > 0 ? 1 : 0;
        if (rows.size() > pageSize + preceding && (long) offset + pageSize >= syncLimit) onLimitReached.run();
        if (detail) return DetailSyncSupport.result(schema, params, rows, offset, pageSize, syncLimit);
        int count = Math.min(pageSize, rows.size());
        JSONObject result = ContoryUtil.kingdeeToDingResult(schema, rows.subList(0, count), offset, Integer.MAX_VALUE, Integer.MAX_VALUE);
        boolean more = rows.size() > count && (long) offset + count < syncLimit;
        result.put("hasMore", more);
        result.put("nextToken", more ? offset + count : "");
        return result;
    }

    private static String id(Object value) {
        if (ContoryUtil.isEmptyValue(value)) return "";
        String text = value.toString();
        try { return new BigDecimal(text).stripTrailingZeros().toPlainString(); }
        catch (NumberFormatException ignored) { return text; }
    }

    private static List<List<Object>> execute(Function<JSONObject, List<List<Object>>> query, JSONObject request) {
        List<List<Object>> rows = query.apply(request);
        KingdeeQueryResponse.requireSuccess(rows);
        return rows;
    }

    private static void resolve(JSONArray fields, List<List<Object>> rows,
            Function<JSONObject, List<List<Object>>> query, Map<String, Map<String, Object>> cache) {
        for (int column = 0; column < fields.size(); column++) {
            JSONArray plan = fields.getJSONObject(column).getJSONArray("referencePlan");
            if (plan == null) continue;
            List<Object> values = new ArrayList<>();
            for (List<Object> row : rows) values.add(row.get(column));
            for (Object item : plan) {
                JSONObject hop = (JSONObject) item;
                if (hop.getString("dictionaryId") != null) {
                    values = ExtensionDictionarySupport.resolve(hop, values, query, cache);
                    continue;
                }
                String pk = hop.getString("primaryKey"), key = hop.getString("fieldKey");
                Map<String, Object> lookup = cache.computeIfAbsent(hop.toJSONString(), unused -> new HashMap<>());
                Set<String> pending = new LinkedHashSet<>();
                for (Object value : values) {
                    String identity = id(value);
                    if (!identity.isBlank() && !"0".equals(identity) && !lookup.containsKey(identity)) pending.add(identity);
                }
                List<String> ids = new ArrayList<>(pending);
                for (int start = 0; start < ids.size(); start += 200) {
                    List<String> chunk = ids.subList(start, Math.min(ids.size(), start + 200));
                    String filter = pk + " in (" + String.join(",", chunk.stream()
                            .map(value -> "'" + value.replace("'", "''") + "'").toList()) + ")";
                    List<List<Object>> found = execute(query, JSONObject.of("FormId", hop.getString("formId"),
                            "FieldKeys", pk + "," + key, "FilterString", filter, "StartRow", 0, "Limit", 2000));
                    Set<String> returned = new HashSet<>();
                    for (List<Object> row : found) {
                        if (row.size() != 2) throw new IllegalArgumentException("引用对象查询字段数量不一致");
                        String identity = id(row.get(0));
                        if (!returned.add(identity) && !Objects.equals(lookup.get(identity), row.get(1)))
                            throw new IllegalArgumentException("引用对象同一主键返回多个显示值，请选择单值字段");
                        lookup.put(identity, row.get(1));
                    }
                    for (String identity : chunk) lookup.putIfAbsent(identity, null);
                }
                for (int i = 0; i < values.size(); i++) values.set(i, lookup.get(id(values.get(i))));
            }
            for (int row = 0; row < rows.size(); row++) rows.get(row).set(column, values.get(row));
        }
    }

    private static LocalDate date(Object value) {
        try { return DateConverterUtil.stringToDate(value.toString()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); }
        catch (Exception e) { throw new IllegalArgumentException("引用字段包含无效日期", e); }
    }

    private static boolean matches(JSONObject field, Object actual, JSONObject condition) {
        String op = condition.getString("operator");
        boolean empty = ContoryUtil.isEmptyValue(actual);
        if ("为空".equals(op)) return empty;
        if ("不为空".equals(op)) return !empty;
        if (empty) return false;
        Object expected = condition.get("value");
        if ("包含于".equals(op)) {
            List<?> choices = expected instanceof List ? (List<?>) expected : Arrays.asList(String.valueOf(expected).split(","));
            return choices.stream().anyMatch(value -> Objects.equals(actual.toString(), String.valueOf(value)));
        }
        if (expected instanceof List) expected = ((List<?>) expected).isEmpty() ? "" : ((List<?>) expected).get(0);
        String text = String.valueOf(expected);
        if ("包含".equals(op)) return actual.toString().contains(text);
        if ("不包含".equals(op)) return !actual.toString().contains(text);
        int compared;
        if ("date".equals(field.getString("type"))) {
            LocalDate actualDate = date(actual), today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
            if ("=".equals(op) && Set.of("last_7_days", "last_30_days", "this_week", "this_month", "this_year").contains(text)) {
                LocalDate start = switch (text) {
                    case "last_7_days" -> today.minusDays(6);
                    case "last_30_days" -> today.minusDays(29);
                    case "this_week" -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    case "this_month" -> today.withDayOfMonth(1);
                    default -> today.withDayOfYear(1);
                };
                LocalDate end = switch (text) {
                    case "this_week" -> start.plusDays(6);
                    case "this_month" -> start.plusMonths(1).minusDays(1);
                    case "this_year" -> start.plusYears(1).minusDays(1);
                    default -> today;
                };
                return !actualDate.isBefore(start) && !actualDate.isAfter(end);
            }
            compared = actualDate.compareTo(LocalDate.parse(text));
        } else compared = compare(field, actual, expected);
        return switch (op) {
            case "=" -> compared == 0;
            case "!=", "<>" -> compared != 0;
            case ">" -> compared > 0;
            case ">=" -> compared >= 0;
            case "<" -> compared < 0;
            case "<=" -> compared <= 0;
            default -> throw new IllegalArgumentException("不支持的引用字段筛选操作符");
        };
    }

    private static int compare(JSONObject field, Object a, Object b) {
        if (ContoryUtil.isEmptyValue(a)) a = null;
        if (ContoryUtil.isEmptyValue(b)) b = null;
        if (a == b) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        if ("number".equals(field.getString("type")) || "number".equals(field.getString("filterType")))
            return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
        if ("date".equals(field.getString("type"))) {
            try { return DateConverterUtil.stringToDate(a.toString()).compareTo(DateConverterUtil.stringToDate(b.toString())); }
            catch (Exception e) { throw new IllegalArgumentException("引用字段包含无效日期", e); }
        }
        return a.toString().compareTo(b.toString());
    }

    private static List<JSONArray> groups(JSONArray filters) {
        if (filters == null || filters.isEmpty()) return List.of(new JSONArray());
        if (!filters.getJSONObject(0).containsKey("conditions")) return List.of(filters);
        List<JSONArray> groups = new ArrayList<>();
        for (Object item : filters) groups.add(((JSONObject) item).getJSONArray("conditions"));
        return groups;
    }

    public static JSONObject page(JSONObject schema, JSONObject params, List<List<Object>> rows,
            int offset, int limit, int syncLimit) throws java.text.ParseException {
        int pageSize = Math.min(limit, Math.max(0, syncLimit - offset));
        if (schema.containsKey("selectedEntryKey")) {
            int begin = Math.min(rows.size(), Math.max(0, offset - (offset > 0 ? 1 : 0)));
            int end = (int) Math.min(rows.size(), (long) offset + pageSize + 1);
            return DetailSyncSupport.result(schema, params, rows.subList(begin, Math.max(begin, end)), offset, pageSize, syncLimit);
        }
        int begin = Math.min(offset, rows.size());
        int end = (int) Math.min(rows.size(), (long) offset + pageSize);
        JSONObject result = ContoryUtil.kingdeeToDingResult(schema, rows.subList(begin, Math.max(begin, end)), offset, Integer.MAX_VALUE, Integer.MAX_VALUE);
        boolean more = end < rows.size() && end < syncLimit;
        result.put("hasMore", more);
        result.put("nextToken", more ? end : "");
        return result;
    }

    /** Materialize only explicitly configured reference syncs so filtering/sorting precede pagination. */
    public static List<List<Object>> collect(JSONObject schema, JSONObject params,
            Function<JSONObject, List<List<Object>>> query) {
        JSONArray fields = schema.getJSONArray("fields");
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) columns.put(fields.getJSONObject(i).getString("id"), i);
        JSONArray filters = params.getJSONArray("filterConditions"), sorts = params.getJSONArray("sortConfigs");
        for (JSONArray group : groups(filters)) for (Object item : group)
            if (!columns.containsKey(((JSONObject) item).getString("fieldId"))) throw new IllegalArgumentException("筛选字段已失效");
        if (sorts != null) for (Object item : sorts) {
            JSONObject sort = (JSONObject) item;
            if (!columns.containsKey(sort.getString("fieldId")) || !Set.of("asc", "desc").contains(sort.getString("order")))
                throw new IllegalArgumentException("排序字段或方向已失效");
        }
        // Reuse existing validation for operators, values and date shortcuts without executing a nested join.
        ContoryUtil.kingdeeToKingdeeQuery(schema, 0, 1, filters, sorts);
        JSONObject projection = projection(schema);
        JSONArray projectedFields = projection.getJSONArray("fields");
        JSONArray sequences = schema.getJSONArray("sequenceKeys");
        if (sequences != null) for (int i = 0; i < sequences.size(); i++) {
            String key = sequences.getString(i);
            if (!columns.containsKey(key)) {
                columns.put(key, projectedFields.size());
                projectedFields.add(JSONObject.of("id", key, "queryKey", key, "type", "number"));
            }
        }
        Map<String, Map<String, Object>> cache = new HashMap<>();
        Map<String, List<Object>> unique = new LinkedHashMap<>();
        boolean detail = schema.containsKey("selectedEntryKey");
        List<Integer> identityColumns = new ArrayList<>(); identityColumns.add(0);
        if (detail) for (Object key : schema.getJSONArray("entryPrimaryIds")) identityColumns.add(columns.get(String.valueOf(key)));
        for (JSONArray group : groups(filters)) {
            JSONArray normal = new JSONArray(), deferred = new JSONArray();
            for (Object item : group) {
                JSONObject condition = (JSONObject) item;
                (fields.getJSONObject(columns.get(condition.getString("fieldId"))).containsKey("referencePlan") ? deferred : normal).add(condition);
            }
            JSONObject sourceParams = JSONObject.of("filterConditions", normal, "sortConfigs", new JSONArray());
            for (int offset = 0; ; offset += 300) {
                JSONObject request = detail ? DetailSyncSupport.query(projection, sourceParams, offset, 300)
                        : ContoryUtil.kingdeeToKingdeeQuery(projection, offset, 300, normal, JSONArray.of(JSONObject.of("fieldId", fields.getJSONObject(0).getString("id"), "order", "asc")));
                List<List<Object>> raw = execute(query, request);
                List<List<Object>> rows = new ArrayList<>();
                for (List<Object> row : raw) {
                    if (row.size() != projectedFields.size()) throw new IllegalArgumentException("金蝶结果字段数量不一致");
                    rows.add(new ArrayList<>(row));
                }
                resolve(fields, rows, query, cache);
                for (List<Object> row : rows) {
                    boolean accept = true;
                    for (Object item : deferred) {
                        JSONObject condition = (JSONObject) item; int column = columns.get(condition.getString("fieldId"));
                        if (!matches(fields.getJSONObject(column), row.get(column), condition)) { accept = false; break; }
                    }
                    if (accept) {
                        JSONArray identity = new JSONArray();
                        for (int column : identityColumns) identity.add(id(row.get(column)));
                        unique.putIfAbsent(identity.toJSONString(), row);
                    }
                }
                if (raw.size() < 300) break;
            }
        }
        List<List<Object>> rows = new ArrayList<>(unique.values());
        List<JSONObject> order = new ArrayList<>();
        int headCount = schema.getIntValue("headerFieldCount");
        if (sorts != null) for (Object item : sorts) {
            JSONObject sort = (JSONObject) item;
            if (!detail || columns.get(sort.getString("fieldId")) < headCount) order.add(sort);
        }
        if (detail) {
            order.add(JSONObject.of("fieldId", fields.getJSONObject(0).getString("id"), "order", "asc"));
            if (sorts != null) for (Object item : sorts) {
                JSONObject sort = (JSONObject) item;
                if (columns.get(sort.getString("fieldId")) >= headCount) order.add(sort);
            }
        }
        if (detail) {
            if (sequences != null) for (Object key : sequences) order.add(JSONObject.of("fieldId", key, "order", "asc"));
            for (Object key : schema.getJSONArray("entryPrimaryIds")) order.add(JSONObject.of("fieldId", key, "order", "asc"));
        }
        // Stable ordering also applies when separate OR groups contribute different source rows.
        rows.sort((a, b) -> {
            for (JSONObject sort : order) {
                int column = columns.get(sort.getString("fieldId"));
                int result = compare(projectedFields.getJSONObject(column), a.get(column), b.get(column));
                if (result != 0) return "desc".equals(sort.getString("order")) ? -result : result;
            }
            return 0;
        });
        if (projectedFields.size() > fields.size())
            for (int i = 0; i < rows.size(); i++) rows.set(i, new ArrayList<>(rows.get(i).subList(0, fields.size())));
        return rows;
    }
}
