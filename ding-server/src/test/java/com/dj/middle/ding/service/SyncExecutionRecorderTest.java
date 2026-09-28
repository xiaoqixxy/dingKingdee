package com.dj.middle.ding.service;

import com.alibaba.fastjson2.JSONObject;
import com.dj.middle.ding.domain.SyncExecution;
import com.dj.middle.ding.mapper.SyncExecutionMapper;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SyncExecutionRecorderTest {
    private final List<String> writes = new ArrayList<>();
    private SyncExecution saved;
    private boolean failStart;
    private boolean failFinish;
    private final SyncExecutionMapper mapper = (SyncExecutionMapper) Proxy.newProxyInstance(
        SyncExecutionMapper.class.getClassLoader(), new Class[]{SyncExecutionMapper.class}, (proxy, method, args) -> {
            if (List.of("byRequest", "lock").contains(method.getName())) {
                SyncExecution batch = new SyncExecution();
                batch.setId(saved.getId()); batch.setCorpId(saved.getCorpId()); batch.setTraceId(saved.getTraceId());
                batch.setSourceKey(saved.getSourceKey()); batch.setExpectedOffset(saved.getPageOffset());
                return batch;
            }
            if ("acceptedPage".equals(method.getName())) return null;
            if ("startPage".equals(method.getName())) { ((SyncExecution)args[0]).setPageId(1L); return 1; }
            if ("finishPage".equals(method.getName())) return 1;
            writes.add(method.getName());
            if ("start".equals(method.getName())) {
                if (failStart) throw new IllegalStateException("DB unavailable");
                saved = (SyncExecution) args[0];
                saved.setId(7L);
                assertEquals("RUNNING", saved.getStatus());
            }
            if ("finish".equals(method.getName()) && failFinish) throw new IllegalStateException("DB unavailable");
            return 1;
        });
    private final SyncExecutionRecorder recorder = new SyncExecutionRecorder(new SyncExecutionWriter(mapper));

    private JSONObject request() {
        JSONObject request = JSONObject.parseObject("{\"requestId\":\"r1\",\"context\":{\"corpId\":\"corp1\"},\"nextToken\":0,\"maxResults\":300}");
        request.put("params", "{\"selectedFormId\":\"TEST\",\"APP_SECRET\":\"super-secret\"}");
        return request;
    }

    @Test void successHasItemsAndBothApiCallsWithoutPayload() throws Exception {
        JSONObject response = JSONObject.parseObject("{\"records\":[{\"id\":\"42\",\"fields\":{\"salary\":1234}}],\"hasMore\":false}");
        assertSame(response, recorder.record(request(), () -> {
            assertEquals(List.of("start"), writes);
            recorder.api("QueryBusinessInfo", () -> "metadata");
            return recorder.api("ExecuteBillQuery", () -> response);
        }));
        assertEquals("SUCCESS", saved.getStatus());
        assertEquals(1, saved.getTotal());
        assertEquals(List.of("42"), saved.getSourceIds());
        assertEquals(List.of("start", "items", "finish", "api", "api", "callback"), writes);
        assertFalse(saved.getRequestSummary().contains("super-secret"));
        assertFalse(saved.getResponseSummary().contains("salary"));
        assertTrue(saved.getRequestSummary().contains("TEST"));
    }

    @Test void emptyPageIsStillSuccessfulAndRecorded() throws Exception {
        recorder.record(request(), () -> JSONObject.parseObject("{\"records\":[],\"hasMore\":false}"));
        assertEquals("SUCCESS", saved.getStatus());
        assertEquals(0, saved.getTotal());
        assertEquals(List.of("start", "finish", "callback"), writes);
    }

    @Test void upstreamTimeoutIsRecordedAndThreadContextCleared() throws Exception {
        Exception thrown = assertThrows(Exception.class, () -> recorder.record(request(), () ->
            recorder.api("ExecuteBillQuery", () -> { throw new RuntimeException(new SocketTimeoutException("APP_SECRET=secret")); })));
        assertEquals("FAILED", saved.getStatus());
        assertEquals("UPSTREAM_TIMEOUT", saved.getErrorCode());
        assertEquals("TIMEOUT", saved.getCalls().get(0).getResult());
        assertFalse(thrown.getMessage().contains("APP_SECRET"));
        assertTrue(thrown.getMessage().contains(saved.getTraceId()));
        recorder.api("unrelated", () -> null);
        assertEquals(1, saved.getCalls().size());
    }

    @Test void malformedParamsStillFinishFailure() {
        JSONObject request = request();
        request.put("params", "not-json");
        assertThrows(Exception.class, () -> recorder.record(request, () -> { fail("Must not query"); return null; }));
        assertEquals("FAILED", saved.getStatus());
        assertEquals(List.of("start", "finish", "callback"), writes);
    }

    @Test void limitNoticeIsNotARealRecord() throws Exception {
        recorder.record(request(), () -> JSONObject.parseObject("{\"records\":[{\"id\":\"1\"},{\"id\":\"当前数据已达到同步上限，可升级权益以同步更多数据\"}]}"));
        assertEquals("PARTIAL", saved.getStatus());
        assertEquals(1, saved.getTotal());
        assertEquals(1, saved.getSuccess());
        assertEquals(0, saved.getSkipped());
    }

    @Test void detailLimitHasSameSemantics() throws Exception {
        recorder.record(request(), () -> { recorder.limitReached(); return JSONObject.parseObject("{\"records\":[]}"); });
        assertEquals("PARTIAL", saved.getStatus());
        assertEquals("SYNC_LIMIT_REACHED", saved.getErrorCode());
    }

    @Test void databaseFailureCannotBecomeUnloggedSuccess() {
        failStart = true;
        assertThrows(Exception.class, () -> recorder.record(request(), () -> { fail("Must not query"); return null; }));
        failStart = false;
        failFinish = true;
        Exception error = assertThrows(Exception.class, () -> recorder.record(request(), () -> JSONObject.parseObject("{\"records\":[]}")));
        assertTrue(error.getMessage().contains("同步日志保存失败"));
    }

    @Test void unknownTenantDoesNotCreateFakeTenantLog() {
        JSONObject request = request();
        request.remove("context");
        assertThrows(IllegalArgumentException.class, () -> recorder.record(request, () -> null));
        assertTrue(writes.isEmpty());
    }

    @Test void initialDeliveryKeyIsStableForRetry() throws Exception {
        recorder.record(request(), () -> JSONObject.parseObject("{\"records\":[]}"));
        String previous = saved.getRequestKey();
        recorder.record(request(), () -> JSONObject.parseObject("{\"records\":[]}"));
        assertEquals(previous, saved.getRequestKey());
    }

    @Test void wrappedUpstreamBusinessErrorIsUsefulAndRedacted() {
        Exception error = assertThrows(Exception.class, () -> recorder.record(request(), () ->
            recorder.api("ExecuteBillQuery", () -> {
                throw new RuntimeException(new com.dj.middle.kingdee.util.KingdeeQueryException("500",
                    "对象名 'FormGroup_L' 无效。 secret echo super-secret APP_SECRET=another-secret"));
            })));
        assertEquals("KINGDEE_QUERY_REJECTED", saved.getErrorCode());
        assertEquals("500", saved.getCalls().get(0).getBusinessCode());
        assertTrue(error.getMessage().contains("FormGroup_L"));
        assertFalse(error.getMessage().contains("super-secret"));
        assertFalse(error.getMessage().contains("another-secret"));
        assertEquals(saved.getErrorMessage(), saved.getCalls().get(0).getErrorMessage());
    }

    @Test void sanitizerRemovesHeadersQuotedSecretsUrlsAndBoundsMessages() {
        String safe = SyncErrorSanitizer.sanitize("对象名 'FormGroup_L' 无效。\nAPP_SECRET: \"secret with spaces\"\n"
            + "Authorization: Bearer private-token\nCookie: a=b; c=d\nhttps://host/path?token=private\n"
            + "password='password value'", List.of());
        assertTrue(safe.contains("FormGroup_L"));
        for (String secret : List.of("secret with spaces", "private-token", "a=b", "c=d", "https://", "password value"))
            assertFalse(safe.contains(secret), secret);
        assertEquals(1000, SyncErrorSanitizer.sanitize("x".repeat(2000), List.of()).length());
    }
}
