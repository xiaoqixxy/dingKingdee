package com.dj.middle.admin;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SyncReportServiceTest {
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
    @Test void noDataDoesNotClaimHealthyOrInfiniteGrowth() {
        var result=SyncReportService.summarize(List.of(),LocalDate.now(),7);
        var current=(Map<?,?>)result.get("current");
        assertNull(current.get("successRate"));
        assertNull(current.get("changePercent"));
        assertNull(current.get("averageDuration"));
    }
    @Test void includesFailuresInRateAndUsesOnlyTimedExecutionsForAverage() {
        var result=SyncReportService.summarize(List.of(Map.of(
            "day","2026-09-20","executions",5,"completed",4,"succeeded",2,"errors",2,"duration",6000,"timed",3
        )),LocalDate.parse("2026-09-20"),7);
        var current=(Map<?,?>)result.get("current");
        assertEquals(50.0,current.get("successRate"));
        assertEquals(2000.0,current.get("averageDuration"));
    }
    @Test void rejectsUnboundedRangesAndMixedScopesBeforeQuery() {
        var service=new SyncReportService(null);
        assertThrows(com.dj.common.exception.ServiceException.class,()->service.report(365,"BATCH"));
        assertThrows(com.dj.common.exception.ServiceException.class,()->service.report(30,"ALL"));
    }
}
