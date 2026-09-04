<template>
  <div class="app-container dashboard">
    <!-- 顶部欢迎 + 时间范围切换 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-inner">
        <div class="welcome-text">
          <h2>欢迎使用金蝶集成系统</h2>
          <p>钉钉 × 金蝶 数据中台 · 今日 {{ today }}，以下是业务核心指标监控</p>
        </div>
        <el-radio-group v-model="days" size="default" @change="loadAll">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="15">近15天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <!-- KPI 指标卡片 -->
    <el-row :gutter="16" class="kpi-row" v-loading="loading">
      <el-col :xs="12" :sm="12" :md="6" v-for="(card, idx) in kpiCards" :key="idx">
        <el-card class="kpi-card" shadow="hover">
          <div class="kpi-icon" :style="{ background: card.bg }">
            <el-icon :size="24"><component :is="card.icon" /></el-icon>
          </div>
          <div class="kpi-body">
            <div class="kpi-label">{{ card.label }}</div>
            <div class="kpi-value">{{ card.value }}</div>
            <div class="kpi-sub">{{ card.sub }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订单与营收趋势（全宽） -->
    <el-card class="chart-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="header-title">📈 订单与营收趋势</span>
          <el-tag size="small" type="info">近 {{ days }} 天</el-tag>
        </div>
      </template>
      <Echart :option="orderTrendOption" height="340px" />
    </el-card>

    <!-- 订单类型 / 支付状态 -->
    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="header-title">🧩 订单类型分布</span>
          </template>
          <Echart :option="orderTypeOption" height="300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="header-title">💳 订单支付状态</span>
          </template>
          <Echart :option="payStatusOption" height="300px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 支付渠道 / 行业分布 -->
    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="header-title">🏦 支付渠道分布</span>
          </template>
          <Echart :option="payChannelOption" height="300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="header-title">🏭 企业行业分布</span>
          </template>
          <Echart :option="industryOption" height="300px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 企业增长 / 同步趋势 -->
    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="header-title">🏢 企业累计增长</span>
          </template>
          <Echart :option="tenantGrowthOption" height="300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="header-title">🔄 数据同步趋势</span>
          </template>
          <Echart :option="syncTrendOption" height="300px" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 热销产品Top5（全宽） -->
    <el-card class="chart-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="header-title">🏆 热销产品 Top5</span>
          <el-tag size="small" type="info">按已支付营收</el-tag>
        </div>
      </template>
      <Echart :option="topProductsOption" height="320px" />
    </el-card>
  </div>
</template>

<script setup name="Index">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  getOverview,
  getOrderTrend,
  getOrderTypeDist,
  getPayStatusDist,
  getPayChannelDist,
  getIndustryDist,
  getSyncTrend,
  getTenantGrowth,
  getTopProducts
} from '@/api/middle/dashboard'

const loading = ref(false)
const days = ref(30)
const overview = ref({})
const today = computed(() => new Date().toLocaleDateString('zh-CN'))

// ============ 工具函数（定义在组件级别，避免作用域问题）============
// 统一图表配色
const PALETTE = ['#5B8FF9', '#5AD8A6', '#F6BD16', '#E8684A', '#6DC8EC', '#9270CA', '#FF9D4D']

