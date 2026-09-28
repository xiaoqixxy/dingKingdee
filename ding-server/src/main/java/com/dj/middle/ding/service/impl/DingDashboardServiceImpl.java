package com.dj.middle.ding.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dj.middle.ding.mapper.DingDashboardMapper;
import com.dj.middle.ding.service.IDingDashboardService;

/**
 * 仪表盘统计 Service 实现
 * 负责对 Mapper 返回的聚合数据进行日期补齐和码值转换，保证前端图表数据连续可读
 */
@Service
public class DingDashboardServiceImpl implements IDingDashboardService
{
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 订单类型码值映射：1=新购 2=升级 3=降级 4=续费 */
    private static final Map<Integer, String> ORDER_TYPE_MAP = new HashMap<>();
    /** 订单支付状态码值映射：0=待支付 1=已支付 2=已取消 3=退款 */
    private static final Map<Integer, String> PAY_STATUS_MAP = new HashMap<>();

    static
    {
        ORDER_TYPE_MAP.put(1, "新购");
        ORDER_TYPE_MAP.put(2, "升级");
        ORDER_TYPE_MAP.put(3, "降级");
        ORDER_TYPE_MAP.put(4, "续费");
        PAY_STATUS_MAP.put(0, "待支付");
        PAY_STATUS_MAP.put(1, "已支付");
        PAY_STATUS_MAP.put(2, "已取消");
        PAY_STATUS_MAP.put(3, "退款");
    }

    @Autowired
    private DingDashboardMapper dingDashboardMapper;

    @Override
    public Map<String, Object> selectOverview()
    {
        Map<String, Object> overview = dingDashboardMapper.selectOverview();
        if (overview == null)
        {
            overview = new HashMap<>();
        }
        return overview;
    }

    @Override
    public List<Map<String, Object>> selectOrderTrend(int days)
    {
        String startDate = LocalDate.now().minusDays(days - 1L).format(DATE_FMT);
        List<Map<String, Object>> raw = dingDashboardMapper.selectOrderTrend(startDate);
        Map<String, Map<String, Object>> indexed = indexByDate(raw, "date");
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--)
        {
            String dateStr = today.minusDays(i).format(DATE_FMT);
            Map<String, Object> item = indexed.get(dateStr);
            if (item == null)
            {
                item = new HashMap<>();
                item.put("date", dateStr);
                item.put("orderCount", 0);
                item.put("revenue", 0);
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectOrderTypeDist()
    {
        List<Map<String, Object>> raw = dingDashboardMapper.selectOrderTypeDist();
        List<Map<String, Object>> result = new ArrayList<>();
        if (raw == null)
        {
            return result;
        }
        for (Map<String, Object> row : raw)
        {
            Object typeVal = row.get("type");
            Integer code = typeVal == null ? null : ((Number) typeVal).intValue();
            String name = ORDER_TYPE_MAP.getOrDefault(code, "未知");
            Map<String, Object> item = new HashMap<>();
            item.put("name", name);
            item.put("value", ((Number) row.get("count")).intValue());
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectPayStatusDist()
    {
        List<Map<String, Object>> raw = dingDashboardMapper.selectPayStatusDist();
        List<Map<String, Object>> result = new ArrayList<>();
        if (raw == null)
        {
            return result;
        }
        for (Map<String, Object> row : raw)
        {
            Object statusVal = row.get("status");
            Integer code = statusVal == null ? null : ((Number) statusVal).intValue();
            String name = PAY_STATUS_MAP.getOrDefault(code, "未知");
            Map<String, Object> item = new HashMap<>();
            item.put("name", name);
            item.put("value", ((Number) row.get("count")).intValue());
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectPayChannelDist()
    {
        return ensureList(dingDashboardMapper.selectPayChannelDist());
    }

    @Override
    public List<Map<String, Object>> selectIndustryDist()
    {
        List<Map<String, Object>> raw = dingDashboardMapper.selectIndustryDist();
        List<Map<String, Object>> result = new ArrayList<>();
        if (raw == null)
        {
            return result;
        }
        for (Map<String, Object> row : raw)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("name", row.get("industry"));
            item.put("value", ((Number) row.get("count")).intValue());
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectSyncTrend(int days)
    {
        String startDate = LocalDate.now().minusDays(days - 1L).format(DATE_FMT);
        List<Map<String, Object>> raw = dingDashboardMapper.selectSyncTrend(startDate);
        Map<String, Map<String, Object>> indexed = indexByDate(raw, "date");
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--)
        {
            String dateStr = today.minusDays(i).format(DATE_FMT);
            Map<String, Object> item = indexed.get(dateStr);
            if (item == null)
            {
                item = new HashMap<>();
                item.put("date", dateStr);
                item.put("totalCount", 0);
                item.put("successCount", 0);
                item.put("failCount", 0);
                item.put("blockedCount", 0);
                item.put("totalSync", 0);
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectTenantGrowth(int days)
    {
        String startDate = LocalDate.now().minusDays(days - 1L).format(DATE_FMT);
        List<Map<String, Object>> raw = dingDashboardMapper.selectTenantDailyNew(startDate);
        Map<String, Map<String, Object>> indexed = indexByDate(raw, "date");

        // 需要计算起始日的累计基数（即 startDate 之前已存在的企业总数），才能得到每日累计值
        // 由于 selectTenantDailyNew 只返回 startDate 之后的数据，这里用 overview 的 totalTenants 减去区间内总新增来推算基数
        Map<String, Object> overview = dingDashboardMapper.selectOverview();
        long totalTenants = overview == null ? 0L : ((Number) overview.getOrDefault("totalTenants", 0)).longValue();
        long rangeNew = 0L;
        for (Map<String, Object> row : raw)
        {
            rangeNew += ((Number) row.get("count")).longValue();
        }
        long base = Math.max(0L, totalTenants - rangeNew);

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        long cumulative = base;
        for (int i = days - 1; i >= 0; i--)
        {
            String dateStr = today.minusDays(i).format(DATE_FMT);
            Map<String, Object> daily = indexed.get(dateStr);
            int newCount = daily == null ? 0 : ((Number) daily.get("count")).intValue();
            cumulative += newCount;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", dateStr);
            item.put("newCount", newCount);
            item.put("totalCount", cumulative);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectTopProducts()
    {
        List<Map<String, Object>> raw = dingDashboardMapper.selectTopProducts();
        List<Map<String, Object>> result = new ArrayList<>();
        if (raw == null)
        {
            return result;
        }
        for (Map<String, Object> row : raw)
        {
            Map<String, Object> item = new HashMap<>();
            Object name = row.get("productName");
            item.put("productName", name == null ? "未命名产品" : name);
            item.put("orderCount", ((Number) row.get("orderCount")).intValue());
            item.put("revenue", row.get("revenue"));
            result.add(item);
        }
        return result;
    }

    /**
     * 将原始聚合行按日期字段建立索引，便于补齐空缺日
     */
    private Map<String, Map<String, Object>> indexByDate(List<Map<String, Object>> raw, String dateKey)
    {
        Map<String, Map<String, Object>> indexed = new HashMap<>();
        if (raw == null)
        {
            return indexed;
        }
        for (Map<String, Object> row : raw)
        {
            Object dateVal = row.get(dateKey);
            if (dateVal != null)
            {
                // 统一格式化为字符串，避免 java.sql.Date 等类型差异
                indexed.put(dateVal.toString(), row);
            }
        }
        return indexed;
    }

    private List<Map<String, Object>> ensureList(List<Map<String, Object>> raw)
    {
        return raw == null ? new ArrayList<>() : raw;
    }
}
