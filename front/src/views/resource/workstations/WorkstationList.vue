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

    <el-dialog v-if="dialogVisible" v-model="dialogVisible" :title="editingId ? '编辑工位' : '新增工位'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="所属楼层" prop="floorCascade">
          <el-cascader
            v-model="form.floorCascade"
            :options="campusTree"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: false }"
            placeholder="选择园区 > 楼宇 > 楼层"
            style="width: 100%"
            clearable
          />
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
import type { Workstation } from '@/types'
import * as resourceApi from '@/api/modules/resource'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<Workstation[]>([])
const campusTree = ref<any[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ name: '', floorCascade: [] as number[] })
const rules: FormRules = {
  name: [{ required: true, message: '请输入工位名称', trigger: 'blur' }],
  floorCascade: [{ required: true, message: '请选择楼层', trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try { const res = await resourceApi.getWorkstationList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
async function loadCampusTree() {
  const campuses = await resourceApi.getAllCampuses()
  const tree = []
  for (const c of campuses) {
    const blds = await resourceApi.getBuildingsByCampus(c.id)
    const bchildren = []
    for (const b of blds) {
      const fls = await resourceApi.getFloorsByBuilding(b.id)
      bchildren.push({ ...b, children: fls.map((f: any) => ({ ...f, leaf: true })) })
    }
    tree.push({ ...c, children: bchildren })
  }
  campusTree.value = tree
}
function search() { resetPage(); fetchData() }
function handleCreate() {
  editingId.value = null
  form.value = { name: '', floorCascade: [] }
  dialogVisible.value = true
}
async function handleEdit(row: any) {
  editingId.value = row.id
  await loadCampusTree()
  const cascade = findCascadePath(row.buildingName, row.floorName)
  form.value = { name: row.name, floorCascade: cascade }
  dialogVisible.value = true
}
function findCascadePath(buildingName: string, floorName: string): number[] {
  for (const c of campusTree.value) {
    for (const b of c.children || []) {
      if (b.name === buildingName) {
        for (const f of b.children || []) {
          if (f.name === floorName) return [c.id, b.id, f.id]
        }
      }
    }
  }
  return []
}
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      const cascade = form.value.floorCascade
      const payload = { name: form.value.name, floorId: cascade[cascade.length - 1] }
      if (editingId.value) { await resourceApi.updateWorkstation(editingId.value, payload as any); ElMessage.success('更新成功') }
      else { await resourceApi.createWorkstation(payload as any); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: Workstation) {
  await ElMessageBox.confirm(`确定删除工位 "${row.name}"？`, '确认删除', { type: 'warning' })
  await resourceApi.deleteWorkstation(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(() => { fetchData(); loadCampusTree() })
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
