package com.dj.middle.ding.service;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计 Service 接口
 */
public interface IDingDashboardService
{
    /** 顶部 KPI 概览 */
    public Map<String, Object> selectOverview();

    /** 订单与营收趋势（已按日期补齐空缺日） */
    public List<Map<String, Object>> selectOrderTrend(int days);

    /** 订单类型分布（已转换为中文名称） */
    public List<Map<String, Object>> selectOrderTypeDist();

    /** 订单支付状态分布（已转换为中文名称） */
    public List<Map<String, Object>> selectPayStatusDist();

    /** 支付渠道分布 */
    public List<Map<String, Object>> selectPayChannelDist();

    /** 企业行业分布 */
    public List<Map<String, Object>> selectIndustryDist();

    /** 同步日志趋势（已按日期补齐空缺日） */
    public List<Map<String, Object>> selectSyncTrend(int days);

    /** 企业累计增长趋势（已按日期补齐空缺日并累计） */
    public List<Map<String, Object>> selectTenantGrowth(int days);

    /** 热销产品 Top5 */
    public List<Map<String, Object>> selectTopProducts();
}
