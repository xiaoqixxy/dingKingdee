package com.dj.middle.admin.mapper;
import com.dj.middle.admin.*;
import java.util.*;
import org.apache.ibatis.annotations.*;
public interface AdminRecordMapper {
 @SelectProvider(type=AdminRecordSql.class,method="list") List<Map<String,Object>> list(@Param("kind")String kind,@Param("q")AdminRecordQuery q);
 @SelectProvider(type=AdminRecordSql.class,method="count") long count(@Param("kind")String kind,@Param("q")AdminRecordQuery q);
 @SelectProvider(type=AdminRecordSql.class,method="detail") Map<String,Object> detail(@Param("kind")String kind,@Param("id")Long id);
 @Select("SELECT id,item_no,source_record_id,target_record_id,status,error_code,error_message,duration_ms,created_at FROM ding_sync_execution_item WHERE execution_id=#{executionId} ORDER BY id LIMIT #{limit} OFFSET #{offset}")
 List<Map<String,Object>> items(@Param("executionId")Long executionId,@Param("limit")int limit,@Param("offset")int offset);
 @Select("SELECT COUNT(*) FROM ding_sync_execution_item WHERE execution_id=#{id}") long itemCount(Long id);
 @Select("SELECT a.id,a.page_id,a.api_name,a.platform,a.result,a.business_code,a.http_status,a.duration_ms,a.error_message,a.request_summary,a.response_summary,a.occurred_at FROM ding_api_call_log a JOIN ding_sync_execution e ON e.id=a.execution_id AND e.ding_corp_id=a.ding_corp_id WHERE e.id=#{id} ORDER BY a.occurred_at,a.id LIMIT #{limit} OFFSET #{offset}")
 List<Map<String,Object>> executionApis(@Param("id")Long id,@Param("limit")int limit,@Param("offset")int offset);
 @Select("SELECT COUNT(*) FROM ding_api_call_log a JOIN ding_sync_execution e ON e.id=a.execution_id AND e.ding_corp_id=a.ding_corp_id WHERE e.id=#{id}")
 long executionApiCount(Long id);
 @Select("SELECT p.id,p.page_offset,p.next_offset,p.status,p.success_count,p.counted,p.has_more,p.duration_ms,p.error_message,p.started_at,p.finished_at FROM ding_sync_execution_page p JOIN ding_sync_execution e ON e.id=p.execution_id AND e.ding_corp_id=p.ding_corp_id WHERE e.id=#{id} ORDER BY p.id LIMIT #{limit} OFFSET #{offset}")
 List<Map<String,Object>> pages(@Param("id")Long id,@Param("limit")int limit,@Param("offset")int offset);
 @Select("SELECT COUNT(*) FROM ding_sync_execution_page p JOIN ding_sync_execution e ON e.id=p.execution_id AND e.ding_corp_id=p.ding_corp_id WHERE e.id=#{id}")
 long pageCount(Long id);
 @Select("SELECT p.error_stack FROM ding_sync_execution_page p JOIN ding_sync_execution e ON e.id=p.execution_id AND e.ding_corp_id=p.ding_corp_id WHERE e.id=#{id} AND p.id=#{pageId}")
 String pageStack(@Param("id")Long id,@Param("pageId")Long pageId);
}
