<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="套餐名称" prop="packageName">
        <el-input
          v-model="queryParams.packageName"
          placeholder="请输入套餐名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="套餐类型" prop="packageType">
        <el-select v-model="queryParams.packageType" placeholder="请选择套餐类型" clearable style="width: 240px">
          <el-option label="包月" :value="1" />
          <el-option label="包年" :value="2" />
          <el-option label="按量付费" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 240px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="2" />
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

    <el-table v-loading="loading" :data="packageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="套餐ID" align="center" prop="id" width="80" />
      <el-table-column label="套餐名称" align="center" prop="packageName" :show-overflow-tooltip="true" />
      <el-table-column label="套餐类型" align="center" prop="packageType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.packageType === 1" type="success">包月</el-tag>
          <el-tag v-else-if="scope.row.packageType === 2" type="primary">包年</el-tag>
          <el-tag v-else type="info">按量付费</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="价格(元)" align="center" prop="price" width="100" />
      <el-table-column label="可绑定表单数" align="center" prop="syncFormLimit" width="110" />
      <el-table-column label="单次同步上限" align="center" prop="singleSyncLimit" width="110" />
      <el-table-column label="每月同步上限" align="center" prop="monthSyncLimit" width="110" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">上架</el-tag>
          <el-tag v-else type="danger">下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
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

    <!-- 添加或修改套餐配置对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="套餐名称" prop="packageName">
          <el-input v-model="form.packageName" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="套餐类型" prop="packageType">
          <el-radio-group v-model="form.packageType">
            <el-radio :label="1">包月</el-radio>
            <el-radio :label="2">包年</el-radio>
            <el-radio :label="3">按量付费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="可绑定表单数量" prop="syncFormLimit">
          <el-input-number v-model="form.syncFormLimit" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="单次同步数据上限" prop="singleSyncLimit">
          <el-input-number v-model="form.singleSyncLimit" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="每月总同步上限" prop="monthSyncLimit">
          <el-input-number v-model="form.monthSyncLimit" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="2">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序权重" prop="sort">
          <el-input-number v-model="form.sort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="套餐描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入套餐描述" :rows="3" />
        </el-form-item>
        <el-row>
          <el-col :span="8">
            <el-form-item label="扩展字段1" prop="ext1">
              <el-input v-model="form.ext1" placeholder="扩展字段1" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="扩展字段2" prop="ext2">
              <el-input v-model="form.ext2" placeholder="扩展字段2" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="扩展字段3" prop="ext3">
              <el-input v-model="form.ext3" placeholder="扩展字段3" />
            </el-form-item>
          </el-col>
        </el-row>
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

<script setup name="DingPackage">
import { listPackage, getPackage, addPackage, updatePackage, delPackage } from '@/api/middle/ding'

const { proxy } = getCurrentInstance()

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
  packageName: undefined,
  packageType: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  packageName: undefined,
  packageType: 1,
  price: 0,
  syncFormLimit: 0,
  singleSyncLimit: 0,
  monthSyncLimit: 0,
  status: 1,
  sort: 0,
  remark: undefined,
  ext1: undefined,
  ext2: undefined,
  ext3: undefined
})

const rules = {
  packageName: [{ required: true, message: '套餐名称不能为空', trigger: 'blur' }],
  packageType: [{ required: true, message: '套餐类型不能为空', trigger: 'change' }],
  price: [{ required: true, message: '价格不能为空', trigger: 'blur' }]
}

const formRef = ref()
const queryRef = ref()

function getList() {
  loading.value = true
  listPackage(queryParams).then(response => {
    packageList.value = response.rows
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
  form.packageName = undefined
  form.packageType = 1
  form.price = 0
  form.syncFormLimit = 0
  form.singleSyncLimit = 0
  form.monthSyncLimit = 0
  form.status = 1
  form.sort = 0
  form.remark = undefined
  form.ext1 = undefined
  form.ext2 = undefined
  form.ext3 = undefined
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
  title.value = '添加套餐配置'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getPackage(id).then(response => {
    Object.assign(form, response.data)
    open.value = true
    title.value = '修改套餐配置'
  })
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.id !== undefined) {
        updatePackage(form).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addPackage(form).then(response => {
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
  proxy.$modal.confirm('是否确认删除套餐配置编号为"' + idsArr + '"的数据项？').then(() => {
    return delPackage(idsArr)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>
