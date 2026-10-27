package com.dj.middle.ding.service;

import com.alibaba.fastjson2.*;
import com.dj.middle.ding.domain.SyncExecution;
import com.dj.middle.ding.mapper.SyncExecutionMapper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** SyncExecutionPersistenceTest：使用隔离夹具验证业务边界与历史兼容，不连接真实业务环境。 */
class SyncExecutionPersistenceTest {
    /** 测试数据库访问工具。 */
    private JdbcTemplate jdbc;
    /** 记录同步执行生命周期的组件。 */
    private SyncExecutionRecorder recorder;

    /** 创建隔离的测试数据库与日志组件。 */
    @BeforeEach void setup() {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE ding_sync_execution (id bigint auto_increment primary key,ding_corp_id varchar(64),sync_form_id bigint null,trace_id varchar(64),request_key varchar(96),source_key varchar(64),log_scope varchar(16),page_count int default 0,next_offset int default 0,error_stack clob,trigger_type varchar(16),direction varchar(32),status varchar(16),started_at timestamp,finished_at timestamp,duration_ms bigint,total_count int default 0,success_count int default 0,failed_count int default 0,skipped_count int default 0,error_code varchar(64),error_message varchar(1000),UNIQUE(ding_corp_id,request_key))");
        jdbc.execute("CREATE TABLE ding_sync_execution_page (id bigint auto_increment primary key,execution_id bigint,ding_corp_id varchar(64),page_offset int,next_offset int default 0,has_more boolean default false,counted boolean default false,status varchar(16),success_count int default 0,skipped_count int default 0,duration_ms bigint,error_code varchar(64),error_message varchar(1000),error_stack clob,request_summary json,response_summary json,started_at timestamp,finished_at timestamp)");
        jdbc.execute("CREATE TABLE ding_sync_execution_item (id bigint auto_increment primary key,execution_id bigint,ding_corp_id varchar(64),item_no int,source_record_id varchar(191),status varchar(16),UNIQUE(execution_id,item_no))");
        jdbc.execute("CREATE TABLE ding_api_call_log (id bigint auto_increment primary key,ding_corp_id varchar(64),trace_id varchar(64),execution_id bigint,page_id bigint,platform varchar(20),api_name varchar(100),request_path varchar(255),http_method varchar(10),business_code varchar(64),result varchar(16),duration_ms bigint,error_message varchar(1000),error_stack clob,request_summary json,response_summary json,occurred_at timestamp)");
        Configuration config = new Configuration(new Environment("test", new SpringManagedTransactionFactory(), source));
        config.addMapper(SyncExecutionMapper.class);
        var mapper = new SqlSessionTemplate(new SqlSessionFactoryBuilder().build(config)).getMapper(SyncExecutionMapper.class);
        TransactionTemplate tx = new TransactionTemplate(new DataSourceTransactionManager(source));
        recorder = new SyncExecutionRecorder(new SyncExecutionWriter(mapper) {
            /** 模拟开始一页同步并建立执行上下文。 */
            @Override public void start(SyncExecution e) { tx.executeWithoutResult(status -> super.start(e)); }
            /** 完成模拟分页执行并写入统计。 */
            @Override public void finish(SyncExecution e) { tx.executeWithoutResult(status -> super.finish(e)); }
        });
    }

    /** 构造不含真实业务凭证的同步请求。 */
    private JSONObject request(String token, String id) {
        return JSONObject.of("requestId", id, "context", JSONObject.of("corpId", "corp1"), "nextToken", token,
            "params", "{\"selectedFormId\":\"TEST\",\"APP_SECRET\":\"secret-value\"}");
    }
    /** 按指定范围生成模拟返回记录。 */
    private JSONObject rows(int count, boolean more) {
        int offset = recorder.currentOffset();
        JSONArray rows = new JSONArray();
        for (int i = 0; i < count; i++) rows.add(JSONObject.of("id", String.valueOf(offset+i)));
        return JSONObject.of("records", rows, "hasMore", more, "nextToken", more ? String.valueOf(offset+count) : "");
    }
    /** 组合一页的执行参数与结果。 */
    private JSONObject page(String token, String id, int count, boolean more) throws Exception {
        return recorder.record(request(token,id), () -> rows(count,more));
    }
    /** 执行统计SQL并读取测试数据库中的整数结果。 */
    private int number(String sql) { return jdbc.queryForObject(sql,Integer.class); }
    /** 读取测试数据库中的批次统计状态。 */
    private String state() { return jdbc.queryForObject("SELECT status FROM ding_sync_execution",String.class); }

    /** 验证：三页数据归入一个批次并完整记录645条源记录。 */
    @Test void threePagesProduceOneBatchAndAll645Items() throws Exception {
        JSONObject first=page("","first",300,true);
        assertEquals("RUNNING",state());
        String token=first.getString("nextToken");
        assertTrue(token.startsWith("s1:"));
        JSONObject second=page(token,"different-request-id",300,true);
        JSONObject third=page(second.getString("nextToken"),"third",45,false);
        assertEquals("",third.getString("nextToken"));
        assertEquals("SUCCESS",state());
        assertEquals(1,number("SELECT count(*) FROM ding_sync_execution"));
        assertEquals(645,number("SELECT success_count FROM ding_sync_execution"));
        assertEquals(3,number("SELECT page_count FROM ding_sync_execution"));
        assertEquals(645,number("SELECT count(*) FROM ding_sync_execution_item"));
        assertEquals(645,number("SELECT max(item_no) FROM ding_sync_execution_item"));
        assertEquals(1,number("SELECT count(DISTINCT trace_id) FROM ding_api_call_log"));
    }

