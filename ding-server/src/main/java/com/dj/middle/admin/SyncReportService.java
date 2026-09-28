package com.dj.middle.admin;

import com.dj.common.exception.ServiceException;
import com.dj.middle.admin.mapper.SyncReportMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class SyncReportService {
    private final SyncReportMapper mapper;
    public SyncReportService(SyncReportMapper mapper) { this.mapper = mapper; }

    public Map<String,Object> report(int days, String scope) {
        if (!List.of(7,15,30).contains(days) || !List.of("BATCH","PAGE","LEGACY").contains(scope))
            throw new ServiceException("统计范围无效",400);
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days-1);
        String end = today.plusDays(1).toString();
        Map<String,Object> result = summarize(mapper.daily(start.minusDays(days).toString(),end,scope),start,days);
        result.put("scope",scope);
        result.put("coverage",mapper.coverage(start.toString(),end));
        result.put("running",mapper.running(scope));
        result.put("errors",mapper.errors(start.toString(),end,scope));
        result.put("generatedAt",java.time.LocalDateTime.now().toString());
        result.put("timezone",java.time.ZoneId.systemDefault().toString());
        Runtime runtime = Runtime.getRuntime();
        result.put("node",Map.of("uptimeMs",java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime(),
            "heapUsed",runtime.totalMemory()-runtime.freeMemory(),"heapMax",runtime.maxMemory()));
        return result;
    }

    static long number(Map<String,Object> row,String key) {
        Object value=row.get(key); return value instanceof Number ? ((Number)value).longValue() : 0;
    }

    static Map<String,Object> summarize(List<Map<String,Object>> rows,LocalDate start,int days) {
        Map<String,Map<String,Object>> indexed=new HashMap<>();
        rows.forEach(row -> indexed.put(String.valueOf(row.get("day")),row));
        List<Map<String,Object>> daily=new ArrayList<>();
        Map<String,Object> current=new LinkedHashMap<>();
        Map<String,Object> previous=new LinkedHashMap<>();
        List<String> keys=List.of("executions","succeeded","errors","completed","active","records","duration","timed");
        for(int i=-days;i<days;i++) {
            LocalDate date=start.plusDays(i);
            Map<String,Object> source=indexed.getOrDefault(date.toString(),Map.of());
            Map<String,Object> total=i<0?previous:current;
            Map<String,Object> point=new LinkedHashMap<>(); point.put("day",date.toString());
            for(String key:keys) {
                long value=number(source,key); point.put(key,value);
                total.put(key,number(total,key)+value);
            }
            if(i>=0) {
                point.put("change",number(source,"records")-number(indexed.getOrDefault(date.minusDays(1).toString(),Map.of()),"records"));
                daily.add(point);
            }
        }
        long delta=number(current,"records")-number(previous,"records");
        current.put("change",delta);
        current.put("changePercent",number(previous,"records")==0?null:100.0*delta/number(previous,"records"));
        current.put("successRate",number(current,"completed")==0?null:100.0*number(current,"succeeded")/number(current,"completed"));
        current.put("averageDuration",number(current,"timed")==0?null:1.0*number(current,"duration")/number(current,"timed"));
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("current",current); result.put("previous",previous); result.put("daily",daily);
        result.put("startDate",start.toString()); result.put("endDate",start.plusDays(days-1).toString());
        return result;
    }
}
