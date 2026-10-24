package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.util.*;
import java.util.function.Function;

/** Resolves display paths against live metadata while keeping output column IDs stable. */
public final class FieldConfigurationSupport {
    private FieldConfigurationSupport() {}

    public static JSONObject referenceSchema(JSONObject metadata) {
        if (ExtensionDictionarySupport.contains(metadata.getString("Id")))
            return ExtensionDictionarySupport.schema(metadata.getString("Id"));
        JSONObject expanded = DetailSyncSupport.expandFlexFields(metadata);
        JSONArray fields = new JSONArray();
        Set<String> seen = new HashSet<>();
        String primaryKey = expanded.getString("PkFieldName");
        // An empty placeholder (notably BOS_FORMGROUP) has no queryable primary key.
        if (primaryKey != null && !primaryKey.isBlank() && !expanded.getJSONArray("Entrys").isEmpty()) {
            fields.add(JSONObject.of("id", primaryKey, "key", primaryKey, "name", "内码", "type", "text", "filterType", "number"));
            seen.add(primaryKey);
        }
        for (Object item : expanded.getJSONArray("Entrys")) {
            JSONObject entity = (JSONObject) item;
            if (!Set.of("HeadEntity", "SubHeadEntity").contains(entity.getString("EntityType"))) continue;
            JSONArray converted = ContoryUtil.kingdeeToDing(expanded, entity.getString("Key")).getJSONArray("fields");
            // The synthetic primary key is not part of this entity's declared fields.
            for (int i = 1; i < converted.size(); i++) {
                JSONObject field = converted.getJSONObject(i);
                String key = field.getString("referenceKey");
                if (key == null) key = field.getString("id");
                if (!seen.add(key)) continue;
                field.put("key", key);
                fields.add(field);
            }
        }
        return JSONObject.of("formId", metadata.getString("Id"), "primaryKey", metadata.getString("PkFieldName"), "fields", fields);
    }

