package com.ruoyi.middle.kingdee.service;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.middle.kingdee.dto.KingdeeLoginRequest;

import java.text.ParseException;
import java.util.Map;

/**
 * 金蝶云星空(K3Cloud)代理服务接口
 * <p>
 * 定义与金蝶云星空ERP系统集成业务逻辑的核心接口。
 * 提供登录验证、元数据查询、数据查询等功能。
 *
 * @author ruoyi
 * @see com.ruoyi.middle.kingdee.service.impl.K3CloudProxyServiceImpl
 */
public interface K3CloudProxyService {

    /**
     * 转发请求到金蝶K3Cloud服务器
     * <p>
     * 验证金蝶账号信息，获取该用户可访问的账套列表。
     * 主要用于第三方系统与金蝶集成的登录验证环节。
     *
     * @param request 包含金蝶登录信息的请求对象
     *                <ul>
     *                  <li>SERVER_URL - 金蝶服务器地址</li>
     *                  <li>CID - 账套ID</li>
     *                  <li>USER_NAME - 用户名</li>
     *                  <li>APP_ID - 应用ID</li>
     *                  <li>APP_SECRET - 应用密钥</li>
     *                  <li>lcid - 语言ID（默认2052）</li>
     *                </ul>
     * @return 账套列表 {@link com.kingdee.bos.webapi.entity.DataCenter}
     * @throws RuntimeException 当参数为空或金蝶接口调用失败时抛出
     */
    Object forwardRequest(KingdeeLoginRequest request);

    /**
     * 查询表单元数据
     * <p>
     * 获取指定业务单据的字段结构信息，包括：
     * <ul>
     *   <li>表单ID和名称</li>
     *   <li>字段定义（名称、类型、是否主键等）</li>
     *   <li>字段选项（枚举类型字段的可选值）</li>
     * </ul>
     *
     * @param requestBody 请求参数，包含：
     *                    <ul>
     *                      <li>selectedFormId - 业务单据ID</li>
     *                      <li>params - 金蝶连接参数</li>
     *                    </ul>
     * @return 表单元数据JSON对象，格式如下：
     *         <pre>
     *         {
     *           "Id": "表单ID",
     *           "sheetName": "表单名称",
     *           "fields": [
     *             {
     *               "id": "FBillNo",
     *               "name": "单据编号",
     *               "type": "text",
     *               "isPrimary": true
     *             },
     *             {
     *               "id": "FSpecifier",
     *               "name": "规格",
     *               "type": "singleSelect",
     *               "property": {
     *                 "choices": [
     *                   {"name": "选项A", "value": "A"},
     *                   {"name": "选项B", "value": "B"}
     *                 ]
     *               }
     *             }
     *           ]
     *         }
     *         </pre>
     * @throws RuntimeException 当金蝶接口调用失败时抛出
     */
    JSONObject sheetMeta(Map<String, Object> requestBody);

    /**
     * 查询业务数据记录
     * <p>
     * 分页查询指定业务单据的数据，支持：
     * <ul>
     *   <li>分页查询 - 通过maxResults和nextToken实现</li>
     *   <li>条件过滤 - 通过filterConditions指定过滤条件</li>
     *   <li>结果排序 - 通过sortConfigs指定排序规则</li>
     * </ul>
     *
     * @param requestBody 请求参数，包含：
     *                    <ul>
     *                      <li>selectedFormId - 业务单据ID</li>
     *                      <li>params - 金蝶连接参数</li>
     *                      <li>maxResults - 每页数量</li>
     *                      <li>nextToken - 下一页令牌（首次为空）</li>
     *                      <li>filterConditions - 过滤条件数组</li>
     *                      <li>sortConfigs - 排序配置数组</li>
     *                    </ul>
     * @return 分页结果JSON对象，格式如下：
     *         <pre>
     *         {
     *           "nextToken": "下一页令牌",
     *           "hasMore": true/false,
     *           "records": [
     *             {
     *               "id": "记录ID",
     *               "fields": {
     *                 "FBillNo": "单据编号值",
     *                 "FSpecifier": "规格值"
     *               }
     *             }
     *           ]
     *         }
     *         </pre>
     * @throws ParseException 日期解析异常
     * @throws RuntimeException 当金蝶接口调用失败时抛出
     */
    JSONObject records(Map<String, Object> requestBody) throws ParseException;

    /**
     * 查询表单元数据（钉钉格式）
     * <p>
     * 供钉钉AI表格数据源同步插件调用，直接使用表单ID和JSON字符串参数。
     *
     * @param selectedFormId 业务单据ID
     * @param paramsJson 金蝶连接参数字符串（JSON格式）
     * @return 钉钉格式的表单元数据
     */
    JSONObject sheetMetaWithFormId(String selectedFormId, String paramsJson);

    /**
     * 查询业务数据记录（钉钉格式）
     * <p>
     * 供钉钉AI表格数据源同步插件调用，直接使用JSON字符串参数。
     *
     * @param maxResults 每页数量
     * @param nextToken 分页起始位置
     * @param paramsJson 查询参数字符串（JSON格式）
     * @return 钉钉格式的分页记录数据
     * @throws ParseException 日期解析异常
     */
    JSONObject recordsWithParams(Integer maxResults, Integer nextToken, String paramsJson) throws ParseException;
}
