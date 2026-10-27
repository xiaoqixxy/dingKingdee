package com.dj.middle.admin;

import com.dj.common.exception.ServiceException;
import com.dj.middle.admin.mapper.SyncReportMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

/** 按批次或分页口径汇总同步统计。 */
@Service
public class SyncReportService {
    /** 提供None数据访问能力的映射接口。 */
    private final SyncReportMapper mapper;
    /** 初始化当前组件并保存构造参数。 */
    public SyncReportService(SyncReportMapper mapper) { this.mapper = mapper; }

    /** 校验统计周期与口径，汇总趋势、覆盖范围、错误及当前节点运行信息。 */
    public Map<String,Object> report(int days, String scope) {
        if (!List.of(7,15,30).contains(days) || !List.of("BATCH","PAGE","LEGACY").contains(scope))
            throw new ServiceException("统计范围无效",400);
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days-1);
        String end = today.plusDays(1).toString();
        // 同时读取上一周期作为对比基线，当前周期的前一天也用于每日增减计算。
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

    /** 将统计字段安全读取为长整数，缺失值按零处理。 */
    static long number(Map<String,Object> row,String key) {
        Object value=row.get(key); return value instanceof Number ? ((Number)value).longValue() : 0;
    }

    /** 补齐每日统计点并计算当前周期、上周期、环比及成功率。 */
    static Map<String,Object> summarize(List<Map<String,Object>> rows,LocalDate start,int days) {
        Map<String,Map<String,Object>> indexed=new HashMap<>();
        rows.forEach(row -> indexed.put(String.valueOf(row.get("day")),row));
        List<Map<String,Object>> daily=new ArrayList<>();
        Map<String,Object> current=new LinkedHashMap<>();
        Map<String,Object> previous=new LinkedHashMap<>();
        List<String> keys=List.of("executions","succeeded","limited","errors","completed","active","records","duration","timed");
        // 按日期补齐上一周期和当前周期的空缺数据，避免图表断点及环比基数不一致。
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
        // 基期为0时百分比没有可比意义，返回空值供界面显示说明，不能制造无限增长。
        current.put("changePercent",number(previous,"records")==0?null:100.0*delta/number(previous,"records"));
        current.put("successRate",number(current,"completed")==0?null:100.0*(number(current,"succeeded")+number(current,"limited"))/number(current,"completed"));
        current.put("averageDuration",number(current,"timed")==0?null:1.0*number(current,"duration")/number(current,"timed"));
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("current",current); result.put("previous",previous); result.put("daily",daily);
        result.put("startDate",start.toString()); result.put("endDate",start.plusDays(days-1).toString());
        return result;
    }
}
