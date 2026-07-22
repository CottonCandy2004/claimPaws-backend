<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6"><el-card shadow="hover"><template #header>今天预约</template><div class="card-value">{{ stats.todayReservations }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><template #header>待审批</template><div class="card-value warning">{{ stats.pendingApprovals }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><template #header>会议室总数</template><div class="card-value">{{ stats.totalRooms }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><template #header>工位总数</template><div class="card-value">{{ stats.totalWorkstations }}</div></el-card></el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>快捷操作</template>
          <el-space wrap>
            <el-button type="primary" @click="$router.push('/reservations/create')">创建预约</el-button>
            <el-button @click="$router.push('/reservations')">我的预约</el-button>
            <el-button @click="$router.push('/approvals')">待审批</el-button>
          </el-space>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>欢迎</template>
          <p>你好，{{ auth.userInfo?.displayName || auth.userInfo?.username }}！</p>
          <p>当前角色：{{ auth.userInfo?.roles?.join('、') || '无' }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useAuthStore } from '@/store/auth'

const auth = useAuthStore()
const stats = reactive({ todayReservations: 0, pendingApprovals: 0, totalRooms: 0, totalWorkstations: 0 })
</script>

<style scoped lang="scss">
.dashboard .card-value { font-size: 32px; font-weight: bold; color: #409EFF; &.warning { color: #E6A23C; } }
</style>
