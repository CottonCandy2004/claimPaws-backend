<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <span></span>
        <el-button type="primary" @click="handleCreate">新增 Webhook</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="url" label="URL" />
        <el-table-column label="启用" width="60"><template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template></el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleTest(row)">测试</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑 Webhook' : '新增 Webhook'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="URL" prop="url"><el-input v-model="form.url" /></el-form-item>
        <el-form-item label="密钥" prop="secret"><el-input v-model="form.secret" show-password /></el-form-item>
        <el-form-item label="事件类型"><el-select v-model="form.events" multiple placeholder="选择事件类型" style="width: 100%"><el-option label="预约创建" value="reservation.created" /><el-option label="预约确认" value="reservation.confirmed" /><el-option label="预约取消" value="reservation.cancelled" /></el-select></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="最大重试" label-width="80px"><el-input-number v-model="form.maxRetries" :min="0" :max="10" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="连接超时(s)" label-width="90px"><el-input-number v-model="form.connectTimeout" :min="1" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="读取超时(s)" label-width="90px"><el-input-number v-model="form.readTimeout" :min="1" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { WebhookConfig } from '@/types'
import * as notifApi from '@/api/modules/notification'
import { usePagination } from '@/composables/usePagination'

const data = ref<WebhookConfig[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ name: '', url: '', secret: '', events: [] as string[], enabled: true, maxRetries: 3, connectTimeout: 5, readTimeout: 30 })
const rules: FormRules = { name: [{ required: true, message: '请输入名称', trigger: 'blur' }], url: [{ required: true, message: '请输入 URL', trigger: 'blur' }] }

async function fetchData() {
  loading.value = true
  try { const res = await notifApi.getWebhookList({ ...pageParams }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
function handleCreate() { editingId.value = null; form.value = { name: '', url: '', secret: '', events: [], enabled: true, maxRetries: 3, connectTimeout: 5, readTimeout: 30 }; dialogVisible.value = true }
function handleEdit(row: WebhookConfig) { editingId.value = row.id; Object.assign(form.value, row); dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      if (editingId.value) { await notifApi.updateWebhook(editingId.value, form.value); ElMessage.success('更新成功') }
      else { await notifApi.createWebhook(form.value); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleTest(row: WebhookConfig) { await notifApi.testWebhook(row.id); ElMessage.success('测试请求已发送') }
async function handleDelete(row: WebhookConfig) {
  await ElMessageBox.confirm(`确定删除 Webhook "${row.name}"？`, '确认删除', { type: 'warning' })
  await notifApi.deleteWebhook(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
