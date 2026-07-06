<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="${comment}" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="dingCorpId">
        <el-input
          v-model="queryParams.dingCorpId"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="serverUrl">
        <el-input
          v-model="queryParams.serverUrl"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="cId">
        <el-input
          v-model="queryParams.cId"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="appId">
        <el-input
          v-model="queryParams.appId"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="appSecret">
        <el-input
          v-model="queryParams.appSecret"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="ext1">
        <el-input
          v-model="queryParams.ext1"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="ext2">
        <el-input
          v-model="queryParams.ext2"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="ext3">
        <el-input
          v-model="queryParams.ext3"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="${comment}" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入${comment}"
          clearable
          @keyup.enter="handleQuery"
        />
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
          v-hasPermi="['kingdee:voucherConfig:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['kingdee:voucherConfig:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['kingdee:voucherConfig:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['kingdee:voucherConfig:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="${comment}" align="center" prop="id" />
      <el-table-column label="${comment}" align="center" prop="name" />
      <el-table-column label="${comment}" align="center" prop="dingCorpId" />
      <el-table-column label="${comment}" align="center" prop="serverUrl" />
      <el-table-column label="${comment}" align="center" prop="cId" />
      <el-table-column label="${comment}" align="center" prop="userName" />
      <el-table-column label="${comment}" align="center" prop="appId" />
      <el-table-column label="${comment}" align="center" prop="appSecret" />
      <el-table-column label="${comment}" align="center" prop="ext1" />
      <el-table-column label="${comment}" align="center" prop="ext2" />
      <el-table-column label="${comment}" align="center" prop="ext3" />
      <el-table-column label="${comment}" align="center" prop="orderNo" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['kingdee:voucherConfig:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['kingdee:voucherConfig:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改【请填写功能名称】对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="configRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="${comment}" prop="name">
          <el-input v-model="form.name" placeholder="请输入${comment}" />
        </el-form-item>
        <el-form-item label="${comment}" prop="dingCorpId">
          <el-input v-model="form.dingCorpId" placeholder="请输入${comment}" />
        </el-form-item>
        <el-form-item label="${comment}" prop="serverUrl">
          <el-input v-model="form.serverUrl" placeholder="请输入${comment}" />
        </el-form-item>
        <el-form-item label="${comment}" prop="cId">
          <el-input v-model="form.cId" placeholder="请输入${comment}" />
        </el-form-item>
        <el-form-item label="${comment}" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入${comment}" />
        </el-form-item>
        <el-form-item label="${comment}" prop="appId">
          <el-input v-model="form.appId" placeholder="请输入${comment}" />
        </el-form-item>
        <el-form-item label="${comment}" prop="appSecret">
          <el-input v-model="form.appSecret" placeholder="请输入${comment}" />
        </el-form-item>
        <el-form-item label="${comment}" prop="ext1">
          <el-input v-model="form.ext1" placeholder="请输入${comment}" />
        </el-form-item>
        <el-form-item label="${comment}" prop="ext2">
          <el-input v-model="form.ext2" placeholder="请输入${comment}" />
        </el-form-item>
        <el-form-item label="${comment}" prop="ext3">
          <el-input v-model="form.ext3" placeholder="请输入${comment}" />
        </el-form-item>
        <el-form-item label="${comment}" prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="请输入${comment}" />
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

<script setup name="Config">
import { listConfig, getConfig, delConfig, addConfig, updateConfig } from "@/api/kingdee/voucherConfig"

const { proxy } = getCurrentInstance()

const configList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    dingCorpId: null,
    serverUrl: null,
    cId: null,
    userName: null,
    appId: null,
    appSecret: null,
    ext1: null,
    ext2: null,
    ext3: null,
    orderNo: null
  },
  rules: {
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询【请填写功能名称】列表 */
function getList() {
  loading.value = true
  listConfig(queryParams.value).then(response => {
    configList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    name: null,
    dingCorpId: null,
    serverUrl: null,
    cId: null,
    userName: null,
    appId: null,
    appSecret: null,
    ext1: null,
    ext2: null,
    ext3: null,
    orderNo: null
  }
  proxy.resetForm("configRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加【请填写功能名称】"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getConfig(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改【请填写功能名称】"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["configRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateConfig(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addConfig(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除【请填写功能名称】编号为"' + _ids + '"的数据项？').then(function() {
    return delConfig(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/config/export', {
    ...queryParams.value
  }, `config_${new Date().getTime()}.xlsx`)
}

getList()
</script>
