package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.util.*;
import java.util.function.Function;

/** 依据当前元数据解析显示路径，同时保持输出列标识稳定。 基于元数据白名单应用字段别名、额外输出列和多级引用。 */
public final class FieldConfigurationSupport {
    /** 工具类仅提供静态方法，禁止外部实例化。 */
    private FieldConfigurationSupport() {}

    /** 返回引用对象的可选字段；扩展对象使用项目维护的字典结构。 */
    public static JSONObject referenceSchema(JSONObject metadata) {
        // 普通元数据未声明扩展属性时，优先使用项目维护的字典，避免伪造不可查询的主键。
        if (ExtensionDictionarySupport.contains(metadata.getString("Id")))
            return ExtensionDictionarySupport.schema(metadata.getString("Id"));
        JSONObject expanded = DetailSyncSupport.expandFlexFields(metadata);
        JSONArray fields = new JSONArray();
        // 表头和子表头可能声明同名字段，按业务字段键去重，保留首次出现的定义。
        Set<String> seen = new HashSet<>();
        String primaryKey = expanded.getString("PkFieldName");
        // 空结构占位对象不应自动补出可查询主键，分组对象改由扩展字典处理。
        if (primaryKey != null && !primaryKey.isBlank() && !expanded.getJSONArray("Entrys").isEmpty()) {
            fields.add(JSONObject.of("id", primaryKey, "key", primaryKey, "name", "内码", "type", "text", "filterType", "number"));
            seen.add(primaryKey);
        }
        // 引用显示值只允许单值的表头、子表头属性，不把多行明细当作一个单元格值。
        for (Object item : expanded.getJSONArray("Entrys")) {
            JSONObject entity = (JSONObject) item;
            if (!Set.of("HeadEntity", "SubHeadEntity").contains(entity.getString("EntityType"))) continue;
            JSONArray converted = ContoryUtil.kingdeeToDing(expanded, entity.getString("Key")).getJSONArray("fields");
            // 转换器额外生成的主键不属于当前实体声明的字段，遍历时需要跳过。
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

    /** 在原字段结构的副本上应用别名、新增列和经过元数据校验的引用路径。 */
    public static JSONObject apply(JSONObject original, JSONObject params, Function<String, JSONObject> loadMetadata) {
        JSONArray configs = params.getJSONArray("fieldConfigs");
        // 未启用字段配置时直接复用旧结构，确保历史同步路径和默认查询不变。
        if (configs == null || configs.isEmpty()) return original;
        // 复制结构后再改名或追加列，避免污染同一份基础元数据与其他配置。
        JSONObject schema = JSONObject.parseObject(original.toJSONString());
        Map<String, JSONObject> fields = new HashMap<>();
        for (Object item : schema.getJSONArray("fields")) {
            JSONObject field = (JSONObject) item;
            fields.put(field.getString("id"), JSONObject.parseObject(field.toJSONString()));
        }
        // 一次配置解析内复用引用对象结构；配置标识集合用于防止同一输出列重复定义。
        Map<String, JSONObject> cache = new HashMap<>();
        Set<String> configured = new HashSet<>();
        for (int i = 0; i < configs.size(); i++) {
            JSONObject config = configs.getJSONObject(i);
            String id = config.getString("fieldId");
            JSONObject source = fields.get(id);
            String outputId = config.getString("outputFieldId");
            boolean additional = outputId != null && !outputId.isBlank();
            // 同一来源可以输出多列，来源标识负责定位字段，输出标识负责保持各列独立。
            String identity = additional ? outputId : id;
            if (source == null || !configured.add(identity)) throw new IllegalArgumentException("字段配置已失效或重复，请重新配置");
            JSONObject field = null;
            JSONArray outputFields = schema.getJSONArray("fields");
            if (additional) {
                // 新增列必须来自引用字段，且只允许服务约定的标识前缀及受限字符。
                String prefix = id + "__ref_";
                if (source.getString("referenceFormId") == null || !outputId.startsWith(prefix)
                        || !outputId.substring(prefix.length()).matches("[A-Za-z0-9_]{1,64}") || fields.containsKey(outputId))
                    throw new IllegalArgumentException("新增引用列标识无效");
                field = JSONObject.parseObject(source.toJSONString());
                field.put("queryKey", source.getString("queryKey") == null ? id : source.getString("queryKey"));
                field.put("id", outputId);
                field.put("isPrimary", false);
                // 把新增输出列放在原来源列之后，并越过此前已插入的同来源列。
                int sourceIndex = -1;
                for (int j = 0; j < outputFields.size(); j++) if (id.equals(outputFields.getJSONObject(j).getString("id"))) { sourceIndex = j; break; }
                int position = sourceIndex + 1;
                while (position < outputFields.size() && id.equals(outputFields.getJSONObject(position).getString("sourceFieldId"))) position++;
                field.put("sourceFieldId", id);
                outputFields.add(position, field);
                // 表头新增列会改变明细起始边界，必须同步修正以免把明细误作表头清空。
                if (schema.containsKey("headerFieldCount") && sourceIndex < schema.getIntValue("headerFieldCount"))
                    schema.put("headerFieldCount", schema.getIntValue("headerFieldCount") + 1);
            } else {
                for (Object item : outputFields) if (id.equals(((JSONObject) item).getString("id"))) { field = (JSONObject) item; break; }
            }
            // 用户别名仅改变显示名称，不改变输出列标识或源查询字段。
            String alias = config.getString("alias");
            if (alias != null && !alias.isBlank()) {
                if (alias.trim().length() > 100) throw new IllegalArgumentException("字段别名不能超过100个字符");
                field.put("name", alias.trim());
            }
            JSONArray path = config.getJSONArray("referencePath");
            if (path == null || path.isEmpty()) continue;
            // 后续会为扩展引用补充默认叶子，先复制路径，避免修改调用方保存的配置。
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
                // 每一层都重新校验目标对象和字段白名单，不能直接拼接用户提供的点号路径执行。
                String dictionaryId = ExtensionDictionarySupport.contains(form) ? form : null;
                JSONObject metadata = cache.computeIfAbsent(form, key -> ExtensionDictionarySupport.contains(key)
                        ? ExtensionDictionarySupport.schema(key) : referenceSchema(loadMetadata.apply(key)));
                String key = path.getString(level);
                // 旧客户端自动选择过错误合成的分组FID，历史配置继续按编码兼容；
                // 带字典版本的新配置可以显式选择真实内码。
                // 兼容旧界面自动补出的分组FID；新版本明确选择的内码才按真实FID读取。
                if ("BOS_FORMGROUP".equals(dictionaryId) && config.getIntValue("dictionaryVersion") < 1 && "FID".equals(key))
                    key = ExtensionDictionarySupport.defaultField(dictionaryId);
                leaf = null;
                for (Object item : metadata.getJSONArray("fields")) {
                    JSONObject candidate = (JSONObject) item;
                    if (Objects.equals(key, candidate.getString("key"))) { leaf = candidate; break; }
                }
                if (leaf == null) throw new IllegalArgumentException("引用字段已失效，请重新选择: " + key);
                // 选到扩展引用但未继续选属性时，追加字典默认显示字段，形成可执行的叶子。
                if (level == path.size() - 1 && leaf.getString("referenceDictionaryId") != null) {
                    if (path.size() >= 10) throw new IllegalArgumentException("引用层级不能超过10级");
                    path.add(ExtensionDictionarySupport.defaultField(leaf.getString("referenceDictionaryId")));
                }
                boolean last = level == path.size() - 1;
                String primaryKey = metadata.getString("primaryKey");
                if (primaryKey == null || primaryKey.isBlank()) throw new IllegalArgumentException("引用对象缺少主键");
                // 引用计划逐跳记录主键与取值字段，执行器据此分批补查，不依赖任意深度联表。
                JSONObject hop = JSONObject.of("formId", form, "primaryKey", primaryKey,
                        "fieldKey", last ? leaf.getString("id") : leaf.getString("referenceKey"));
                // 分组内码只有在所属表单及分组字段上下文中才有意义，计划内必须保存这组信息。
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
            // 输出类型跟随最终叶子，清除原来源字段的格式配置后再复制叶子的类型属性。
            field.put("type", leaf.getString("type"));
            field.remove("property");
            field.remove("filterType");
            if (leaf.containsKey("property")) field.put("property", leaf.get("property"));
            if (leaf.containsKey("filterType")) field.put("filterType", leaf.get("filterType"));
        }
        return schema;
    }
}
