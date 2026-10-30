package com.dj.middle.kingdee.service.impl;

import com.dj.middle.kingdee.util.DetailSyncSupport;
import com.dj.middle.kingdee.util.FieldConfigurationSupport;
import com.dj.middle.kingdee.util.ReferenceQuerySupport;
import com.dj.middle.kingdee.util.ReportClient;
import com.dj.middle.kingdee.util.ReportSupport;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kingdee.bos.webapi.entity.RepoResult;
import com.dj.middle.kingdee.util.KingdeeAuthResponse;
import com.dj.middle.ding.domain.DingProduct;
import com.dj.middle.ding.domain.DingSubscription;
import com.dj.middle.ding.service.IDingSubscriptionService;
import com.dj.middle.kingdee.dto.KingdeeLoginRequest;
import com.dj.middle.kingdee.service.K3CloudProxyService;
import com.dj.middle.kingdee.util.ContoryUtil;
import com.dj.middle.util.KingdeeK3CloudUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * 金蝶云星空(K3Cloud)代理服务实现类
 * <p>
 * 实现与金蝶云星空ERP系统的集成业务逻辑，包括：
 * <ul>
 *   <li>登录验证 - 验证金蝶账号并获取账套信息</li>
 *   <li>元数据查询 - 获取业务单据的字段结构</li>
 *   <li>数据查询 - 分页查询业务单据数据</li>
 * </ul>
 * <p>
 * 数据格式已适配钉钉多维表规范，方便与钉钉系统集成。
 *
 * @author ruoyi
 * @see K3CloudProxyService
 * @see KingdeeK3CloudUtil
 * @see ContoryUtil
 */
@Slf4j
@Service
/** 组装元数据、权益限制、明细和引用查询的同步业务实现。 */
public class K3CloudProxyServiceImpl implements K3CloudProxyService {

    /** 处理企业订阅业务的服务组件。 */
    @Autowired
    private IDingSubscriptionService dingSubscriptionService;

    /** 记录同步执行生命周期的组件。 */
    @Autowired
    private com.dj.middle.ding.service.SyncExecutionRecorder syncRecorder;

