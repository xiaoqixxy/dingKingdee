package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dj.middle.kingdee.constant.DateConverterUtil;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;

/** 逐层执行引用查询，不能假定金蝶支持任意深度的嵌套联表。 分层批量读取引用值，按需执行后置筛选、排序与分页。 */
public final class ReferenceQuerySupport {
    /** 工具类仅提供静态方法，禁止外部实例化。 */
    private ReferenceQuerySupport() {}
    /** 判断当前字段结构是否包含需要额外解析的引用路径。 */
    public static boolean required(JSONObject schema) {
        return schema.getJSONArray("fields").stream().anyMatch(item -> ((JSONObject) item).containsKey("referencePlan"));
    }

    /** 收集需要后置引用解析的输出列标识。 */
    private static Set<String> configuredIds(JSONObject schema) {
        Set<String> ids = new HashSet<>();
        for (Object item : schema.getJSONArray("fields")) {
            JSONObject field = (JSONObject) item;
            if (field.containsKey("referencePlan")) ids.add(field.getString("id"));
        }
        return ids;
    }

    /** 判断筛选和排序是否依赖引用值，决定能否仅补查当前页。 */
    public static boolean canStream(JSONObject schema, JSONObject params) {
        // 只有引用值参与筛选或排序时才需要读取候选集；否则继续按页读取，控制调用量。
        Set<String> ids = configuredIds(schema);
        for (JSONArray group : groups(params.getJSONArray("filterConditions")))
            for (Object item : group) if (ids.contains(((JSONObject) item).getString("fieldId"))) return false;
        JSONArray sorts = params.getJSONArray("sortConfigs");
        if (sorts != null) for (Object item : sorts) if (ids.contains(((JSONObject) item).getString("fieldId"))) return false;
        return true;
    }

    /** 复制字段结构，将引用输出列投影为源引用内码。 */
    private static JSONObject projection(JSONObject schema) {
        JSONObject result = JSONObject.parseObject(schema.toJSONString());
        for (Object item : result.getJSONArray("fields")) {
            JSONObject field = (JSONObject) item;
            if (field.containsKey("referencePlan")) field.put("queryKey", field.getString("referenceKey"));
        }
        return result;
    }

