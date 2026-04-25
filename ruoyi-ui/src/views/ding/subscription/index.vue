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
      <el-form-item label="订阅状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 240px">
          <el-option label="生效中" :value="1" />
          <el-option label="已过期" :value="2" />
          <el-option label="已取消" :value="3" />
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

    <el-table v-loading="loading" :data="subscriptionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="订阅ID" align="center" prop="id" width="80" />
      <el-table-column label="钉钉企业ID" align="center" prop="dingCorpId" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="产品名称" align="center" prop="productName" width="120" />
      <el-table-column label="产品类型" align="center" prop="productType" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.productType === 1" type="success">包月</el-tag>
          <el-tag v-else type="primary">包年</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="生效时间" align="center" prop="startTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="到期时间" align="center" prop="endTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="自动续费" align="center" prop="autoRenew" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.autoRenew === 1" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="已绑表单" align="center" prop="usedFormCount" width="90" />
      <el-table-column label="本月已同步" align="center" prop="monthUsedSync" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">生效中</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="danger">已过期</el-tag>
          <el-tag v-else type="info">已取消</el-tag>
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

    <!-- 添加或修改套餐订阅对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="钉钉企业ID" prop="dingCorpId">
          <el-input v-model="form.dingCorpId" placeholder="请输入钉钉企业ID" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="选择套餐" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择套餐" style="width: 100%">
            <el-option v-for="pkg in packageList" :key="pkg.id" :label="pkg.productName + ' (¥' + pkg.price + ')'" :value="pkg.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品类型" prop="productType">
          <el-radio-group v-model="form.productType">
            <el-radio :label="1">包月</el-radio>
            <el-radio :label="2">包年</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生效时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择生效时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="到期时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择到期时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="自动续费" prop="autoRenew">
          <el-radio-group v-model="form.autoRenew">
            <el-radio :label="0">否</el-radio>
            <el-radio :label="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="订阅状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">生效中</el-radio>
            <el-radio :label="2">已过期</el-radio>
            <el-radio :label="3">已取消</el-radio>
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

<script setup name="DingSubscription">
import { listSubscription, getSubscription, addSubscription, updateSubscription, delSubscription, getPackageOptions } from '@/api/middle/ding'

const { proxy } = getCurrentInstance()

const subscriptionList = ref([])
const packageList = ref([])
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
  status: undefined
})

const form = reactive({
  id: undefined,
  dingCorpId: undefined,
  productId: undefined,
  productType: 1,
  startTime: undefined,
  endTime: undefined,
  autoRenew: 0,
  usedFormCount: 0,
  monthUsedSync: 0,
  status: 1
})

const rules = {
  dingCorpId: [{ required: true, message: '钉钉企业ID不能为空', trigger: 'blur' }],
  productId: [{ required: true, message: '请选择套餐', trigger: 'change' }],
  startTime: [{ required: true, message: '生效时间不能为空', trigger: 'change' }],
  endTime: [{ required: true, message: '到期时间不能为空', trigger: 'change' }]
}

const formRef = ref()
const queryRef = ref()

function getList() {
  loading.value = true
  listSubscription(queryParams).then(response => {
    subscriptionList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function getPackageList() {
  getPackageOptions().then(response => {
    packageList.value = response.data || []
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.id = undefined
  form.dingCorpId = undefined
  form.productId = undefined
  form.productType = 1
  form.startTime = undefined
  form.endTime = undefined
  form.autoRenew = 0
  form.usedFormCount = 0
  form.monthUsedSync = 0
  form.status = 1
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
  title.value = '添加套餐订阅'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getSubscription(id).then(response => {
    Object.assign(form, response.data)
    open.value = true
    title.value = '修改套餐订阅'
  })
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.id !== undefined) {
        updateSubscription(form).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addSubscription(form).then(response => {
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
  proxy.$modal.confirm('是否确认删除订阅编号为"' + idsArr + '"的数据项？').then(() => {
    return delSubscription(idsArr)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
getPackageList()
</script>
