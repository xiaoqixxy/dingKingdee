<template>
 <div class="app-container record-page">
  <div class="record-heading"><div><span class="eyebrow">业务工作空间</span><h1>{{ config.icon }} {{ config.title }}</h1><p>{{ config.subtitle }}</p></div><div><el-button v-if="kind==='execution'" v-hasPermi="['ding:syncLog:list']" @click="$router.push('/integration/syncLog')">历史同步日志</el-button><el-button v-if="kind==='refund'" v-hasPermi="['ding:refund:add']" type="primary" icon="Plus" @click="openRefund">申请退款</el-button></div></div>
  <el-card shadow="never" class="record-card">
   <template #header><strong>🔎 筛选条件</strong><span class="filter-note">企业ID与业务编号精确匹配</span></template>
   <el-form :model="query" label-position="top" class="record-filter" @submit.prevent="search">
    <el-form-item label="企业ID"><el-input v-model="query.dingCorpId" clearable placeholder="输入钉钉企业ID" @keyup.enter="search" /></el-form-item>
    <el-form-item :label="config.no"><el-input v-model="query.businessNo" clearable :placeholder="'输入'+config.no" @keyup.enter="search" /></el-form-item>
    <el-form-item v-if="Object.keys(config.statuses).length" label="状态"><el-select v-model="query.status" clearable placeholder="全部状态"><el-option v-for="(name,value) in config.statuses" :key="value" :label="name" :value="value" /></el-select></el-form-item>
    <el-form-item label="时间范围"><el-date-picker v-model="dates" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" /></el-form-item>
    <el-form-item v-if="kind!=='refund'" label="链路编号"><el-input v-model="query.traceId" clearable placeholder="输入 trace_id" @keyup.enter="search" /></el-form-item>
    <div class="filter-actions"><el-button type="primary" icon="Search" @click="search">查询</el-button><el-button icon="Refresh" @click="reset">重置</el-button></div>
   </el-form>
  </el-card>
  <el-card shadow="never" class="record-card">
   <template #header><strong>{{ config.title }}</strong><span class="filter-note">共 {{ total }} 条</span></template>
   <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="mb16" />
   <el-table v-loading="loading" :data="rows" row-key="id" empty-text="暂无记录，产生业务活动后将在这里显示">
    <el-table-column v-for="[key,label,width] in config.columns" :key="key" :prop="key" :label="label" :min-width="width" show-overflow-tooltip><template #default="{row}"><el-tag v-if="['status','result','change_type'].includes(key)" :type="tagType(row[key])" effect="light">{{ config.statuses[row[key]] || row[key] }}</el-tag><span v-else>{{ displayValue(key,row[key]) }}</span></template></el-table-column>
    <el-table-column label="操作" width="150" fixed="right"><template #default="{row}"><el-button link type="primary" @click="showDetail(row)">详情</el-button><el-button v-if="kind==='refund'&&row.status==='REQUESTED'" v-hasPermi="['ding:refund:cancel']" link type="danger" @click="cancel(row)">撤销</el-button></template></el-table-column>
   </el-table>
   <pagination v-show="total>0" :total="total" v-model:page="query.pageNum" v-model:limit="query.pageSize" @pagination="load" />
  </el-card>
  <el-drawer v-model="detailOpen" :title="config.title+' · 详情'" size="min(760px, 100vw)">
   <div v-loading="detailLoading"><el-descriptions :column="1" border><el-descriptions-item v-for="(value,key) in detail" :key="key" :label="fieldNames[key]||key"><el-button v-if="key==='error_stack' && value" link type="danger" @click="showInlineStack(value)">展开完整错误堆栈</el-button><pre v-else class="detail-value">{{ displayValue(key,value) }}</pre></el-descriptions-item></el-descriptions>
   <template v-if="kind==='execution'"><el-alert title="批次日志汇总所有分页，重复分页不重复累计；历史单页日志保持原口径。成功表示源数据已返回，不代表钉钉最终入表。" type="info" :closable="false" /><h3>分页与重试记录</h3><el-alert v-if="pageError" :title="pageError" type="error" :closable="false" /><el-table :data="pages" empty-text="历史记录暂无分页信息"><el-table-column prop="id" label="尝试编号" width="95" /><el-table-column prop="page_offset" label="起始位置" /><el-table-column prop="success_count" label="返回条数" /><el-table-column prop="status" label="状态" /><el-table-column label="计入总数"><template #default="{row}">{{ row.counted ? '是' : '否' }}</template></el-table-column><el-table-column label="错误"><template #default="{row}"><el-button v-if="row.error_message" link type="danger" @click="showPageStack(row)">完整堆栈</el-button></template></el-table-column></el-table><pagination v-show="pageTotal>0" :total="pageTotal" v-model:page="pageNum" v-model:limit="pageSize" @pagination="loadPages" /><h3>记录明细</h3><el-table :data="items" empty-text="该执行暂无记录明细"><el-table-column prop="source_record_id" label="源记录" /><el-table-column prop="status" label="状态" /><el-table-column prop="error_message" label="错误摘要" show-overflow-tooltip /></el-table><pagination v-show="itemTotal>0" :total="itemTotal" v-model:page="itemPage" v-model:limit="itemSize" @pagination="loadItems" /><template v-if="checkPermi(['ding:apiLog:list'])"><h3>接口调用记录</h3><el-alert v-if="apiError" :title="apiError" type="error" :closable="false" /><el-timeline><el-timeline-item v-for="call in apiCalls" :key="call.id" :timestamp="displayValue('occurred_at',call.occurred_at)"><strong>{{ call.api_name }}</strong> · {{ call.result }} · {{ call.duration_ms }} ms<p v-if="call.error_message">{{ call.error_message }} <el-button link type="danger" @click="showApiStack(call)">完整堆栈</el-button></p><pre v-if="call.request_summary" class="detail-value">{{ valueText(call.request_summary) }}</pre><pre v-if="call.response_summary" class="detail-value">{{ valueText(call.response_summary) }}</pre></el-timeline-item></el-timeline><pagination v-show="apiTotal>0" :total="apiTotal" v-model:page="apiPage" v-model:limit="apiSize" @pagination="loadApis" /></template></template></div>
  </el-drawer>
  <el-drawer v-model="stackOpen" title="完整错误堆栈（已脱敏）" size="min(1000px, 100vw)" append-to-body><pre v-loading="stackLoading" class="detail-value">{{ stackText }}</pre></el-drawer>
  <el-drawer v-model="refundOpen" title="申请退款" size="min(560px, 100vw)" :close-on-click-modal="!saving">
   <el-alert title="提交后生成待处理申请，不会立即退回资金，请在支付平台完成退款处理。" type="info" :closable="false" show-icon />
   <el-form ref="refundForm" :model="refund" :rules="refundRules" label-position="top" class="refund-form">
    <el-form-item label="📋 订单号" prop="orderNo"><el-input v-model="refund.orderNo" maxlength="64" placeholder="填写已支付订单号" /></el-form-item>
    <el-form-item label="🏦 支付商户号" prop="merchantAccountId"><el-input v-model="refund.merchantAccountId" maxlength="64" placeholder="填写原支付订单所属商户号" /></el-form-item>
    <el-form-item label="💰 退款金额（元）" prop="amount"><el-input-number v-model="refund.amount" :precision="2" :min="0.01" :max="99999999.99" controls-position="right" /></el-form-item>
    <el-form-item label="📝 退款原因" prop="reason"><el-input v-model="refund.reason" type="textarea" :rows="4" maxlength="500" show-word-limit /></el-form-item>
    <el-button type="primary" :loading="saving" @click="submitRefund">提交申请</el-button><el-button :disabled="saving" @click="refundOpen=false">取消</el-button>
   </el-form>
  </el-drawer>
 </div>
