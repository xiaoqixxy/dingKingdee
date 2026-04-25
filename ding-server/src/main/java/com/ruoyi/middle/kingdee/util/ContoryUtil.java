package com.ruoyi.middle.kingdee.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.middle.kingdee.constant.DateConverterUtil;
import com.ruoyi.middle.kingdee.constant.FieldTypeConstant;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 金蝶与钉钉数据格式转换工具类
 * <p>
 * 提供金蝶云星空与钉钉多维表之间的数据格式转换功能：
 * <ul>
 *   <li>kingdeeToDing - 将金蝶元数据转换为钉钉字段格式</li>
 *   <li>kingdeeToKingdeeQuery - 构建金蝶查询参数</li>
 *   <li>kingdeeToDingResult - 将金蝶查询结果转换为钉钉格式</li>
 * </ul>
 *
 * @author ruoyi
 */
public class ContoryUtil {

    /**
     * 钉钉日期格式（主格式）
     */
    private static final String DING_DATE_FORMATTER = "YYYY-MM-DD HH:mm";

    /**
     * 钉钉日期格式（备用格式）
     */
    private static final String DING_DATE_FORMATTER_FORMATTER = "YYYY-MM-dd HH:mm";

    /**
     * 将金蝶元数据转换为钉钉字段格式
     * <p>
     * 解析金蝶业务单据的字段定义，转换为钉钉多维表规范的字段结构。
     * 主要处理逻辑：
     * <ul>
     *   <li>提取表单ID和名称</li>
     *   <li>识别主键字段</li>
     *   <li>转换字段类型（枚举、日期、关联等）</li>
     *   <li>处理枚举类型的选项值</li>
     * </ul>
     *
     * @param needReturnData 金蝶返回的表单元数据
     * @return 钉钉格式的字段定义JSON对象
     */
    public static JSONObject kingdeeToDing(JSONObject needReturnData) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("Id", needReturnData.getString("Id"));
        resultJson.put("sheetName", getName(needReturnData));
        
        // 构建字段数组
        JSONArray fields = new JSONArray();
        
        // 添加主键字段
        String PkFieldName = needReturnData.getString("PkFieldName");
        JSONObject PkField = new JSONObject();
        PkField.put("id", PkFieldName);
        PkField.put("name", PkFieldName);
        PkField.put("type", "text");
        PkField.put("isPrimary", true);
        PkField.put("description", "字段主键");
        fields.add(PkField);
        
