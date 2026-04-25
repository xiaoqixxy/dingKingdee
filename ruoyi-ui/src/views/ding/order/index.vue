<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="企业ID" prop="dingCorpId">
        <el-input
          v-model="queryParams.dingCorpId"
          placeholder="请输入钉钉企业ID"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单类型" prop="orderType">
        <el-select v-model="queryParams.orderType" placeholder="请选择订单类型" clearable style="width: 240px">
          <el-option label="新购" :value="1" />
          <el-option label="升级" :value="2" />
          <el-option label="降级" :value="3" />
          <el-option label="续费" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" placeholder="请选择支付状态" clearable style="width: 240px">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已取消" :value="2" />
          <el-option label="退款" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付时间" style="width: 308px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-select v-model="testPayChannel" placeholder="选择支付渠道" style="width: 140px">
          <el-option label="支付宝" value="alipay" />
          <el-option label="微信支付" value="wechat_pay" />
          <el-option label="钉钉支付" value="ding_pay" />
        </el-select>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Money"
          @click="handleOnlinePayTest"
          :loading="payLoadingChannel !== ''"
        >发起支付测试</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订单ID" align="center" prop="id" width="80" />
      <el-table-column label="订单号" align="center" prop="orderNo" :show-overflow-tooltip="true" width="180" />
      <el-table-column label="钉钉企业ID" align="center" prop="dingCorpId" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="订单类型" align="center" prop="orderType" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.orderType === 1" type="primary">新购</el-tag>
          <el-tag v-else-if="scope.row.orderType === 2" type="success">升级</el-tag>
          <el-tag v-else-if="scope.row.orderType === 3" type="warning">降级</el-tag>
          <el-tag v-else type="info">续费</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单金额" align="center" prop="totalAmount" width="100">
        <template #default="scope">
          ¥{{ scope.row.totalAmount }}
        </template>
      </el-table-column>
      <el-table-column label="实付金额" align="center" prop="payAmount" width="100">
        <template #default="scope">
          ¥{{ scope.row.payAmount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" prop="payStatus" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.payStatus === 0" type="warning">待支付</el-tag>
          <el-tag v-else-if="scope.row.payStatus === 1" type="success">已支付</el-tag>
          <el-tag v-else-if="scope.row.payStatus === 2" type="info">已取消</el-tag>
          <el-tag v-else type="danger">退款</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="payTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.payTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付渠道" align="center" prop="payChannel" width="100" />
      <el-table-column label="流水号" align="center" prop="transactionId" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改订单对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入订单号" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="钉钉企业ID" prop="dingCorpId">
          <el-input v-model="form.dingCorpId" placeholder="请输入钉钉企业ID" />
        </el-form-item>
        <el-form-item label="套餐选择" prop="packageId">
          <el-select v-model="form.packageId" placeholder="请选择套餐" style="width: 100%">
            <el-option v-for="pkg in packageList" :key="pkg.id" :label="pkg.productName" :value="pkg.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单类型" prop="orderType">
          <el-select v-model="form.orderType" placeholder="请选择订单类型" style="width: 100%">
            <el-option label="新购" :value="1" />
            <el-option label="升级" :value="2" />
            <el-option label="降级" :value="3" />
            <el-option label="续费" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单金额" prop="totalAmount">
          <el-input-number v-model="form.totalAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实付金额" prop="payAmount">
          <el-input-number v-model="form.payAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付状态" prop="payStatus">
          <el-select v-model="form.payStatus" placeholder="请选择支付状态" style="width: 100%">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已取消" :value="2" />
            <el-option label="退款" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付时间" prop="payTime">
          <el-date-picker
            v-model="form.payTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择支付时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="支付渠道" prop="payChannel">
          <el-select v-model="form.payChannel" placeholder="请选择支付渠道" style="width: 100%">
            <el-option label="支付宝" value="支付宝" />
            <el-option label="微信" value="微信" />
            <el-option label="钉钉支付" value="钉钉支付" />
            <el-option label="线下" value="线下" />
          </el-select>
        </el-form-item>
        <el-form-item label="流水号" prop="transactionId">
          <el-input v-model="form.transactionId" placeholder="请输入第三方支付流水号" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 在线支付二维码弹窗 -->
    <el-dialog title="在线支付" v-model="payDialogVisible" width="420px" append-to-body :close-on-click-modal="false" :close-on-press-escape="false">
      <div style="text-align: center; padding: 20px 0;">
        <p style="font-size: 16px; margin-bottom: 8px;">订单号：<strong>{{ payInfo.orderNo }}</strong></p>
        <p style="font-size: 14px; margin-bottom: 8px; color: #606266;">支付渠道：{{ payInfo.channelName || '支付宝' }}</p>
        <img :src="payInfo.qrCode" alt="支付二维码" style="width: 250px; height: 250px;" />
        <p style="margin-top: 16px; font-size: 14px; color: #909399;">
          请使用手机扫码支付
        </p>
        <p style="margin-top: 8px; font-size: 16px; color: #e6a23c; font-weight: bold;">
          剩余时间：{{ payCountdown }}
        </p>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: space-between; gap: 12px;">
          <el-button type="success" @click="handlePaySuccess" style="flex: 1;">模拟支付成功</el-button>
          <el-button @click="handlePayClose" style="flex: 1;">关闭（取消订单）</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DingOrder">
import { listOrder, getOrder, addOrder, updateOrder, delOrder, getPackageOptions, onlinePay, paymentCallback, getOrderByNo } from '@/api/middle/ding'

const { proxy } = getCurrentInstance()

const orderList = ref([])
const packageList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const open = ref(false)
const dateRange = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  orderNo: undefined,
  dingCorpId: undefined,
  orderType: undefined,
  payStatus: undefined
})

