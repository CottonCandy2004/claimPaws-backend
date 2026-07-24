<template>
  <div class="page-container">
    <el-card>
      <template #header>投递审计</template>
      <el-table :data="data" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="webhookId" label="Webhook ID" />
        <el-table-column prop="eventType" label="事件类型" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" v-if="row.status === 'FAILED'" @click="handleRetry(row)">重试</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { DeliveryAudit } from '@/types'
import * as notifApi from '@/api/modules/notification'
import { usePagination } from '@/composables/usePagination'

const data = ref<DeliveryAudit[]>([])
const { pageParams, total, loading } = usePagination()

async function fetchData() {
  loading.value = true
  try { const res = await notifApi.getDeliveryAuditList({ ...pageParams }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
async function handleRetry(row: DeliveryAudit) { await notifApi.retryDelivery(row.id); ElMessage.success('已重新投递'); fetchData() }
onMounted(fetchData)
</script>