// 千分位格式化金额
function formatMoney(num) {
  const n = Number(num || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

// 大数值简写（万 / 亿）
function formatCompact(num) {
  const n = Number(num || 0)
  if (n >= 1e8) return (n / 1e8).toFixed(2) + '亿'
  if (n >= 1e4) return (n / 1e4).toFixed(2) + '万'
  return formatMoney(n)
}

// ============ KPI 卡片 ============
const kpiCards = computed(() => {
  const o = overview.value || {}
  return [
    {
      label: '订单总数',
      value: formatMoney(o.totalOrders),
      sub: `今日新增 ${formatMoney(o.todayOrders)}`,
      icon: 'List',
      bg: 'linear-gradient(135deg,#5B8FF9,#3D6FE0)'
    },
    {
      label: '总营收 (¥)',
      value: formatCompact(o.totalRevenue),
      sub: `今日营收 ¥${formatCompact(o.todayRevenue)}`,
      icon: 'Money',
      bg: 'linear-gradient(135deg,#5AD8A6,#3CB371)'
    },
    {
      label: '企业总数',
      value: formatMoney(o.totalTenants),
      sub: `生效订阅 ${formatMoney(o.activeSubscriptions)}`,
      icon: 'OfficeBuilding',
      bg: 'linear-gradient(135deg,#F6BD16,#E8930C)'
    },
    {
      label: '待支付订单',
      value: formatMoney(o.pendingPayments),
      sub: `今日同步 ${formatMoney(o.todaySyncCount)} 条`,
      icon: 'Bell',
      bg: 'linear-gradient(135deg,#E8684A,#D04A2C)'
    }
  ]
})

// ============ 图表配置项 ============
const orderTrendOption = ref({})
const orderTypeOption = ref({})
const payStatusOption = ref({})
const payChannelOption = ref({})
const industryOption = ref({})
const tenantGrowthOption = ref({})
const syncTrendOption = ref({})
const topProductsOption = ref({})

// 订单与营收趋势：柱状(订单数) + 折线(营收) 双Y轴
function buildOrderTrendOption(list) {
  const dates = list.map((i) => i.date)
  const orders = list.map((i) => Number(i.orderCount || 0))
  const revenues = list.map((i) => Number(i.revenue || 0))
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['订单数', '营收(¥)'], top: 0 },
    grid: { left: 40, right: 50, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 10 } },
    yAxis: [
      { type: 'value', name: '订单数', splitLine: { lineStyle: { type: 'dashed' } } },
      { type: 'value', name: '营收(¥)', axisLabel: { formatter: (v) => formatCompact(v) }, splitLine: { show: false } }
    ],
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: orders,
        barMaxWidth: 24,
        itemStyle: { color: '#5B8FF9', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '营收(¥)',
        type: 'line',
        yAxisIndex: 1,
        data: revenues,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: '#E8684A' },
        itemStyle: { color: '#E8684A' }
      }
    ]
  }
}

// 通用饼图配置
function buildPieOption(data, { radius = ['0%', '65%'], donut = false } = {}) {
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    color: PALETTE,
    series: [
      {
        type: 'pie',
        radius: radius,
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { show: !donut, formatter: '{b}\n{d}%' },
        data: data && data.length ? data : [{ name: '暂无数据', value: 1 }]
      }
    ]
  }
}

// 支付渠道横向柱状图
function buildPayChannelOption(list) {
  const sorted = [...list].sort((a, b) => Number(a.count) - Number(b.count))
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (p) => `${p[0].name}<br/>订单: ${p[0].value}<br/>金额: ¥${formatMoney(p[0].data.amount)}` },
    grid: { left: 80, right: 30, top: 20, bottom: 30 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: sorted.map((i) => i.channel || '未知') },
    series: [
      {
        type: 'bar',
        data: sorted.map((i) => ({ value: Number(i.count || 0), amount: Number(i.amount || 0) })),
        barMaxWidth: 22,
        itemStyle: { color: '#6DC8EC', borderRadius: [0, 4, 4, 0] }
      }
    ]
  }
}

// 企业累计增长面积图
function buildTenantGrowthOption(list) {
  const dates = list.map((i) => i.date)
  const total = list.map((i) => Number(i.totalCount || 0))
  const newCount = list.map((i) => Number(i.newCount || 0))
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['累计企业', '当日新增'], top: 0 },
    grid: { left: 40, right: 30, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
    series: [
      {
        name: '累计企业',
        type: 'line',
        data: total,
        smooth: true,
        symbol: 'none',
        areaStyle: {
          color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(91,143,249,0.4)' }, { offset: 1, color: 'rgba(91,143,249,0.02)' }] }
        },
        lineStyle: { width: 2, color: '#5B8FF9' },
        itemStyle: { color: '#5B8FF9' }
      },
      {
        name: '当日新增',
        type: 'bar',
        data: newCount,
        barMaxWidth: 16,
        itemStyle: { color: '#5AD8A6', borderRadius: [4, 4, 0, 0] }
      }
    ]
  }
}

