<!-- execution业务入口：固定数据集类型并复用后台记录页的查询、详情和权限逻辑。 -->
<template><RecordPage kind="execution" /></template>
<script setup>
import RecordPage from '@/views/admin/records/index.vue'
defineOptions({name:"AdminExecution"})
</script>
