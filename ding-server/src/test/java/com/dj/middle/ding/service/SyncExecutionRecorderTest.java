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

/** SyncExecutionRecorderTest：使用隔离夹具验证业务边界与历史兼容，不连接真实业务环境。 */
class SyncExecutionRecorderTest {
    /** 测试中记录的持久化调用次数。 */
    private final List<String> writes = new ArrayList<>();
    /** 测试中捕获的已保存同步批次。 */
    private SyncExecution saved;
    /** 模拟开始同步时的持久化失败。 */
    private boolean failStart;
    /** 模拟结束同步时的持久化失败。 */
    private boolean failFinish;
    /** 提供同步执行批次数据访问能力的映射接口。 */
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
    /** 记录同步执行生命周期的组件。 */
    private final SyncExecutionRecorder recorder = new SyncExecutionRecorder(new SyncExecutionWriter(mapper));

    /** 构造不含真实业务凭证的同步请求。 */
    private JSONObject request() {
        JSONObject request = JSONObject.parseObject("{\"requestId\":\"r1\",\"context\":{\"corpId\":\"corp1\"},\"nextToken\":0,\"maxResults\":300}");
        request.put("params", "{\"selectedFormId\":\"TEST\",\"APP_SECRET\":\"super-secret\"}");
        return request;
    }

    /** 验证：成功页记录源标识与接口计数但不保存业务正文。 */
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

    /** 验证：空页仍属于成功并应留执行记录。 */
    @Test void emptyPageIsStillSuccessfulAndRecorded() throws Exception {
        recorder.record(request(), () -> JSONObject.parseObject("{\"records\":[],\"hasMore\":false}"));
        assertEquals("SUCCESS", saved.getStatus());
        assertEquals(0, saved.getTotal());
        assertEquals(List.of("start", "finish", "callback"), writes);
    }

    /** 验证：上游超时正确分类且线程上下文始终清理。 */
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

    /** 验证：配置解析损坏也能登记失败结果。 */
    @Test void malformedParamsStillFinishFailure() {
        JSONObject request = request();
        request.put("params", "not-json");
        assertThrows(Exception.class, () -> recorder.record(request, () -> { fail("Must not query"); return null; }));
        assertEquals("FAILED", saved.getStatus());
        assertEquals(List.of("start", "finish", "callback"), writes);
    }

    /** 验证：限额提示不计为真实业务记录。 */
    @Test void limitNoticeIsNotARealRecord() throws Exception {
        recorder.record(request(), () -> JSONObject.parseObject("{\"records\":[{\"id\":\"1\"},{\"id\":\"当前数据已达到同步上限，可升级权益以同步更多数据\"}]}"));
        assertEquals("PARTIAL", saved.getStatus());
        assertEquals(1, saved.getTotal());
        assertEquals(1, saved.getSuccess());
        assertEquals(0, saved.getSkipped());
    }

    /** 验证：明细限额与表头限额统计口径一致。 */
    @Test void detailLimitHasSameSemantics() throws Exception {
        recorder.record(request(), () -> { recorder.limitReached(); return JSONObject.parseObject("{\"records\":[]}"); });
        assertEquals("PARTIAL", saved.getStatus());
        assertEquals("SYNC_LIMIT_REACHED", saved.getErrorCode());
    }

    /** 验证：日志数据库故障不能降级为无日志成功。 */
    @Test void databaseFailureCannotBecomeUnloggedSuccess() {
        failStart = true;
        assertThrows(Exception.class, () -> recorder.record(request(), () -> { fail("Must not query"); return null; }));
        failStart = false;
        failFinish = true;
        Exception error = assertThrows(Exception.class, () -> recorder.record(request(), () -> JSONObject.parseObject("{\"records\":[]}")));
        assertTrue(error.getMessage().contains("同步日志保存失败"));
    }

    /** 验证：无效企业请求不能生成虚假的可信企业日志。 */
    @Test void unknownTenantDoesNotCreateFakeTenantLog() {
        JSONObject request = request();
        request.remove("context");
        assertThrows(IllegalArgumentException.class, () -> recorder.record(request, () -> null));
        assertTrue(writes.isEmpty());
    }

    /** 验证：首次请求的幂等键在重试时保持稳定。 */
    @Test void initialDeliveryKeyIsStableForRetry() throws Exception {
        recorder.record(request(), () -> JSONObject.parseObject("{\"records\":[]}"));
        String previous = saved.getRequestKey();
        recorder.record(request(), () -> JSONObject.parseObject("{\"records\":[]}"));
        assertEquals(previous, saved.getRequestKey());
    }

    /** 验证：多层包装的业务异常仍可读且凭证被脱敏。 */
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

    /** 验证：错误脱敏覆盖请求头引号密钥及地址并限制长度。 */
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
