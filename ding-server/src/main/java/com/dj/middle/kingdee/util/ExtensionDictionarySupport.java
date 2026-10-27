package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/** Explicit schemas for APIs whose return fields are absent from QueryBusinessInfo. 维护普通元数据缺失的扩展对象字段，并适配专用接口取值。 */
public final class ExtensionDictionarySupport {
    /** 工具类仅提供静态方法，禁止外部实例化。 */
    private ExtensionDictionarySupport() {}
    /** 启动时加载并校验完成的扩展对象字典集合。 */
    private static final JSONObject DICTIONARIES = load();

    /** 从类路径读取扩展字典，启动时校验接口、字段唯一性、类型、主键和默认属性。 */
    private static JSONObject load() {
        try (InputStream input = ExtensionDictionarySupport.class.getResourceAsStream("/kingdee/extension-dictionaries.json")) {
            if (input == null) throw new IllegalStateException("扩展数据字典文件不存在");
            // 字典随服务包加载，字段与响应键的映射来自固定资源，不接受前端任意定义接口。
            JSONObject definitions = JSONObject.parseObject(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            for (String id : definitions.keySet()) {
                JSONObject definition = definitions.getJSONObject(id);
                if (!"QueryGroupInfo".equals(definition.getString("api"))) throw new IllegalStateException("扩展字典查询接口不受支持");
                // 逐个校验字段唯一性与类型；默认字段和主键必须确实存在于声明中。
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

    /** 判断业务对象是否由项目扩展字典维护。 */
    public static boolean contains(String id) { return id != null && DICTIONARIES.containsKey(id); }
    /** 获取扩展字典声明的默认显示字段。 */
    public static String defaultField(String id) { return definition(id).getString("defaultField"); }
    /** 读取指定扩展字典定义，不允许使用未注册对象。 */
    private static JSONObject definition(String id) {
        JSONObject result = DICTIONARIES.getJSONObject(id);
        if (result == null) throw new IllegalArgumentException("扩展数据字典不存在");
        return result;
    }
    /** 根据字段键读取扩展字典中的白名单定义。 */
    private static JSONObject field(String id, String key) {
        for (Object item : definition(id).getJSONArray("fields"))
            if (Objects.equals(key, ((JSONObject) item).getString("key"))) return (JSONObject) item;
        throw new IllegalArgumentException("扩展字典字段不存在: " + key);
    }

    /** 复制扩展字典为引用下拉结构，为父级等引用属性附上继续选择的目标字典。 */
    public static JSONObject schema(String id) {
        JSONObject definition = definition(id);
        JSONArray fields = new JSONArray();
        for (Object item : definition.getJSONArray("fields")) {
            // 向外返回字段副本，避免前端结构转换或调用方修改全局字典定义。
            JSONObject field = JSONObject.parseObject(((JSONObject) item).toJSONString());
            String reference = field.getString("referenceFormId");
            field.put("id", field.getString("key") + (reference == null ? "" : "." + defaultField(reference)));
            // 父级分组等属性保留原引用内码，并提供下一层默认显示路径供下拉继续展开。
            if (reference != null) {
                field.put("referenceKey", field.getString("key"));
                field.put("referenceDictionaryId", reference);
            }
            fields.add(field);
        }
        return JSONObject.of("formId", id, "primaryKey", definition.getString("primaryKey"), "fields", fields,
                "dictionaryVersion", definition.getIntValue("version"));
    }

    /** 统一引用内码的数字表示，空白内码不触发查询。 */
    public static String identity(Object value) {
        if (ContoryUtil.isEmptyValue(value)) return "";
        try { return new BigDecimal(value.toString()).stripTrailingZeros().toPlainString(); }
        catch (NumberFormatException ignored) { return value.toString(); }
    }

    /** 适配器返回分组内码与完整分组记录，供一次请求内多个输出列共享缓存。 校验分组接口业务状态，将完整记录适配为内码与记录的二元列表。 */
    public static List<List<Object>> response(JSONObject response, String dictionaryId) {
        JSONObject result = response == null ? null : response.getJSONObject("Result");
        JSONObject status = result == null ? null : result.getJSONObject("ResponseStatus");
        if (status == null || !Boolean.TRUE.equals(status.getBoolean("IsSuccess"))) {
            String message = "金蝶分组查询失败";
            if (status != null && status.getJSONArray("Errors") != null)
                for (Object item : status.getJSONArray("Errors")) message += "；" + ((JSONObject) item).getString("Message");
            throw new KingdeeQueryException(status == null ? "" : status.getString("ErrorCode"), message);
        }
        // 兼容接口把结果直接返回为数组，或把数组再次编码为JSON字符串的两种情况。
        Object data = result.get("NeedReturnData");
        if (data instanceof String) data = com.alibaba.fastjson2.JSON.parse((String) data);
        // 无分组记录属于正常空结果；结构损坏和业务失败不能静默降级为空。
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

    /** 按所属表单及分组字段分批取值，缓存完整分组记录并按字典响应字段映射输出。 */
    public static List<Object> resolve(JSONObject hop, List<Object> values,
            Function<JSONObject, List<List<Object>>> query, Map<String, Map<String, Object>> cache) {
        String dictionaryId = hop.getString("dictionaryId");
        JSONObject context = hop.getJSONObject("context");
        if (context == null || context.getString("formId") == null || context.getString("groupFieldKey") == null)
            throw new IllegalArgumentException("分组查询缺少所属表单和分组字段");
        // 缓存按字典、所属表单和分组字段隔离，防止不同分组中相同内码相互覆盖。
        String cacheKey = "dictionary:" + dictionaryId + ":" + context.toJSONString();
        Map<String, Object> records = cache.computeIfAbsent(cacheKey, unused -> new HashMap<>());
        // 去重后仅补查未缓存的有效内码；空值与0不应触发无约束的全量分组读取。
        Set<String> pending = new LinkedHashSet<>();
        for (Object value : values) {
            String id = identity(value);
            if (!id.isBlank() && !"0".equals(id) && !records.containsKey(id)) pending.add(id);
        }
        List<String> ids = new ArrayList<>(pending);
        // 每批最多200个内码；使用GroupPkIds且清空Ids，使接口明确按分组内码查找。
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
                // 同一内码若返回冲突的记录就中止，避免不稳定结果随机覆盖。
                Object prior = records.putIfAbsent(id, row.get(1));
                if (prior != null && !prior.equals(row.get(1))) throw new IllegalArgumentException("分组内码返回多个不同值");
            }
            // 不存在的内码也作空结果缓存，同一次同步中其他列无需重复请求。
            for (String id : batch) records.putIfAbsent(id, null);
        }
        // 缓存保存完整记录，名称、编码、描述等多个输出列共用同一响应，再按字典映射各自取值。
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
