<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="表单名称" prop="formName">
        <el-input
          v-model="queryParams.formName"
          placeholder="请输入表单名称"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="表单标识" prop="formKey">
        <el-input
          v-model="queryParams.formKey"
          placeholder="请输入表单标识"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间" style="width: 308px">
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
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Upload"
          @click="handleImport"
        >导入</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tableConfigList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="configId" />
      <el-table-column label="表单名称" align="center" prop="formName" :show-overflow-tooltip="true" />
      <el-table-column label="表单标识" align="center" prop="formKey" :show-overflow-tooltip="true" />
      <el-table-column label="排序号" align="center" prop="sortOrder" width="100" />
      <el-table-column label="扩展字段1" align="center" prop="extend1" :show-overflow-tooltip="true" />
      <el-table-column label="扩展字段2" align="center" prop="extend2" :show-overflow-tooltip="true" />
      <el-table-column label="扩展字段3" align="center" prop="extend3" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
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
            type="primary"
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

    <!-- 添加或修改金蝶表单配置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="表单名称" prop="formName">
          <el-input v-model="form.formName" placeholder="请输入表单名称" />
        </el-form-item>
        <el-form-item label="表单标识" prop="formKey">
          <el-input v-model="form.formKey" placeholder="请输入表单标识" :disabled="!!form.configId" />
        </el-form-item>
        <el-form-item label="表单配置">
          <el-input v-model="form.formConfig" type="textarea" placeholder="请输入表单配置(JSON)" :rows="3" />
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="扩展字段1" prop="extend1">
          <el-input v-model="form.extend1" placeholder="请输入扩展字段1" />
        </el-form-item>
        <el-form-item label="扩展字段2" prop="extend2">
          <el-input v-model="form.extend2" placeholder="请输入扩展字段2" />
        </el-form-item>
        <el-form-item label="扩展字段3" prop="extend3">
          <el-input v-model="form.extend3" placeholder="请输入扩展字段3" />
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

    <!-- 导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="KingdeeTableConfig">
import { listTableConfig, getTableConfig, addTableConfig, updateTableConfig, delTableConfig } from '@/api/kingdee'
import { getToken } from '@/utils/auth'

const { proxy } = getCurrentInstance()

const tableConfigList = ref([])
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
  formName: undefined,
  formKey: undefined
})

const form = reactive({
  configId: undefined,
  formName: undefined,
  formKey: undefined,
  formConfig: undefined,
  sortOrder: 0,
  extend1: undefined,
  extend2: undefined,
  extend3: undefined,
  remark: undefined
})

const rules = {
  formName: [{ required: true, message: '表单名称不能为空', trigger: 'blur' }],
  formKey: [{ required: true, message: '表单标识不能为空', trigger: 'blur' }]
}

const formRef = ref()
const queryRef = ref()
const uploadRef = ref()

/** 导入参数 */
const upload = reactive({
  open: false,
  title: '',
  isUploading: false,
  headers: { Authorization: 'Bearer ' + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + '/kingdee/tableConfig/importData'
})

/** 查询金蝶表单配置列表 */
function getList() {
  loading.value = true
  listTableConfig(proxy.addDateRange(queryParams, dateRange.value)).then(response => {
    tableConfigList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.configId = undefined
  form.formName = undefined
  form.formKey = undefined
  form.formConfig = undefined
  form.sortOrder = 0
  form.extend1 = undefined
  form.extend2 = undefined
  form.extend3 = undefined
  form.remark = undefined
  proxy.resetForm('formRef')
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.configId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加金蝶表单配置'
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const configId = row.configId || ids.value
  getTableConfig(configId).then(response => {
    Object.assign(form, response.data)
    open.value = true
    title.value = '修改金蝶表单配置'
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.configId !== undefined) {
        updateTableConfig(form).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addTableConfig(form).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const configIds = row.configId || ids.value
  proxy.$modal.confirm('是否确认删除金蝶表单配置编号为"' + configIds + '"的数据项？').then(() => {
    return delTableConfig(configIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('kingdee/tableConfig/export', {
    ...queryParams
  }, `kingdee_table_config_${new Date().getTime()}.xlsx`)
}

/** 导入按钮操作 */
function handleImport() {
  upload.title = '金蝶表单配置导入'
  upload.open = true
}

/** 下载模板操作 */
function importTemplate() {
  proxy.download('kingdee/tableConfig/importTemplate', {
  }, `kingdee_table_config_template_${new Date().getTime()}.xlsx`)
}

/** 文件上传中处理 */
function handleFileUploadProgress(event, file, fileList) {
  upload.isUploading = true
}

/** 文件选择处理 */
function handleFileChange(file, fileList) {
  upload.selectedFile = file
}

/** 文件删除处理 */
function handleFileRemove(file, fileList) {
  upload.selectedFile = null
}

/** 文件上传成功处理 */
function handleFileSuccess(response, file, fileList) {
  upload.open = false
  upload.isUploading = false
  proxy.$refs['uploadRef'].handleRemove(file)
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true })
  getList()
}

/** 提交上传文件 */
function submitFileForm() {
  const file = upload.selectedFile
  if (!file || !file.name.toLowerCase().endsWith('.xls') && !file.name.toLowerCase().endsWith('.xlsx')) {
    proxy.$modal.msgError("请选择后缀为 \"xls\"或\"xlsx\"的文件。")
    return
  }
  proxy.$refs['uploadRef'].submit()
}

getList()
</script>