// 数据同步趋势堆叠柱状图
function buildSyncTrendOption(list) {
  const dates = list.map((i) => i.date)
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['成功', '失败', '超量拦截'], top: 0 },
    grid: { left: 40, right: 30, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
    color: ['#5AD8A6', '#E8684A', '#F6BD16'],
    series: [
      { name: '成功', type: 'bar', stack: 'sync', data: list.map((i) => Number(i.successCount || 0)), barMaxWidth: 24 },
      { name: '失败', type: 'bar', stack: 'sync', data: list.map((i) => Number(i.failCount || 0)) },
      { name: '超量拦截', type: 'bar', stack: 'sync', data: list.map((i) => Number(i.blockedCount || 0)) }
    ]
  }
}

// 热销产品Top5横向柱状图
function buildTopProductsOption(list) {
  const sorted = [...list].reverse() // 倒序使最大的显示在顶部
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (p) => `${p[0].name}<br/>订单数: ${p[0].data.orderCount}<br/>营收: ¥${formatMoney(p[0].value)}`
    },
    grid: { left: 120, right: 40, top: 20, bottom: 30 },
    xAxis: { type: 'value', axisLabel: { formatter: (v) => formatCompact(v) } },
    yAxis: { type: 'category', data: sorted.map((i) => i.productName || '未命名') },
    series: [
      {
        type: 'bar',
        data: sorted.map((i) => ({ value: Number(i.revenue || 0), orderCount: Number(i.orderCount || 0) })),
        barMaxWidth: 22,
        itemStyle: {
          color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0, colorStops: [{ offset: 0, color: '#9270CA' }, { offset: 1, color: '#5B8FF9' }] },
          borderRadius: [0, 4, 4, 0]
        },
        label: { show: true, position: 'right', formatter: (p) => '¥' + formatCompact(p.value), fontSize: 11 }
      }
    ]
  }
}

// ============ 数据加载 ============
async function loadAll() {
  loading.value = true
  try {
    const [
      overviewRes,
      orderTrendRes,
      orderTypeRes,
      payStatusRes,
      payChannelRes,
      industryRes,
      syncTrendRes,
      tenantGrowthRes,
      topProductsRes
    ] = await Promise.all([
      getOverview(),
      getOrderTrend(days.value),
      getOrderTypeDist(),
      getPayStatusDist(),
      getPayChannelDist(),
      getIndustryDist(),
      getSyncTrend(days.value),
      getTenantGrowth(days.value),
      getTopProducts()
    ])

    overview.value = overviewRes.data || {}
    orderTrendOption.value = buildOrderTrendOption(orderTrendRes.data || [])
    orderTypeOption.value = buildPieOption(orderTypeRes.data || [])
    payStatusOption.value = buildPieOption(payStatusRes.data || [], { donut: true, radius: ['40%', '70%'] })
    payChannelOption.value = buildPayChannelOption(payChannelRes.data || [])
    industryOption.value = buildPieOption(industryRes.data || [])
    tenantGrowthOption.value = buildTenantGrowthOption(tenantGrowthRes.data || [])
    syncTrendOption.value = buildSyncTrendOption(syncTrendRes.data || [])
    topProductsOption.value = buildTopProductsOption(topProductsRes.data || [])
  } catch (e) {
    console.error('仪表盘数据加载失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 16px;
}

.welcome-card {
  margin-bottom: 16px;
  border-radius: 8px;

  .welcome-inner {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
  }

  .welcome-text {
    h2 {
      margin: 0 0 6px 0;
      font-size: 20px;
      color: #303133;
    }
    p {
      margin: 0;
      color: #909399;
      font-size: 13px;
    }
  }
}

.kpi-row {
  margin-bottom: 16px;
}

.kpi-card {
  margin-bottom: 16px;
  border-radius: 8px;

  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
  }

  .kpi-icon {
    flex-shrink: 0;
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .kpi-body {
    flex: 1;
    min-width: 0;
  }

  .kpi-label {
    font-size: 13px;
    color: #909399;
    margin-bottom: 4px;
  }

  .kpi-value {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    line-height: 1.2;
    word-break: break-all;
  }

  .kpi-sub {
    margin-top: 6px;
    font-size: 12px;
    color: #C0C4CC;
  }
}

.chart-card {
  margin-bottom: 16px;
  border-radius: 8px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }
}
</style>
