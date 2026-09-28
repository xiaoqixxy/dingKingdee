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

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncExecutionRecorder {
    private final SyncExecutionWriter writer;
    private final ThreadLocal<SyncExecution> current = new ThreadLocal<>();
    private final ThreadLocal<java.util.List<String>> credentials = new ThreadLocal<>();

    public JSONObject record(JSONObject request, Callable<JSONObject> action) throws Exception {
        JSONObject context = request.getJSONObject("context");
        String corpId = context == null ? null : context.getString("corpId");
        if (corpId == null || corpId.isBlank() || corpId.length() > 64)
            throw new IllegalArgumentException("缺少有效企业ID");
        SyncExecution execution = new SyncExecution();
        execution.setCorpId(corpId);
        execution.setTraceId(UUID.randomUUID().toString());
        SyncPageToken.Position position = SyncPageToken.parse(request.getString("nextToken"));
        execution.setPageOffset(position.offset());
        execution.setContinuationTrace(position.trace());
        execution.setSourceKey(SyncPageToken.sourceKey(request));
        if (position.trace() == null && position.offset() > 0) execution.setLogScope("LEGACY");
        String requestId = request.getString("requestId");
        execution.setRequestKey(requestId == null || requestId.isBlank() ? execution.getTraceId()
            : SyncPageToken.digest(requestId + ":" + position.offset() + ":" + execution.getSourceKey()));
        JSONObject summary = new JSONObject();
        summary.put("scope", "BATCH_PAGE");
        summary.put("requestId", bounded(request.getString("requestId"), 128));
        summary.put("nextToken", bounded(request.getString("nextToken"), 96));
        summary.put("maxResults", bounded(request.getString("maxResults"), 16));
        execution.setRequestSummary(summary.toJSONString());
        credentials.set(extractCredentials(request));
        try {
            writer.start(execution); // Do not silently serve an unrecorded synchronization.
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
        long started = System.nanoTime();
        current.set(execution);
        JSONObject response = null;
        try {
            JSONObject params = JSONObject.parseObject(request.getString("params"));
            summary.put("sourceFormId", params == null ? null : bounded(params.getString("selectedFormId"), 128));
            execution.setRequestSummary(summary.toJSONString());
            response = action.call();
            complete(execution, response);
            return response;
        } catch (Exception error) {
            execution.setStatus("FAILED");
            execution.setErrorCode(errorCode(error));
            execution.setErrorMessage(SyncErrorSanitizer.sanitize(errorMessage(error), credentials.get()));
            execution.setErrorStack(SyncErrorSanitizer.stackTrace(error, credentials.get()));
            log.error("SYNC_FAILED traceId={} executionId={} pageId={} stack={}", execution.getTraceId(),
                execution.getId(), execution.getPageId(), execution.getErrorStack());
            throw new IllegalStateException(execution.getErrorMessage() + "，链路编号：" + execution.getTraceId(), error);
        } finally {
            current.remove();
            execution.setDuration(elapsed(started));
            try {
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

    public <T> T api(String name, Callable<T> action) throws Exception {
        SyncExecution execution = current.get();
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
            com.dj.middle.kingdee.util.KingdeeQueryException rejection = rejection(error);
            if (rejection != null) {
                String code = rejection.getBusinessCode();
                // Only preserve a real, bounded upstream code. HTTP status remains unknown.
                if (code != null && code.matches("[A-Za-z0-9_.-]{1,64}")) call.setBusinessCode(code);
            }
            call.setResult("UPSTREAM_TIMEOUT".equals(call.getErrorCode()) ? "TIMEOUT" : "FAILED");
            throw error;
        } finally {
            call.setDuration(elapsed(started));
            execution.getCalls().add(call);
        }
    }

    static void complete(SyncExecution execution, JSONObject result) {
        JSONArray records = result.getJSONArray("records");
        if (records == null) throw new IllegalStateException("同步响应缺少记录数组");
        java.util.List<String> ids = new java.util.ArrayList<>();
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

    public int currentOffset() {
        SyncExecution execution = current.get();
        if (execution == null) throw new IllegalStateException("同步上下文不存在");
        return execution.getPageOffset();
    }

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

    /** Also covers input/signature errors before a trusted tenant batch can be created. */
    public String sanitizedStack(Throwable error, String rawBody) {
        return SyncErrorSanitizer.stackTrace(error, credentialsFromBody(rawBody));
    }

    public String clientMessage(Throwable error, String rawBody) {
        return SyncErrorSanitizer.sanitize(error.getMessage(), credentialsFromBody(rawBody));
    }

    private static java.util.List<String> credentialsFromBody(String rawBody) {
        try { return extractCredentials(JSONObject.parseObject(rawBody)); }
        catch (RuntimeException invalidJson) { return java.util.List.of(); }
    }

    public void limitReached() {
        SyncExecution execution = current.get();
        if (execution != null) execution.setErrorCode("SYNC_LIMIT_REACHED");
    }

    static String errorCode(Throwable error) {
        if (rejection(error) != null) return "KINGDEE_QUERY_REJECTED";
        java.util.Set<Throwable> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Throwable cause = error; cause != null && seen.add(cause); cause = cause.getCause()) {
            if (cause instanceof java.net.SocketTimeoutException || cause instanceof java.util.concurrent.TimeoutException)
                return "UPSTREAM_TIMEOUT";
        }
        return error instanceof IllegalArgumentException ? "INVALID_SYNC_REQUEST" : "SYNC_QUERY_FAILED";
    }

    static String errorMessage(Throwable error) {
        com.dj.middle.kingdee.util.KingdeeQueryException rejection = rejection(error);
        if (rejection != null) return rejection.getMessage();
        return switch (errorCode(error)) {
            case "UPSTREAM_TIMEOUT" -> "金蝶接口调用超时";
            case "INVALID_SYNC_REQUEST" -> "同步参数无效，请检查表单、筛选与分页设置";
            default -> "查询或转换失败，请根据链路编号检查接口调用记录";
        };
    }

    private static com.dj.middle.kingdee.util.KingdeeQueryException rejection(Throwable error) {
        java.util.Set<Throwable> seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Throwable cause = error; cause != null && seen.add(cause); cause = cause.getCause()) {
            if (cause instanceof com.dj.middle.kingdee.util.KingdeeQueryException rejection) return rejection;
        }
        return null;
    }

    private static long elapsed(long start) { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start); }
    private static String bounded(String value, int max) { return value == null ? "" : value.substring(0, Math.min(value.length(), max)); }
}
