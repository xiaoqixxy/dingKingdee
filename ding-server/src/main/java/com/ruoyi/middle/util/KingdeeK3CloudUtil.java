package com.ruoyi.middle.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kingdee.bos.webapi.entity.*;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 金蝶云星空(K3Cloud)API工具类
 * <p>
 * 提供与金蝶云星空WebAPI交互的封装方法，包括：
 * <ul>
 *   <li>连接创建 - 创建API连接实例</li>
 *   <li>数据查询 - executeBillQuery 查询业务数据</li>
 *   <li>数据保存 - save 保存单据</li>
 *   <li>提交审核 - saveAndSubmit, submit, audit</li>
 *   <li>反审核 - unAudit</li>
 *   <li>元数据查询 - queryBusinessInfo</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 创建连接
 * KingdeeK3CloudUtil util = new KingdeeK3CloudUtil(
 *     "http://192.168.1.1/k3cloud",
 *     "6882df0b41e0a4",
 *     "admin",
 *     "285402_xxxxx",
 *     "2f9880a4956c42af8df761c6039f803d"
 * );
 *
 * // 查询数据
 * JSONObject param = new JSONObject();
 * param.put("FormId", "PUR_PurchaseOrder");
 * param.put("FieldKeys", "FBillNo,FBillDate,FSupplierId.FName");
 * List&lt;List&lt;Object&gt;&gt; result = util.query(param);
 * </pre>
 *
 * @author ruoyi
 */
@Slf4j
public class KingdeeK3CloudUtil {

    /**
     * 金蝶服务器地址
     */
    private String SERVER_URL;

    /**
     * 账套ID
     */
    private String CID;

    /**
     * 集成用户名
     */
    private String USER_NAME;

    /**
     * 三方授权APP_ID
     */
    private String APP_ID;

    /**
     * 三方授权APP_SECRET
     */
    private String APP_SECRET;

    /**
     * K3Cloud API实例
     */
    private K3CloudApi api;

    /**
     * 构造函数
     *
     * @param SERVER_URL 金蝶服务器地址
     * @param CID        账套ID
     * @param USER_NAME  用户名
     * @param APP_ID     应用ID
     * @param APP_SECRET 应用密钥
     */
    public KingdeeK3CloudUtil(String SERVER_URL, String CID, String USER_NAME, String APP_ID, String APP_SECRET) {
        this.SERVER_URL = SERVER_URL;
        this.CID = CID;
        this.USER_NAME = USER_NAME;
        this.APP_ID = APP_ID;
        this.APP_SECRET = APP_SECRET;
        this.api = createApi();
    }

    /**
     * 获取API实例
     *
     * @return K3CloudApi实例
     */
    public K3CloudApi getApi() {
        return api;
    }

    /**
     * 设置API实例
     *
     * @param api K3CloudApi实例
     */
    public void setApi(K3CloudApi api) {
        this.api = api;
    }

    /**
     * 创建API连接
     *
     * @return K3CloudApi实例
     */
    public K3CloudApi createApi() {
        IdentifyInfo identifyInfo = new IdentifyInfo();
        identifyInfo.setUserName(USER_NAME);
        identifyInfo.setAppId(APP_ID);
        identifyInfo.setAppSecret(APP_SECRET);
        identifyInfo.setdCID(CID);
        identifyInfo.setlCID(2052); // 默认简体中文
        identifyInfo.setServerUrl(SERVER_URL);
        return new K3CloudApi(identifyInfo);
    }

