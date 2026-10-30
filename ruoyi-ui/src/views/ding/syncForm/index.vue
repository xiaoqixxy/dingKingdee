<template>
  <div class="app-container business-page">
    <div class="business-heading"><span class="business-eyebrow">业务工作空间</span><h1>🔗 同步表单配置</h1><p>管理企业表单与数据源的同步关系。</p></div>
    <h2 class="business-filter-title">🔎 筛选条件</h2>
    <!-- 筛选或编辑表单由响应式状态驱动，校验规则在脚本区维护。 -->
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
      <el-form-item label="表单名称" prop="formName">
        <el-input
          v-model="queryParams.formName"
          placeholder="请输入表单名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 240px">
          <el-option label="启用" :value="1" />
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

    <!-- 列表按稳定记录主键展示，操作按钮遵循页面权限控制。 -->
    <el-table v-loading="loading" :data="syncFormList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="配置ID" align="center" prop="id" width="80" />
      <el-table-column label="企业名称" prop="corpName" min-width="180" show-overflow-tooltip><template #default="{row}">{{ row.corpName || "未登记企业" }}</template></el-table-column>
      <el-table-column label="钉钉企业ID" align="center" prop="dingCorpId" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="钉钉多维表ID" align="center" prop="dingFormId" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="表单名称" align="center" prop="formName" :show-overflow-tooltip="true" />
      <el-table-column label="来源系统" align="center" prop="sourceSystem" width="120" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">启用</el-tag>
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

    <!-- 添加或修改同步表单配置对话框 -->
    <!-- 独立业务弹窗，关闭和提交由脚本区统一处理。 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <!-- 筛选或编辑表单由响应式状态驱动，校验规则在脚本区维护。 -->
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="钉钉企业ID" prop="dingCorpId">
          <el-input v-model="form.dingCorpId" placeholder="请输入钉钉企业ID" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="钉钉多维表ID" prop="dingFormId">
          <el-input v-model="form.dingFormId" placeholder="请输入钉钉多维表ID" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="表单名称" prop="formName">
          <el-input v-model="form.formName" placeholder="请输入表单名称" />
        </el-form-item>
        <el-form-item label="来源系统" prop="sourceSystem">
          <el-input v-model="form.sourceSystem" placeholder="请输入三方数据来源系统（如：CRM/ERP）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="2">停用</el-radio>
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

<script setup name="DingSyncForm">
import { listSyncForm, getSyncForm, addSyncForm, updateSyncForm, delSyncForm } from '@/api/middle/ding'

const { proxy } = getCurrentInstance()

const syncFormList = ref([])
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
  formName: undefined,
  status: undefined
})

const form = reactive({
  id: undefined,
  dingCorpId: undefined,
  dingFormId: undefined,
  formName: undefined,
  sourceSystem: undefined,
  status: 1
})

const rules = {
  dingCorpId: [{ required: true, message: '钉钉企业ID不能为空', trigger: 'blur' }],
  dingFormId: [{ required: true, message: '钉钉多维表ID不能为空', trigger: 'blur' }],
  formName: [{ required: true, message: '表单名称不能为空', trigger: 'blur' }],
  sourceSystem: [{ required: true, message: '来源系统不能为空', trigger: 'blur' }]
}

const formRef = ref()
const queryRef = ref()

/** 按当前筛选和页码读取列表，并更新加载状态。 */
function getList() {
  loading.value = true
  listSyncForm(queryParams).then(response => {
    syncFormList.value = response.rows
    total.value = response.total
  }).catch(() => {}).finally(() => {
    loading.value = false
  })
}

/** 关闭编辑窗口并重置未提交的表单状态。 */
function cancel() {
  open.value = false
  reset()
}

/** 重建编辑表单默认值并清理校验提示。 */
function reset() {
  form.id = undefined
  form.dingCorpId = undefined
  form.dingFormId = undefined
  form.formName = undefined
  form.sourceSystem = undefined
  form.status = 1
  proxy.resetForm('formRef')
}

/** 变更查询条件后从第一页重新加载。 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 清空查询表单并恢复默认分页。 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 同步勾选行标识及批量操作按钮状态。 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 重置表单并打开新增窗口。 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加同步表单配置'
}

/** 按行主键读取详情后打开编辑窗口。 */
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value
  getSyncForm(id).then(response => {
    Object.assign(form, response.data)
    open.value = true
    title.value = '修改同步表单配置'
  })
}

/** 通过表单校验后按主键选择新增或更新接口，成功后刷新列表。 */
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.id !== undefined) {
        updateSyncForm(form).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addSyncForm(form).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 确认删除后调用接口，并重新加载列表。 */
function handleDelete(row) {
  const idsArr = row.id || ids.value
  proxy.$modal.confirm('是否确认删除同步表单编号为"' + idsArr + '"的数据项？').then(() => {
    return delSyncForm(idsArr)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>