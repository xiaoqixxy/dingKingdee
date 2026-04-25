package com.ruoyi.middle.kingdee.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kingdee.bos.webapi.entity.DataCenter;
import com.ruoyi.middle.kingdee.dto.KingdeeLoginRequest;
import com.ruoyi.middle.kingdee.service.K3CloudProxyService;
import com.ruoyi.middle.kingdee.util.ContoryUtil;
import com.ruoyi.middle.util.KingdeeK3CloudUtil;
import lombok.extern.slf4j.Slf4j;
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
public class K3CloudProxyServiceImpl implements K3CloudProxyService {

    /**
     * 转发请求到金蝶K3Cloud服务器
     * <p>
     * 验证金蝶账号信息的有效性，成功后返回该用户可访问的账套列表。
     * 方法会校验所有必填参数，参数为空或格式错误时抛出异常。
     *
     * @param request 金蝶登录请求参数
     * @return List&lt;DataCenter&gt; 账套列表
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
        log.info("APP ID（APP_ID）：{}", appId);
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

        // 调用金蝶接口获取账套列表
        try {
            KingdeeK3CloudUtil util = new KingdeeK3CloudUtil(serverUrl, cid, userName, appId, appSecret);
            List<DataCenter> dataCenters = util.getApi().getDataCenters();
            log.info("金蝶登录接口返回结果：{}", dataCenters);
            return dataCenters;
        } catch (Exception e) {
            String errorMsg = "金蝶接口处理失败：" + e.getMessage();
            log.error(errorMsg, e);
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
        System.err.println(requestBody);
        // 解析连接参数
        JSONObject params = JSONObject.parseObject(requestBody.get("params").toString());
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
        JSONObject resultObject = ContoryUtil.kingdeeToDing(NeedReturnData);
        return resultObject;
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
        JSONObject result = ContoryUtil.kingdeeToDingResult(dingFieldData, query, nextToken, maxResult);
        log.info("查询结果：{}", result);
        return result;
    }

    /**
     * 查询表单元数据（钉钉格式）
     */
    @Override
    public JSONObject sheetMetaWithFormId(String selectedFormId, String paramsJson) {
        try {
            // 解析params
            JSONObject params = JSONObject.parseObject(paramsJson);
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
            
            JSONObject resultObject = ContoryUtil.kingdeeToDing(NeedReturnData);
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
    public JSONObject recordsWithParams(Integer maxResults, Integer nextToken, String paramsJson) throws ParseException {
        try {
            // 解析params
            JSONObject params = JSONObject.parseObject(paramsJson);
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
            JSONObject jsonObject = util.queryBusinessInfo(TABLE_NAME);
            JSONObject NeedReturnData = jsonObject.getJSONObject("Result").getJSONObject("NeedReturnData");
            JSONObject dingFieldData = ContoryUtil.kingdeeToDing(NeedReturnData);
            
            JSONObject queryJson = ContoryUtil.kingdeeToKingdeeQuery(dingFieldData, nextToken, maxResults, filterConditions, sortConfigs);
            List<List<Object>> query = util.query(queryJson);
            
            JSONObject result = ContoryUtil.kingdeeToDingResult(dingFieldData, query, nextToken, maxResults);
            //添加权限过滤

            log.info("records查询成功: {}", result);
            return result;
        } catch (Exception e) {
            log.error("records查询失败", e);
            throw new RuntimeException("查询记录失败: " + e.getMessage(), e);
        }
    }
}
