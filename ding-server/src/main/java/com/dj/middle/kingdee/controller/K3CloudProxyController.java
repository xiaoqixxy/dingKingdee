package com.dj.middle.kingdee.controller;

import com.alibaba.fastjson2.JSONObject;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.domain.AjaxResult;
import com.dj.middle.kingdee.dto.KingdeeLoginRequest;
import com.dj.middle.kingdee.service.K3CloudProxyService;
import com.dj.middle.kingdee.util.SignatureUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    private final com.dj.middle.ding.service.SyncExecutionRecorder syncRecorder;

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
    @PostMapping({"/sheetMeta", "/formSchema"})
    public JSONObject sheetMeta(@RequestBody String rawBody, HttpServletRequest request) {
//        String timestamp = request.getHeader("Ding-Docs-Timestamp");
//        String signature = request.getHeader("Ding-Docs-Signature");
        // 使用 rawBody 验签
//        if (!SignatureUtil.verifySignature(SignatureUtil.secretKey, rawBody, Long.parseLong(timestamp), signature)) {
//            throw new RuntimeException("Signature verification failed");
//        }
        try {
            JSONObject requestBody = JSONObject.parseObject(rawBody);
            log.info("收到钉钉sheetMeta请求");
            
            String requestId = requestBody.getString("requestId");
            String paramsJson = requestBody.getString("params");
            JSONObject context = requestBody.getJSONObject("context");
            
            log.info("请求ID: {}, UnionId: {}, CorpId: {}", 
                requestId, 
                context != null ? context.getString("unionId") : "",
                context != null ? context.getString("corpId") : "");
            
            JSONObject params = JSONObject.parseObject(paramsJson);
            String selectedFormId = params.getString("selectedFormId");
            
            log.info("查询表单: {}", selectedFormId);
            
            JSONObject sheetMetaResult = k3CloudProxyService.sheetMetaWithFormId(selectedFormId, paramsJson);
            if (!request.getRequestURI().endsWith("/formSchema")) {
                sheetMetaResult.remove("entryOptions");
                sheetMetaResult.remove("headFields");
                sheetMetaResult.remove("headerFieldCount");
                sheetMetaResult.remove("selectedEntryKey");
                sheetMetaResult.remove("sequenceKey");
                com.alibaba.fastjson2.JSONArray fields = sheetMetaResult.getJSONArray("fields");
                for (int i = 0; i < fields.size(); i++) fields.getJSONObject(i).remove("queryKey");
            }
            
            JSONObject result = new JSONObject();
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", sheetMetaResult);
            
            log.info("sheetMeta返回: {}", result);
            return result;
        } catch (Exception e) {
            log.error("sheetMeta处理失败 stack={}", syncRecorder.sanitizedStack(e, rawBody));
            JSONObject result = new JSONObject();
            result.put("code", 10005);
            result.put("msg", "三方系统异常: " + syncRecorder.clientMessage(e, rawBody));
            return result;
        }
    }

    /**
     * 获取表记录接口（钉钉格式）
     */
    @PostMapping("/records")
    public JSONObject records(@RequestBody String rawBody, HttpServletRequest request) {
        try {
        String timestamp = request.getHeader("Ding-Docs-Timestamp");
        String signature = request.getHeader("Ding-Docs-Signature");
        // 使用 rawBody 验签
        if (!SignatureUtil.verifySignature(SignatureUtil.secretKey, rawBody, Long.parseLong(timestamp), signature)) {
            throw new RuntimeException("Signature verification failed");
        }
            JSONObject requestBody = JSONObject.parseObject(rawBody);
            log.info("收到已验签的钉钉records请求");
            
            JSONObject recordsResult = syncRecorder.record(requestBody, () -> {
                Integer requestedSize = requestBody.getInteger("maxResults");
                int pageSize = requestedSize == null ? 300 : requestedSize;
                int offset = syncRecorder.currentOffset();
                if (pageSize <= 0 || offset < 0) throw new IllegalArgumentException("无效的分页参数");
                return k3CloudProxyService.recordsWithParams(Math.min(pageSize, 300), offset,
                    requestBody.getString("params"), requestBody.getJSONObject("context").getString("corpId"));
            });

            JSONObject result = new JSONObject();
            result.put("code", 0);
            result.put("msg", "success");
            result.put("data", recordsResult);
            
//            log.info("records返回: {}", result);
            return result;
        } catch (Exception e) {
            log.error("records处理失败 stack={}", syncRecorder.sanitizedStack(e, rawBody));
            JSONObject result = new JSONObject();
            result.put("code", 10005);
            result.put("msg", "三方系统异常: " + syncRecorder.clientMessage(e, rawBody));
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
