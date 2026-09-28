package com.dj.middle.ding.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 仪表盘统计 Mapper
 * 提供各业务维度的聚合统计查询，返回结果统一使用 Map 承载
 */
public interface DingDashboardMapper
{
    /**
     * 顶部 KPI 概览：企业数、订单数、营收、生效订阅、待支付订单、今日新增订单
     */
    public Map<String, Object> selectOverview();

    /**
     * 订单与营收趋势（按支付时间日聚合，仅返回有数据的日子）
     */
    public List<Map<String, Object>> selectOrderTrend(@Param("startDate") String startDate);

    /**
     * 订单类型分布
     */
    public List<Map<String, Object>> selectOrderTypeDist();

    /**
     * 订单支付状态分布
     */
    public List<Map<String, Object>> selectPayStatusDist();

    /**
     * 支付渠道分布（仅已支付订单）
     */
    public List<Map<String, Object>> selectPayChannelDist();

    /**
     * 企业按行业分布
     */
    public List<Map<String, Object>> selectIndustryDist();

    /**
     * 同步日志趋势（按创建时间日聚合）
     */
    public List<Map<String, Object>> selectSyncTrend(@Param("startDate") String startDate);

    /**
     * 企业每日新增（用于计算累计增长曲线）
     */
    public List<Map<String, Object>> selectTenantDailyNew(@Param("startDate") String startDate);

    /**
     * 热销产品 Top5（按已支付订单营收排序）
     */
    public List<Map<String, Object>> selectTopProducts();
}
