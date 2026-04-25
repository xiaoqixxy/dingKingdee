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
      <el-form-item label="同步状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 240px">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="2" />
          <el-option label="超量拦截" :value="3" />
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
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="syncLogList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="日志ID" align="center" prop="id" width="80" />
      <el-table-column label="钉钉企业ID" align="center" prop="dingCorpId" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="钉钉多维表ID" align="center" prop="dingFormId" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="同步条数" align="center" prop="syncCount" width="90" />
      <el-table-column label="同步状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 1" type="success">成功</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="danger">失败</el-tag>
          <el-tag v-else type="warning">超量拦截</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="异常信息" align="center" prop="errorMsg" :show-overflow-tooltip="true" />
      <el-table-column label="同步时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="80">
        <template #default="scope">
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
  </div>
</template>

<script setup name="DingSyncLog">
import { listSyncLog, delSyncLog } from '@/api/middle/ding'

const { proxy } = getCurrentInstance()

const syncLogList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  dingCorpId: undefined,
  status: undefined
})

const queryRef = ref()

function getList() {
  loading.value = true
  listSyncLog(queryParams).then(response => {
    syncLogList.value = response.rows
    total.value = response.total
    loading.value = false
  })
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
  multiple.value = !selection.length
}

function handleDelete(row) {
  const idsArr = row.id || ids.value
  proxy.$modal.confirm('是否确认删除日志编号为"' + idsArr + '"的数据项？').then(() => {
    return delSyncLog(idsArr)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>
