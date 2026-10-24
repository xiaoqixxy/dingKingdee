package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/** Explicit schemas for APIs whose return fields are absent from QueryBusinessInfo. */
public final class ExtensionDictionarySupport {
    private ExtensionDictionarySupport() {}
    private static final JSONObject DICTIONARIES = load();

    private static JSONObject load() {
        try (InputStream input = ExtensionDictionarySupport.class.getResourceAsStream("/kingdee/extension-dictionaries.json")) {
            if (input == null) throw new IllegalStateException("扩展数据字典文件不存在");
            JSONObject definitions = JSONObject.parseObject(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            for (String id : definitions.keySet()) {
                JSONObject definition = definitions.getJSONObject(id);
                if (!"QueryGroupInfo".equals(definition.getString("api"))) throw new IllegalStateException("扩展字典查询接口不受支持");
                Set<String> keys = new HashSet<>();
                for (Object item : definition.getJSONArray("fields")) {
                    JSONObject field = (JSONObject) item;
                    if (!keys.add(field.getString("key")) || field.getString("responseKey") == null
                            || !Set.of("text", "number", "date").contains(field.getString("type")))
                        throw new IllegalStateException("扩展字典字段定义无效");
                }
                if (!keys.contains(definition.getString("primaryKey")) || !keys.contains(definition.getString("defaultField")))
                    throw new IllegalStateException("扩展字典缺少主键或默认字段");
            }
            return definitions;
        } catch (Exception e) { throw new IllegalStateException("加载扩展数据字典失败", e); }
    }

    public static boolean contains(String id) { return id != null && DICTIONARIES.containsKey(id); }
    public static String defaultField(String id) { return definition(id).getString("defaultField"); }
    private static JSONObject definition(String id) {
        JSONObject result = DICTIONARIES.getJSONObject(id);
        if (result == null) throw new IllegalArgumentException("扩展数据字典不存在");
        return result;
    }
    private static JSONObject field(String id, String key) {
        for (Object item : definition(id).getJSONArray("fields"))
            if (Objects.equals(key, ((JSONObject) item).getString("key"))) return (JSONObject) item;
        throw new IllegalArgumentException("扩展字典字段不存在: " + key);
    }

    public static JSONObject schema(String id) {
        JSONObject definition = definition(id);
        JSONArray fields = new JSONArray();
        for (Object item : definition.getJSONArray("fields")) {
            JSONObject field = JSONObject.parseObject(((JSONObject) item).toJSONString());
            String reference = field.getString("referenceFormId");
            field.put("id", field.getString("key") + (reference == null ? "" : "." + defaultField(reference)));
            if (reference != null) {
                field.put("referenceKey", field.getString("key"));
                field.put("referenceDictionaryId", reference);
            }
            fields.add(field);
        }
        return JSONObject.of("formId", id, "primaryKey", definition.getString("primaryKey"), "fields", fields,
                "dictionaryVersion", definition.getIntValue("version"));
    }

    public static String identity(Object value) {
        if (ContoryUtil.isEmptyValue(value)) return "";
        try { return new BigDecimal(value.toString()).stripTrailingZeros().toPlainString(); }
        catch (NumberFormatException ignored) { return value.toString(); }
    }

    /** The adapter returns [group ID, complete group record] for request-local cross-column caching. */
    public static List<List<Object>> response(JSONObject response, String dictionaryId) {
        JSONObject result = response == null ? null : response.getJSONObject("Result");
        JSONObject status = result == null ? null : result.getJSONObject("ResponseStatus");
        if (status == null || !Boolean.TRUE.equals(status.getBoolean("IsSuccess"))) {
            String message = "金蝶分组查询失败";
            if (status != null && status.getJSONArray("Errors") != null)
                for (Object item : status.getJSONArray("Errors")) message += "；" + ((JSONObject) item).getString("Message");
            throw new KingdeeQueryException(status == null ? "" : status.getString("ErrorCode"), message);
        }
        Object data = result.get("NeedReturnData");
        if (data instanceof String) data = com.alibaba.fastjson2.JSON.parse((String) data);
        if (data == null) return Collections.emptyList();
        if (!(data instanceof JSONArray)) throw new IllegalArgumentException("金蝶分组查询结果格式异常");
        String pk = field(dictionaryId, definition(dictionaryId).getString("primaryKey")).getString("responseKey");
        List<List<Object>> rows = new ArrayList<>();
        for (Object item : (JSONArray) data) {
            if (!(item instanceof JSONObject) || ((JSONObject) item).get(pk) == null)
                throw new IllegalArgumentException("金蝶分组查询结果缺少主键");
            JSONObject row = (JSONObject) item;
            rows.add(Arrays.asList(row.get(pk), row));
        }
        return rows;
    }

    public static List<Object> resolve(JSONObject hop, List<Object> values,
            Function<JSONObject, List<List<Object>>> query, Map<String, Map<String, Object>> cache) {
        String dictionaryId = hop.getString("dictionaryId");
        JSONObject context = hop.getJSONObject("context");
        if (context == null || context.getString("formId") == null || context.getString("groupFieldKey") == null)
            throw new IllegalArgumentException("分组查询缺少所属表单和分组字段");
        String cacheKey = "dictionary:" + dictionaryId + ":" + context.toJSONString();
        Map<String, Object> records = cache.computeIfAbsent(cacheKey, unused -> new HashMap<>());
        Set<String> pending = new LinkedHashSet<>();
        for (Object value : values) {
            String id = identity(value);
            if (!id.isBlank() && !"0".equals(id) && !records.containsKey(id)) pending.add(id);
        }
        List<String> ids = new ArrayList<>(pending);
        for (int start = 0; start < ids.size(); start += 200) {
            List<String> batch = ids.subList(start, Math.min(ids.size(), start + 200));
            JSONObject request = JSONObject.of("queryType", definition(dictionaryId).getString("api"), "dictionaryId", dictionaryId,
                    "FormId", context.getString("formId"), "GroupFieldKey", context.getString("groupFieldKey"),
                    "GroupPkIds", String.join(",", batch), "Ids", "");
            List<List<Object>> found = query.apply(request);
            if (found == null) throw new IllegalArgumentException("金蝶分组查询返回空响应");
            for (List<Object> row : found) {
                if (row.size() != 2 || !(row.get(1) instanceof JSONObject)) throw new IllegalArgumentException("分组查询结果格式异常");
                String id = identity(row.get(0));
                if (!batch.contains(id)) throw new IllegalArgumentException("分组查询返回了请求范围外的记录");
                Object prior = records.putIfAbsent(id, row.get(1));
                if (prior != null && !prior.equals(row.get(1))) throw new IllegalArgumentException("分组内码返回多个不同值");
            }
            for (String id : batch) records.putIfAbsent(id, null);
        }
        String responseKey = field(dictionaryId, hop.getString("fieldKey")).getString("responseKey");
        List<Object> result = new ArrayList<>();
        for (Object value : values) {
            JSONObject record = (JSONObject) records.get(identity(value));
            if (record != null && !record.containsKey(responseKey)) throw new IllegalArgumentException("分组响应缺少字典字段: " + responseKey);
            result.add(record == null ? null : record.get(responseKey));
        }
        return result;
    }
}
