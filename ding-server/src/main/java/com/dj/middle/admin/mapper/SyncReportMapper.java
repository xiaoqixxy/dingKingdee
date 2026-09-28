package com.dj.middle.admin.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SyncReportMapper {
    @Select("SELECT DATE_FORMAT(created_at,'%Y-%m-%d') AS day, COUNT(*) AS executions, "
        + "SUM(status='SUCCESS') AS succeeded, SUM(status IN ('FAILED','PARTIAL')) AS errors, "
        + "SUM(status IN ('SUCCESS','FAILED','PARTIAL','CANCELLED')) AS completed, "
        + "SUM(status IN ('PENDING','RUNNING')) AS active, SUM(success_count) AS records, "
        + "SUM(CASE WHEN finished_at IS NOT NULL THEN duration_ms ELSE 0 END) AS duration, "
        + "SUM(finished_at IS NOT NULL) AS timed FROM ding_sync_execution "
        + "WHERE created_at >= #{start} AND created_at < #{end} AND log_scope=#{scope} "
        + "GROUP BY DATE_FORMAT(created_at,'%Y-%m-%d') ORDER BY day")
    List<Map<String,Object>> daily(@Param("start") String start, @Param("end") String end, @Param("scope") String scope);

    @Select("SELECT log_scope AS scope, COUNT(*) AS count FROM ding_sync_execution "
        + "WHERE created_at >= #{start} AND created_at < #{end} GROUP BY log_scope")
    List<Map<String,Object>> coverage(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) AS active, COALESCE(SUM(updated_at < DATE_SUB(NOW(), INTERVAL 30 MINUTE)),0) AS stale "
        + "FROM ding_sync_execution WHERE status IN ('PENDING','RUNNING') AND log_scope=#{scope}")
    Map<String,Object> running(@Param("scope") String scope);

    @Select("SELECT id,ding_corp_id,trace_id,status,success_count,error_message,created_at "
        + "FROM ding_sync_execution WHERE created_at >= #{start} AND created_at < #{end} "
        + "AND log_scope=#{scope} AND status IN ('FAILED','PARTIAL') ORDER BY created_at DESC,id DESC LIMIT 10")
    List<Map<String,Object>> errors(@Param("start") String start, @Param("end") String end, @Param("scope") String scope);
}
