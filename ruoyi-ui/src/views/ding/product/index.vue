<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="产品名称" prop="productName">
        <el-input
          v-model="queryParams.productName"
          placeholder="请输入产品名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品类型" prop="productType">
        <el-select v-model="queryParams.productType" placeholder="请选择产品类型" clearable style="width: 240px">
          <el-option label="包月" :value="1" />
          <el-option label="包年" :value="2" />
          <el-option label="永久免费" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="产品分类" prop="productCategory">
        <el-select v-model="queryParams.productCategory" placeholder="请选择产品分类" clearable style="width: 240px">
          <el-option label="基础版" value="基础版" />
          <el-option label="专业版" value="专业版" />
          <el-option label="企业版" value="企业版" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否默认" prop="isDefault">
        <el-select v-model="queryParams.isDefault" placeholder="请选择" clearable style="width: 240px">
          <el-option label="是" :value="1" />
          <el-option label="否" :value="0" />
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

    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="产品ID" align="center" prop="id" width="80" />
      <el-table-column label="产品名称" align="center" prop="productName" :show-overflow-tooltip="true" />
      <el-table-column label="产品分类" align="center" prop="productCategory" width="100" />
      <el-table-column label="产品类型" align="center" prop="productType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.productType === 1" type="success">包月</el-tag>
          <el-tag v-else-if="scope.row.productType === 2" type="primary">包年</el-tag>
          <el-tag v-else-if="scope.row.productType === 3" type="warning">永久免费</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="默认" align="center" prop="isDefault" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.isDefault === 1" type="success">是</el-tag>
          <el-tag v-else type="info">否</el-tag>
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

    <!-- 添加或修改产品配置对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="产品分类" prop="productCategory">
          <el-select v-model="form.productCategory" placeholder="请选择产品分类" style="width: 100%">
            <el-option label="基础版" value="基础版" />
            <el-option label="专业版" value="专业版" />
            <el-option label="企业版" value="企业版" />
          </el-select>
        </el-form-item>
<el-form-item label="产品类型" prop="productType">
          <el-radio-group v-model="form.productType">
            <el-radio :label="1">包月</el-radio>
            <el-radio :label="2">包年</el-radio>
            <el-radio :label="3">永久免费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否默认产品" prop="isDefault">
          <el-radio-group v-model="form.isDefault">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
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
        <el-form-item label="产品描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入产品描述" :rows="3" />
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

<script setup name="DingProduct">
import { listProduct, getProduct, addProduct, updateProduct, delProduct } from '@/api/middle/ding'

const { proxy } = getCurrentInstance()

const productList = ref([])
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
  productName: undefined,
  productType: undefined,
  productCategory: undefined,
  isDefault: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  productName: undefined,
  productCategory: undefined,
  productType: 1,
  isDefault: 0,
  price: 0,
  syncFormLimit: 0,
  singleSyncLimit: 0,
  monthSyncLimit: 0,
  status: 1,
  sort: 0,
  remark: undefined
})

const rules = {
  productName: [{ required: true, message: '产品名称不能为空', trigger: 'blur' }],
  productType: [{ required: true, message: '产品类型不能为空', trigger: 'change' }],
  price: [{ required: true, message: '价格不能为空', trigger: 'blur' }]
}

const formRef = ref()
const queryRef = ref()

function getList() {
  loading.value = true
  listProduct(queryParams).then(response => {
    productList.value = response.rows
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
  form.productName = undefined
  form.productCategory = undefined
  form.productType = 1
  form.isDefault = 0
  form.price = 0
  form.syncFormLimit = 0
  form.singleSyncLimit = 0
  form.monthSyncLimit = 0
  form.status = 1
  form.sort = 0
  form.remark = undefined
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
  title.value = '添加产品配置'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getProduct(id).then(response => {
    Object.assign(form, response.data)
    open.value = true
    title.value = '修改产品配置'
  })
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.id !== undefined) {
        updateProduct(form).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addProduct(form).then(response => {
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
  proxy.$modal.confirm('是否确认删除产品配置编号为"' + idsArr + '"的数据项？').then(() => {
    return delProduct(idsArr)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>