    /** 保留原分页查询，仅补查本页及分页衔接所需的引用值。 */
    public static JSONObject streamPage(JSONObject schema, JSONObject params, int offset, int limit, int syncLimit,
            Function<JSONObject, List<List<Object>>> query, Runnable onLimitReached) throws java.text.ParseException {
        // 权益上限按输出记录计数，本页大小不能越过剩余可同步数量。
        int pageSize = Math.min(limit, Math.max(0, syncLimit - offset));
        boolean detail = schema.containsKey("selectedEntryKey");
        JSONObject projection = projection(schema);
        // 明细页向前多取一行用于判断跨页单据衔接，并向后多取一行判断是否还有下一页。
        JSONObject request = detail ? DetailSyncSupport.query(projection, params, Math.max(0, offset - (offset > 0 ? 1 : 0)), pageSize + (offset > 0 ? 1 : 0) + 1)
                : ContoryUtil.kingdeeToKingdeeQuery(projection, offset, pageSize + 1, params.getJSONArray("filterConditions"), params.getJSONArray("sortConfigs"));
        List<List<Object>> rows = new ArrayList<>();
        for (List<Object> row : execute(query, request)) {
            if (row.size() != schema.getJSONArray("fields").size()) throw new IllegalArgumentException("金蝶结果字段数量不一致");
            rows.add(new ArrayList<>(row));
        }
        // 逐列逐层解析引用值；普通对象分批查询，扩展对象调用专用字典适配器并共享请求级缓存。
        // 源查询先取得引用内码，再逐跳补充显示值；记录顺序和列位置保持不变。
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

    /** 规范化引用内码的数字表示，将纯空白视为空引用。 */
    private static String id(Object value) {
        if (ContoryUtil.isEmptyValue(value)) return "";
        String text = value.toString();
        try { return new BigDecimal(text).stripTrailingZeros().toPlainString(); }
        catch (NumberFormatException ignored) { return text; }
    }

    /** 执行单据查询并识别金蝶嵌套业务错误。 */
    private static List<List<Object>> execute(Function<JSONObject, List<List<Object>>> query, JSONObject request) {
        List<List<Object>> rows = query.apply(request);
        KingdeeQueryResponse.requireSuccess(rows);
        return rows;
    }

    /** 逐列逐层解析引用值；普通对象分批查询，扩展对象调用专用字典适配器并共享请求级缓存。 */
    private static void resolve(JSONArray fields, List<List<Object>> rows,
            Function<JSONObject, List<List<Object>>> query, Map<String, Map<String, Object>> cache) {
        // 每列按自身引用计划执行，结果仍写回原列，新增输出列因而不会覆盖来源列。
        for (int column = 0; column < fields.size(); column++) {
            JSONArray plan = fields.getJSONObject(column).getJSONArray("referencePlan");
            if (plan == null) continue;
            List<Object> values = new ArrayList<>();
            for (List<Object> row : rows) values.add(row.get(column));
            for (Object item : plan) {
                JSONObject hop = (JSONObject) item;
                // 扩展对象使用专用接口及上下文缓存，不能对通用分组对象执行单据查询。
                if (hop.getString("dictionaryId") != null) {
                    values = ExtensionDictionarySupport.resolve(hop, values, query, cache);
                    continue;
                }
                String pk = hop.getString("primaryKey"), key = hop.getString("fieldKey");
                // 普通引用按完整跳转计划缓存；不同目标对象或取值字段不会共用错误的缓存。
                Map<String, Object> lookup = cache.computeIfAbsent(hop.toJSONString(), unused -> new HashMap<>());
                Set<String> pending = new LinkedHashSet<>();
                for (Object value : values) {
                    String identity = id(value);
                    if (!identity.isBlank() && !"0".equals(identity) && !lookup.containsKey(identity)) pending.add(identity);
                }
                List<String> ids = new ArrayList<>(pending);
                // 引用内码分批查询并转义引号，避免请求过大或破坏筛选字面量。
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
                        // 主键对应多个不同显示值通常意味着选到了多值属性，应明确拒绝而不是随意取一条。
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

    /** 把引用字段的日期值解析为用于比较的本地日期。 */
    private static LocalDate date(Object value) {
        try { return DateConverterUtil.stringToDate(value.toString()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); }
        catch (Exception e) { throw new IllegalArgumentException("引用字段包含无效日期", e); }
    }

    /** 按字段类型在解析后的引用值上计算筛选结果。 */
    private static boolean matches(JSONObject field, Object actual, JSONObject condition) {
        String op = condition.getString("operator");
        // 筛选中的空值口径与输出转换一致，纯空格不能被视作有内容的字段。
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

    /** 以空值、数字、日期或文本语义比较值，纯空白与空值保持一致。 */
    private static int compare(JSONObject field, Object a, Object b) {
        // 排序也先统一空白和空值，防止把空格解析为数字或日期。
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

    /** 统一旧扁平筛选与条件组结构；无条件时生成一个无约束组。 */
    private static List<JSONArray> groups(JSONArray filters) {
        if (filters == null || filters.isEmpty()) return List.of(new JSONArray());
        if (!filters.getJSONObject(0).containsKey("conditions")) return List.of(filters);
        List<JSONArray> groups = new ArrayList<>();
        for (Object item : filters) groups.add(((JSONObject) item).getJSONArray("conditions"));
        return groups;
    }

    /** 对已经解析并筛选排序的记录分页，保持明细表头填充规则。 */
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

    /** 仅对显式配置且依赖引用值的同步读取候选集，确保筛选排序发生在分页之前。 分批读取候选记录，解析引用后执行筛选、去重和排序，最后由调用方分页。 */
    public static List<List<Object>> collect(JSONObject schema, JSONObject params,
            Function<JSONObject, List<List<Object>>> query) {
        JSONArray fields = schema.getJSONArray("fields");
        // 建立稳定输出列到物理列位置的索引，同时拒绝失效的筛选排序配置。
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
        // 只复用操作符、取值和日期快捷项的校验，此处不执行嵌套路径查询。
        ContoryUtil.kingdeeToKingdeeQuery(schema, 0, 1, filters, sorts);
        JSONObject projection = projection(schema);
        // 为明细的稳定排序补上必要序号列，这些辅助列只用于内部计算。
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
        // 多个或者条件组可能返回同一记录，按单据及明细主键去重并保留首次出现顺序。
        Map<String, List<Object>> unique = new LinkedHashMap<>();
        boolean detail = schema.containsKey("selectedEntryKey");
        List<Integer> identityColumns = new ArrayList<>(); identityColumns.add(0);
        if (detail) for (Object key : schema.getJSONArray("entryPrimaryIds")) identityColumns.add(columns.get(String.valueOf(key)));
        for (JSONArray group : groups(filters)) {
            // 普通条件下推给金蝶；依赖配置后引用值的条件留到引用补查完成再计算。
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
                // 逐列逐层解析引用值；普通对象分批查询，扩展对象调用专用字典适配器并共享请求级缓存。
                // 先解析引用再判断延后条件，确保结果总量与后续分页基于真实显示值。
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
        // 先排序后分页；明细模式仍以单据分组，明细排序只在各单据内部生效。
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
        // 不同或者条件组贡献的记录合并后，同样应用稳定排序。
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
