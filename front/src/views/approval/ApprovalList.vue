<template>
  <div class="page-container">
    <el-card>
      <template #header>待审批预约</template>
      <el-table :data="data" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="username" label="申请人" />
        <el-table-column prop="resourceName" label="资源" />
        <el-table-column prop="startAt" label="开始时间" width="170" />
        <el-table-column prop="endAt" label="结束时间" width="170" />
        <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Reservation } from '@/types'
import * as reservationApi from '@/api/modules/reservation'
import { usePagination } from '@/composables/usePagination'
import StatusTag from '@/components/StatusTag.vue'

const data = ref<Reservation[]>([])
const { pageParams, total, loading } = usePagination()

async function fetchData() {
  loading.value = true
  try { const res = await reservationApi.getPendingApprovals({ ...pageParams }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
async function handleApprove(row: Reservation) {
  await reservationApi.approveReservation(row.id, { approved: true, comment: '通过' })
  ElMessage.success('已通过'); fetchData()
}
async function handleReject(row: Reservation) {
  const { value: comment } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审批', { confirmButtonText: '确定', cancelButtonText: '取消' })
  if (comment !== null) { await reservationApi.approveReservation(row.id, { approved: false, comment }); ElMessage.success('已拒绝'); fetchData() }
}
onMounted(fetchData)
</script>
