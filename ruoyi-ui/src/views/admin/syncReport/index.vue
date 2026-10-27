<template>
  <div class="app-container dashboard" v-loading="loading">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-inner"><div><h2>同步执行情况报表</h2><p>钉钉 × 金蝶 · 运行情况与同步条数变化</p></div>
        <el-button :loading="loading" @click="load">刷新数据</el-button></div>
      <div class="filters"><strong>📊 统计范围</strong>
        <el-radio-group v-model="days" @change="load"><el-radio-button v-for="n in [7,15,30]" :key="n" :value="n">近{{ n }}天</el-radio-button></el-radio-group>
        <el-select v-model="scope" style="width:180px" @change="load"><el-option v-for="(label,key) in scopes" :key="key" :label="label" :value="key" /></el-select>
      </div>
    </el-card>
    <el-alert v-if="failed" title="报表加载失败，请点击刷新重试" type="error" :closable="false" show-icon />
    <template v-if="report">
      <el-alert class="notice" type="info" :closable="false" show-icon>
        <template #title>{{ report.startDate }} 至 {{ report.endDate }}（{{ report.timezone }}，含今日未结束数据），与前一个等长周期比较。</template>
        达到同步上限属于正常结束，不计入异常；正常结束率包含成功和达到上限的执行。同步条数是成功返回给钉钉的记录数，不代表源数据实际新增、删除或钉钉落库数。各口径独立统计：{{ coverage }}。
      </el-alert>
      <el-row :gutter="16">
        <el-col v-for="card in cards" :key="card.label" :xs="24" :sm="12" :lg="6">
          <el-card shadow="hover" class="kpi-card"><div class="kpi-icon" :style="{background:card.color}">{{ card.icon }}</div><div><div class="muted">{{ card.label }}</div><div class="kpi-value">{{ card.value }}</div><div class="kpi-sub">{{ card.sub }}</div></div></el-card>
        </el-col>
      </el-row>
      <el-card class="chart-card" shadow="never"><template #header><strong>🟢 当前运行情况</strong></template>
        <div class="runtime"><span>当前节点：请求正常</span><span>数据库：统计查询正常</span><span>节点已运行 {{ uptime }}</span><span>JVM 堆内存 {{ mb(report.node.heapUsed) }} / {{ mb(report.node.heapMax) }} MB</span><span>当前口径未结束 {{ format(report.running.active) }} 个</span><span :class="{warning:report.running.stale>0}">超过 30 分钟未更新 {{ format(report.running.stale) }} 个</span></div>
        <p class="muted small">未结束任务统计覆盖全部日期；长时间未更新需结合执行日志排查，不直接判定失败。节点状态仅反映本次请求所在服务。</p>
      </el-card>
      <el-card class="chart-card" shadow="never"><template #header><strong>同步条数与每日增减</strong></template><Echart v-if="hasData" :option="volumeChart" height="320px" /><el-empty v-else description="当前范围暂无执行记录，可切换历史单页口径查看旧日志" /></el-card>
      <el-card class="chart-card" shadow="never"><template #header><strong>执行状态趋势</strong></template><Echart v-if="hasData" :option="executionChart" height="300px" /><el-empty v-else description="暂无执行数据" /></el-card>
      <el-card class="chart-card" shadow="never"><template #header><div class="welcome-inner"><strong>最近异常执行（最多 10 条）</strong><span class="muted">完整堆栈请查看同步执行记录</span></div></template>
        <!-- 列表按稳定记录主键展示，操作按钮遵循页面权限控制。 -->
        <el-table :data="report.errors" empty-text="当前范围没有异常执行" stripe>
          <el-table-column prop="id" label="执行编号" width="100" /><el-table-column prop="corp_name" label="企业名称" min-width="180" show-overflow-tooltip /><el-table-column prop="ding_corp_id" label="企业 ID" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" width="110"><template #default="{row}"><el-tag type="danger">{{ row.status==='PARTIAL'?'部分成功':'失败' }}</el-tag></template></el-table-column>
          <el-table-column prop="error_message" label="异常摘要" min-width="250" show-overflow-tooltip /><el-table-column prop="trace_id" label="链路编号" min-width="240" show-overflow-tooltip /><el-table-column prop="created_at" label="创建时间" min-width="180" />
        </el-table>
      </el-card>
      <p class="muted small">更新于 {{ report.generatedAt.replace('T',' ') }} · 数据按执行创建日期归属，后续分页或重试可能更新历史日期的统计。</p>
    </template>
  </div>
</template>

<script setup name="SyncReport">
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

