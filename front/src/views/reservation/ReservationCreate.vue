<template>
  <div class="page-container">
    <el-card>
      <template #header>创建预约</template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px">
        <el-form-item label="预约标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" placeholder="选择资源类型" style="width: 100%" @change="handleResourceTypeChange">
            <el-option label="会议室" value="MEETING_ROOM" /><el-option label="工位" value="WORKSTATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择资源" prop="resourceId">
          <el-select v-model="form.resourceId" placeholder="选择资源" style="width: 100%" filterable>
            <el-option v-for="r in availableResources" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交预约</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { MeetingRoom, Workstation } from '@/types'
import * as resourceApi from '@/api/modules/resource'
import * as reservationApi from '@/api/modules/reservation'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const availableResources = ref<Array<MeetingRoom | Workstation>>([])
const form = ref({ title: '', resourceType: 'MEETING_ROOM' as 'MEETING_ROOM' | 'WORKSTATION', resourceId: undefined as number | undefined, startTime: '', endTime: '', description: '' })
const rules: FormRules = {
  title: [{ required: true, message: '请输入预约标题', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }],
  resourceId: [{ required: true, message: '请选择资源', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

async function handleResourceTypeChange() {
  form.value.resourceId = undefined
  const res = form.value.resourceType === 'MEETING_ROOM'
    ? await resourceApi.getRoomList({ page: 1, size: 1000 })
    : await resourceApi.getWorkstationList({ page: 1, size: 1000 })
  availableResources.value = res.records
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      await reservationApi.createReservation({ resourceId: form.value.resourceId!, title: form.value.title, description: form.value.description, startTime: form.value.startTime, endTime: form.value.endTime, attendees: [] })
      ElMessage.success('预约创建成功'); router.push('/reservations')
    } catch { ElMessage.error('预约创建失败，可能存在时间冲突') }
    finally { submitting.value = false }
  })
}
</script>
