package com.dj.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import java.text.ParseException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Selected entry entities share a bill query; header suppression is applied after filtering. */
public final class DetailSyncSupport {
    private DetailSyncSupport() {}

    private static String name(JSONObject entity) {
        JSONArray names = entity.getJSONArray("Name");
        if (names != null) {
            for (int i = 0; i < names.size(); i++) {
                if ("2052".equals(names.getJSONObject(i).getString("Key")))
                    return names.getJSONObject(i).getString("Value");
            }
        }
        return entity.getString("Key");
    }

    public static String mode(JSONObject params) {
        String mode = params.getString("headerFillMode");
        if (mode == null || mode.isBlank()) return "FIRST_ROW";
        if (!"FIRST_ROW".equals(mode) && !"ALL_ROWS".equals(mode))
            throw new IllegalArgumentException("无效的表头同步方式");
        return mode;
    }

    public static JSONObject schema(JSONObject metadata, JSONObject params) {
        metadata = expandFlexFields(metadata);
        JSONObject result = ContoryUtil.kingdeeToDing(metadata);
        JSONArray headFields = result.getJSONArray("fields");
        JSONArray options = new JSONArray();
        JSONArray requested = params.getJSONArray("selectedEntryKeys");
        Set<String> selectedKeys = new LinkedHashSet<>();
        if (requested != null) for (int i = 0; i < requested.size(); i++) selectedKeys.add(requested.getString(i));
        else {
            String legacy = params.getString("selectedEntryKey");
            if (legacy != null && !legacy.isBlank()) selectedKeys.add(legacy);
        }
        List<JSONObject> selectedOptions = new ArrayList<>();
        JSONArray entities = metadata.getJSONArray("Entrys");
        Set<String> headEntityKeys = new HashSet<>();
        for (int i = 0; i < entities.size(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            if ("HeadEntity".equals(entity.getString("EntityType"))) headEntityKeys.add(entity.getString("Key"));
        }
        for (int i = 0; i < entities.size(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            if (!"EntryEntity".equals(entity.getString("EntityType"))) continue;
            JSONObject option = new JSONObject();
            String key = entity.getString("Key");
            option.put("key", key);
            option.put("name", name(entity));
            // Reuse the established type conversion without changing the source metadata.
            JSONObject copy = JSONObject.parseObject(metadata.toJSONString());
            String pk = entity.getString("EntryPkFieldName");
            String parent = entity.getString("ParentKey");
            boolean supported = pk != null && !pk.isBlank()
                    && (parent == null || parent.isBlank() || headEntityKeys.contains(parent));
            option.put("supported", supported);
            option.put("reason", supported ? "" : "缺少明细主键或属于多层子明细，暂不支持");
            copy.put("PkFieldName", key + "_" + pk);
            JSONArray fields = ContoryUtil.kingdeeToDing(copy, key).getJSONArray("fields");
            fields.getJSONObject(0).put("name", name(entity) + "·明细内码");
            for (int j = 1; j < fields.size(); j++) {
                JSONObject field = fields.getJSONObject(j);
                // Output IDs are entity-scoped; ExecuteBillQuery uses business field keys without that prefix.
                field.put("queryKey", field.getString("id"));
                field.put("id", key + "_" + field.getString("id"));
                field.put("name", name(entity) + "·" + field.getString("name"));
            }
            option.put("fields", fields);
            String seq = entity.getString("SeqFieldKey");
            option.put("sequenceKey", seq == null || seq.isBlank() ? null : key + "_" + seq);
            options.add(option);
            if (selectedKeys.contains(key)) selectedOptions.add(option);
        }
        result.put("entryOptions", options);
        result.put("headFields", JSONArray.parseArray(headFields.toJSONString()));
        if (selectedKeys.isEmpty()) return result;
        mode(params);
        if (selectedOptions.size() != selectedKeys.size()) throw new IllegalArgumentException("所选明细表不存在，请重新选择");
        result.put("headerFieldCount", headFields.size());
        result.put("selectedEntryKey", selectedOptions.get(0).getString("key"));
        JSONArray keys = new JSONArray();
        JSONArray primaryIds = new JSONArray();
        JSONArray sequences = new JSONArray();
        headFields.getJSONObject(0).put("isPrimary", false);
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < headFields.size(); i++) ids.add(headFields.getJSONObject(i).getString("id"));
        for (JSONObject option : selectedOptions) {
            if (!option.getBooleanValue("supported")) throw new IllegalArgumentException(option.getString("reason"));
            keys.add(option.getString("key"));
            if (option.getString("sequenceKey") != null) sequences.add(option.getString("sequenceKey"));
            JSONArray entryFields = option.getJSONArray("fields");
            primaryIds.add(entryFields.getJSONObject(0).getString("id"));
            for (int i = 0; i < entryFields.size(); i++) {
                JSONObject field = entryFields.getJSONObject(i);
                if (!ids.add(field.getString("id"))) throw new IllegalArgumentException("表头与明细字段标识冲突: " + field.getString("id"));
                if (option != selectedOptions.get(0)) field.put("isPrimary", false);
                headFields.add(field);
            }
        }
        result.put("selectedEntryKeys", keys);
        result.put("entryPrimaryIds", primaryIds);
        result.put("sequenceKeys", sequences);
        result.put("sequenceKey", selectedOptions.get(0).getString("sequenceKey"));
        return result;
    }

    static JSONObject expandFlexFields(JSONObject metadata) {
        JSONObject copy = JSONObject.parseObject(metadata.toJSONString());
        JSONArray entities = copy.getJSONArray("Entrys");
        for (int i = 0; i < entities.size(); i++) {
            JSONObject entity = entities.getJSONObject(i);
            JSONArray expanded = new JSONArray();
            JSONArray fields = entity.getJSONArray("Fields");
            for (int j = 0; j < fields.size(); j++) {
                JSONObject field = fields.getJSONObject(j);
                JSONArray flex = field.getJSONArray("FlexFields");
                if (flex == null) { expanded.add(field); continue; }
                for (int k = 0; k < flex.size(); k++) {
                    JSONObject child = flex.getJSONObject(k);
                    String column = child.getString("FieldName");
                    if (column == null || column.isBlank())
                        throw new IllegalArgumentException("辅助属性子字段缺少查询标识: " + child.getString("Key"));
                    String childName = name(field) + "·" + name(child);
                    child.put("Key", field.getString("Key") + "." + column);
                    JSONObject caption = new JSONObject(); caption.put("Key", 2052); caption.put("Value", childName);
                    JSONArray names = new JSONArray(); names.add(caption); child.put("Name", names);
                    expanded.add(child);
                }
            }
            entity.put("Fields", expanded);
        }
        return copy;
    }

    private static void translateFilters(JSONArray filters, Map<String, String> keys) {
        if (filters == null) return;
        for (int i = 0; i < filters.size(); i++) {
            JSONObject filter = filters.getJSONObject(i);
            if (filter.containsKey("conditions")) translateFilters(filter.getJSONArray("conditions"), keys);
            else filter.put("fieldId", keys.get(filter.getString("fieldId")));
        }
    }

    private static void validateFilters(JSONArray filters, Set<String> allowed) {
        if (filters == null) return;
        Set<String> operators = Set.of("=", "!=", "<>", ">", ">=", "<", "<=", "包含于", "包含", "不包含", "为空", "不为空");
        for (int i = 0; i < filters.size(); i++) {
            JSONObject filter = filters.getJSONObject(i);
            if (filter.containsKey("conditions")) {
                validateFilters(filter.getJSONArray("conditions"), allowed);
            } else if (!allowed.contains(filter.getString("fieldId"))
                    || !operators.contains(filter.getString("operator"))) {
                throw new IllegalArgumentException("筛选字段或操作符已失效，请重新配置");
            }
        }
    }

    public static JSONObject query(JSONObject schema, JSONObject params, int start, int limit) {
        JSONArray fields = schema.getJSONArray("fields");
        int headCount = schema.getIntValue("headerFieldCount");
        Set<String> allowed = new HashSet<>();
        Set<String> header = new HashSet<>();
        for (int i = 0; i < fields.size(); i++) {
            String id = fields.getJSONObject(i).getString("id");
            allowed.add(id);
            if (i < headCount) header.add(id);
        }
        validateFilters(params.getJSONArray("filterConditions"), allowed);
        JSONArray headSorts = new JSONArray();
        JSONArray entrySorts = new JSONArray();
        JSONArray sorts = params.getJSONArray("sortConfigs");
        if (sorts != null) for (int i = 0; i < sorts.size(); i++) {
            JSONObject sort = sorts.getJSONObject(i);
            if (!allowed.contains(sort.getString("fieldId"))
                    || !Set.of("asc", "desc").contains(sort.getString("order")))
                throw new IllegalArgumentException("排序字段或方向已失效，请重新配置");
            (header.contains(sort.getString("fieldId")) ? headSorts : entrySorts).add(sort);
        }
        String billId = fields.getJSONObject(0).getString("id");
        appendSort(headSorts, billId);
        headSorts.addAll(entrySorts);
        JSONArray sequences = schema.getJSONArray("sequenceKeys");
        for (int i = 0; i < sequences.size(); i++) appendSort(headSorts, sequences.getString(i));
        JSONArray primaryIds = schema.getJSONArray("entryPrimaryIds");
        for (int i = 0; i < primaryIds.size(); i++) appendSort(headSorts, primaryIds.getString(i));
        JSONObject querySchema = JSONObject.parseObject(schema.toJSONString());
        Map<String, String> queryKeys = new HashMap<>();
        JSONArray queryFields = querySchema.getJSONArray("fields");
        for (int i = 0; i < queryFields.size(); i++) {
            JSONObject field = queryFields.getJSONObject(i);
            String id = field.getString("id");
            String key = field.getString("queryKey");
            queryKeys.put(id, key == null ? id : key);
            field.put("id", queryKeys.get(id));
        }
        JSONArray queryFilters = params.getJSONArray("filterConditions") == null ? null
                : JSONArray.parseArray(params.getJSONArray("filterConditions").toJSONString());
        translateFilters(queryFilters, queryKeys);
        JSONArray querySorts = JSONArray.parseArray(headSorts.toJSONString());
        for (int i = 0; i < querySorts.size(); i++) {
            JSONObject sort = querySorts.getJSONObject(i);
            sort.put("fieldId", queryKeys.getOrDefault(sort.getString("fieldId"), sort.getString("fieldId")));
        }
        JSONObject query = ContoryUtil.kingdeeToKingdeeQuery(querySchema, start, limit, queryFilters, querySorts);
        String filter = query.getString("FilterString");
        // Exclude header-only documents before pagination, not after it.
        query.put("FilterString", (filter == null || filter.isBlank() ? "" : "(" + filter + ") and ")
                + "(" + String.join(" or ", primaryIds.toJavaList(String.class).stream()
                    .map(id -> id + " > 0").collect(java.util.stream.Collectors.toList())) + ")");
        return query;
    }

    private static void appendSort(JSONArray sorts, String field) {
        if (field == null || field.isBlank()) return;
        for (int i = 0; i < sorts.size(); i++)
            if (field.equals(sorts.getJSONObject(i).getString("fieldId"))) return;
        JSONObject sort = new JSONObject();
        sort.put("fieldId", field);
        sort.put("order", "asc");
        sorts.add(sort);
    }

    public static JSONObject result(JSONObject schema, JSONObject params, List<List<Object>> rows,
            int offset, int limit, int syncLimit) throws ParseException {
        KingdeeQueryResponse.requireSuccess(rows);
        JSONArray fields = schema.getJSONArray("fields");
        int headCount = schema.getIntValue("headerFieldCount");
        boolean firstOnly = "FIRST_ROW".equals(mode(params));
        int begin = offset > 0 ? 1 : 0; // preceding row supplied by the query
        String previousBill = begin == 1 && !rows.isEmpty() ? String.valueOf(rows.get(0).get(0)) : null;
        int count = Math.min(limit, Math.max(0, rows.size() - begin));
        JSONArray records = new JSONArray();
        for (int rowIndex = begin; rowIndex < begin + count; rowIndex++) {
            List<Object> row = rows.get(rowIndex);
            if (row.size() != fields.size() || row.get(0) == null
                    || (schema.getJSONArray("entryPrimaryIds").size() == 1 && row.get(headCount) == null))
                throw new IllegalArgumentException("金蝶明细结果缺少主键或字段数量不一致");
            String bill = String.valueOf(row.get(0));
            JSONObject values = new JSONObject();
            for (int i = 0; i < fields.size(); i++) {
                JSONObject field = fields.getJSONObject(i);
                // Explicit empty value survives JSON serialization and clears prior header values.
                values.put(field.getString("id"), firstOnly && bill.equals(previousBill) && i < headCount
                        ? "" : ContoryUtil.getKingdeeFieldValue(field, row.get(i)));
            }
            JSONArray identity = new JSONArray();
            identity.add(params.getString("CID"));
            identity.add(schema.getString("Id"));
            identity.add(schema.getString("selectedEntryKey"));
            identity.add(bill);
            JSONArray primaryIds = schema.getJSONArray("entryPrimaryIds");
            if (primaryIds.size() == 1) identity.add(String.valueOf(row.get(headCount)));
            else for (int i = 0; i < fields.size(); i++)
                if (primaryIds.contains(fields.getJSONObject(i).getString("id"))) {
                    identity.add(fields.getJSONObject(i).getString("id"));
                    identity.add(row.get(i));
                }
            JSONObject record = new JSONObject();
            record.put("id", UUID.nameUUIDFromBytes(identity.toJSONString().getBytes(StandardCharsets.UTF_8)).toString());
            record.put("fields", values);
            records.add(record);
            previousBill = bill;
        }
        boolean more = rows.size() > begin + count && offset + count < syncLimit;
        JSONObject result = new JSONObject();
        result.put("records", records);
        result.put("hasMore", more);
        result.put("nextToken", more ? String.valueOf(offset + count) : "");
        return result;
    }
}
