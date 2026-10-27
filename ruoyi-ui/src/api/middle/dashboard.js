/** 金蝶及钉钉业务接口或展示字典，沿用统一请求封装与服务端字段约定。 */
import request from '@/utils/request'

// 仪表盘统计接口模块

// 顶部 KPI 概览
/** 读取业务看板汇总指标。 */
export function getOverview() {
  return request({
    url: '/ding/dashboard/overview',
    method: 'get'
  })
}

// 订单与营收趋势
/** 读取订单数量和金额的日期趋势。 */
export function getOrderTrend(days = 30) {
  return request({
    url: '/ding/dashboard/orderTrend',
    method: 'get',
    params: { days }
  })
}

// 订单类型分布
/** 读取订单类型分布。 */
export function getOrderTypeDist() {
  return request({
    url: '/ding/dashboard/orderTypeDist',
    method: 'get'
  })
}

// 订单支付状态分布
/** 读取支付状态分布。 */
export function getPayStatusDist() {
  return request({
    url: '/ding/dashboard/payStatusDist',
    method: 'get'
  })
}

// 支付渠道分布
/** 读取支付渠道分布。 */
export function getPayChannelDist() {
  return request({
    url: '/ding/dashboard/payChannelDist',
    method: 'get'
  })
}

// 企业行业分布
/** 读取企业行业分布。 */
export function getIndustryDist() {
  return request({
    url: '/ding/dashboard/industryDist',
    method: 'get'
  })
}

// 同步日志趋势
/** 读取同步记录数量趋势。 */
export function getSyncTrend(days = 30) {
  return request({
    url: '/ding/dashboard/syncTrend',
    method: 'get',
    params: { days }
  })
}

// 企业累计增长趋势
/** 读取新增企业趋势。 */
export function getTenantGrowth(days = 30) {
  return request({
    url: '/ding/dashboard/tenantGrowth',
    method: 'get',
    params: { days }
  })
}

// 热销产品Top5
/** 读取产品销量排名。 */
export function getTopProducts() {
  return request({
    url: '/ding/dashboard/topProducts',
    method: 'get'
  })
}
