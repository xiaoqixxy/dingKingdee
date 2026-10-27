package com.dj.middle.ding.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dj.common.annotation.Anonymous;
import com.dj.common.core.controller.BaseController;
import com.dj.common.core.domain.AjaxResult;
import com.dj.middle.ding.service.IDingDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 仪表盘统计 Controller
 * 为后台首页提供各业务指标的聚合统计数据，支撑前端 ECharts 图表展示
 */
@Tag(name = "仪表盘统计")
@Anonymous
@RestController
@RequestMapping("/ding/dashboard")
/** 钉钉企业、订单和同步业务看板接口。 */
public class DingDashboardController extends BaseController
{
    /** 汇总钉钉业务看板指标的服务。 */
    @Autowired
    private IDingDashboardService dingDashboardService;

    /** 查询业务看板汇总指标。 */
    @Operation(summary = "顶部 KPI 概览")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return AjaxResult.success(dingDashboardService.selectOverview());
    }

    /** 查询订单按日变化趋势。 */
    @Operation(summary = "订单与营收趋势")
    @GetMapping("/orderTrend")
    public AjaxResult orderTrend(
            @Parameter(name = "days", description = "统计天数，默认30") @RequestParam(defaultValue = "30") int days)
    {
        return AjaxResult.success(dingDashboardService.selectOrderTrend(days));
    }

    /** 查询订单类型分布。 */
    @Operation(summary = "订单类型分布")
    @GetMapping("/orderTypeDist")
    public AjaxResult orderTypeDist()
    {
        return AjaxResult.success(dingDashboardService.selectOrderTypeDist());
    }

    /** 查询支付状态分布。 */
    @Operation(summary = "订单支付状态分布")
    @GetMapping("/payStatusDist")
    public AjaxResult payStatusDist()
    {
        return AjaxResult.success(dingDashboardService.selectPayStatusDist());
    }

    /** 查询支付渠道分布。 */
    @Operation(summary = "支付渠道分布")
    @GetMapping("/payChannelDist")
    public AjaxResult payChannelDist()
    {
        return AjaxResult.success(dingDashboardService.selectPayChannelDist());
    }

    /** 查询企业行业分布。 */
    @Operation(summary = "企业行业分布")
    @GetMapping("/industryDist")
    public AjaxResult industryDist()
    {
        return AjaxResult.success(dingDashboardService.selectIndustryDist());
    }

    /** 查询同步数量变化趋势。 */
    @Operation(summary = "同步日志趋势")
    @GetMapping("/syncTrend")
    public AjaxResult syncTrend(
            @Parameter(name = "days", description = "统计天数，默认30") @RequestParam(defaultValue = "30") int days)
    {
        return AjaxResult.success(dingDashboardService.selectSyncTrend(days));
    }

    /** 查询企业新增趋势。 */
    @Operation(summary = "企业累计增长趋势")
    @GetMapping("/tenantGrowth")
    public AjaxResult tenantGrowth(
            @Parameter(name = "days", description = "统计天数，默认30") @RequestParam(defaultValue = "30") int days)
    {
        return AjaxResult.success(dingDashboardService.selectTenantGrowth(days));
    }

    /** 查询产品销量排名。 */
    @Operation(summary = "热销产品Top5")
    @GetMapping("/topProducts")
    public AjaxResult topProducts()
    {
        return AjaxResult.success(dingDashboardService.selectTopProducts());
    }
}