        // 解析单据头字段
        JSONArray jsonArray = needReturnData.getJSONArray("Entrys");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject groupFieldJson = jsonArray.getJSONObject(i);
            // 只处理单据头（FBillHead）
            if ("FBillHead".equals(groupFieldJson.getString("Key"))) {
                JSONArray fieldsKingdee = groupFieldJson.getJSONArray("Fields");
                for (int j = 0; j < fieldsKingdee.size(); j++) {
                    JSONObject rowFieldJson = fieldsKingdee.getJSONObject(j);
                    JSONObject rowFieldResult = new JSONObject();
                    rowFieldResult.put("name", getName(rowFieldJson));
                    
                    // 处理枚举类型字段（从Extends中获取选项）
                    if (rowFieldJson.containsKey("Extends") && rowFieldJson.get("Extends") instanceof JSONArray) {
                        rowFieldResult.put("id", rowFieldJson.getString("Key"));
                        rowFieldResult.put("type", "singleSelect");
                        JSONObject property = new JSONObject();
                        JSONArray choices = new JSONArray();
                        JSONArray Extends = rowFieldJson.getJSONArray("Extends");
                        for (int i1 = 0; i1 < Extends.size(); i1++) {
                            JSONObject Extend = Extends.getJSONObject(i1);
                            JSONObject rowChoices = new JSONObject();
                            rowChoices.put("name", Extend.getString("Caption"));
                            rowChoices.put("value", Extend.getString("Value"));
                            choices.add(rowChoices);
                        }
                        property.put("choices", choices);
                        rowFieldResult.put("property", property);
                    }
                    // 处理布尔类型
                    else if (FieldTypeConstant.FieldType_BOOLEN == rowFieldJson.getInteger("ElementType")) {
                        rowFieldResult.put("id", rowFieldJson.getString("Key"));
                        rowFieldResult.put("type", "singleSelect");
                        JSONObject property = new JSONObject();
                        JSONArray choices = new JSONArray();
                        JSONObject rowChoices_true = new JSONObject();
                        rowChoices_true.put("name", "是");
                        rowChoices_true.put("value", "true");
                        choices.add(rowChoices_true);
                        JSONObject rowChoices_false = new JSONObject();
                        rowChoices_false.put("name", "否");
                        rowChoices_false.put("value", "false");
                        choices.add(rowChoices_false);
                        property.put("choices", choices);
                        rowFieldResult.put("property", property);
                    }
                    // 处理关联表单类型
                    else if (FieldTypeConstant.FieldType_OBJ == rowFieldJson.getInteger("FieldType")) {
                        rowFieldResult.put("id", rowFieldJson.getString("Key") + ".FName");
                        rowFieldResult.put("type", "text");
                    }
                    // 处理关联表单类型
                    else if (FieldTypeConstant.ElementType_OBJ_FNAME.contains(rowFieldJson.getInteger("ElementType"))) {
                        rowFieldResult.put("id", rowFieldJson.getString("Key") + ".FName");
                        rowFieldResult.put("type", "text");
                    }
                    // 处理对象类型
                    else if (FieldTypeConstant.ElementType_OBJ.contains(rowFieldJson.getInteger("ElementType"))) {
                        rowFieldResult.put("id", rowFieldJson.getString("Key") + ".FNumber");
                        rowFieldResult.put("type", "text");
                    }
                    // 处理日期类型
                    else if (FieldTypeConstant.FieldType_DATE == rowFieldJson.getInteger("FieldType")) {
                        rowFieldResult.put("id", rowFieldJson.getString("Key"));
                        rowFieldResult.put("type", "date");
                        JSONObject property = new JSONObject();
                        property.put("formatter", DING_DATE_FORMATTER);
                        rowFieldResult.put("property", property);
                    }
                    // 其他类型统一处理为文本
                    else {
                        rowFieldResult.put("id", rowFieldJson.getString("Key"));
                        rowFieldResult.put("type", "text");
                    }

                    rowFieldResult.put("isPrimary", false);
                    rowFieldResult.put("description", "");
                    fields.add(rowFieldResult);
                }
            }
        }
        resultJson.put("fields", fields);
        return resultJson;
    }

    /**
     * 获取多语言名称
     *
     * @param needReturnData 包含Name数组的JSON对象
     * @return 指定语言（默认2052简体中文）的名称值
     */
    private static String getName(JSONObject needReturnData) {
        return getName(needReturnData, "2052");
    }

    /**
     * 根据语言ID获取多语言名称
     *
     * @param needReturnData 包含Name数组的JSON对象
     * @param lcid 语言ID
     * @return 对应语言的名称值
     */
    private static String getName(JSONObject needReturnData, String lcid) {
        JSONArray names = needReturnData.getJSONArray("Name");
        for (int i = 0; i < names.size(); i++) {
            JSONObject name = names.getJSONObject(i);
            if (lcid.equals(name.getString("Key"))) {
                return name.getString("Value");
            }
        }
        return "";
    }

    /**
     * 构建金蝶查询参数
     * <p>
     * 将钉钉格式的查询条件转换为金蝶API所需的查询参数格式。
     *
     * @param needReturnData 字段定义（来自kingdeeToDing转换结果）
     * @param nextToken      起始行号（分页用）
     * @param maxResult      每页数量
     * @param filterConditions 过滤条件数组
     * @param sortConfigs    排序配置数组
     * @return 金蝶格式的查询参数
     */
    public static JSONObject kingdeeToKingdeeQuery(JSONObject needReturnData, Integer nextToken,
            Integer maxResult, JSONArray filterConditions, JSONArray sortConfigs) {
        JSONObject queryJson = new JSONObject();
        List<String> FieldKeys = new ArrayList<>();
        
        // 提取所有字段ID
        JSONArray fields = needReturnData.getJSONArray("fields");
        for (int i = 0; i < fields.size(); i++) {
            JSONObject rowFieldJson = fields.getJSONObject(i);
            FieldKeys.add(rowFieldJson.getString("id"));
        }
        
        // 构建金蝶查询参数
        queryJson.put("FormId", needReturnData.getString("Id"));
        queryJson.put("FieldKeys", ListToString(FieldKeys));
        queryJson.put("FilterString", buildFilterString(filterConditions));
        queryJson.put("OrderString", buildOrderString(sortConfigs));
        queryJson.put("StartRow", nextToken);
        queryJson.put("Limit", maxResult);

        return queryJson;
    }

    /**
     * 将字符串列表转换为逗号分隔字符串
     *
     * @param list 字符串列表
     * @return 逗号分隔的字符串
     */
    private static String ListToString(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (String s : list) {
            result.append(s).append(",");
        }
        return result.substring(0, result.length() - 1);
    }

    /**
     * 构建金蝶过滤条件字符串
     * <p>
     * 将钉钉格式的过滤条件转换为金蝶API的FilterString格式。
     * 支持多条件组合，使用AND连接。
     *
     * @param filterConditions 钉钉格式的过滤条件数组
     * @return 金蝶格式的过滤字符串
     * @example
     * <pre>
     * // 输入：[{fieldId: "FBillNo", operator: "=", value: "PUR00001"}]
     * // 输出：FBillNo = 'PUR00001'
     * </pre>
     */
    private static String buildFilterString(JSONArray filterConditions) {
        if (filterConditions == null || filterConditions.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < filterConditions.size(); i++) {
            if (i != 0) {
                result.append(" and ");
            }
            JSONObject filterCondition = filterConditions.getJSONObject(i);
            result.append(filterCondition.getString("fieldId"));
            result.append(" ");
            result.append(filterCondition.getString("operator"));
            result.append(" '");
            result.append(filterCondition.getString("value"));
            result.append("'");
        }
        return result.toString();
    }

    /**
     * 构建金蝶排序字符串
     * <p>
     * 将钉钉格式的排序配置转换为金蝶API的OrderString格式。
     *
     * @param sortConfigs 钉钉格式的排序配置数组
     * @return 金蝶格式的排序字符串
     * @example
     * <pre>
     * // 输入：[{"fieldId": "FCreatorId", "order": "asc"}]
     * // 输出：FCreatorId asc
     * </pre>
     */
    private static String buildOrderString(JSONArray sortConfigs) {
        if (sortConfigs == null || sortConfigs.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < sortConfigs.size(); i++) {
            if (i != 0) {
                result.append(" , ");
            }
            JSONObject filterCondition = sortConfigs.getJSONObject(i);
            result.append(filterCondition.getString("fieldId"));
            result.append(" ");
            result.append(filterCondition.getString("order"));
        }
        return result.toString();
    }

    /**
     * 将金蝶查询结果转换为钉钉格式
     * <p>
     * 处理金蝶返回的二维数组数据，转换为钉钉多维表的记录格式。
     * 同时处理字段值的格式化，如枚举值转名称、日期转时间戳等。
     *
     * @param dingFieldData 字段定义（来自kingdeeToDing转换结果）
     * @param kingdeeData   金蝶查询返回的二维数组数据
     * @param nextToken     当前页起始行号
     * @param maxResult     每页数量
     * @return 钉钉格式的分页查询结果
     * @throws ParseException 日期解析异常
     */
    public static JSONObject kingdeeToDingResult(JSONObject dingFieldData, List<List<Object>> kingdeeData,
            Integer nextToken, Integer maxResult) throws ParseException {
        JSONArray records = new JSONArray();
        JSONArray dingFields = dingFieldData.getJSONArray("fields");
        
        // 遍历每一行数据
        for (List<Object> kingdeeDatum : kingdeeData) {
            JSONObject record = new JSONObject();
            JSONObject fields = new JSONObject();
            
            // 遍历每个字段
            for (int i = 0; i < dingFields.size(); i++) {
                JSONObject rowDingFieldJson = dingFields.getJSONObject(i);
                fields.put(rowDingFieldJson.getString("id"),
                    getKingdeeFieldValue(rowDingFieldJson, kingdeeDatum.get(i)));
            }
            
            // 第一列通常为主键ID
            record.put("id", kingdeeDatum.get(0));
            record.put("fields", fields);
            records.add(record);
        }
        
        // 构建分页结果
        JSONObject resultJson = new JSONObject();
        if (!records.isEmpty() && records.size() >= maxResult) {
            // 后续还有数据
            resultJson.put("nextToken", nextToken + maxResult);
            resultJson.put("hasMore", true);
            resultJson.put("records", records);
        } else {
            // 数据已全部返回
            resultJson.put("nextToken", "");
            resultJson.put("hasMore", false);
            resultJson.put("records", records);
        }
        return resultJson;
    }

    /**
     * 获取金蝶字段值并转换格式
     * <p>
     * 根据字段类型对值进行格式化处理：
     * <ul>
     *   <li>singleSelect - 枚举值转名称</li>
     *   <li>date - 日期转时间戳</li>
     *   <li>其他 - 转为字符串</li>
     * </ul>
     *
     * @param fieldJson 字段定义
     * @param fieldValue 原始字段值
     * @return 格式化后的值
     * @throws ParseException 日期解析异常
     */
    private static Object getKingdeeFieldValue(JSONObject fieldJson, Object fieldValue) throws ParseException {
        if (fieldValue == null) {
            return "";
        }
        
        String type = fieldJson.getString("type");
        
        // 单选枚举类型 - 将值转换为显示名称
        if ("singleSelect".equals(type)) {
            JSONArray choices = fieldJson.getJSONObject("property").getJSONArray("choices");
            for (int i = 0; i < choices.size(); i++) {
                JSONObject choice = choices.getJSONObject(i);
                if (fieldValue.equals(choice.getString("value"))) {
                    return choice.getString("name");
                }
            }
            return "";
        }
        // 日期类型 - 转换为时间戳
        else if ("date".equals(type)) {
            return DateConverterUtil.stringToDate(String.valueOf(fieldValue)).getTime();
        }
        // 其他类型 - 转为字符串
        else {
            return String.valueOf(fieldValue);
        }
    }
}
