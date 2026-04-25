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
      <el-form-item label="企业名称" prop="corpName">
        <el-input
          v-model="queryParams.corpName"
          placeholder="请输入企业名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系人" prop="contactUser">
        <el-input
          v-model="queryParams.contactUser"
          placeholder="请输入企业联系人"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 240px">
          <el-option label="正常" :value="1" />
          <el-option label="停用" :value="2" />
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

    <el-table v-loading="loading" :data="tenantList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="钉钉企业ID" align="center" prop="dingCorpId" :show-overflow-tooltip="true" width="180" />
      <el-table-column label="企业名称" align="center" prop="corpName" :show-overflow-tooltip="true" />
      <el-table-column label="联系人" align="center" prop="contactUser" width="120" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" width="130" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
          <el-tag v-else type="danger">停用</el-tag>
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

    <!-- 添加或修改租户企业对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="钉钉企业ID" prop="dingCorpId">
          <el-input v-model="form.dingCorpId" placeholder="请输入钉钉企业ID" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="企业名称" prop="corpName">
          <el-input v-model="form.corpName" placeholder="请输入企业名称" />
        </el-form-item>
        <el-form-item label="企业联系人" prop="contactUser">
          <el-input v-model="form.contactUser" placeholder="请输入企业联系人" />
        </el-form-item>
        <el-form-item label="联系人电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系人电话" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">停用</el-radio>
          </el-radio-group>
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

<script setup name="DingTenant">
import { listTenant, getTenant, addTenant, updateTenant, delTenant } from '@/api/middle/ding'

const { proxy } = getCurrentInstance()

const tenantList = ref([])
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
  corpName: undefined,
  contactUser: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  dingCorpId: undefined,
  corpName: undefined,
  contactUser: undefined,
  contactPhone: undefined,
  status: 1,
  ext1: undefined,
  ext2: undefined,
  ext3: undefined
})

const rules = {
  dingCorpId: [{ required: true, message: '钉钉企业ID不能为空', trigger: 'blur' }],
  corpName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }]
}

const formRef = ref()
const queryRef = ref()

function getList() {
  loading.value = true
  listTenant(queryParams).then(response => {
    tenantList.value = response.rows
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
  form.corpName = undefined
  form.contactUser = undefined
  form.contactPhone = undefined
  form.status = 1
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
  title.value = '添加租户企业'
}

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getTenant(id).then(response => {
    Object.assign(form, response.data)
    open.value = true
    title.value = '修改租户企业'
  })
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.id !== undefined) {
        updateTenant(form).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addTenant(form).then(response => {
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
  proxy.$modal.confirm('是否确认删除租户企业编号为"' + idsArr + '"的数据项？').then(() => {
    return delTenant(idsArr)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>
