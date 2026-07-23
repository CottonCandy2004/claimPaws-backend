<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索预约" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="$router.push('/reservations/create')">创建预约</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="resourceName" label="资源" />
        <el-table-column label="类型" width="80"><template #default="{ row }">{{ row.resourceType === 'MEETING_ROOM' || row.resourceType === 'ROOM' ? '会议室' : '工位' }}</template></el-table-column>
        <el-table-column prop="startAt" label="开始时间" width="170" />
        <el-table-column prop="endAt" label="结束时间" width="170" />
        <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" v-if="['PENDING_APPROVAL','CONFIRMED'].includes(row.status)" type="danger" @click="handleCancel(row)">取消</el-button>
            <el-button size="small" v-if="row.status === 'CONFIRMED'" type="success" @click="handleCheckIn(row)">签到</el-button>
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

const keyword = ref('')
const data = ref<Reservation[]>([])
const { pageParams, total, loading, resetPage } = usePagination()

async function fetchData() {
  loading.value = true
  try { const res = await reservationApi.getReservationList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
function search() { resetPage(); fetchData() }
async function handleCancel(row: Reservation) {
  try {
    await ElMessageBox.confirm(`确定取消预约 "${row.title}"？`, '确认取消', { type: 'warning' })
    await reservationApi.cancelReservation(row.id)
    ElMessage.success('已取消')
    fetchData()
  } catch { /* 用户取消或网络错误由拦截器处理 */ }
}
async function handleCheckIn(row: Reservation) {
  try {
    await ElMessageBox.confirm(`确定签到 "${row.title}"？`, '确认签到', { type: 'info' })
    await reservationApi.checkInReservation(row.id)
    ElMessage.success('签到成功')
    fetchData()
  } catch { /* 用户取消或网络错误由拦截器处理 */ }
}
onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