    public static JSONObject apply(JSONObject original, JSONObject params, Function<String, JSONObject> loadMetadata) {
        JSONArray configs = params.getJSONArray("fieldConfigs");
        if (configs == null || configs.isEmpty()) return original;
        JSONObject schema = JSONObject.parseObject(original.toJSONString());
        Map<String, JSONObject> fields = new HashMap<>();
        for (Object item : schema.getJSONArray("fields")) {
            JSONObject field = (JSONObject) item;
            fields.put(field.getString("id"), JSONObject.parseObject(field.toJSONString()));
        }
        Map<String, JSONObject> cache = new HashMap<>();
        Set<String> configured = new HashSet<>();
        for (int i = 0; i < configs.size(); i++) {
            JSONObject config = configs.getJSONObject(i);
            String id = config.getString("fieldId");
            JSONObject source = fields.get(id);
            String outputId = config.getString("outputFieldId");
            boolean additional = outputId != null && !outputId.isBlank();
            String identity = additional ? outputId : id;
            if (source == null || !configured.add(identity)) throw new IllegalArgumentException("字段配置已失效或重复，请重新配置");
            JSONObject field = null;
            JSONArray outputFields = schema.getJSONArray("fields");
            if (additional) {
                String prefix = id + "__ref_";
                if (source.getString("referenceFormId") == null || !outputId.startsWith(prefix)
                        || !outputId.substring(prefix.length()).matches("[A-Za-z0-9_]{1,64}") || fields.containsKey(outputId))
                    throw new IllegalArgumentException("新增引用列标识无效");
                field = JSONObject.parseObject(source.toJSONString());
                field.put("queryKey", source.getString("queryKey") == null ? id : source.getString("queryKey"));
                field.put("id", outputId);
                field.put("isPrimary", false);
                int sourceIndex = -1;
                for (int j = 0; j < outputFields.size(); j++) if (id.equals(outputFields.getJSONObject(j).getString("id"))) { sourceIndex = j; break; }
                int position = sourceIndex + 1;
                while (position < outputFields.size() && id.equals(outputFields.getJSONObject(position).getString("sourceFieldId"))) position++;
                field.put("sourceFieldId", id);
                outputFields.add(position, field);
                if (schema.containsKey("headerFieldCount") && sourceIndex < schema.getIntValue("headerFieldCount"))
                    schema.put("headerFieldCount", schema.getIntValue("headerFieldCount") + 1);
            } else {
                for (Object item : outputFields) if (id.equals(((JSONObject) item).getString("id"))) { field = (JSONObject) item; break; }
            }
            String alias = config.getString("alias");
            if (alias != null && !alias.isBlank()) {
                if (alias.trim().length() > 100) throw new IllegalArgumentException("字段别名不能超过100个字符");
                field.put("name", alias.trim());
            }
            JSONArray path = config.getJSONArray("referencePath");
            if (path == null || path.isEmpty()) continue;
            path = JSONArray.parseArray(path.toJSONString());
            if (path.size() > 10) throw new IllegalArgumentException("引用层级不能超过10级");
            String form = field.getString("referenceFormId");
            String queryKey = field.getString("referenceKey");
            if (form == null || queryKey == null) throw new IllegalArgumentException("该字段不是可配置的引用字段");
            JSONObject leaf = null;
            JSONArray plan = new JSONArray();
            JSONObject context = field.getJSONObject("referenceContext");
            for (int level = 0; level < path.size(); level++) {
                if (form == null) throw new IllegalArgumentException("引用路径经过了非引用字段");
                String dictionaryId = ExtensionDictionarySupport.contains(form) ? form : null;
                JSONObject metadata = cache.computeIfAbsent(form, key -> ExtensionDictionarySupport.contains(key)
                        ? ExtensionDictionarySupport.schema(key) : referenceSchema(loadMetadata.apply(key)));
                String key = path.getString(level);
                // The old client auto-selected the synthetic group FID. Keep that historical
                // configuration on codes; versioned dictionary choices may select the real ID.
                if ("BOS_FORMGROUP".equals(dictionaryId) && config.getIntValue("dictionaryVersion") < 1 && "FID".equals(key))
                    key = ExtensionDictionarySupport.defaultField(dictionaryId);
                leaf = null;
                for (Object item : metadata.getJSONArray("fields")) {
                    JSONObject candidate = (JSONObject) item;
                    if (Objects.equals(key, candidate.getString("key"))) { leaf = candidate; break; }
                }
                if (leaf == null) throw new IllegalArgumentException("引用字段已失效，请重新选择: " + key);
                if (level == path.size() - 1 && leaf.getString("referenceDictionaryId") != null) {
                    if (path.size() >= 10) throw new IllegalArgumentException("引用层级不能超过10级");
                    path.add(ExtensionDictionarySupport.defaultField(leaf.getString("referenceDictionaryId")));
                }
                boolean last = level == path.size() - 1;
                String primaryKey = metadata.getString("primaryKey");
                if (primaryKey == null || primaryKey.isBlank()) throw new IllegalArgumentException("引用对象缺少主键");
                JSONObject hop = JSONObject.of("formId", form, "primaryKey", primaryKey,
                        "fieldKey", last ? leaf.getString("id") : leaf.getString("referenceKey"));
                if (dictionaryId != null) {
                    if (context == null) throw new IllegalArgumentException("扩展引用缺少所属表单与字段信息");
                    hop.put("dictionaryId", dictionaryId);
                    hop.put("context", JSONObject.parseObject(context.toJSONString()));
                }
                plan.add(hop);
                queryKey += "." + (last ? leaf.getString("id") : leaf.getString("referenceKey"));
                if (last) break;
                if (leaf.getJSONObject("referenceContext") != null) context = leaf.getJSONObject("referenceContext");
                form = leaf.getString("referenceFormId");
                if (!last && leaf.getString("referenceKey") == null)
                    throw new IllegalArgumentException("引用路径经过了非引用字段");
            }
            field.put("queryKey", queryKey);
            field.put("referencePlan", plan);
            field.put("type", leaf.getString("type"));
            field.remove("property");
            field.remove("filterType");
            if (leaf.containsKey("property")) field.put("property", leaf.get("property"));
            if (leaf.containsKey("filterType")) field.put("filterType", leaf.get("filterType"));
        }
        return schema;
    }
}
