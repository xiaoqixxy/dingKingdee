package com.ruoyi.middle.kingdee.controller;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.middle.kingdee.dto.KingdeeLoginRequest;
import com.ruoyi.middle.kingdee.service.K3CloudProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 钉钉AI表格数据源同步插件 - 金蝶云星空(K3Cloud)服务端
 * <p>
 * 提供两个核心接口供钉钉AI表格调用：
 * <ul>
 *   <li>/api/sheetMeta - 获取表结构</li>
 *   <li>/api/records - 获取表记录</li>
 * </ul>
 * <p>
 * 请求格式符合钉钉AI表格数据源同步规范。
 *
 * @author ruoyi
 * @see <a href="https://alidocs.dingtalk.com/i/nodes/1OQX0akWmBejogd0TAlmRyQGVGlDd3mE">钉钉数据源同步插件开发文档</a>
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Anonymous
public class K3CloudProxyController {

    private final K3CloudProxyService k3CloudProxyService;

    /**
     * 登录验证接口
     */
    @PostMapping("/login")
    public AjaxResult kingdeeLogin(@RequestBody KingdeeLoginRequest request) {
        try {
            Object result = k3CloudProxyService.forwardRequest(request);
            return AjaxResult.successDing("请求成功", result);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 获取表结构接口（钉钉格式）
     */
    @PostMapping("/sheetMeta")
    public JSONObject sheetMeta(@RequestBody JSONObject request) {
        try {
            log.info("收到钉钉sheetMeta请求: {}", request);
            
            String requestId = request.getString("requestId");
            String paramsJson = request.getString("params");
            JSONObject context = request.getJSONObject("context");
            
            log.info("请求ID: {}, UnionId: {}, CorpId: {}", 
                requestId, 
                context != null ? context.getString("unionId") : "",
                context != null ? context.getString("corpId") : "");
            
            JSONObject params = JSONObject.parseObject(paramsJson);
            String selectedFormId = params.getString("selectedFormId");
            
            log.info("查询表单: {}", selectedFormId);
            
            JSONObject sheetMetaResult = k3CloudProxyService.sheetMetaWithFormId(selectedFormId, paramsJson);
            
            JSONObject result = new JSONObject();
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", sheetMetaResult);
            
            log.info("sheetMeta返回: {}", result);
            return result;
        } catch (Exception e) {
            log.error("sheetMeta处理失败", e);
            JSONObject result = new JSONObject();
            result.put("code", 10005);
            result.put("msg", "三方系统异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取表记录接口（钉钉格式）
     */
    @PostMapping("/records")
    public JSONObject records(@RequestBody JSONObject request) {
        try {
            log.info("收到钉钉records请求: {}", request);
            
            String requestId = request.getString("requestId");
            Integer maxResults = request.getInteger("maxResults");
            String nextToken = request.getString("nextToken");
            String paramsJson = request.getString("params");
            JSONObject context = request.getJSONObject("context");
            
            JSONObject recordsResult = k3CloudProxyService.recordsWithParams(
                maxResults != null ? maxResults : 300,
                nextToken != null && !nextToken.isEmpty() ? Integer.parseInt(nextToken) : 0,
                paramsJson
            );
            
            JSONObject result = new JSONObject();
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", recordsResult);
            
            log.info("records返回: {}", result);
            return result;
        } catch (Exception e) {
            log.error("records处理失败", e);
            JSONObject result = new JSONObject();
            result.put("code", 10005);
            result.put("msg", "三方系统异常: " + e.getMessage());
            return result;
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping
    public AjaxResult check() {
        return AjaxResult.success();
    }
}
