<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索策略" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="handleCreate">新增策略</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="策略名称" />
        <el-table-column label="资源类型" width="100">
          <template #default="{ row }">{{ row.resourceType === 'MEETING_ROOM' ? '会议室' : '工位' }}</template>
        </el-table-column>
        <el-table-column prop="approvalLevel" label="审批级别" width="80" />
        <el-table-column prop="maxDuration" label="最长时长(min)" width="110" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑策略' : '新增策略'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="策略名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" style="width: 100%">
            <el-option label="会议室" value="MEETING_ROOM" /><el-option label="工位" value="WORKSTATION" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="时段粒度(min)" prop="timeSlotGranularity"><el-input-number v-model="form.timeSlotGranularity" :min="5" :step="5" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="提前预约天数" prop="advanceBookingDays"><el-input-number v-model="form.advanceBookingDays" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="最短时长(min)" prop="minDuration"><el-input-number v-model="form.minDuration" :min="5" :step="5" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="最长时长(min)" prop="maxDuration"><el-input-number v-model="form.maxDuration" :min="5" :step="5" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="取消截止(min)" prop="cancelDeadline"><el-input-number v-model="form.cancelDeadline" :min="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="签到窗口(min)" prop="checkInWindow"><el-input-number v-model="form.checkInWindow" :min="0" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="审批级别" prop="approvalLevel"><el-input-number v-model="form.approvalLevel" :min="0" :max="2" /></el-form-item>
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
import type { ReservationPolicy } from '@/types'
import * as policyApi from '@/api/modules/policy'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<ReservationPolicy[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ name: '', resourceType: 'MEETING_ROOM' as 'MEETING_ROOM' | 'WORKSTATION', timeSlotGranularity: 30, advanceBookingDays: 7, minDuration: 30, maxDuration: 240, cancelDeadline: 60, checkInWindow: 15, noShowPenalty: 0, approvalLevel: 0 as 0 | 1 | 2 })
const rules: FormRules = { name: [{ required: true, message: '请输入策略名称', trigger: 'blur' }], resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }] }

async function fetchData() {
  loading.value = true
  try { const res = await policyApi.getPolicyList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
function search() { resetPage(); fetchData() }
function handleCreate() { editingId.value = null; form.value = { name: '', resourceType: 'MEETING_ROOM', timeSlotGranularity: 30, advanceBookingDays: 7, minDuration: 30, maxDuration: 240, cancelDeadline: 60, checkInWindow: 15, noShowPenalty: 0, approvalLevel: 0 }; dialogVisible.value = true }
function handleEdit(row: ReservationPolicy) { editingId.value = row.id; Object.assign(form.value, row); dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      if (editingId.value) { await policyApi.updatePolicy(editingId.value, form.value); ElMessage.success('更新成功') }
      else { await policyApi.createPolicy(form.value); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: ReservationPolicy) {
  await ElMessageBox.confirm(`确定删除策略 "${row.name}"？`, '确认删除', { type: 'warning' })
  await policyApi.deletePolicy(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
