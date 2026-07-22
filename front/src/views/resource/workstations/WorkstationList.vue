<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索工位" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="handleCreate">新增工位</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="floorName" label="所属楼层" />
        <el-table-column prop="buildingName" label="所属楼宇" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑工位' : '新增工位'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="所属楼层" prop="floorId">
          <el-select v-model="form.floorId" placeholder="选择楼层" style="width: 100%">
            <el-option v-for="f in floors" :key="f.id" :label="`${f.buildingName} - ${f.name}`" :value="f.id" />
          </el-select>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { Workstation, Floor } from '@/types'
import * as resourceApi from '@/api/modules/resource'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<Workstation[]>([])
const floors = ref<Floor[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ name: '', floorId: undefined as number | undefined })
const rules: FormRules = { name: [{ required: true, message: '请输入工位名称', trigger: 'blur' }], floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }] }

async function fetchData() {
  loading.value = true
  try { const res = await resourceApi.getWorkstationList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
async function loadFloors() {
  const campuses = await resourceApi.getAllCampuses()
  const allFloors: Floor[] = []
  for (const c of campuses) {
    const blds = await resourceApi.getBuildingsByCampus(c.id)
    for (const b of blds) { const fls = await resourceApi.getFloorsByBuilding(b.id); allFloors.push(...fls.map(f => ({ ...f, buildingName: b.name }))) }
  }
  floors.value = allFloors
}
function search() { resetPage(); fetchData() }
function handleCreate() { editingId.value = null; form.value = { name: '', floorId: undefined }; dialogVisible.value = true }
function handleEdit(row: Workstation) { editingId.value = row.id; form.value = { name: row.name, floorId: row.floorId }; dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      if (editingId.value) { await resourceApi.updateWorkstation(editingId.value, form.value as any); ElMessage.success('更新成功') }
      else { await resourceApi.createWorkstation(form.value as any); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: Workstation) {
  await ElMessageBox.confirm(`确定删除工位 "${row.name}"？`, '确认删除', { type: 'warning' })
  await resourceApi.deleteWorkstation(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(() => { fetchData(); loadFloors() })
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
