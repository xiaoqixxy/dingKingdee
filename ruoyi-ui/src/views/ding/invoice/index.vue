<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="企业ID" prop="dingCorpId">
        <el-input
          v-model="queryParams.dingCorpId"
          placeholder="请输入钉钉企业ID"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="发票类型" prop="invoiceType">
        <el-select v-model="queryParams.invoiceType" placeholder="请选择发票类型" clearable style="width: 240px">
          <el-option label="普通发票" :value="1" />
          <el-option label="增值税专用发票" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="开票状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 240px">
          <el-option label="待开票" :value="0" />
          <el-option label="已开票" :value="1" />
        </el-select>
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

    <el-table v-loading="loading" :data="invoiceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="发票ID" align="center" prop="id" width="80" />
      <el-table-column label="钉钉企业ID" align="center" prop="dingCorpId" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="订单号" align="center" prop="orderNo" :show-overflow-tooltip="true" width="180" />
      <el-table-column label="发票类型" align="center" prop="invoiceType" width="130">
        <template #default="scope">
          <el-tag v-if="scope.row.invoiceType === 1">普通发票</el-tag>
          <el-tag v-else type="primary">增值税专用发票</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发票抬头" align="center" prop="invoiceTitle" :show-overflow-tooltip="true" />
      <el-table-column label="纳税人识别号" align="center" prop="taxNo" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="开票金额" align="center" prop="amount" width="100">
        <template #default="scope">
          ¥{{ scope.row.amount }}
        </template>
      </el-table-column>
      <el-table-column label="开票状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">待开票</el-tag>
          <el-tag v-else type="success">已开票</el-tag>
        </template>
      </el-table-column>
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

    <!-- 添加或修改发票申请对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item label="钉钉企业ID" prop="dingCorpId">
          <el-input v-model="form.dingCorpId" placeholder="请输入钉钉企业ID" />
        </el-form-item>
        <el-form-item label="关联订单号" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入关联订单号" />
        </el-form-item>
        <el-form-item label="发票类型" prop="invoiceType">
          <el-radio-group v-model="form.invoiceType">
            <el-radio :label="1">普通发票</el-radio>
            <el-radio :label="2">增值税专用发票</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发票抬头" prop="invoiceTitle">
          <el-input v-model="form.invoiceTitle" placeholder="请输入发票抬头" />
        </el-form-item>
        <el-form-item label="纳税人识别号" prop="taxNo">
          <el-input v-model="form.taxNo" placeholder="请输入纳税人识别号（专票必填）" />
        </el-form-item>
        <el-form-item label="开票金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开票状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">待开票</el-radio>
            <el-radio :label="1">已开票</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DingInvoice">
import { listInvoice, getInvoice, addInvoice, updateInvoice, delInvoice } from '@/api/middle/ding'

const { proxy } = getCurrentInstance()

const invoiceList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const open = ref(false)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  dingCorpId: undefined,
  orderNo: undefined,
  invoiceType: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  dingCorpId: undefined,
  orderNo: undefined,
  invoiceType: 1,
  invoiceTitle: undefined,
  taxNo: undefined,
  amount: 0,
  status: 0
})

const rules = {
  dingCorpId: [{ required: true, message: '钉钉企业ID不能为空', trigger: 'blur' }],
  orderNo: [{ required: true, message: '订单号不能为空', trigger: 'blur' }],
  invoiceTitle: [{ required: true, message: '发票抬头不能为空', trigger: 'blur' }],
  amount: [{ required: true, message: '开票金额不能为空', trigger: 'blur' }]
}

const formRef = ref()
const queryRef = ref()

function getList() {
  loading.value = true
  listInvoice(queryParams).then(response => {
    invoiceList.value = response.rows
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
  form.dingCorpId = undefined
  form.orderNo = undefined
  form.invoiceType = 1
  form.invoiceTitle = undefined
  form.taxNo = undefined
  form.amount = 0
  form.status = 0
  proxy.resetForm('formRef')
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
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
  open.value = true
  title.value = '添加发票申请'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getInvoice(id).then(response => {
    Object.assign(form, response.data)
    open.value = true
    title.value = '修改发票申请'
  })
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.id !== undefined) {
        updateInvoice(form).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addInvoice(form).then(response => {
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
  proxy.$modal.confirm('是否确认删除发票编号为"' + idsArr + '"的数据项？').then(() => {
    return delInvoice(idsArr)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>