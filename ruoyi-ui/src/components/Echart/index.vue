<template>
  <div ref="chartRef" :style="{ width: width, height: height }"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { onMounted, onBeforeUnmount, ref, watch, nextTick } from 'vue'

const props = defineProps({
  // ECharts 配置项
  option: {
    type: Object,
    required: true
  },
  // 图表高度，默认 320px
  height: {
    type: String,
    default: '320px'
  },
  // 图表宽度，默认 100%
  width: {
    type: String,
    default: '100%'
  },
  // 是否在 option 变化时合并而非替换，默认 false（替换）
  notMerge: {
    type: Boolean,
    default: true
  }
})

const chartRef = ref(null)
let chartInstance = null
let resizeObserver = null

function initChart() {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(props.option || {}, props.notMerge)
}

function resize() {
  chartInstance && chartInstance.resize()
}

watch(
  () => props.option,
  (newOption) => {
    if (chartInstance && newOption) {
      chartInstance.setOption(newOption, props.notMerge)
    }
  },
  { deep: true }
)

onMounted(() => {
  nextTick(() => {
    initChart()
    // 使用 ResizeObserver 监听容器尺寸变化，比 window resize 更可靠（响应布局/抽屉折叠）
    if (chartRef.value && typeof ResizeObserver !== 'undefined') {
      resizeObserver = new ResizeObserver(() => resize())
      resizeObserver.observe(chartRef.value)
    }
  })
})

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

// 暴露实例便于父组件调用（如手动 resize）
defineExpose({
  resize,
  getInstance: () => chartInstance
})
</script>