    /**
     * 转发请求到金蝶K3Cloud服务器
     * <p>
     * 验证金蝶账号与应用授权，成功后返回 verified=true。
     * 方法会校验所有必填参数，参数为空或格式错误时抛出异常。
     *
     * @param request 金蝶登录请求参数
     * @return 身份校验成功标记
     * @throws RuntimeException 当必填参数为空或金蝶接口调用失败时抛出
     */
    @Override
    public Object forwardRequest(KingdeeLoginRequest request) {
        // 参数获取与空值处理
        String serverUrl = request.getSERVER_URL() == null ? "" : request.getSERVER_URL().trim();
        String cid = request.getCID() == null ? "" : request.getCID().trim();
        String userName = request.getUSER_NAME() == null ? "" : request.getUSER_NAME().trim();
        String appId = request.getAPP_ID() == null ? "" : request.getAPP_ID().trim();
        String appSecret = request.getAPP_SECRET() == null ? "" : request.getAPP_SECRET().trim();
        Integer lcid = request.getLcid() == null ? 2052 : request.getLcid();

        // 日志记录入参信息
        log.info("=====================================");
        log.info("======== 金蝶登录接口入参明细 ========");
        log.info("=====================================");
        log.info("金蝶服务器地址（SERVER_URL）：{}", serverUrl);
        log.info("帐套ID（CID）：{}", cid);
        log.info("用户名（USER_NAME）：{}", userName);
        log.info("APP ID（APP_ID）：[已隐藏]");
        log.info("APP 密钥（APP_SECRET）：{}", appSecret.replaceAll(".", "*"));
        log.info("语言类型（lcid）：{}", lcid);
        log.info("=====================================\n");

        // 必填参数校验
        if (serverUrl.isEmpty()) {
            String errorMsg = "参数错误：SERVER_URL（金蝶服务器地址）不能为空！";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        if (cid.isEmpty()) {
            String errorMsg = "参数错误：CID（帐套ID）不能为空！";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        if (userName.isEmpty()) {
            String errorMsg = "参数错误：USER_NAME（用户名）不能为空！";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        if (appId.isEmpty()) {
            String errorMsg = "参数错误：APP_ID 不能为空！";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        if (appSecret.isEmpty()) {
            String errorMsg = "参数错误：APP_SECRET 不能为空！";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // 账套列表接口不能证明凭证有效，必须执行带身份认证的只读检查。
        try {
            KingdeeK3CloudUtil util = new KingdeeK3CloudUtil(serverUrl, cid, userName, appId, appSecret);
            RepoResult<?> result = util.getApi().CheckAuthInfo();
            KingdeeAuthResponse.requireSuccess(result);
            JSONObject verified = new JSONObject();
            verified.put("verified", true);
            return verified;
        } catch (Exception e) {
            String errorMsg = "金蝶校验失败：" + String.valueOf(e.getMessage()).replace(appSecret, "[已隐藏]").replace(appId, "[已隐藏]");
            log.warn("金蝶身份校验失败");
            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * 查询表单元数据
     * <p>
     * 根据表单ID查询业务单据的字段结构信息，将金蝶格式转换为钉钉多维表格式。
     *
     * @param requestBody 请求参数，包含：
     *                    <ul>
     *                      <li>params - 金蝶连接参数（包含SERVER_URL、CID、USER_NAME等）</li>
     *                      <li>selectedFormId - 业务单据ID，如PUR_PurchaseOrder</li>
     *                    </ul>
     * @return 适配钉钉的表单元数据JSON对象
     * @throws RuntimeException 当参数解析或金蝶接口调用失败时抛出
     */
    @Override
    public JSONObject sheetMeta(Map<String, Object> requestBody) {
        // 解析连接参数
        JSONObject params = JSONObject.parseObject(requestBody.get("params").toString());
        if (ReportSupport.enabled(params)) return reportSchema(params.getString("selectedFormId"), params);
        String SERVER_URL = params.getString("SERVER_URL");
        String CID = params.getString("CID");
        String USER_NAME = params.getString("USER_NAME");
        String APP_ID = params.getString("APP_ID");
        String APP_SECRET = params.getString("APP_SECRET");
        String TABLE_NAME = params.getString("selectedFormId");

        // 创建金蝶工具类实例并查询元数据
        KingdeeK3CloudUtil util = new KingdeeK3CloudUtil(SERVER_URL, CID, USER_NAME, APP_ID, APP_SECRET);
        JSONObject jsonObject = util.queryBusinessInfo(TABLE_NAME);
        JSONObject NeedReturnData = jsonObject.getJSONObject("Result").getJSONObject("NeedReturnData");
        
        // 转换为钉钉格式
        return ContoryUtil.kingdeeToDing(NeedReturnData);
    }

    /**
     * 查询业务数据记录
     * <p>
     * 分页查询指定业务单据的数据记录，支持条件过滤和排序。
     * 将金蝶查询结果转换为钉钉多维表格式返回。
     *
     * @param requestBody 请求参数，包含：
     *                   <ul>
     *                     <li>params - 金蝶连接参数</li>
     *                     <li>selectedFormId - 业务单据ID</li>
     *                     <li>maxResults - 每页数量</li>
     *                     <li>nextToken - 下一页令牌</li>
     *                     <li>filterConditions - 过滤条件数组</li>
     *                     <li>sortConfigs - 排序配置数组</li>
     *                   </ul>
     * @return 适配钉钉的分页查询结果JSON对象
     * @throws ParseException 日期解析异常
     * @throws RuntimeException 当参数解析或金蝶接口调用失败时抛出
     */
    @Override
    public JSONObject records(Map<String, Object> requestBody) throws ParseException {
        JSONObject sourceParams = JSONObject.parseObject(requestBody.get("params").toString());
        if (ReportSupport.enabled(sourceParams))
            throw new IllegalArgumentException("报表同步请使用带企业权益校验的 records 接口");
        // 处理分页参数
        Integer nextToken = 0;
        Integer maxResult = (Integer) requestBody.get("maxResults");
        if (requestBody.containsKey("nextToken")) {
            nextToken = Integer.valueOf(requestBody.get("nextToken").toString());
        }

        // 解析连接参数和查询条件
        JSONObject params = JSONObject.parseObject(requestBody.get("params").toString());
        String SERVER_URL = params.getString("SERVER_URL");
        String CID = params.getString("CID");
        String USER_NAME = params.getString("USER_NAME");
        String APP_ID = params.getString("APP_ID");
        String APP_SECRET = params.getString("APP_SECRET");
        String TABLE_NAME = params.getString("selectedFormId");
        JSONArray filterConditions = params.getJSONArray("filterConditions");
        JSONArray sortConfigs = params.getJSONArray("sortConfigs");

        // 创建金蝶工具类实例
        KingdeeK3CloudUtil util = new KingdeeK3CloudUtil(SERVER_URL, CID, USER_NAME, APP_ID, APP_SECRET);

        // 查询元数据并转换为金蝶查询格式
        JSONObject jsonObject = util.queryBusinessInfo(TABLE_NAME);
        JSONObject NeedReturnData = jsonObject.getJSONObject("Result").getJSONObject("NeedReturnData");
        JSONObject dingFieldData = ContoryUtil.kingdeeToDing(NeedReturnData);
        JSONObject queryJson = ContoryUtil.kingdeeToKingdeeQuery(dingFieldData, nextToken, maxResult, filterConditions, sortConfigs);

        // 执行查询
        List<List<Object>> query = util.query(queryJson);

        // 转换结果并返回
        JSONObject result = ContoryUtil.kingdeeToDingResult(dingFieldData, query, nextToken, maxResult,50000);
        log.info("查询结果：{}", result.size());
        return result;
    }

    /** 校验对象标识后读取引用对象元数据，防止无效标识进入查询。 */
    private JSONObject loadReferenceMetadata(KingdeeK3CloudUtil util, String formId) {
        if (formId == null || !formId.matches("[A-Za-z0-9_]{1,100}"))
            throw new IllegalArgumentException("引用对象标识无效");
        JSONObject response = util.queryBusinessInfo(formId);
        JSONObject result = response == null ? null : response.getJSONObject("Result");
        JSONObject metadata = result == null ? null : result.getJSONObject("NeedReturnData");
        if (metadata == null) throw new IllegalArgumentException("无法读取引用对象元数据: " + formId);
        return metadata;
    }

    /** 在表头和明细结构上应用别名及引用路径配置。 */
    private JSONObject configuredSchema(JSONObject metadata, JSONObject params, KingdeeK3CloudUtil util) {
        return FieldConfigurationSupport.apply(DetailSyncSupport.schema(metadata, params), params,
                formId -> loadReferenceMetadata(util, formId));
    }

    /** 每次使用独立会话，防止多租户 Cookie 或字段缓存串用。 */
    private JSONObject reportSchema(String formId, JSONObject params) {
        ReportSupport.validateForm(formId);
        ReportClient client = createReportClient(params);
        client.login();
        return ReportSupport.configured(client.discover(formId), params);
    }

    /** 报表走专用查询，并在发起请求前裁剪到服务端套餐额度。 */
    private JSONObject reportRecords(JSONObject params, Integer requested, Integer offset, int syncLimit) throws Exception {
        if (requested == null || requested <= 0 || offset == null || offset < 0)
            throw new IllegalArgumentException("无效的报表分页参数");
        if (offset >= syncLimit) {
            syncRecorder.limitReached();
            return JSONObject.of("records", new JSONArray(), "hasMore", false, "nextToken", "");
        }
        String formId = params.getString("selectedFormId");
        ReportSupport.validateForm(formId);
        ReportClient client = createReportClient(params);
        syncRecorder.api("ReportLogin", () -> { client.login(); return true; });
        JSONObject schema = syncRecorder.api("ReportFieldDiscovery", () -> ReportSupport.configured(client.discover(formId), params));
        int size = Math.min(Math.min(requested, 300), syncLimit - offset);
        JSONObject query = ReportSupport.query(schema, params, offset, size);
        JSONObject result = syncRecorder.api("GetSysReportData", () ->
                ReportSupport.page(schema, params, client.query(formId, query), offset, size, syncLimit));
        if (result.getBooleanValue("limitReached")) syncRecorder.limitReached();
        result.remove("limitReached");
        return result;
    }

    protected ReportClient createReportClient(JSONObject params) { return new ReportClient(params); }

    /** 返回引用对象的可选字段；扩展对象使用项目维护的字典结构。 */
    @Override
    public JSONObject referenceSchema(String formId, String paramsJson) {
        if (com.dj.middle.kingdee.util.ExtensionDictionarySupport.contains(formId))
            return com.dj.middle.kingdee.util.ExtensionDictionarySupport.schema(formId);
        JSONObject params = JSONObject.parseObject(paramsJson);
        KingdeeK3CloudUtil util = new KingdeeK3CloudUtil(params.getString("SERVER_URL"), params.getString("CID"),
                params.getString("USER_NAME"), params.getString("APP_ID"), params.getString("APP_SECRET"));
        return FieldConfigurationSupport.referenceSchema(loadReferenceMetadata(util, formId));
    }

    /**
     * 查询表单元数据（钉钉格式）
     */
    @Override
    public JSONObject sheetMetaWithFormId(String selectedFormId, String paramsJson) {
        try {
            // 解析params
            JSONObject params = JSONObject.parseObject(paramsJson);
            if (ReportSupport.enabled(params)) return reportSchema(
                    selectedFormId == null || selectedFormId.isBlank() ? params.getString("selectedFormId") : selectedFormId, params);
            String SERVER_URL = params.getString("SERVER_URL");
            String CID = params.getString("CID");
            String USER_NAME = params.getString("USER_NAME");
            String APP_ID = params.getString("APP_ID");
            String APP_SECRET = params.getString("APP_SECRET");
            String TABLE_NAME = selectedFormId != null && !selectedFormId.isEmpty() 
                ? selectedFormId 
                : params.getString("selectedFormId");

            KingdeeK3CloudUtil util = new KingdeeK3CloudUtil(SERVER_URL, CID, USER_NAME, APP_ID, APP_SECRET);
            JSONObject jsonObject = util.queryBusinessInfo(TABLE_NAME);
            JSONObject NeedReturnData = jsonObject.getJSONObject("Result").getJSONObject("NeedReturnData");
            
            JSONObject resultObject = configuredSchema(NeedReturnData, params, util);
            log.info("sheetMeta查询成功: {}", resultObject);
            return resultObject;
        } catch (Exception e) {
            log.error("sheetMeta查询失败", e);
            throw new RuntimeException("查询表结构失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询业务数据记录（钉钉格式）
     */
    @Override
    public JSONObject recordsWithParams(Integer maxResults, Integer nextToken, String paramsJson,String corpId) throws ParseException {
        try {
            // 读取企业当前订阅，缺失时按既有规则登记默认产品权益。
            // 同步额度来自服务端当前订阅，不能由前端请求自行放宽。
            DingProduct dingProduct = dingSubscriptionService.requireActiveProduct(corpId);
            //获取同步数量上线
            Integer singleSyncLimit = dingProduct.getSingleSyncLimit();
            if (nextToken == null || nextToken < 0 || maxResults == null || maxResults <= 0)
                throw new IllegalArgumentException("无效的分页参数");


            // 解析params
            JSONObject params = JSONObject.parseObject(paramsJson);
            if (ReportSupport.enabled(params)) return reportRecords(params, maxResults, nextToken,
                    singleSyncLimit == null ? 0 : singleSyncLimit);
            String SERVER_URL = params.getString("SERVER_URL");
            String CID = params.getString("CID");
            String USER_NAME = params.getString("USER_NAME");
            String APP_ID = params.getString("APP_ID");
            String APP_SECRET = params.getString("APP_SECRET");
            String TABLE_NAME = params.getString("selectedFormId");
            
            // 解析过滤和排序条件
            JSONArray filterConditions = params.getJSONArray("filterConditions");
            JSONArray sortConfigs = params.getJSONArray("sortConfigs");

            KingdeeK3CloudUtil util = new KingdeeK3CloudUtil(SERVER_URL, CID, USER_NAME, APP_ID, APP_SECRET);
            // 元数据查询也纳入当前批次的接口调用统计，失败时保留可追踪原因。
            JSONObject jsonObject = syncRecorder.api("QueryBusinessInfo", () -> {
                JSONObject metadata = util.queryBusinessInfo(TABLE_NAME);
                JSONObject result = metadata == null ? null : metadata.getJSONObject("Result");
                if (result == null || result.getJSONObject("NeedReturnData") == null)
                    throw new IllegalStateException("金蝶元数据响应异常");
                return metadata;
            });
            JSONObject NeedReturnData = jsonObject.getJSONObject("Result").getJSONObject("NeedReturnData");
            JSONObject dingFieldData = configuredSchema(NeedReturnData, params, util);

            // 显式配置引用路径时使用逐跳补查，筛选排序依赖引用值则先物化候选集。
            if (ReferenceQuerySupport.required(dingFieldData)) {
                if (nextToken == null || nextToken < 0 || maxResults == null || maxResults <= 0)
                    throw new IllegalArgumentException("无效的分页参数");
                int syncLimit = singleSyncLimit == null ? 0 : singleSyncLimit;
                if (nextToken >= syncLimit) {
                    syncRecorder.limitReached();
                    return ReferenceQuerySupport.page(dingFieldData, params, java.util.Collections.emptyList(), nextToken, 0, syncLimit);
                }
                java.util.function.Function<JSONObject, List<List<Object>>> executeReferenceQuery = query -> {
                    try {
                        // 扩展分组计划路由到专用接口，普通计划仍走单据查询，两种调用分开计时。
                        if ("QueryGroupInfo".equals(query.getString("queryType")))
                            return syncRecorder.api("QueryGroupInfo", () -> util.queryGroupInfo(query));
                        return syncRecorder.api("ExecuteBillQuery", () -> util.query(query));
                    }
                    catch (Exception e) { throw new IllegalStateException("引用数据查询失败", e); }
                };
                // 不依赖引用值筛选排序时，只查询当前页，避免为了改一个显示属性读取全部数据。
                if (ReferenceQuerySupport.canStream(dingFieldData, params))
                    return ReferenceQuerySupport.streamPage(dingFieldData, params, nextToken, Math.min(maxResults, 300), syncLimit,
                            executeReferenceQuery, () -> syncRecorder.limitReached());
                List<List<Object>> resolved = ReferenceQuerySupport.collect(dingFieldData, params, executeReferenceQuery);
                if (resolved.size() > syncLimit) syncRecorder.limitReached();
                return ReferenceQuerySupport.page(dingFieldData, params, resolved, nextToken, Math.min(maxResults, 300), syncLimit);
            }

            // 未配置额外引用的明细沿用原分页方案，单页限制及权益仍同时生效。
            if (dingFieldData.containsKey("selectedEntryKey")) {
                if (nextToken == null || nextToken < 0 || maxResults == null || maxResults <= 0)
                    throw new IllegalArgumentException("无效的分页参数");
                int syncLimit = singleSyncLimit == null ? 0 : singleSyncLimit;
                int pageSize = Math.min(Math.min(maxResults, 300), Math.max(0, syncLimit - nextToken));
                if (pageSize == 0) {
                    syncRecorder.limitReached();
                    return DetailSyncSupport.result(dingFieldData, params, java.util.Collections.emptyList(), nextToken, 0, syncLimit);
                }
                // 多取上一条用于跨页表头衔接，并超取下一条判断是否还有后续页。
                int start = Math.max(0, nextToken - 1);
                int count = pageSize + (nextToken > 0 ? 1 : 0) + 1;
                JSONObject detailQuery = DetailSyncSupport.query(dingFieldData, params, start, count);
                List<List<Object>> detailRows = syncRecorder.api("ExecuteBillQuery", () -> util.query(detailQuery));
                int available = Math.max(0, detailRows.size() - (nextToken > 0 ? 1 : 0));
                if (available > pageSize && (long) nextToken + pageSize >= syncLimit) syncRecorder.limitReached();
                return DetailSyncSupport.result(dingFieldData, params, detailRows, nextToken, pageSize, syncLimit);
            }
            
            // 纯表头且未配置额外引用时保留历史直接查询分支。
            int remaining = Math.max(0, (singleSyncLimit == null ? 0 : singleSyncLimit) - nextToken);
            if (remaining == 0) {
                syncRecorder.limitReached();
                return JSONObject.of("records", new JSONArray(), "hasMore", false, "nextToken", "");
            }
            int boundedSize = Math.min(Math.min(maxResults, 300), remaining);
            JSONObject queryJson = ContoryUtil.kingdeeToKingdeeQuery(dingFieldData, nextToken, boundedSize, filterConditions, sortConfigs);
            List<List<Object>> query = syncRecorder.api("ExecuteBillQuery", () -> util.query(queryJson));
            
            if (query.size() >= boundedSize && (long) nextToken + boundedSize >= singleSyncLimit)
                syncRecorder.limitReached();
            JSONObject result = ContoryUtil.kingdeeToDingResult(dingFieldData, query, nextToken, boundedSize,singleSyncLimit);
            //添加权限过滤

            log.info("records查询成功: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("records查询失败，错误类型：{}", e.getClass().getSimpleName());
            throw new RuntimeException("查询记录失败: " + e.getMessage(), e);
        }
    }
}
