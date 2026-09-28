import request from '@/utils/request'

// 仪表盘统计接口模块

// 顶部 KPI 概览
export function getOverview() {
  return request({
    url: '/ding/dashboard/overview',
    method: 'get'
  })
}

// 订单与营收趋势
export function getOrderTrend(days = 30) {
  return request({
    url: '/ding/dashboard/orderTrend',
    method: 'get',
    params: { days }
  })
}

// 订单类型分布
export function getOrderTypeDist() {
  return request({
    url: '/ding/dashboard/orderTypeDist',
    method: 'get'
  })
}

// 订单支付状态分布
export function getPayStatusDist() {
  return request({
    url: '/ding/dashboard/payStatusDist',
    method: 'get'
  })
}

// 支付渠道分布
export function getPayChannelDist() {
  return request({
    url: '/ding/dashboard/payChannelDist',
    method: 'get'
  })
}

// 企业行业分布
export function getIndustryDist() {
  return request({
    url: '/ding/dashboard/industryDist',
    method: 'get'
  })
}

// 同步日志趋势
export function getSyncTrend(days = 30) {
  return request({
    url: '/ding/dashboard/syncTrend',
    method: 'get',
    params: { days }
  })
}

// 企业累计增长趋势
export function getTenantGrowth(days = 30) {
  return request({
    url: '/ding/dashboard/tenantGrowth',
    method: 'get',
    params: { days }
  })
}

// 热销产品Top5
export function getTopProducts() {
  return request({
    url: '/ding/dashboard/topProducts',
    method: 'get'
  })
}
