package com.dj.middle.admin.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 按同步日志口径聚合趋势、覆盖范围与异常执行。 */
public interface SyncReportMapper {
    /** 按创建日期和指定日志口径汇总每日执行与返回记录数。 */
    @Select("SELECT DATE_FORMAT(created_at,'%Y-%m-%d') AS day, COUNT(*) AS executions, "
        + "SUM(status='SUCCESS') AS succeeded, SUM(status='FAILED' OR (status='PARTIAL' AND COALESCE(error_code,'')<>'SYNC_LIMIT_REACHED')) AS errors, "
        + "SUM(status='PARTIAL' AND error_code='SYNC_LIMIT_REACHED') AS limited, "
        + "SUM(status IN ('SUCCESS','FAILED','PARTIAL','CANCELLED')) AS completed, "
        + "SUM(status IN ('PENDING','RUNNING')) AS active, SUM(success_count) AS records, "
        + "SUM(CASE WHEN finished_at IS NOT NULL THEN duration_ms ELSE 0 END) AS duration, "
        + "SUM(finished_at IS NOT NULL) AS timed FROM ding_sync_execution "
        + "WHERE created_at >= #{start} AND created_at < #{end} AND log_scope=#{scope} "
        + "GROUP BY DATE_FORMAT(created_at,'%Y-%m-%d') ORDER BY day")
    List<Map<String,Object>> daily(@Param("start") String start, @Param("end") String end, @Param("scope") String scope);

    /** 统计当前日期范围内各日志口径的覆盖数量。 */
    @Select("SELECT log_scope AS scope, COUNT(*) AS count FROM ding_sync_execution "
        + "WHERE created_at >= #{start} AND created_at < #{end} GROUP BY log_scope")
    List<Map<String,Object>> coverage(@Param("start") String start, @Param("end") String end);

    /** 统计指定口径仍在执行及长时间未更新的批次。 */
    @Select("SELECT COUNT(*) AS active, COALESCE(SUM(updated_at < DATE_SUB(NOW(), INTERVAL 30 MINUTE)),0) AS stale "
        + "FROM ding_sync_execution WHERE status IN ('PENDING','RUNNING') AND log_scope=#{scope}")
    Map<String,Object> running(@Param("scope") String scope);

    /** 查询近期异常执行摘要，区别业务失败与正常权益截断。 */
    @Select("SELECT records.*,tenant.corp_name FROM (SELECT id,ding_corp_id,trace_id,status,success_count,error_message,created_at "
        + "FROM ding_sync_execution WHERE created_at >= #{start} AND created_at < #{end} "
        + "AND log_scope=#{scope} AND (status='FAILED' OR (status='PARTIAL' AND COALESCE(error_code,'')<>'SYNC_LIMIT_REACHED')) ORDER BY created_at DESC,id DESC LIMIT 10) records LEFT JOIN ding_tenant tenant ON tenant.ding_corp_id=records.ding_corp_id ORDER BY records.created_at DESC,records.id DESC")
    List<Map<String,Object>> errors(@Param("start") String start, @Param("end") String end, @Param("scope") String scope);
}