const days=ref(30), scope=ref('BATCH'), report=ref(null), loading=ref(false), failed=ref(false)
const scopes={ BATCH:'完整同步批次', PAGE:'历史单页日志', LEGACY:'旧游标续传' }
let requestId=0
const format=value=>Number(value||0).toLocaleString('zh-CN')
const signed=value=>(value>0?'+':'')+format(value)
const mb=value=>(Number(value)/1024/1024).toFixed(0)
const coverage=computed(()=>report.value.coverage.map(row=>`${scopes[row.scope]||row.scope} ${format(row.count)} 条`).join('，')||'暂无日志')
const hasData=computed(()=>report.value.current.executions>0)
const uptime=computed(()=>{const mins=Math.floor(report.value.node.uptimeMs/60000); return `${Math.floor(mins/1440)} 天 ${Math.floor(mins%1440/60)} 小时 ${mins%60} 分钟`})
const cards=computed(()=>{const c=report.value.current;return [
  {label:'执行数量',value:format(c.executions),sub:`已结束 ${format(c.completed)} · 进行中 ${format(c.active)}`,icon:'↻',color:'#5B8FF9'},
  {label:'正常结束率',value:c.successRate==null?'—':c.successRate.toFixed(1)+'%',sub:`异常 ${format(c.errors)} · 达到上限 ${format(c.limited)}`,icon:'✓',color:'#5AD8A6'},
  {label:'成功同步条数',value:format(c.records),sub:`较上周期 ${signed(c.change)} 条（${c.changePercent==null?'上周期为 0，无百分比':signed(Number(c.changePercent.toFixed(1)))+'%'}）`,icon:'⇅',color:'#9270CA'},
  {label:'平均执行耗时',value:c.averageDuration==null?'—':(c.averageDuration/1000).toFixed(2)+' 秒',sub:'已结束执行的分页处理累计耗时',icon:'◷',color:'#F6BD16'}
]})
/** 组装共享的日期坐标轴、图例和提示框，再接收各图表的数据系列。 */
function chart(series){return {color:['#5B8FF9','#5AD8A6','#E8684A','#F6BD16','#9270CA'],tooltip:{trigger:'axis'},legend:{top:0},grid:{left:65,right:30,top:45,bottom:30},xAxis:{type:'category',data:report.value.daily.map(r=>r.day)},yAxis:{type:'value',splitLine:{lineStyle:{type:'dashed'}}},series}}
const volumeChart=computed(()=>chart([
  {name:'成功同步条数',type:'line',smooth:true,data:report.value.daily.map(r=>r.records)},
  {name:'较前一日增减（条）',type:'bar',data:report.value.daily.map(r=>({value:r.change,itemStyle:{color:r.change<0?'#E8684A':'#5AD8A6'}}))}
]))
const executionChart=computed(()=>chart([
  {name:'执行总量',type:'line',data:report.value.daily.map(r=>r.executions)},
  {name:'成功',type:'bar',data:report.value.daily.map(r=>r.succeeded)},
  {name:'异常（不含达到上限）',type:'bar',data:report.value.daily.map(r=>r.errors)},
  {name:'达到同步上限',type:'bar',data:report.value.daily.map(r=>r.limited)},
  {name:'未结束',type:'bar',data:report.value.daily.map(r=>r.active)}
]))
/** 加载当前数据集并以请求序号丢弃旧响应。 */
async function load(){const id=++requestId;loading.value=true;failed.value=false;report.value=null;try{const res=await request({url:'/admin/sync-report',method:'get',params:{days:days.value,scope:scope.value}});if(id===requestId)report.value=res.data}catch{if(id===requestId)failed.value=true}finally{if(id===requestId)loading.value=false}}
onMounted(load)
</script>

<style scoped lang="scss">
.dashboard{padding:16px}.welcome-card,.chart-card,.notice{margin-bottom:16px;border-radius:8px}.welcome-inner{display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px}h2{font-size:20px;margin:0 0 6px;color:#303133}p{margin:6px 0;color:#909399;font-size:13px}.filters{display:flex;align-items:center;gap:16px;flex-wrap:wrap;background:#f7f9fc;padding:14px;margin-top:16px;border-radius:8px}.muted{color:#909399;font-size:13px}.small{font-size:12px}.kpi-card{margin-bottom:16px;border-radius:8px;min-height:140px;:deep(.el-card__body){display:flex;align-items:center;gap:16px;padding:20px}}.kpi-icon{width:48px;height:48px;border-radius:12px;flex-shrink:0;color:white;display:flex;align-items:center;justify-content:center;font-size:28px}.kpi-value{font-size:26px;font-weight:600;color:#303133;margin:6px 0;word-break:break-all}.kpi-sub{font-size:12px;color:#909399;min-height:32px}.runtime{display:flex;flex-wrap:wrap;gap:16px 28px;font-size:14px;color:#303133}.warning{color:#e6a23c}
</style>