</template>
<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { checkPermi } from '@/utils/permission'
import { catalog, fieldNames } from './catalog'
import { listRecords, recordDetail, executionItems, executionApis, executionPages, executionPageStack, requestRefund, cancelRefund } from '@/api/middle/admin'
const props=defineProps({ kind:{type:String,required:true} })
const config=computed(()=>catalog[props.kind]);const kind=computed(()=>props.kind)
const query=reactive({pageNum:1,pageSize:20,dingCorpId:'',businessNo:'',status:'',traceId:''})
const rows=ref([]),total=ref(0),loading=ref(false),error=ref(''),dates=ref([])
const detail=ref({}),detailOpen=ref(false),detailLoading=ref(false),items=ref([]),itemTotal=ref(0),itemPage=ref(1),itemSize=ref(20)
const apiCalls=ref([]),apiError=ref(''),apiTotal=ref(0),apiPage=ref(1),apiSize=ref(20)
const pages=ref([]),pageTotal=ref(0),pageNum=ref(1),pageSize=ref(20),pageError=ref('')
const stackOpen=ref(false),stackText=ref(''),stackLoading=ref(false)
const refundOpen=ref(false),saving=ref(false),refundForm=ref(),refund=reactive({})
const refundRules={orderNo:[{required:true,message:'请输入订单号',trigger:'blur'}],merchantAccountId:[{required:true,message:'请输入商户号',trigger:'blur'}],amount:[{required:true,message:'请输入金额',trigger:'blur'}],reason:[{required:true,message:'请输入退款原因',trigger:'blur'}]}
let sequence=0,detailSequence=0
async function load(){const seq=++sequence;loading.value=true;error.value='';try{const res=await listRecords(props.kind,{...query,startDate:dates.value?.[0],endDate:dates.value?.[1]});if(seq===sequence){rows.value=res.rows;total.value=res.total}}catch{if(seq===sequence){error.value='记录加载失败，请重试或检查访问权限。';rows.value=[];total.value=0}}finally{if(seq===sequence)loading.value=false}}
function search(){query.pageNum=1;load()}
function reset(){Object.assign(query,{pageNum:1,pageSize:20,dingCorpId:'',businessNo:'',status:'',traceId:''});dates.value=[];load()}
function tagType(v){return ['FAILED','TIMEOUT','2'].includes(String(v))?'danger':['SUCCESS','SUCCEEDED','1'].includes(String(v))?'success':['PENDING','REQUESTED','PROCESSING','PARTIAL'].includes(String(v))?'warning':'info'}
const labels={log_scope:{BATCH:'完整批次',PAGE:'历史单页',LEGACY:'旧游标续传'},business_type:{TENANT:'企业',ORDER:'订单',REFUND:'退款',INVOICE:'发票',SUBSCRIPTION:'订阅',CONFIG:'配置'},actor_type:{USER:'用户',SYSTEM:'系统',CALLBACK:'回调'},direction:{DING_TO_KINGDEE:'钉钉 → 金蝶',KINGDEE_TO_DING:'金蝶 → 钉钉'},trigger_type:{MANUAL:'手动',SCHEDULE:'定时',RETRY:'重试',EVENT:'事件触发'},platform:{DING:'钉钉',KINGDEE:'金蝶',PAYMENT:'支付平台'},event_type:{TENANT_STUB_CREATED:'建立待完善企业档案',SUBSCRIPTION_TYPE_REPAIRED:'修复订阅类型',SUBSCRIPTION_EXPIRED_REPAIRED:'修复订阅过期状态',REFUND_REQUESTED:'申请退款',REFUND_CANCELLED:'撤销退款申请'}}
function displayValue(key,value){if(value==null)return '—';if(labels[key]?.[value])return labels[key][value];if(['status','result','change_type'].includes(key))return config.value.statuses[value]||value;if(typeof value==='string' && /^\d{4}-\d{2}-\d{2}T\d{2}:/.test(value))return value.replace('T',' ').slice(0,19);return valueText(value)}
function valueText(value){if(value===null||value===undefined||value==='')return '—';if(typeof value==='object')return JSON.stringify(value,null,2);if(typeof value==='string'&&value.startsWith('{')){try{return JSON.stringify(JSON.parse(value),null,2)}catch{}}return String(value)}
async function showDetail(row){
 const seq=++detailSequence;detail.value={};items.value=[];pages.value=[];apiCalls.value=[];apiError.value='';pageError.value='';itemTotal.value=0;pageTotal.value=0;apiTotal.value=0;detailOpen.value=true;detailLoading.value=true
 try{const res=await recordDetail(props.kind,row.id);if(seq!==detailSequence)return;detail.value=res.data
  if(props.kind==='execution'){itemPage.value=1;pageNum.value=1;apiPage.value=1;await Promise.all([loadItems(),loadPages(),checkPermi(['ding:apiLog:list'])?loadApis():Promise.resolve()])}
 }catch{if(seq===detailSequence)detailOpen.value=false}finally{if(seq===detailSequence)detailLoading.value=false}
}
async function loadPages(){const id=detail.value.id,seq=detailSequence;if(!id)return;pageError.value='';try{const res=await executionPages(id,{pageNum:pageNum.value,pageSize:pageSize.value});if(seq===detailSequence){pages.value=res.rows;pageTotal.value=res.total}}catch{if(seq===detailSequence)pageError.value='分页记录加载失败，请重试'}}
async function loadApis(){const id=detail.value.id,seq=detailSequence;if(!id)return;apiError.value='';try{const res=await executionApis(id,{pageNum:apiPage.value,pageSize:apiSize.value});if(seq===detailSequence){apiCalls.value=res.rows;apiTotal.value=res.total}}catch{if(seq===detailSequence)apiError.value='接口调用记录加载失败，请重试'}}
function showInlineStack(value){stackText.value=value;stackLoading.value=false;stackOpen.value=true}
async function showPageStack(row){stackOpen.value=true;stackLoading.value=true;stackText.value='';try{const res=await executionPageStack(detail.value.id,row.id);stackText.value=res.data||row.error_message||'暂无堆栈'}catch{stackText.value='堆栈加载失败，请重试'}finally{stackLoading.value=false}}
async function showApiStack(row){stackOpen.value=true;stackLoading.value=true;stackText.value='';try{const res=await recordDetail('apiLog',row.id);stackText.value=res.data.error_stack||row.error_message||'暂无堆栈'}catch{stackText.value='堆栈加载失败，请重试'}finally{stackLoading.value=false}}
async function loadItems(){const id=detail.value.id;if(!id)return;const res=await executionItems(id,{pageNum:itemPage.value,pageSize:itemSize.value});if(detail.value.id===id){items.value=res.rows;itemTotal.value=res.total}}
function openRefund(){Object.assign(refund,{refundNo:'RF'+crypto.randomUUID(),orderNo:'',merchantAccountId:'',amount:undefined,reason:''});refundOpen.value=true}
async function submitRefund(){try{await refundForm.value.validate()}catch{return}saving.value=true;try{await requestRefund({...refund});ElMessage.success('退款申请已提交');refundOpen.value=false;load()}finally{saving.value=false}}
async function cancel(row){try{await ElMessageBox.confirm('确认撤销这笔待处理的退款申请？','撤销申请',{type:'warning'})}catch{return}await cancelRefund(row.id);ElMessage.success('申请已撤销');load()}
watch(()=>props.kind,()=>{detailOpen.value=false;reset()},{immediate:true})
</script>
<style scoped>
.record-heading{display:flex;align-items:center;justify-content:space-between;gap:16px;margin-bottom:22px}.eyebrow{font-size:11px;color:#8993a7;letter-spacing:2px}h1{font-size:24px;color:#25314a;margin:7px 0}.record-heading p{font-size:13px;color:#8893a7;margin:0}.record-card{margin-bottom:18px;border-radius:12px}.filter-note{margin-left:16px;color:#929db0;font-size:12px}.record-filter{padding:16px;background:#f7f8fc;border-radius:8px;display:flex;flex-wrap:wrap;gap:0 16px}.record-filter .el-form-item{width:220px;margin-bottom:14px}.record-filter .el-form-item:has(.el-date-editor){width:300px}.record-filter :deep(.el-date-editor){width:100%}.filter-actions{align-self:flex-end;margin-bottom:14px}.detail-value{font:13px/1.7 inherit;white-space:pre-wrap;word-break:break-all;margin:0}.refund-form{margin-top:24px}.refund-form .el-input-number{width:100%}.record-card :deep(.pagination-container){margin-bottom:12px}.record-card :deep(.el-table th){background:#f8faff;color:#7c899f}.record-card :deep(.el-card__header){border-bottom:1px solid #f0f2f7}@media(max-width:600px){.record-heading{align-items:flex-start;flex-direction:column}.record-filter .el-form-item,.record-filter .el-form-item:has(.el-date-editor){width:100%}.filter-note{display:block;margin:5px 0 0}}
</style>
