package com.dj.middle.ding.mapper;

import com.dj.middle.ding.domain.SyncExecution;
import org.apache.ibatis.annotations.*;

public interface SyncExecutionMapper {
    String BATCH = "SELECT id,ding_corp_id AS corpId,trace_id AS traceId,source_key AS sourceKey,log_scope AS logScope,"
        + "status,total_count AS total,success_count AS success,skipped_count AS skipped,COALESCE(duration_ms,0) AS duration,"
        + "page_count AS pageCount,next_offset AS expectedOffset,error_code AS errorCode,error_message AS errorMessage,error_stack AS errorStack FROM ding_sync_execution ";

    @Select(BATCH + "WHERE ding_corp_id=#{corpId} AND request_key=#{requestKey} FOR UPDATE")
    SyncExecution byRequest(SyncExecution execution);
    @Select(BATCH + "WHERE ding_corp_id=#{corpId} AND trace_id=#{continuationTrace} FOR UPDATE")
    SyncExecution byTrace(SyncExecution execution);
    @Select(BATCH + "WHERE ding_corp_id=#{corpId} AND id=#{id} FOR UPDATE")
    SyncExecution lock(SyncExecution execution);

    @Insert("INSERT INTO ding_sync_execution_page (execution_id,ding_corp_id,page_offset,status,request_summary,started_at) "
        + "VALUES (#{id},#{corpId},#{pageOffset},'RUNNING',#{requestSummary},CURRENT_TIMESTAMP(3))")
    @Options(useGeneratedKeys = true, keyProperty = "pageId")
    int startPage(SyncExecution execution);

    @Select("SELECT next_offset AS nextOffset,has_more AS hasMore FROM ding_sync_execution_page "
        + "WHERE execution_id=#{id} AND ding_corp_id=#{corpId} AND page_offset=#{pageOffset} AND counted=1 LIMIT 1")
    SyncExecution acceptedPage(SyncExecution execution);

    @Update("UPDATE ding_sync_execution_page SET status=#{status},success_count=#{success},skipped_count=#{skipped},counted=#{counted},"
        + "has_more=#{hasMore},next_offset=#{nextOffset},duration_ms=#{duration},error_code=#{errorCode},error_message=#{errorMessage},"
        + "error_stack=#{errorStack},request_summary=#{requestSummary},response_summary=#{responseSummary},finished_at=CURRENT_TIMESTAMP(3) "
        + "WHERE id=#{pageId} AND execution_id=#{id} AND ding_corp_id=#{corpId} AND status='RUNNING'")
    int finishPage(SyncExecution execution);

    @Insert("INSERT INTO ding_sync_execution (ding_corp_id,sync_form_id,trace_id,request_key,source_key,log_scope,next_offset,trigger_type,direction,status,started_at) "
        + "VALUES (#{corpId},NULL,#{traceId},#{requestKey},#{sourceKey},#{logScope},#{pageOffset},'EVENT','KINGDEE_TO_DING','RUNNING',CURRENT_TIMESTAMP(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int start(SyncExecution execution);

    @Update("UPDATE ding_sync_execution SET status=#{status},total_count=#{total},success_count=#{success},failed_count=0,"
        + "skipped_count=#{skipped},duration_ms=#{duration},page_count=#{pageCount},next_offset=#{expectedOffset},"
        + "finished_at=CASE WHEN #{status}='RUNNING' THEN NULL ELSE CURRENT_TIMESTAMP(3) END,"
        + "error_code=#{errorCode},error_message=#{errorMessage},error_stack=#{errorStack} WHERE id=#{id} AND ding_corp_id=#{corpId}")
    int finish(SyncExecution execution);

    @Insert("<script>INSERT INTO ding_sync_execution_item (execution_id,ding_corp_id,item_no,source_record_id,status) VALUES "
        + "<foreach collection='sourceIds' item='sourceId' index='i' separator=','>(#{id},#{corpId},#{pageOffset}+#{i}+1,#{sourceId},'SUCCESS')</foreach></script>")
    int items(SyncExecution execution);

    // HTTP status is deliberately NULL: the SDK does not expose the transport response.
    @Insert("INSERT INTO ding_api_call_log (ding_corp_id,trace_id,execution_id,page_id,platform,api_name,http_method,"
        + "business_code,result,duration_ms,error_message,error_stack,occurred_at) VALUES "
        + "(#{e.corpId},#{e.traceId},#{e.id},#{e.pageId},'KINGDEE',#{a.name},'POST',#{a.businessCode},#{a.result},#{a.duration},#{a.errorMessage},#{a.errorStack},#{a.occurredAt})")
    int api(@Param("e") SyncExecution execution, @Param("a") SyncExecution.ApiCall call);

    @Insert("INSERT INTO ding_api_call_log (ding_corp_id,trace_id,execution_id,page_id,platform,api_name,request_path,http_method,"
        + "business_code,result,duration_ms,error_message,error_stack,request_summary,response_summary,occurred_at) VALUES "
        + "(#{corpId},#{traceId},#{id},#{pageId},'DING','records','/api/records','POST',CASE WHEN #{status}='FAILED' THEN '10005' ELSE '0' END,"
        + "CASE WHEN #{status}='FAILED' THEN 'FAILED' ELSE 'SUCCESS' END,#{duration},#{errorMessage},#{errorStack},"
        + "#{requestSummary},#{responseSummary},CURRENT_TIMESTAMP(3))")
    int callback(SyncExecution execution);
}