    /**
     * 查询业务单据数据
     * <p>
     * 调用金蝶executeBillQuery接口查询业务数据。
     *
     * @param param 查询参数，包含：
     *              <ul>
     *                <li>FormId - 单据ID</li>
     *                <li>FieldKeys - 查询字段，多个用逗号分隔</li>
     *                <li>FilterString - 过滤条件</li>
     *                <li>OrderString - 排序条件</li>
     *                <li>StartRow - 起始行</li>
     *                <li>Limit - 返回行数</li>
     *              </ul>
     * @return 查询结果二维列表，每行是一个对象数组
     * @throws RuntimeException 查询失败时抛出
     */
    public List<List<Object>> query(JSONObject param) {
        try {
            log.info("调用WebApi查询接口开始 ==> 请求报文: {}", param);
            List<List<Object>> result = api.executeBillQuery(param.toJSONString());
            log.info("调用WebApi查询接口成功 ==> 返回行数: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("调用WebApi查询接口失败 ==> 错误: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存业务单据
     * <p>
     * 调用金蝶保存接口，支持自动暂存功能。
     *
     * @param param       单据数据
     * @param tableName   单据ID
     * @param isAutoDraft 保存失败时是否自动暂存
     * @return 保存结果，包含单据ID和Number
     * @throws Exception 保存失败时抛出
     */
    public JSONObject save(Map<String, Object> param, String tableName, boolean isAutoDraft) throws Exception {
        log.info("调用WebApi保存接口开始,触发表单:{} ==> 请求报文: {}", tableName, JSON.toJSONString(new SaveParam<>(param)));
        SaveResult saveResult = api.save(tableName, new SaveParam<>(param));
        JSONObject saveResultJson = JSONObject.parseObject(JSON.toJSONString(saveResult.getResult()));
        
        if (saveResult.isSuccessfully()) {
            log.info("调用WebApi保存接口成功,触发表单:{} ==> 返回报文: {}", tableName, saveResultJson);
            return saveResultJson.getJSONObject("ResponseStatus").getJSONArray("SuccessEntitys").getJSONObject(0);
        } else {
            if (isAutoDraft) {
                // 自动暂存
                log.info("调用WebApi保存接口失败,进入暂存模式");
                String draftResult = api.draft(tableName, JSON.toJSONString(new SaveParam<>(param)));
                log.info("调用WebApi暂存接口完成,触发表单:{} ==> 返回报文: {}", tableName, draftResult);
                return JSONObject.parseObject(draftResult)
                    .getJSONObject("Result")
                    .getJSONObject("ResponseStatus")
                    .getJSONArray("SuccessEntitys")
                    .getJSONObject(0);
            } else {
                log.error("调用WebApi保存接口失败,触发表单:{} ==> 返回报文: {}", tableName, saveResultJson);
                throw new RuntimeException(saveResultJson.toJSONString());
            }
        }
    }

    /**
     * 保存业务单据（默认不自动暂存）
     *
     * @see #save(Map, String, boolean)
     */
    public JSONObject save(Map<String, Object> param, String tableName) throws Exception {
        return this.save(param, tableName, false);
    }

    /**
     * 保存并提交审核业务单据
     *
     * @param param       单据数据
     * @param tableName   单据ID
     * @param isAutoDraft 保存失败时是否自动暂存
     * @return 保存结果
     * @throws Exception 保存失败时抛出
     */
    public JSONObject saveAndSubmit(Map<String, Object> param, String tableName, boolean isAutoDraft) throws Exception {
        log.info("调用WebApi保存并审核接口开始,触发表单:{} ==> 请求报文: {}", tableName, JSON.toJSONString(param));
        SaveResult saveResult = api.save(tableName, new SaveParam<Map>(param));
        JSONObject saveResultJson = JSONObject.parseObject(JSON.toJSONString(saveResult.getResult()));
        log.info("调用WebApi保存并审核接口结束,触发表单:{} ==> 响应: {}", tableName, saveResultJson);
        
        if (saveResult.isSuccessfully()) {
            return saveResultJson.getJSONObject("ResponseStatus").getJSONArray("SuccessEntitys").getJSONObject(0);
        } else {
            if (isAutoDraft) {
                String draftResult = api.draft(tableName, JSON.toJSONString(param));
                log.info("调用WebApi暂存接口结束,触发表单:{} ==> 响应: {}", tableName, draftResult);
                JSONObject jsonObject = JSONObject.parseObject(draftResult);
                JSONObject mainResult = jsonObject.getJSONObject("Result").getJSONObject("ResponseStatus");
                Boolean isSuccess = mainResult.getBoolean("IsSuccess");
                if (!isSuccess) {
                    throw new RuntimeException("云星空-暂存失败-返回结果为" + draftResult);
                }
                return mainResult;
            } else {
                throw new RuntimeException(saveResultJson.toJSONString());
            }
        }
    }

    /**
     * 保存并提交审核（默认不自动暂存）
     */
    public JSONObject saveAndSubmit(Map<String, Object> param, String tableName) throws Exception {
        return this.saveAndSubmit(param, tableName, false);
    }

    /**
     * 提交单据
     *
     * @param numbers   单据编码列表
     * @param tableName 单据ID
     * @return 提交结果
     */
    public JSONObject submit(List<String> numbers, String tableName) {
        try {
            OperateParam operateParam = new OperateParam();
            operateParam.setNumbers(numbers);
            log.info("调用WebApi提交接口开始,触发表单:{} ==> 请求报文: {}", tableName, JSON.toJSONString(operateParam));
            OperatorResult saveResult = api.submit(tableName, operateParam);
            JSONObject operatorResultJson = JSONObject.parseObject(JSON.toJSONString(saveResult.getResult()));
            
            if (saveResult.isSuccessfully()) {
                log.info("调用WebApi提交接口成功,触发表单:{} ==> 返回报文: {}", tableName, operatorResultJson);
                return operatorResultJson;
            } else {
                log.error("调用WebApi提交接口失败,触发表单:{} ==> 返回报文: {}", tableName, operatorResultJson);
                throw new RuntimeException(operatorResultJson.toJSONString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 审核单据
     *
     * @param numbers   单据编码列表
     * @param tableName 单据ID
     * @return 审核结果
     */
    public JSONObject audit(List<String> numbers, String tableName) {
        try {
            OperateParam operateParam = new OperateParam();
            operateParam.setNumbers(numbers);
            log.info("调用WebApi审核接口开始,触发表单:{} ==> 请求报文: {}", tableName, JSON.toJSONString(operateParam));
            OperatorResult saveResult = api.audit(tableName, operateParam);
            JSONObject operatorResultJson = JSONObject.parseObject(JSON.toJSONString(saveResult.getResult()));
            
            if (saveResult.isSuccessfully()) {
                log.info("调用WebApi审核接口成功,触发表单:{} ==> 返回报文: {}", tableName, operatorResultJson);
                return operatorResultJson;
            } else {
                log.error("调用WebApi审核接口失败,触发表单:{} ==> 返回报文: {}", tableName, operatorResultJson);
                throw new RuntimeException(operatorResultJson.toJSONString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 反审核单据
     *
     * @param numbers   单据编码列表（为空或null时不执行操作）
     * @param tableName 单据ID
     * @return 反审核结果，numbers为空时返回null
     */
    public JSONObject unAudit(List<String> numbers, String tableName) {
        try {
            if (numbers == null || numbers.isEmpty() || (numbers.size() == 1 && numbers.get(0).isEmpty())) {
                log.info("未获取到ERP编码，不执行反审核操作");
                return null;
            }
            OperateParam operateParam = new OperateParam();
            operateParam.setNumbers(numbers);
            log.info("调用WebApi反审核接口开始,触发表单:{} ==> 请求报文: {}", tableName, JSON.toJSONString(operateParam));
            OperatorResult saveResult = api.unAudit(tableName, operateParam);
            JSONObject operatorResultJson = JSONObject.parseObject(JSON.toJSONString(saveResult.getResult()));
            
            if (saveResult.isSuccessfully()) {
                log.info("调用WebApi反审核接口成功,触发表单:{} ==> 返回报文: {}", tableName, operatorResultJson);
                return operatorResultJson;
            } else {
                log.error("调用WebApi反审核接口失败,触发表单:{} ==> 返回报文: {}", tableName, operatorResultJson);
                throw new RuntimeException(operatorResultJson.toJSONString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询业务单据元数据
     * <p>
     * 获取指定单据的字段结构信息。
     *
     * @param tableName 业务单据ID
     * @return 单据元数据JSON
     */
    public JSONObject queryBusinessInfo(String tableName) {
        JSONObject params = new JSONObject();
        params.put("FormId", tableName);
        try {
            String resultStr = api.queryBusinessInfo(params.toJSONString());
            JSONObject result = JSONObject.parseObject(resultStr);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== 过滤条件常量 ====================

    /** 等于比较符 */
    public static final String eq = "67";

    /** 小于比较符 */
    public static final String xiaoyu = "32";

    /** 大于比较符 */
    public static final String dayu = "39";

    /** 不等于比较符 */
    public static final String not_eq = "83";

    /**
     * 过滤条件内部类
     * <p>
     * 用于构建金蝶API的过滤条件，支持链式调用。
     * <p>
     * 使用示例：
     * <pre>
     * List&lt;FilterCondition&gt; conditions = new ArrayList&lt;&gt;();
     * conditions.add(new FilterCondition("FBillNo", eq, "PUR00001"));
     * conditions.add(new FilterCondition("FCreatorId", dayu, "2025-01-01", 1)); // OR
     * </pre>
     */
    public static class FilterCondition {
        private String fieldName;
        private String compare;
        private String value;
        private String left = "";
        private String right = "";
        private int logic = 0;

        public FilterCondition() {}

        public FilterCondition(String fieldName, String compare, String value) {
            this.fieldName = fieldName;
            this.compare = compare;
            this.value = value;
        }

        public FilterCondition(String fieldName, String compare, String value, int logic) {
            this.fieldName = fieldName;
            this.compare = compare;
            this.value = value;
            this.logic = logic;
        }

        // Getter和Setter方法
        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }
        public String getCompare() { return compare; }
        public void setCompare(String compare) { this.compare = compare; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getLeft() { return left; }
        public void setLeft(String left) { this.left = left; }
        public String getRight() { return right; }
        public void setRight(String right) { this.right = right; }
        public int getLogic() { return logic; }
        public void setLogic(int logic) { this.logic = logic; }

        /**
         * 转换为JSONObject格式
         *
         * @return JSON对象
         */
        public JSONObject toJSONObject() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("FieldName", this.fieldName);
            jsonObject.put("Compare", this.compare);
            jsonObject.put("value", this.value);
            jsonObject.put("Left", this.left);
            jsonObject.put("Right", this.right);
            jsonObject.put("Logic", this.logic);
            return jsonObject;
        }
    }

    /**
     * 查询数据并返回Map列表
     *
     * @param formId      单据ID
     * @param fieldKeys   查询字段（逗号分隔）
     * @param filterList  过滤条件列表
     * @return 字段名与值的映射列表
     */
    public List<Map<String, Object>> queryDataList(String formId, String fieldKeys, List<FilterCondition> filterList) {
        JSONObject param = new JSONObject();
        param.put("FormId", formId);
        param.put("FieldKeys", fieldKeys);
        JSONArray filterString = new JSONArray();
        if (filterList != null && !filterList.isEmpty()) {
            for (FilterCondition condition : filterList) {
                filterString.add(condition.toJSONObject());
            }
        }
        param.put("filterString", filterString);
        List<List<Object>> rawData = query(param);
        return mergeData(fieldKeys, rawData);
    }

    /**
     * 将二维数组数据合并为Map列表
     *
     * @param fieldKey 字段列表（逗号分隔）
     * @param rawData  原始二维数组数据
     * @return 字段名与值的映射列表
     */
    private List<Map<String, Object>> mergeData(String fieldKey, List<List<Object>> rawData) {
        String[] fieldArray = fieldKey.split(",");
        List<Map<String, Object>> result = new ArrayList<>();
        for (List<Object> row : rawData) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 0; i < fieldArray.length && i < row.size(); i++) {
                rowMap.put(fieldArray[i], row.get(i));
            }
            result.add(rowMap);
        }
        return result;
    }
}