    /** 验证：重复页与并发重试不重复累计统计。 */
    @Test void duplicatePagesAndConcurrentRetriesDoNotDoubleCount() throws Exception {
        String token=page("","first",300,true).getString("nextToken");
        assertEquals(token,page("","first",300,true).getString("nextToken"));
        CountDownLatch started=new CountDownLatch(2);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Callable<JSONObject> work=() -> recorder.record(request(token,UUID.randomUUID().toString()), () -> {
                started.countDown(); assertTrue(started.await(5,TimeUnit.SECONDS)); return rows(10,false);
            });
            var one=pool.submit(work);var two=pool.submit(work);
            one.get(10,TimeUnit.SECONDS);two.get(10,TimeUnit.SECONDS);
        } finally { pool.shutdownNow(); }
        assertEquals(310,number("SELECT success_count FROM ding_sync_execution"));
        assertEquals(2,number("SELECT page_count FROM ding_sync_execution"));
        assertEquals(4,number("SELECT count(*) FROM ding_sync_execution_page"));
        assertEquals(2,number("SELECT count(*) FROM ding_sync_execution_page WHERE counted=1"));
        assertEquals(310,number("SELECT count(*) FROM ding_sync_execution_item"));
    }

    /** 验证：失败保存完整脱敏堆栈，重试恢复且保留失败尝试。 */
    @Test void failureKeepsFullStackAndRetryRecoversWithoutLosingFailedAttempt() throws Exception {
        String token=page("","first",300,true).getString("nextToken");
        RuntimeException failure=new RuntimeException("APP_SECRET=secret-value",new IllegalStateException("root cause"));
        StackTraceElement[] frames=new StackTraceElement[100];
        Arrays.fill(frames,new StackTraceElement("example.LongStack","method","LongStack.java",42));
        failure.setStackTrace(frames);failure.addSuppressed(new IllegalArgumentException("suppressed"));
        assertThrows(Exception.class,() -> recorder.record(request(token,"failed"),() -> recorder.api("ExecuteBillQuery",() -> {throw failure;})));
        assertEquals("PARTIAL",state());
        String stack=jdbc.queryForObject("SELECT error_stack FROM ding_sync_execution",String.class);
        assertTrue(stack.length()>1000);assertTrue(stack.contains("Caused by:"));assertTrue(stack.contains("Suppressed:"));
        assertTrue(stack.contains("LongStack.java:42"));assertFalse(stack.contains("secret-value"));
        assertEquals(2,number("SELECT count(*) FROM ding_api_call_log WHERE error_stack IS NOT NULL"));
        page(token,"retry",10,false);
        assertEquals("SUCCESS",state());
        assertEquals(310,number("SELECT success_count FROM ding_sync_execution"));
        assertEquals(1,number("SELECT count(*) FROM ding_sync_execution_page WHERE status='FAILED' AND error_stack IS NOT NULL"));
    }

    /** 验证：令牌不能跨企业跨数据源或跳跃偏移。 */
    @Test void tokenCannotCrossTenantSourceOrSkipOffsets() throws Exception {
        String token=page("","first",300,true).getString("nextToken");
        JSONObject other=request(token,"other");other.getJSONObject("context").put("corpId","corp2");
        assertThrows(IllegalArgumentException.class,() -> recorder.record(other,() -> rows(1,false)));
        JSONObject changed=request(token,"changed");changed.put("params","{}");
        assertThrows(IllegalArgumentException.class,() -> recorder.record(changed,() -> rows(1,false)));
        String skipped=token.replace(":300",":600");
        assertThrows(IllegalArgumentException.class,() -> page(skipped,"skip",1,false));
        assertEquals(1,number("SELECT count(*) FROM ding_sync_execution_page"));
        page("","independent",1,false);
        assertEquals(2,number("SELECT count(*) FROM ding_sync_execution"));
    }

    /** 验证：接口日志写入失败时整次完成事务回滚。 */
    @Test void finishTransactionRollsBackIfApiLogWriteFails() {
        jdbc.execute("DROP TABLE ding_api_call_log");
        assertThrows(Exception.class,() -> page("","first",10,false));
        assertEquals("RUNNING",state());
        assertEquals(0,number("SELECT success_count FROM ding_sync_execution"));
        assertEquals(0,number("SELECT count(*) FROM ding_sync_execution_item"));
        assertEquals("RUNNING",jdbc.queryForObject("SELECT status FROM ding_sync_execution_page",String.class));
    }

    /** 验证：兼容旧数字游标并明确拒绝非法令牌。 */
    @Test void legacyNumericPageAndInvalidTokenAreHandledExplicitly() throws Exception {
        page("300","legacy",10,false);
        assertEquals("LEGACY",jdbc.queryForObject("SELECT log_scope FROM ding_sync_execution",String.class));
        assertThrows(IllegalArgumentException.class,() -> page("s1:invalid:300","invalid",1,false));
    }
}
