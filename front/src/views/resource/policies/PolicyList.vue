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
        <el-table-column label="审批" width="120">
          <template #default="{ row }">{{ approvalLabel(row.approvalLevel) }}</template>
        </el-table-column>
        <el-table-column label="审批角色" width="150">
          <template #default="{ row }">{{ row.approverRoles || '-' }}</template>
        </el-table-column>
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

    <el-dialog v-if="dialogVisible" v-model="dialogVisible" :title="editingId ? '编辑策略' : '新增策略'" width="600px">
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
        <el-form-item label="审批模式">
          <el-radio-group v-model="form.approvalLevel">
            <el-radio :value="0">免审批</el-radio>
            <el-radio :value="1">一级审批</el-radio>
            <el-radio :value="2">二级审批</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.approvalLevel > 0" label="审批角色(按序)">
          <span style="font-size:12px;color:#909399">按审批顺序排列，第一个为一级审批人，以此类推</span>
          <div style="display:flex; gap:16px; margin-top:8px">
            <div style="flex:1">
              <div style="font-size:13px;margin-bottom:4px;font-weight:500">可选角色</div>
              <div style="border:1px solid #dcdfe6;border-radius:4px;min-height:120px;max-height:200px;overflow-y:auto;padding:4px">
                <div v-for="r in availableRoles" :key="r.id" style="display:flex;justify-content:space-between;align-items:center;padding:4px 8px;cursor:pointer" @click="addRole(r)">
                  <span>{{ r.name }}</span>
                  <span style="color:#409EFF;font-size:12px">添加 →</span>
                </div>
                <div v-if="availableRoles.length === 0" style="color:#909399;font-size:12px;text-align:center;padding:16px">无可选角色</div>
              </div>
            </div>
            <div style="flex:1">
              <div style="font-size:13px;margin-bottom:4px;font-weight:500">审批链</div>
              <div style="border:1px solid #dcdfe6;border-radius:4px;min-height:120px;max-height:200px;overflow-y:auto;padding:4px">
                <div v-for="(r, i) in selectedRoles" :key="r.id" style="display:flex;justify-content:space-between;align-items:center;padding:4px 8px;background:#f0f9eb;margin-bottom:2px;border-radius:2px">
                  <span>{{ i + 1 }}. {{ r.name }}</span>
                  <div>
                    <el-button size="small" text @click="moveUp(i)" :disabled="i === 0">↑</el-button>
                    <el-button size="small" text @click="moveDown(i)" :disabled="i === selectedRoles.length - 1">↓</el-button>
                    <span style="color:#f56c6c;font-size:12px;cursor:pointer;margin-left:4px" @click="removeRole(i)">×</span>
                  </div>
                </div>
                <div v-if="selectedRoles.length === 0" style="color:#909399;font-size:12px;text-align:center;padding:16px">请从左侧添加审批角色</div>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { ReservationPolicy } from '@/types'
import * as policyApi from '@/api/modules/policy'
import * as roleApi from '@/api/modules/role'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<ReservationPolicy[]>([])
const allRoles = ref<any[]>([])
const selectedRoles = ref<any[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({
  name: '', resourceType: 'MEETING_ROOM' as string,
  timeSlotGranularity: 30, advanceBookingDays: 7, minDuration: 30,
  maxDuration: 240, cancelDeadline: 60, checkInWindow: 15,
  noShowPenalty: 0, approvalLevel: 0 as number, approverRoles: ''
})
const rules: FormRules = { name: [{ required: true, message: '请输入策略名称', trigger: 'blur' }], resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }] }

const availableRoles = computed(() => allRoles.value.filter(r => !selectedRoles.value.find(s => s.id === r.id)))

function approvalLabel(level: number) { return level === 0 ? '免审批' : level === 1 ? '一级审批' : '二级审批' }

async function loadRoles() {
  const res = await roleApi.getRoleList({ page: 1, size: 100 })
  allRoles.value = res.records || []
}
function addRole(r: any) { selectedRoles.value.push(r) }
function removeRole(i: number) { selectedRoles.value.splice(i, 1) }
function moveUp(i: number) { if (i > 0) { const t = selectedRoles.value[i]; selectedRoles.value[i] = selectedRoles.value[i - 1]; selectedRoles.value[i - 1] = t } }
function moveDown(i: number) { if (i < selectedRoles.value.length - 1) { const t = selectedRoles.value[i]; selectedRoles.value[i] = selectedRoles.value[i + 1]; selectedRoles.value[i + 1] = t } }

async function fetchData() {
  loading.value = true
  try { const res = await policyApi.getPolicyList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
function search() { resetPage(); fetchData() }
function handleCreate() {
  editingId.value = null; selectedRoles.value = []
  form.value = { name: '', resourceType: 'MEETING_ROOM', timeSlotGranularity: 30, advanceBookingDays: 7, minDuration: 30, maxDuration: 240, cancelDeadline: 60, checkInWindow: 15, noShowPenalty: 0, approvalLevel: 0, approverRoles: '' }
  dialogVisible.value = true
}
function handleEdit(row: any) {
  editingId.value = row.id
  selectedRoles.value = row.approverRoles ? row.approverRoles.split(',').map((id: string) => allRoles.value.find((r: any) => r.id === Number(id))).filter(Boolean) : []
  Object.assign(form.value, { ...row, approvalLevel: row.approvalLevel ?? 0, approverRoles: row.approverRoles || '' })
  dialogVisible.value = true
}
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      form.value.approverRoles = selectedRoles.value.map(r => r.id).join(',')
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
onMounted(() => { fetchData(); loadRoles() })
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
