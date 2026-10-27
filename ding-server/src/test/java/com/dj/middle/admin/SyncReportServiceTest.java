package com.dj.middle.admin;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** SyncReportServiceTest：使用隔离夹具验证业务边界与历史兼容，不连接真实业务环境。 */
class SyncReportServiceTest {
    /** 验证：达到权益上限属于正常截断并保留独立计数。 */
    @Test void quotaCompletionIsNormalAndRetainsSeparateCount() {
        var result=SyncReportService.summarize(List.of(Map.of(
            "day","2026-09-20","executions",5,"completed",4,"succeeded",2,"limited",1,"errors",1
        )),LocalDate.parse("2026-09-20"),7);
        var current=(Map<?,?>)result.get("current");
        assertEquals(75.0,current.get("successRate"));
        assertEquals(1L,current.get("limited"));
        assertEquals(1L,current.get("errors"));
    }
    /** 验证：趋势补齐缺失日期并读取区间前一天计算增减。 */
    @Test void fillsDaysAndUsesPreviousDayOutsideRange() {
        var result=SyncReportService.summarize(List.of(
            Map.of("day","2026-09-19","records",100),
            Map.of("day","2026-09-20","records",300,"executions",2,"completed",1,"succeeded",1,"active",1)
        ),LocalDate.parse("2026-09-20"),7);
        var current=(Map<?,?>)result.get("current");
        assertEquals(200L,current.get("change"));
        assertEquals(200.0,current.get("changePercent"));
        assertEquals(100.0,current.get("successRate"));
        var daily=(List<Map<String,Object>>)result.get("daily");
        assertEquals(7,daily.size());
        assertEquals(200L,daily.get(0).get("change"));
        assertEquals(-300L,daily.get(1).get("change"));
        assertEquals(0L,daily.get(6).get("records"));
    }
    /** 验证：无数据时不虚构成功率或无限增长。 */
    @Test void noDataDoesNotClaimHealthyOrInfiniteGrowth() {
        var result=SyncReportService.summarize(List.of(),LocalDate.now(),7);
        var current=(Map<?,?>)result.get("current");
        assertNull(current.get("successRate"));
        assertNull(current.get("changePercent"));
        assertNull(current.get("averageDuration"));
    }
    /** 验证：成功率包含失败记录而平均耗时只统计已计时执行。 */
    @Test void includesFailuresInRateAndUsesOnlyTimedExecutionsForAverage() {
        var result=SyncReportService.summarize(List.of(Map.of(
            "day","2026-09-20","executions",5,"completed",4,"succeeded",2,"errors",2,"duration",6000,"timed",3
        )),LocalDate.parse("2026-09-20"),7);
        var current=(Map<?,?>)result.get("current");
        assertEquals(50.0,current.get("successRate"));
        assertEquals(2000.0,current.get("averageDuration"));
    }
    /** 验证：查询前拒绝过大周期与无效统计口径。 */
    @Test void rejectsUnboundedRangesAndMixedScopesBeforeQuery() {
        var service=new SyncReportService(null);
        assertThrows(com.dj.common.exception.ServiceException.class,()->service.report(365,"BATCH"));
        assertThrows(com.dj.common.exception.ServiceException.class,()->service.report(30,"ALL"));
    }
}
