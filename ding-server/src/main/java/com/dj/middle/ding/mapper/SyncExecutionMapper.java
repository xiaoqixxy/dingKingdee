package com.dj.middle.ding.mapper;

import com.dj.middle.ding.domain.SyncExecution;
import org.apache.ibatis.annotations.*;

/** 同步执行批次数据访问契约。 */
public interface SyncExecutionMapper {
    /** 同步执行批次查询共用的固定字段投影。 */
    String BATCH = "SELECT id,ding_corp_id AS corpId,trace_id AS traceId,source_key AS sourceKey,log_scope AS logScope,"
        + "status,total_count AS total,success_count AS success,skipped_count AS skipped,COALESCE(duration_ms,0) AS duration,"
        + "page_count AS pageCount,next_offset AS expectedOffset,error_code AS errorCode,error_message AS errorMessage,error_stack AS errorStack FROM ding_sync_execution ";

    /** 按企业及幂等请求键锁定批次，复用重复的首次请求。 */
    @Select(BATCH + "WHERE ding_corp_id=#{corpId} AND request_key=#{requestKey} FOR UPDATE")
    SyncExecution byRequest(SyncExecution execution);
    /** 按企业及续传链路编号锁定已有批次。 */
    @Select(BATCH + "WHERE ding_corp_id=#{corpId} AND trace_id=#{continuationTrace} FOR UPDATE")
    SyncExecution byTrace(SyncExecution execution);
    /** 按企业及批次主键加行锁，串行合并分页结果。 */
    @Select(BATCH + "WHERE ding_corp_id=#{corpId} AND id=#{id} FOR UPDATE")
    SyncExecution lock(SyncExecution execution);

    /** 登记一次分页处理尝试并回填页记录主键。 */
    @Insert("INSERT INTO ding_sync_execution_page (execution_id,ding_corp_id,page_offset,status,request_summary,started_at) "
        + "VALUES (#{id},#{corpId},#{pageOffset},'RUNNING',#{requestSummary},CURRENT_TIMESTAMP(3))")
    @Options(useGeneratedKeys = true, keyProperty = "pageId")
    int startPage(SyncExecution execution);

    /** 查找同一偏移此前已经成功计数的页，供重试幂等复用。 */
    @Select("SELECT next_offset AS nextOffset,has_more AS hasMore FROM ding_sync_execution_page "
        + "WHERE execution_id=#{id} AND ding_corp_id=#{corpId} AND page_offset=#{pageOffset} AND counted=1 LIMIT 1")
    SyncExecution acceptedPage(SyncExecution execution);

    /** 保存本页完成状态、游标、计数与错误摘要。 */
    @Update("UPDATE ding_sync_execution_page SET status=#{status},success_count=#{success},skipped_count=#{skipped},counted=#{counted},"
        + "has_more=#{hasMore},next_offset=#{nextOffset},duration_ms=#{duration},error_code=#{errorCode},error_message=#{errorMessage},"
        + "error_stack=#{errorStack},request_summary=#{requestSummary},response_summary=#{responseSummary},finished_at=CURRENT_TIMESTAMP(3) "
        + "WHERE id=#{pageId} AND execution_id=#{id} AND ding_corp_id=#{corpId} AND status='RUNNING'")
    int finishPage(SyncExecution execution);

    /** 插入新的同步批次及初始执行状态。 */
    @Insert("INSERT INTO ding_sync_execution (ding_corp_id,sync_form_id,trace_id,request_key,source_key,log_scope,next_offset,trigger_type,direction,status,started_at) "
        + "VALUES (#{corpId},NULL,#{traceId},#{requestKey},#{sourceKey},#{logScope},#{pageOffset},'EVENT','KINGDEE_TO_DING','RUNNING',CURRENT_TIMESTAMP(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int start(SyncExecution execution);

    /** 更新已锁定批次的累计统计、下一偏移与最终状态。 */
    @Update("UPDATE ding_sync_execution SET status=#{status},total_count=#{total},success_count=#{success},failed_count=0,"
        + "skipped_count=#{skipped},duration_ms=#{duration},page_count=#{pageCount},next_offset=#{expectedOffset},"
        + "finished_at=CASE WHEN #{status}='RUNNING' THEN NULL ELSE CURRENT_TIMESTAMP(3) END,"
        + "error_code=#{errorCode},error_message=#{errorMessage},error_stack=#{errorStack} WHERE id=#{id} AND ding_corp_id=#{corpId}")
    int finish(SyncExecution execution);

    /** 保存或查询同步批次关联的源记录明细。 */
    @Insert("<script>INSERT INTO ding_sync_execution_item (execution_id,ding_corp_id,item_no,source_record_id,status) VALUES "
        + "<foreach collection='sourceIds' item='sourceId' index='i' separator=','>(#{id},#{corpId},#{pageOffset}+#{i}+1,#{sourceId},'SUCCESS')</foreach></script>")
    int items(SyncExecution execution);

    // SDK没有提供原始传输响应，HTTP状态明确保持未知，不能根据业务码推测。
    /** 在当前同步上下文中记录单次上游接口调用的结果与耗时。 */
    @Insert("INSERT INTO ding_api_call_log (ding_corp_id,trace_id,execution_id,page_id,platform,api_name,http_method,"
        + "business_code,result,duration_ms,error_message,error_stack,occurred_at) VALUES "
        + "(#{e.corpId},#{e.traceId},#{e.id},#{e.pageId},'KINGDEE',#{a.name},'POST',#{a.businessCode},#{a.result},#{a.duration},#{a.errorMessage},#{a.errorStack},#{a.occurredAt})")
    /** 写入本次上游接口调用日志，HTTP状态因SDK未提供而保持未知。 */
    int api(@Param("e") SyncExecution execution, @Param("a") SyncExecution.ApiCall call);

    /** 验证支付通知后解析支付结果，更新订单并创建订阅和开票申请。 */
    @Insert("INSERT INTO ding_api_call_log (ding_corp_id,trace_id,execution_id,page_id,platform,api_name,request_path,http_method,"
        + "business_code,result,duration_ms,error_message,error_stack,request_summary,response_summary,occurred_at) VALUES "
        + "(#{corpId},#{traceId},#{id},#{pageId},'DING','records','/api/records','POST',CASE WHEN #{status}='FAILED' THEN '10005' ELSE '0' END,"
        + "CASE WHEN #{status}='FAILED' THEN 'FAILED' ELSE 'SUCCESS' END,#{duration},#{errorMessage},#{errorStack},"
        + "#{requestSummary},#{responseSummary},CURRENT_TIMESTAMP(3))")
    /** 记录本页回调的审计信息，不将数据源返回视为钉钉已经落表。 */
    int callback(SyncExecution execution);
}
