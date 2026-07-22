<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <span></span>
        <el-button type="primary" @click="handleCreate(null)">新增部门</el-button>
      </div>
      <el-table :data="data" v-loading="loading" row-key="id" default-expand-all stripe border style="margin-top: 16px">
        <el-table-column prop="name" label="部门名称" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" @click="handleCreate(row)">添加子部门</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑部门' : '新增部门'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="父部门" v-if="!editingId">
          <el-tree-select v-model="form.parentId" :data="data" :props="{ value: 'id', label: 'name', children: 'children' }" check-strictly clearable placeholder="选择父部门（留空则为根部门）" style="width: 100%" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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
import type { Department } from '@/types'
import * as deptApi from '@/api/modules/department'

const data = ref<Department[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = ref<{ name: string; parentId?: number; sort: number }>({ name: '', sort: 0 })
const rules: FormRules = { name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }] }

async function fetchData() { loading.value = true; try { data.value = await deptApi.getDepartmentTree() } finally { loading.value = false } }
function handleCreate(parent?: Department | null) { editingId.value = null; form.value = { name: '', parentId: parent?.id, sort: 0 }; dialogVisible.value = true }
function handleEdit(row: Department) { editingId.value = row.id; form.value = { name: row.name, sort: row.sort }; dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return; submitting.value = true
    try {
      if (editingId.value) { await deptApi.updateDepartment(editingId.value, form.value); ElMessage.success('更新成功') }
      else { await deptApi.createDepartment(form.value); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: Department) {
  await ElMessageBox.confirm(`确定删除部门 "${row.name}"？`, '确认删除', { type: 'warning' })
  await deptApi.deleteDepartment(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
