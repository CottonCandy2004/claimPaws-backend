<template>
  <div class="page-container">
    <el-card>
      <template #header>创建预约</template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px">
        <el-form-item label="预约标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" placeholder="选择资源类型" style="width: 100%" @change="onResourceTypeChange">
            <el-option label="会议室" value="MEETING_ROOM" /><el-option label="工位" value="WORKSTATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择资源" prop="resourceCascade">
          <el-cascader v-model="form.resourceCascade" :options="campusTree" :key="form.resourceType"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: false, lazy: true, lazyLoad }"
            placeholder="选择园区 > 楼宇 > 楼层 > 资源" style="width: 100%" clearable />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <div style="display:flex;gap:8px">
            <el-date-picker v-model="form.startDate" type="date" placeholder="日期" style="width: 100%" value-format="YYYY-MM-DD" />
            <el-time-select v-model="form.startTime" placeholder="时间" start="00:00" end="23:30" step="00:30" style="width: 160px" />
          </div>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <div style="display:flex;gap:8px">
            <el-date-picker v-model="form.endDate" type="date" placeholder="日期" style="width: 100%" value-format="YYYY-MM-DD" />
            <el-time-select v-model="form.endTime" placeholder="时间" start="00:00" end="23:30" step="00:30" style="width: 160px" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交预约</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import * as resourceApi from '@/api/modules/resource'
import * as reservationApi from '@/api/modules/reservation'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const campusTree = ref<any[]>([])
const form = ref({ title: '', resourceType: 'MEETING_ROOM' as 'MEETING_ROOM' | 'WORKSTATION', resourceCascade: [] as number[], startDate: '', startTime: '00:00', endDate: '', endTime: '00:30' })
const rules: FormRules = {
  title: [{ required: true, message: '请输入预约标题', trigger: 'blur' }],
  resourceType: [{ required: true, message: '请选择资源类型', trigger: 'change' }],
  resourceCascade: [{ required: true, message: '请选择资源', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const lazyLoad = async (node: any, resolve: any) => {
  if (node.level === 0) {
    const campuses = await resourceApi.getAllCampuses()
    resolve(campuses.map((c: any) => ({ ...c, leaf: false })))
  } else if (node.level === 1) {
    const blds = await resourceApi.getBuildingsByCampus(node.value)
    if (blds.length === 0) {
      // No buildings, try loading resources directly
      resolve([])
    } else {
      resolve(blds.map((b: any) => ({ ...b, leaf: false })))
    }
  } else if (node.level === 2) {
    const fls = await resourceApi.getFloorsByBuilding(node.value)
    if (fls.length === 0) {
      resolve([])
    } else {
      resolve(fls.map((f: any) => ({ ...f, leaf: false })))
    }
  } else if (node.level === 3) {
    const type = form.value.resourceType === 'MEETING_ROOM' ? 'ROOM' : 'WORKSTATION'
    const res = type === 'ROOM'
      ? await resourceApi.getRoomList({ page: 1, size: 1000, floorId: node.value })
      : await resourceApi.getWorkstationList({ page: 1, size: 1000, floorId: node.value })
    resolve((res.records || []).map((r: any) => ({ ...r, leaf: true })))
  }
}

async function onResourceTypeChange() {
  form.value.resourceCascade = []
}

async function loadInitialCampuses() {
  const campuses = await resourceApi.getAllCampuses()
  campusTree.value = campuses.map((c: any) => ({ ...c, leaf: false }))
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      const cascade = form.value.resourceCascade
      await reservationApi.createReservation({
        resourceId: cascade[cascade.length - 1],
        title: form.value.title,
        startTime: form.value.startDate + ' ' + form.value.startTime + ':00',
        endTime: form.value.endDate + ' ' + form.value.endTime + ':00',
        attendees: []
      })
      ElMessage.success('预约创建成功'); router.push('/reservations')
    } catch { ElMessage.error('预约创建失败，可能存在时间冲突') }
    finally { submitting.value = false }
  })
}

onMounted(loadInitialCampuses)
</script>
