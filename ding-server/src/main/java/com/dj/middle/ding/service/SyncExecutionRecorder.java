package com.dj.middle.ding.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.dj.middle.ding.domain.SyncExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/** 协调同步批次上下文、接口计时、错误脱敏和分页日志。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncExecutionRecorder {
    /** 在独立事务中持久化同步执行状态的服务。 */
    private final SyncExecutionWriter writer;
    /** 当前线程正在记录的同步执行上下文。 */
    private final ThreadLocal<SyncExecution> current = new ThreadLocal<>();
    /** 当前线程调用第三方接口所需的认证上下文。 */
    private final ThreadLocal<java.util.List<String>> credentials = new ThreadLocal<>();

    /** 记录一次同步分页请求，绑定企业和数据源，统计接口调用并持久化执行结果。 */
    public JSONObject record(JSONObject request, Callable<JSONObject> action) throws Exception {
        JSONObject context = request.getJSONObject("context");
        // 首先确认企业身份，避免把未绑定企业的请求写入可信同步批次。
        String corpId = context == null ? null : context.getString("corpId");
        if (corpId == null || corpId.isBlank() || corpId.length() > 64)
            throw new IllegalArgumentException("缺少有效企业ID");
        SyncExecution execution = new SyncExecution();
        execution.setCorpId(corpId);
        execution.setTraceId(UUID.randomUUID().toString());
        // 解析游标和数据源指纹，使后续分页只能延续同一企业及同一同步配置。
        SyncPageToken.Position position = SyncPageToken.parse(request.getString("nextToken"));
        execution.setPageOffset(position.offset());
        execution.setContinuationTrace(position.trace());
        execution.setSourceKey(SyncPageToken.sourceKey(request));
        if (position.trace() == null && position.offset() > 0) execution.setLogScope("LEGACY");
        String requestId = request.getString("requestId");
        execution.setRequestKey(requestId == null || requestId.isBlank() ? execution.getTraceId()
            : SyncPageToken.digest(requestId + ":" + position.offset() + ":" + execution.getSourceKey()));
        // 日志仅保存经过长度限制的摘要，凭证另存于当前线程供异常脱敏，不写入摘要。
        JSONObject summary = new JSONObject();
        summary.put("scope", "BATCH_PAGE");
        summary.put("requestId", bounded(request.getString("requestId"), 128));
        summary.put("nextToken", bounded(request.getString("nextToken"), 96));
        summary.put("maxResults", bounded(request.getString("maxResults"), 16));
        execution.setRequestSummary(summary.toJSONString());
        credentials.set(extractCredentials(request));
        try {
            // 日志登记是同步入口的一部分，无法登记时直接终止，避免产生不可追踪的业务同步。
            writer.start(execution); // 不能静默放行没有执行日志的同步。
        } catch (IllegalArgumentException invalidBatch) {
            credentials.remove();
            throw invalidBatch;
        } catch (RuntimeException persistenceError) {
            log.error("SYNC_LOG_START_FAILED traceId={} stack={}", execution.getTraceId(),
                SyncErrorSanitizer.stackTrace(persistenceError, credentials.get()));
            credentials.remove();
            throw new IllegalStateException("无法创建同步日志，请检查日志表迁移和数据库连接，链路编号：" + execution.getTraceId());
        }
        log.info("SYNC_STARTED traceId={} executionId={}", execution.getTraceId(), execution.getId());
        // 使用单调时钟统计耗时，并用线程上下文关联本页所有上游调用。
        long started = System.nanoTime();
        current.set(execution);
        JSONObject response = null;
        try {
            JSONObject params = JSONObject.parseObject(request.getString("params"));
            summary.put("sourceFormId", params == null ? null : bounded(params.getString("selectedFormId"), 128));
            execution.setRequestSummary(summary.toJSONString());
            response = action.call();
            // 检查返回页及游标推进情况，汇总成功条数、限额状态和响应摘要。
            complete(execution, response);
            return response;
        } catch (Exception error) {
            // 异常同时记录分类、可展示说明和堆栈；返回前再次脱敏并附上链路编号。
            execution.setStatus("FAILED");
            execution.setErrorCode(errorCode(error));
            execution.setErrorMessage(SyncErrorSanitizer.sanitize(errorMessage(error), credentials.get()));
            execution.setErrorStack(SyncErrorSanitizer.stackTrace(error, credentials.get()));
            log.error("SYNC_FAILED traceId={} executionId={} pageId={} stack={}", execution.getTraceId(),
                execution.getId(), execution.getPageId(), execution.getErrorStack());
            throw new IllegalStateException(execution.getErrorMessage() + "，链路编号：" + execution.getTraceId(), error);
        } finally {
            // 无论成功失败都释放线程上下文，防止线程池复用时把其他请求归入当前批次。
            current.remove();
            execution.setDuration(elapsed(started));
            try {
                // 结果落库成功后才改写对外续传令牌，确保客户端游标与持久化的分页状态一致。
                writer.finish(execution);
                if (response != null && !"FAILED".equals(execution.getStatus())) {
                    response.put("hasMore", execution.isHasMore());
                    response.put("nextToken", execution.isHasMore()
                        ? SyncPageToken.encode(execution.getTraceId(), execution.getNextOffset()) : "");
                }
                log.info("SYNC_FINISHED traceId={} executionId={} status={} returnedCount={} durationMs={}",
                    execution.getTraceId(), execution.getId(), execution.getStatus(), execution.getSuccess(), execution.getDuration());
            } catch (RuntimeException persistenceError) {
                log.error("SYNC_LOG_PERSIST_FAILED traceId={} executionId={} stack={}",
                    execution.getTraceId(), execution.getId(), SyncErrorSanitizer.stackTrace(persistenceError, credentials.get()));
                throw new IllegalStateException("同步日志保存失败，链路编号：" + execution.getTraceId());
            } finally {
                credentials.remove();
            }
        }
    }

    /** 在当前同步上下文中记录单次上游接口调用的结果与耗时。 */
    public <T> T api(String name, Callable<T> action) throws Exception {
        SyncExecution execution = current.get();
        // 不处于同步批次内的调用按原逻辑执行，不额外虚构执行上下文。
        if (execution == null) return action.call();
        SyncExecution.ApiCall call = new SyncExecution.ApiCall();
        call.setName(name);
        call.setOccurredAt(new Date());
        long started = System.nanoTime();
        try {
            T result = action.call();
            call.setResult("SUCCESS");
            return result;
        } catch (Exception error) {
            call.setErrorCode(errorCode(error));
            call.setErrorMessage(SyncErrorSanitizer.sanitize(errorMessage(error), credentials.get()));
            call.setErrorStack(SyncErrorSanitizer.stackTrace(error, credentials.get()));
            // 仅在确实存在金蝶业务拒绝时记录上游业务码，不把HTTP状态或包装异常混作业务码。
            com.dj.middle.kingdee.util.KingdeeQueryException rejection = rejection(error);
            if (rejection != null) {
                String code = rejection.getBusinessCode();
                // 仅保存格式和长度受限的真实上游业务码；HTTP状态仍然未知。
                if (code != null && code.matches("[A-Za-z0-9_.-]{1,64}")) call.setBusinessCode(code);
            }
            call.setResult("UPSTREAM_TIMEOUT".equals(call.getErrorCode()) ? "TIMEOUT" : "FAILED");
            throw error;
        } finally {
            call.setDuration(elapsed(started));
            execution.getCalls().add(call);
        }
    }

    /** 检查返回页及游标推进情况，汇总成功条数、限额状态和响应摘要。 */
    static void complete(SyncExecution execution, JSONObject result) {
        JSONArray records = result.getJSONArray("records");
        if (records == null) throw new IllegalStateException("同步响应缺少记录数组");
        java.util.List<String> ids = new java.util.ArrayList<>();
        // 权益限额提示不计入业务记录数，批次可标为部分成功而非未知错误。
        boolean limited = "SYNC_LIMIT_REACHED".equals(execution.getErrorCode());
        for (int i = 0; i < records.size(); i++) {
            JSONObject row = records.getJSONObject(i);
            if ("当前数据已达到同步上限，可升级权益以同步更多数据".equals(row.getString("id"))) {
                limited = true;
            } else {
                ids.add(bounded(row.getString("id"), 191));
            }
        }
        boolean hasMore = result.getBooleanValue("hasMore");
        int nextOffset = hasMore ? Integer.parseInt(result.getString("nextToken")) : Math.addExact(execution.getPageOffset(), ids.size());
        // 返回有后续页但游标未前进时立即报错，避免客户端无限请求同一页。
        if (hasMore && (ids.isEmpty() || nextOffset <= execution.getPageOffset()))
            throw new IllegalStateException("分页游标未前进");
        execution.setHasMore(hasMore);
        execution.setNextOffset(nextOffset);
        execution.getSourceIds().addAll(ids);
        execution.setSuccess(execution.getSourceIds().size());
        execution.setTotal(execution.getSuccess() + execution.getSkipped());
        execution.setStatus(limited ? "PARTIAL" : "SUCCESS");
        if (limited) {
            execution.setErrorCode("SYNC_LIMIT_REACHED");
            execution.setErrorMessage("达到同步上限；未读取数据数量未知，限额提示不计入记录数");
        }
        JSONObject summary = new JSONObject();
        summary.put("returnedCount", execution.getSuccess());
        summary.put("hasMore", result.getBooleanValue("hasMore"));
        summary.put("scope", "SOURCE_PAGE_RETURNED");
        execution.setResponseSummary(summary.toJSONString());
    }

    /** 读取当前同步上下文的分页偏移，不允许脱离批次使用。 */
    public int currentOffset() {
        SyncExecution execution = current.get();
        if (execution == null) throw new IllegalStateException("同步上下文不存在");
        return execution.getPageOffset();
    }

    /** 从本次请求提取仅用于内存脱敏的凭证值。 */
    private static java.util.List<String> extractCredentials(JSONObject request) {
        java.util.List<String> values = new java.util.ArrayList<>();
        try {
            JSONObject params = JSONObject.parseObject(request.getString("params"));
            if (params != null) for (String key : java.util.List.of("APP_SECRET", "APP_ID", "USER_NAME", "CID", "SERVER_URL", "password", "access_token")) {
                String value = params.getString(key);
                if (value != null && !value.isBlank()) values.add(value);
            }
        } catch (RuntimeException ignored) { /* Regex redaction still handles malformed credential fields. */ }
        return values;
    }

    /** 企业批次尚未建立时发生的输入和签名错误也要执行脱敏。 对尚未建立同步上下文的入口异常也执行凭证脱敏。 */
    public String sanitizedStack(Throwable error, String rawBody) {
        return SyncErrorSanitizer.stackTrace(error, credentialsFromBody(rawBody));
    }

    /** 生成面向前端的脱敏异常说明。 */
    public String clientMessage(Throwable error, String rawBody) {
        return SyncErrorSanitizer.sanitize(error.getMessage(), credentialsFromBody(rawBody));
    }

    /** 从原始请求安全提取凭证；请求损坏时返回空列表交由规则脱敏。 */
    private static java.util.List<String> credentialsFromBody(String rawBody) {
        try { return extractCredentials(JSONObject.parseObject(rawBody)); }
        catch (RuntimeException invalidJson) { return java.util.List.of(); }
    }

    /** 标记当前批次已达到同步权益上限。 */
    public void limitReached() {
        SyncExecution execution = current.get();
        if (execution != null) execution.setErrorCode("SYNC_LIMIT_REACHED");
    }

    /** 沿异常原因链识别查询拒绝、超时等类型并返回统一错误编码。 */
    static String errorCode(Throwable error) {
        SubscriptionAccessException access = SubscriptionAccessException.find(error);
        if (access != null) return access.getErrorCode();
        if (rejection(error) != null) return "KINGDEE_QUERY_REJECTED";
        java.util.Set<Throwable> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Throwable cause = error; cause != null && seen.add(cause); cause = cause.getCause()) {
            if (cause instanceof java.net.SocketTimeoutException || cause instanceof java.util.concurrent.TimeoutException)
                return "UPSTREAM_TIMEOUT";
        }
        return error instanceof IllegalArgumentException ? "INVALID_SYNC_REQUEST" : "SYNC_QUERY_FAILED";
    }

    /** 从异常原因链提取可读错误说明。 */
    static String errorMessage(Throwable error) {
        SubscriptionAccessException access = SubscriptionAccessException.find(error);
        if (access != null) return access.getMessage();
        com.dj.middle.kingdee.util.KingdeeQueryException rejection = rejection(error);
        if (rejection != null) return rejection.getMessage();
        return switch (errorCode(error)) {
            case "UPSTREAM_TIMEOUT" -> "金蝶接口调用超时";
            case "INVALID_SYNC_REQUEST" -> "同步参数无效，请检查表单、筛选与分页设置";
            default -> "查询或转换失败，请根据链路编号检查接口调用记录";
        };
    }

    /** 沿原因链寻找金蝶业务拒绝异常，防止误把网络错误当作业务错误。 */
    private static com.dj.middle.kingdee.util.KingdeeQueryException rejection(Throwable error) {
        java.util.Set<Throwable> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Throwable cause = error; cause != null && seen.add(cause); cause = cause.getCause()) {
            if (cause instanceof com.dj.middle.kingdee.util.KingdeeQueryException rejection) return rejection;
        }
        return null;
    }

    /** 将单调时钟的耗时转换为毫秒。 */
    private static long elapsed(long start) { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start); }
    /** 限制日志摘要字段长度，避免外部输入无限扩张。 */
    private static String bounded(String value, int max) { return value == null ? "" : value.substring(0, Math.min(value.length(), max)); }
}