const form = reactive({
  id: undefined,
  orderNo: undefined,
  dingCorpId: undefined,
  packageId: undefined,
  orderType: 1,
  totalAmount: 0,
  payAmount: 0,
  payStatus: 0,
  payTime: undefined,
  payChannel: undefined,
  transactionId: undefined,
  remark: undefined
})

const rules = {
  orderNo: [{ required: true, message: '订单号不能为空', trigger: 'blur' }],
  dingCorpId: [{ required: true, message: '钉钉企业ID不能为空', trigger: 'blur' }],
  packageId: [{ required: true, message: '请选择套餐', trigger: 'change' }],
  orderType: [{ required: true, message: '订单类型不能为空', trigger: 'change' }],
  totalAmount: [{ required: true, message: '订单金额不能为空', trigger: 'blur' }]
}

const formRef = ref()
const queryRef = ref()
const payDialogVisible = ref(false)
const payLoadingChannel = ref('')
const testPayChannel = ref('alipay')
const payInfo = ref({})
const payCountdown = ref('')
let payCountdownTimer = null
let payExpireTimer = null
let payStatusCheckTimer = null

function getList() {
  loading.value = true
  listOrder(proxy.addDateRange(queryParams, dateRange.value)).then(response => {
    orderList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.id = undefined
  form.orderNo = undefined
  form.dingCorpId = undefined
  form.packageId = undefined
  form.orderType = 1
  form.totalAmount = 0
  form.payAmount = 0
  form.payStatus = 0
  form.payTime = undefined
  form.payChannel = undefined
  form.transactionId = undefined
  form.remark = undefined
  proxy.resetForm('formRef')
}

function getPackageList() {
  getPackageOptions().then(response => {
    packageList.value = response.data || []
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  form.orderNo = 'ORD' + new Date().getTime()
  open.value = true
  title.value = '添加订单'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getOrder(id).then(response => {
    Object.assign(form, response.data)
    open.value = true
    title.value = '修改订单'
  })
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.id !== undefined) {
        updateOrder(form).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addOrder(form).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const idsArr = row.id || ids.value
  proxy.$modal.confirm('是否确认删除订单编号为"' + idsArr + '"的数据项？').then(() => {
    return delOrder(idsArr)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleOnlinePayTest() {
  payLoadingChannel.value = testPayChannel.value
  const channelName = testPayChannel.value === 'alipay' ? '支付宝' : testPayChannel.value === 'wechat_pay' ? '微信支付' : '钉钉支付'
  onlinePay({ dingCorpId: 'TEST-001', productId: 2, channel: testPayChannel.value }).then(response => {
    const data = response.data
    payInfo.value = {
      orderNo: data.orderNo,
      qrCode: data.qrCode,
      channelName: channelName
    }
    payDialogVisible.value = true
    startCountdown(data.expireTime)
    startExpireTimer(data.orderNo)
    startOrderStatusCheck(data.orderNo)
  }).catch(() => {
    proxy.$modal.msgError('创建支付订单失败')
  }).finally(() => {
    payLoadingChannel.value = ''
  })
}

function startOrderStatusCheck(orderNo) {
  if (payStatusCheckTimer) clearInterval(payStatusCheckTimer)
  payStatusCheckTimer = setInterval(() => {
    getOrderByNo(orderNo).then(response => {
      const order = response.data
      if (order && order.payStatus === 1) {
        clearInterval(payStatusCheckTimer)
        proxy.$modal.msgSuccess('支付成功，订单已生效')
        closePayDialog()
        getList()
      }
    }).catch(() => {})
  }, 2000)
}

function startCountdown(expireTimeStr) {
  if (payCountdownTimer) clearInterval(payCountdownTimer)
  payCountdownTimer = setInterval(() => {
    const now = new Date().getTime()
    const expire = new Date(expireTimeStr).getTime()
    const diff = expire - now
    if (diff <= 0) {
      clearInterval(payCountdownTimer)
      payCountdown.value = '已过期'
      return
    }
    const minutes = Math.floor(diff / 60000)
    const seconds = Math.floor((diff % 60000) / 1000)
    payCountdown.value = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  }, 1000)
}

function startExpireTimer(orderNo) {
  if (payExpireTimer) clearTimeout(payExpireTimer)
  payExpireTimer = setTimeout(() => {
    paymentCallback({ orderNo: orderNo, action: 'timeout' }).then(() => {
      proxy.$modal.msgWarning('订单已超时取消')
      closePayDialog()
      getList()
    })
  }, 15 * 60 * 1000)
}

function handlePaySuccess() {
  paymentCallback({ orderNo: payInfo.value.orderNo, action: 'pay' }).then(() => {
    proxy.$modal.msgSuccess('支付成功，订单已生效，已自动生成开票申请')
    closePayDialog()
    getList()
  }).catch(() => {
    proxy.$modal.msgError('支付回调处理失败')
  })
}

function handlePayClose() {
  paymentCallback({ orderNo: payInfo.value.orderNo, action: 'cancel' }).then(() => {
    proxy.$modal.msgInfo('订单已取消')
    closePayDialog()
    getList()
  }).catch(() => {
    proxy.$modal.msgError('取消订单失败')
  })
}

function closePayDialog() {
  payDialogVisible.value = false
  if (payCountdownTimer) clearInterval(payCountdownTimer)
  if (payExpireTimer) clearTimeout(payExpireTimer)
  if (payStatusCheckTimer) clearInterval(payStatusCheckTimer)
  payCountdown.value = ''
  payInfo.value = {}
}

getList()
getPackageList()
</script>
