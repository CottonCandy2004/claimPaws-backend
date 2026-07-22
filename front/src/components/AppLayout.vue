<template>
  <el-container class="app-layout">
    <el-aside :width="app.sidebarCollapsed ? '64px' : '220px'" class="app-sidebar">
      <div class="app-logo" @click="$router.push('/dashboard')">
        <span v-if="!app.sidebarCollapsed">claimPaws</span>
        <span v-else>CP</span>
      </div>
      <el-menu :default-active="activeMenu" :collapse="app.sidebarCollapsed" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><span>仪表盘</span></el-menu-item>

        <el-sub-menu index="/resources">
          <template #title><el-icon><OfficeBuilding /></el-icon><span>资源管理</span></template>
          <el-menu-item index="/resources/campus" v-if="auth.hasPermission('campus:read')">园区管理</el-menu-item>
          <el-menu-item index="/resources/buildings" v-if="auth.hasPermission('building:read')">楼宇管理</el-menu-item>
          <el-menu-item index="/resources/floors" v-if="auth.hasPermission('floor:read')">楼层管理</el-menu-item>
          <el-menu-item index="/resources/rooms" v-if="auth.hasPermission('room:read')">会议室管理</el-menu-item>
          <el-menu-item index="/resources/workstations" v-if="auth.hasPermission('workstation:read')">工位管理</el-menu-item>
          <el-menu-item index="/resources/facilities" v-if="auth.hasPermission('facility:read')">设施管理</el-menu-item>
          <el-menu-item index="/resources/policies" v-if="auth.hasPermission('policy:read')">预约策略</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/reservations"><el-icon><Calendar /></el-icon><span>我的预约</span></el-menu-item>
        <el-menu-item index="/approvals"><el-icon><Checked /></el-icon><span>待审批</span></el-menu-item>

        <el-sub-menu index="/notifications" v-if="auth.hasPermission('webhook:read')">
          <template #title><el-icon><Bell /></el-icon><span>通知管理</span></template>
          <el-menu-item index="/notifications">Webhook 配置</el-menu-item>
          <el-menu-item index="/notifications/audit">投递审计</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/system">
          <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/system/users" v-if="auth.hasPermission('user:read')">用户管理</el-menu-item>
          <el-menu-item index="/system/roles" v-if="auth.hasPermission('role:read')">角色管理</el-menu-item>
          <el-menu-item index="/system/departments" v-if="auth.hasPermission('department:read')">部门管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="app.toggleSidebar()"><Fold v-if="!app.sidebarCollapsed" /><Expand v-else /></el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in app.breadcrumbs" :key="item.title" :to="item.path ? { path: item.path } : undefined">{{ item.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">{{ auth.userInfo?.displayName || auth.userInfo?.username }}<el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu><el-dropdown-item command="logout">退出登录</el-dropdown-item></el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="app-main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Odometer, OfficeBuilding, Calendar, Checked, Bell, Setting, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/auth'
import { useAppStore } from '@/store/app'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const app = useAppStore()
const activeMenu = computed(() => route.path)

function handleCommand(cmd: string) {
  if (cmd === 'logout') { auth.logout(); router.push('/login') }
}
</script>

<style scoped lang="scss">
.app-layout { height: 100vh; }
.app-sidebar {
  background-color: #304156; overflow-y: auto; overflow-x: hidden; transition: width 0.3s;
  .app-logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 20px; font-weight: bold; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,0.1); }
  .el-menu { border-right: none; }
}
.app-header {
  display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #e6e6e6; padding: 0 20px; height: 60px;
  .header-left { display: flex; align-items: center; gap: 12px; .collapse-btn { font-size: 20px; cursor: pointer; &:hover { color: #409EFF; } } }
  .header-right .user-info { cursor: pointer; display: flex; align-items: center; gap: 4px; }
}
.app-main { background: #f0f2f5; min-height: calc(100vh - 60px); padding: 20px; }
</style>
