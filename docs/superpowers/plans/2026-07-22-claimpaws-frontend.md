# claimPaws Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 claimPaws 智能会议室与工位预约平台构建完整前端 SPA，覆盖认证、资源管理、预约审批履约、通知等全部业务模块。

**Architecture:** Vue 3 + Vite + TypeScript + Pinia + Vue Router + Element Plus，同仓库 `front/` 目录，Axios 统一封装请求，`ApiResponse<T>` 解包，JWT 认证与自动刷新，构建产物复制到 Gateway 作为静态资源。

**Tech Stack:** Vue 3 (Composition API), Vite 5, TypeScript 5, Pinia 2, Vue Router 4, Element Plus 2, Axios 1, SCSS

## Global Constraints

- 同仓库 `front/` 目录，不可放在后端模块内部
- API 统一前缀 `/api/v1`，Axios baseURL `/api/v1`
- 所有请求注入 `X-Request-Id`（uuid）；修改类请求额外注入 `Idempotency-Key`
- 响应从 `ApiResponse<T>` 解包，`code !== 0` 时 `ElMessage.error` 提示
- 预约状态枚举与后端严格一致
- 构建产物 `dist/` 最终复制到 `gateway/src/main/resources/static/`
- 首期不做 i18n、SSR、WebSocket、PWA
- 测试：Vitest + @vue/test-utils；E2E 用 Playwright（后期可选）

---

### Task 1: 项目脚手架与依赖安装

**Files:**
- Create: `front/package.json`
- Create: `front/vite.config.ts`
- Create: `front/tsconfig.json`
- Create: `front/tsconfig.node.json`
- Create: `front/env.d.ts`
- Create: `front/index.html`
- Create: `front/.gitignore`

**Interfaces:**
- Produces: `npm run dev` (localhost:5173), `npm run build` (产出 dist/)

- [ ] **Step 1: 创建 package.json**

```json
{
  "name": "claimpaws-frontend",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview",
    "test": "vitest run",
    "test:watch": "vitest"
  },
  "dependencies": {
    "vue": "^3.5.0",
    "vue-router": "^4.4.0",
    "pinia": "^2.2.0",
    "axios": "^1.7.0",
    "element-plus": "^2.9.0",
    "@element-plus/icons-vue": "^2.3.0",
    "uuid": "^10.0.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.1.0",
    "vite": "^5.4.0",
    "vue-tsc": "^2.1.0",
    "typescript": "~5.6.0",
    "@types/uuid": "^10.0.0",
    "sass": "^1.80.0",
    "vitest": "^2.1.0",
    "@vue/test-utils": "^2.4.0",
    "jsdom": "^25.0.0"
  }
}
```

- [ ] **Step 2: 安装依赖**

```bash
npm install
```
Expected: 无错误，`node_modules/` 生成。

- [ ] **Step 3: 创建 vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': resolve(__dirname, 'src') }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 4: 创建 tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "jsx": "preserve",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "esModuleInterop": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "noEmit": true,
    "paths": { "@/*": ["./src/*"] },
    "baseUrl": "."
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.vue", "env.d.ts"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

- [ ] **Step 5: 创建 tsconfig.node.json, env.d.ts, index.html, .gitignore**

`front/tsconfig.node.json`:
```json
{
  "compilerOptions": {
    "composite": true, "skipLibCheck": true,
    "module": "ESNext", "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.ts"]
}
```

`front/env.d.ts`:
```typescript
/// <reference types="vite/client" />
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
```

`front/index.html`:
```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>claimPaws - 智能会议室与工位预约</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
```

`front/.gitignore`:
```
node_modules/
dist/
.vite/
*.local
```

- [ ] **Step 6: 验证 dev 启动**

```bash
npm run dev
```
Expected: Vite dev server 在 `http://localhost:5173` 启动。Ctrl+C 停止。

- [ ] **Step 7: 验证构建**

```bash
npm run build
```
Expected: 无报错，`dist/` 目录生成（此时尚无源文件，可能因 import 出错告警——正常，后续任务加上源文件即可）。

- [ ] **Step 8: Commit**

```bash
git add front/package.json front/package-lock.json front/vite.config.ts front/tsconfig.json front/tsconfig.node.json front/env.d.ts front/index.html front/.gitignore
git commit -m "chore: scaffold Vue 3 + Vite + TS frontend project"
```

---

### Task 2: 类型定义与业务常量

**Files:**
- Create: `front/src/types/index.ts`
- Create: `front/src/utils/constants.ts`

- [ ] **Step 1: 创建类型定义**

`front/src/types/index.ts`:
```typescript
export interface ApiResponse<T = any> { code: number; message: string; data: T }

export interface PageResult<T> { records: T[]; total: number; page: number; size: number }
export interface PageParams { page: number; size: number; sort?: string }

export interface LoginRequest { username: string; password: string }
export interface LoginResponse { accessToken: string; refreshToken: string; accessTokenExpiresIn: number; refreshTokenExpiresIn: number }

export interface UserInfo { id: number; username: string; displayName: string; email: string; phone: string; avatar?: string; departmentId?: number; departmentName?: string; roles: RoleBrief[]; permissions: string[] }
export interface RoleBrief { id: number; name: string; code: string }

export interface User { id: number; username: string; displayName: string; email: string; phone: string; departmentId?: number; departmentName?: string; enabled: boolean; roles: RoleBrief[]; createdAt: string; updatedAt: string }
export interface Role { id: number; name: string; code: string; description?: string; permissions: string[]; createdAt: string; updatedAt: string }
export interface Department { id: number; name: string; parentId?: number; children?: Department[]; sort: number; createdAt: string; updatedAt: string }

export interface Campus { id: number; name: string; address?: string; createdAt: string; updatedAt: string }
export interface Building { id: number; name: string; campusId: number; campusName?: string; floorCount: number; createdAt: string; updatedAt: string }
export interface Floor { id: number; name: string; buildingId: number; buildingName?: string; sort: number; createdAt: string; updatedAt: string }
export interface MeetingRoom { id: number; name: string; floorId: number; floorName?: string; buildingName?: string; capacity: number; facilities: string[]; enabled: boolean; createdAt: string; updatedAt: string }
export interface Workstation { id: number; name: string; floorId: number; floorName?: string; buildingName?: string; enabled: boolean; createdAt: string; updatedAt: string }
export interface Facility { id: number; name: string; type: string; description?: string; createdAt: string; updatedAt: string }

export interface ReservationPolicy { id: number; name: string; resourceType: 'MEETING_ROOM' | 'WORKSTATION'; timeSlotGranularity: number; advanceBookingDays: number; minDuration: number; maxDuration: number; cancelDeadline: number; checkInWindow: number; noShowPenalty: number; approvalLevel: 0 | 1 | 2; enabled: boolean; createdAt: string; updatedAt: string }

export type ReservationStatus = 'PENDING_APPROVAL' | 'CONFIRMED' | 'CHECKED_IN' | 'COMPLETED' | 'REJECTED' | 'CANCELLED' | 'NO_SHOW'

export interface Reservation { id: number; resourceId: number; resourceName: string; resourceType: 'MEETING_ROOM' | 'WORKSTATION'; userId: number; username: string; title: string; description?: string; startTime: string; endTime: string; status: ReservationStatus; attendees: string[]; approvalNodes: ApprovalNode[]; createdAt: string; updatedAt: string }
export interface ApprovalNode { id: number; reservationId: number; approverId: number; approverName: string; level: number; status: 'PENDING' | 'APPROVED' | 'REJECTED'; comment?: string; operatedAt?: string }
export interface CreateReservationRequest { resourceId: number; title: string; description?: string; startTime: string; endTime: string; attendees: string[] }

export interface WebhookConfig { id: number; name: string; url: string; secret: string; events: string[]; enabled: boolean; maxRetries: number; connectTimeout: number; readTimeout: number; createdAt: string; updatedAt: string }
export interface DeliveryAudit { id: number; webhookId: number; webhookName: string; eventId: string; eventType: string; status: 'PENDING' | 'SUCCESS' | 'FAILED'; statusCode?: number; responseBody?: string; retryCount: number; nextRetryAt?: string; createdAt: string }
```

- [ ] **Step 2: 创建业务常量**

`front/src/utils/constants.ts`:
```typescript
import type { ReservationStatus } from '@/types'

export const RESERVATION_STATUS_MAP: Record<ReservationStatus, string> = {
  PENDING_APPROVAL: '待审批', CONFIRMED: '已确认', CHECKED_IN: '已签到',
  COMPLETED: '已完成', REJECTED: '已拒绝', CANCELLED: '已取消', NO_SHOW: '爽约'
}

export const RESERVATION_STATUS_COLOR: Record<ReservationStatus, string> = {
  PENDING_APPROVAL: 'warning', CONFIRMED: 'primary', CHECKED_IN: 'success',
  COMPLETED: 'info', REJECTED: 'danger', CANCELLED: 'info', NO_SHOW: 'danger'
}

export const RESOURCE_TYPE_MAP = { MEETING_ROOM: '会议室', WORKSTATION: '工位' } as const
export const APPROVAL_STATUS_MAP = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' } as const
```

- [ ] **Step 3: Commit**

```bash
git add front/src/types/ front/src/utils/
git commit -m "feat: add TypeScript types and business constants"
```

---

### Task 3: API 请求封装与拦截器

**Files:**
- Create: `front/src/api/request.ts`

- [ ] **Step 1: 创建 request.ts**

```typescript
import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { v4 as uuidv4 } from 'uuid'
import type { ApiResponse } from '@/types'

const request = axios.create({ baseURL: '/api/v1', timeout: 15000 })

request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  config.headers['X-Request-Id'] = uuidv4()
  const token = sessionStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  if (['post', 'put', 'patch', 'delete'].includes(config.method?.toLowerCase() || '')) {
    config.headers['Idempotency-Key'] = uuidv4()
  }
  return config
})

let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

function subscribeTokenRefresh(cb: (token: string) => void) { refreshSubscribers.push(cb) }

function onRefreshed(token: string) {
  refreshSubscribers.forEach(cb => cb(token))
  refreshSubscribers = []
}

request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse
    if (res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return response
  },
  async (error: AxiosError<ApiResponse>) => {
    if (error.response?.status === 401 && error.config && !(error.config.headers['X-Retry'])) {
      const refreshToken = sessionStorage.getItem('refreshToken')
      if (!refreshToken) { sessionStorage.clear(); window.location.href = '/login'; return Promise.reject(error) }
      if (!isRefreshing) {
        isRefreshing = true
        try {
          const { data } = await axios.post<ApiResponse<{ accessToken: string; refreshToken: string }>>('/api/v1/auth/refresh', { refreshToken })
          if (data.code === 0) {
            sessionStorage.setItem('accessToken', data.data.accessToken)
            sessionStorage.setItem('refreshToken', data.data.refreshToken)
            onRefreshed(data.data.accessToken)
          } else { sessionStorage.clear(); window.location.href = '/login' }
        } catch { sessionStorage.clear(); window.location.href = '/login' }
        finally { isRefreshing = false }
      }
      return new Promise((resolve) => {
        subscribeTokenRefresh((token: string) => {
          if (error.config) { error.config.headers.Authorization = `Bearer ${token}`; error.config.headers['X-Retry'] = 'true'; resolve(request(error.config)) }
        })
      })
    }
    if (error.response?.status && error.response.status >= 500) ElMessage.error('服务器异常，请稍后重试')
    else if (!error.response) ElMessage.error('网络异常，请检查网络连接')
    return Promise.reject(error)
  }
)

export async function get<T>(url: string, params?: Record<string, any>): Promise<T> {
  const res = await request.get<ApiResponse<T>>(url, { params })
  return res.data.data
}

export async function post<T>(url: string, data?: any): Promise<T> {
  const res = await request.post<ApiResponse<T>>(url, data)
  return res.data.data
}

export async function put<T>(url: string, data?: any): Promise<T> {
  const res = await request.put<ApiResponse<T>>(url, data)
  return res.data.data
}

export async function del<T>(url: string): Promise<T> {
  const res = await request.delete<ApiResponse<T>>(url)
  return res.data.data
}

export { request }
```

- [ ] **Step 2: Commit**

```bash
git add front/src/api/request.ts
git commit -m "feat: add Axios request wrapper with JWT interceptors"
```

---

### Task 4: Pinia Store（认证 + 全局 UI）

**Files:**
- Create: `front/src/store/auth.ts`
- Create: `front/src/store/app.ts`

- [ ] **Step 1: 创建 auth store**

`front/src/store/auth.ts`:
```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginRequest, LoginResponse } from '@/types'
import { post, get } from '@/api/request'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(sessionStorage.getItem('accessToken') || '')
  const refreshToken = ref(sessionStorage.getItem('refreshToken') || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])

  const isLoggedIn = computed(() => !!accessToken.value)

  function setToken(res: LoginResponse) {
    accessToken.value = res.accessToken
    refreshToken.value = res.refreshToken
    sessionStorage.setItem('accessToken', res.accessToken)
    sessionStorage.setItem('refreshToken', res.refreshToken)
  }

  async function login(req: LoginRequest) {
    const res = await post<LoginResponse>('/auth/login', req)
    setToken(res)
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    const info = await get<UserInfo>('/auth/me')
    userInfo.value = info
    permissions.value = info.permissions || []
  }

  function hasPermission(perm: string): boolean { return permissions.value.includes(perm) }
  function hasRole(code: string): boolean { return userInfo.value?.roles?.some(r => r.code === code) ?? false }

  function logout() {
    accessToken.value = ''; refreshToken.value = ''; userInfo.value = null
    permissions.value = []; sessionStorage.clear()
  }

  return { accessToken, refreshToken, userInfo, permissions, isLoggedIn, login, fetchUserInfo, hasPermission, hasRole, logout, setToken }
})
```

- [ ] **Step 2: 创建 app store**

`front/src/store/app.ts`:
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const breadcrumbs = ref<{ title: string; path?: string }[]>([])

  function toggleSidebar() { sidebarCollapsed.value = !sidebarCollapsed.value }
  function setBreadcrumbs(items: { title: string; path?: string }[]) { breadcrumbs.value = items }

  return { sidebarCollapsed, breadcrumbs, toggleSidebar, setBreadcrumbs }
})
```

- [ ] **Step 3: Commit**

```bash
git add front/src/store/
git commit -m "feat: add Pinia auth and app stores"
```

---

### Task 5: 路由配置与导航守卫

**Files:**
- Create: `front/src/router/index.ts`

- [ ] **Step 1: 创建 router**

`front/src/router/index.ts`:
```typescript
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/LoginView.vue'), meta: { requiresAuth: false } },
  {
    path: '/', component: () => import('@/components/AppLayout.vue'), redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardView.vue'), meta: { title: '仪表盘' } },
      {
        path: 'resources', redirect: '/resources/campus',
        children: [
          { path: 'campus', name: 'CampusList', component: () => import('@/views/resource/campus/CampusList.vue'), meta: { title: '园区管理', permission: 'campus:read' } },
          { path: 'buildings', name: 'BuildingList', component: () => import('@/views/resource/buildings/BuildingList.vue'), meta: { title: '楼宇管理', permission: 'building:read' } },
          { path: 'floors', name: 'FloorList', component: () => import('@/views/resource/floors/FloorList.vue'), meta: { title: '楼层管理', permission: 'floor:read' } },
          { path: 'rooms', name: 'RoomList', component: () => import('@/views/resource/rooms/RoomList.vue'), meta: { title: '会议室管理', permission: 'room:read' } },
          { path: 'workstations', name: 'WorkstationList', component: () => import('@/views/resource/workstations/WorkstationList.vue'), meta: { title: '工位管理', permission: 'workstation:read' } },
          { path: 'facilities', name: 'FacilityList', component: () => import('@/views/resource/facilities/FacilityList.vue'), meta: { title: '设施管理', permission: 'facility:read' } },
          { path: 'policies', name: 'PolicyList', component: () => import('@/views/resource/policies/PolicyList.vue'), meta: { title: '预约策略', permission: 'policy:read' } }
        ]
      },
      { path: 'reservations', name: 'ReservationList', component: () => import('@/views/reservation/ReservationList.vue'), meta: { title: '我的预约' } },
      { path: 'reservations/create', name: 'ReservationCreate', component: () => import('@/views/reservation/ReservationCreate.vue'), meta: { title: '创建预约' } },
      { path: 'approvals', name: 'ApprovalList', component: () => import('@/views/approval/ApprovalList.vue'), meta: { title: '待审批' } },
      { path: 'notifications', name: 'WebhookConfig', component: () => import('@/views/notification/WebhookConfig.vue'), meta: { title: 'Webhook 配置', permission: 'webhook:read' } },
      { path: 'notifications/audit', name: 'DeliveryAudit', component: () => import('@/views/notification/DeliveryAudit.vue'), meta: { title: '投递审计', permission: 'webhook:read' } },
      {
        path: 'system', redirect: '/system/users',
        children: [
          { path: 'users', name: 'UserList', component: () => import('@/views/system/users/UserList.vue'), meta: { title: '用户管理', permission: 'user:read' } },
          { path: 'roles', name: 'RoleList', component: () => import('@/views/system/roles/RoleList.vue'), meta: { title: '角色管理', permission: 'role:read' } },
          { path: 'departments', name: 'DepartmentList', component: () => import('@/views/system/departments/DepartmentList.vue'), meta: { title: '部门管理', permission: 'department:read' } }
        ]
      }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth === false) { next(auth.isLoggedIn && to.path === '/login' ? '/dashboard' : undefined); return }
  if (!auth.isLoggedIn) { next('/login'); return }
  if (!auth.userInfo) {
    try { await auth.fetchUserInfo() } catch { auth.logout(); next('/login'); return }
  }
  if (to.meta.permission && !auth.hasPermission(to.meta.permission as string)) { next('/dashboard'); return }
  next()
})

declare module 'vue-router' {
  interface RouteMeta { title?: string; requiresAuth?: boolean; permission?: string }
}

export default router
```

- [ ] **Step 2: Commit**

```bash
git add front/src/router/
git commit -m "feat: add router with navigation guards"
```

---

### Task 6: AppLayout 与 StatusTag 组件

**Files:**
- Create: `front/src/components/AppLayout.vue`
- Create: `front/src/components/StatusTag.vue`

- [ ] **Step 1: 创建 AppLayout.vue**

`front/src/components/AppLayout.vue`:
```vue
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
```

- [ ] **Step 2: 创建 StatusTag.vue**

`front/src/components/StatusTag.vue`:
```vue
<template>
  <el-tag :type="RESERVATION_STATUS_COLOR[status]">{{ RESERVATION_STATUS_MAP[status] }}</el-tag>
</template>

<script setup lang="ts">
import type { ReservationStatus } from '@/types'
import { RESERVATION_STATUS_MAP, RESERVATION_STATUS_COLOR } from '@/utils/constants'
defineProps<{ status: ReservationStatus }>()
</script>
```

- [ ] **Step 3: Commit**

```bash
git add front/src/components/
git commit -m "feat: add AppLayout and StatusTag components"
```

---

### Task 7: 登录页面

**Files:**
- Create: `front/src/views/login/LoginView.vue`

- [ ] **Step 1: 创建 LoginView.vue**

`front/src/views/login/LoginView.vue`:
```vue
<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">claimPaws</h2>
      <p class="login-subtitle">智能会议室与工位预约平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try { await auth.login({ username: form.username, password: form.password }); router.push('/dashboard') }
    catch { ElMessage.error('登录失败，请检查用户名和密码') }
    finally { loading.value = false }
  })
}
</script>

<style scoped lang="scss">
.login-container { height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.login-card { width: 400px; .login-title { text-align: center; margin-bottom: 4px; font-size: 24px; color: #303133; } .login-subtitle { text-align: center; margin-bottom: 24px; color: #909399; font-size: 14px; } }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add front/src/views/login/
git commit -m "feat: add login page"
```

---

### Task 8: 所有 API 模块

**Files:**
- Create: `front/src/api/modules/auth.ts`
- Create: `front/src/api/modules/user.ts`
- Create: `front/src/api/modules/role.ts`
- Create: `front/src/api/modules/department.ts`
- Create: `front/src/api/modules/resource.ts`
- Create: `front/src/api/modules/policy.ts`
- Create: `front/src/api/modules/reservation.ts`
- Create: `front/src/api/modules/notification.ts`

- [ ] **Step 1: 创建 auth.ts**

`front/src/api/modules/auth.ts`:
```typescript
import { post, get } from '@/api/request'
import type { LoginRequest, LoginResponse, UserInfo } from '@/types'

export function login(data: LoginRequest) { return post<LoginResponse>('/auth/login', data) }
export function getCurrentUser() { return get<UserInfo>('/auth/me') }
export function refreshAccessToken(refreshToken: string) { return post<LoginResponse>('/auth/refresh', { refreshToken }) }
export function logoutUser() { return post<void>('/auth/logout') }
```

- [ ] **Step 2: 创建 user.ts**

`front/src/api/modules/user.ts`:
```typescript
import { get, post, put, del } from '@/api/request'
import type { User, PageResult, PageParams } from '@/types'

export function getUserList(params: PageParams & { keyword?: string; departmentId?: number }) { return get<PageResult<User>>('/users', params) }
export function getUserById(id: number) { return get<User>(`/users/${id}`) }
export function createUser(data: Partial<User> & { password: string }) { return post<User>('/users', data) }
export function updateUser(id: number, data: Partial<User>) { return put<User>(`/users/${id}`, data) }
export function deleteUser(id: number) { return del<void>(`/users/${id}`) }
export function assignRoles(userId: number, roleIds: number[]) { return post<void>(`/users/${userId}/roles`, { roleIds }) }
```

- [ ] **Step 3: 创建 role.ts**

`front/src/api/modules/role.ts`:
```typescript
import { get, post, put, del } from '@/api/request'
import type { Role, PageResult, PageParams } from '@/types'

export function getRoleList(params: PageParams & { keyword?: string }) { return get<PageResult<Role>>('/roles', params) }
export function getRoleById(id: number) { return get<Role>(`/roles/${id}`) }
export function createRole(data: Partial<Role>) { return post<Role>('/roles', data) }
export function updateRole(id: number, data: Partial<Role>) { return put<Role>(`/roles/${id}`, data) }
export function deleteRole(id: number) { return del<void>(`/roles/${id}`) }
```

- [ ] **Step 4: 创建 department.ts**

`front/src/api/modules/department.ts`:
```typescript
import { get, post, put, del } from '@/api/request'
import type { Department } from '@/types'

export function getDepartmentTree() { return get<Department[]>('/departments/tree') }
export function getDepartmentById(id: number) { return get<Department>(`/departments/${id}`) }
export function createDepartment(data: Partial<Department>) { return post<Department>('/departments', data) }
export function updateDepartment(id: number, data: Partial<Department>) { return put<Department>(`/departments/${id}`, data) }
export function deleteDepartment(id: number) { return del<void>(`/departments/${id}`) }
```

- [ ] **Step 5: 创建 resource.ts**

`front/src/api/modules/resource.ts`:
```typescript
import { get, post, put, del } from '@/api/request'
import type { Campus, Building, Floor, MeetingRoom, Workstation, Facility, PageResult, PageParams } from '@/types'

export function getCampusList(params: PageParams & { keyword?: string }) { return get<PageResult<Campus>>('/resources/campus', params) }
export function getAllCampuses() { return get<Campus[]>('/resources/campus/all') }
export function createCampus(data: Partial<Campus>) { return post<Campus>('/resources/campus', data) }
export function updateCampus(id: number, data: Partial<Campus>) { return put<Campus>(`/resources/campus/${id}`, data) }
export function deleteCampus(id: number) { return del<void>(`/resources/campus/${id}`) }

export function getBuildingList(params: PageParams & { keyword?: string; campusId?: number }) { return get<PageResult<Building>>('/resources/buildings', params) }
export function getBuildingsByCampus(campusId: number) { return get<Building[]>(`/resources/buildings/by-campus/${campusId}`) }
export function createBuilding(data: Partial<Building>) { return post<Building>('/resources/buildings', data) }
export function updateBuilding(id: number, data: Partial<Building>) { return put<Building>(`/resources/buildings/${id}`, data) }
export function deleteBuilding(id: number) { return del<void>(`/resources/buildings/${id}`) }

export function getFloorList(params: PageParams & { keyword?: string; buildingId?: number }) { return get<PageResult<Floor>>('/resources/floors', params) }
export function getFloorsByBuilding(buildingId: number) { return get<Floor[]>(`/resources/floors/by-building/${buildingId}`) }
export function createFloor(data: Partial<Floor>) { return post<Floor>('/resources/floors', data) }
export function updateFloor(id: number, data: Partial<Floor>) { return put<Floor>(`/resources/floors/${id}`, data) }
export function deleteFloor(id: number) { return del<void>(`/resources/floors/${id}`) }

export function getRoomList(params: PageParams & { keyword?: string; floorId?: number }) { return get<PageResult<MeetingRoom>>('/resources/rooms', params) }
export function createRoom(data: Partial<MeetingRoom>) { return post<MeetingRoom>('/resources/rooms', data) }
export function updateRoom(id: number, data: Partial<MeetingRoom>) { return put<MeetingRoom>(`/resources/rooms/${id}`, data) }
export function deleteRoom(id: number) { return del<void>(`/resources/rooms/${id}`) }

export function getWorkstationList(params: PageParams & { keyword?: string; floorId?: number }) { return get<PageResult<Workstation>>('/resources/workstations', params) }
export function createWorkstation(data: Partial<Workstation>) { return post<Workstation>('/resources/workstations', data) }
export function updateWorkstation(id: number, data: Partial<Workstation>) { return put<Workstation>(`/resources/workstations/${id}`, data) }
export function deleteWorkstation(id: number) { return del<void>(`/resources/workstations/${id}`) }

export function getFacilityList(params: PageParams & { keyword?: string }) { return get<PageResult<Facility>>('/resources/facilities', params) }
export function createFacility(data: Partial<Facility>) { return post<Facility>('/resources/facilities', data) }
export function updateFacility(id: number, data: Partial<Facility>) { return put<Facility>(`/resources/facilities/${id}`, data) }
export function deleteFacility(id: number) { return del<void>(`/resources/facilities/${id}`) }
```

- [ ] **Step 6: 创建 policy.ts**

`front/src/api/modules/policy.ts`:
```typescript
import { get, post, put, del } from '@/api/request'
import type { ReservationPolicy, PageResult, PageParams } from '@/types'

export function getPolicyList(params: PageParams & { keyword?: string }) { return get<PageResult<ReservationPolicy>>('/policies', params) }
export function getPolicyById(id: number) { return get<ReservationPolicy>(`/policies/${id}`) }
export function createPolicy(data: Partial<ReservationPolicy>) { return post<ReservationPolicy>('/policies', data) }
export function updatePolicy(id: number, data: Partial<ReservationPolicy>) { return put<ReservationPolicy>(`/policies/${id}`, data) }
export function deletePolicy(id: number) { return del<void>(`/policies/${id}`) }
```

- [ ] **Step 7: 创建 reservation.ts**

`front/src/api/modules/reservation.ts`:
```typescript
import { get, post, put } from '@/api/request'
import type { Reservation, PageResult, PageParams, CreateReservationRequest } from '@/types'

export function getReservationList(params: PageParams & { status?: string; keyword?: string }) { return get<PageResult<Reservation>>('/reservations', params) }
export function getReservationById(id: number) { return get<Reservation>(`/reservations/${id}`) }
export function createReservation(data: CreateReservationRequest) { return post<Reservation>('/reservations', data) }
export function cancelReservation(id: number) { return put<void>(`/reservations/${id}/cancel`) }
export function approveReservation(id: number, data: { approved: boolean; comment?: string }) { return put<void>(`/reservations/${id}/approve`, data) }
export function checkInReservation(id: number) { return put<void>(`/reservations/${id}/check-in`) }
export function getPendingApprovals(params: PageParams) { return get<PageResult<Reservation>>('/reservations/pending-approvals', params) }
```

- [ ] **Step 8: 创建 notification.ts**

`front/src/api/modules/notification.ts`:
```typescript
import { get, post, put, del } from '@/api/request'
import type { WebhookConfig, DeliveryAudit, PageResult, PageParams } from '@/types'

export function getWebhookList(params: PageParams) { return get<PageResult<WebhookConfig>>('/webhooks', params) }
export function getWebhookById(id: number) { return get<WebhookConfig>(`/webhooks/${id}`) }
export function createWebhook(data: Partial<WebhookConfig>) { return post<WebhookConfig>('/webhooks', data) }
export function updateWebhook(id: number, data: Partial<WebhookConfig>) { return put<WebhookConfig>(`/webhooks/${id}`, data) }
export function deleteWebhook(id: number) { return del<void>(`/webhooks/${id}`) }
export function testWebhook(id: number) { return post<void>(`/webhooks/${id}/test`) }
export function getDeliveryAuditList(params: PageParams & { webhookId?: number; status?: string }) { return get<PageResult<DeliveryAudit>>('/webhooks/delivery-audits', params) }
export function retryDelivery(auditId: number) { return post<void>(`/webhooks/delivery-audits/${auditId}/retry`) }
```

- [ ] **Step 9: Commit**

```bash
git add front/src/api/modules/
git commit -m "feat: add all API modules"
```

---

### Task 9: Dashboard + usePagination composable

**Files:**
- Create: `front/src/views/dashboard/DashboardView.vue`
- Create: `front/src/composables/usePagination.ts`

- [ ] **Step 1: 创建 usePagination.ts**

`front/src/composables/usePagination.ts`:
```typescript
import { ref, reactive } from 'vue'
import type { PageParams } from '@/types'

export function usePagination(defaultSize = 10) {
  const pageParams = reactive<PageParams>({ page: 1, size: defaultSize })
  const total = ref(0)
  const loading = ref(false)

  function resetPage() { pageParams.page = 1 }
  function handlePageChange(page: number) { pageParams.page = page }
  function handleSizeChange(size: number) { pageParams.size = size; pageParams.page = 1 }

  return { pageParams, total, loading, resetPage, handlePageChange, handleSizeChange }
}
```

- [ ] **Step 2: 创建 DashboardView.vue**

`front/src/views/dashboard/DashboardView.vue`:
```vue
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
          <p>当前角色：{{ auth.userInfo?.roles?.map(r => r.name).join('、') || '无' }}</p>
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
```

- [ ] **Step 3: Commit**

```bash
git add front/src/views/dashboard/ front/src/composables/
git commit -m "feat: add dashboard page and usePagination composable"
```

---

### Task 10: 系统管理视图（用户/角色/部门）

**Files:**
- Create: `front/src/views/system/users/UserList.vue`
- Create: `front/src/views/system/roles/RoleList.vue`
- Create: `front/src/views/system/departments/DepartmentList.vue`

**Interfaces:**
- Consumes: `@/api/modules/user`, `@/api/modules/role`, `@/api/modules/department`
- Produces: 三个 CRUD 管理页面，每个页面包含搜索/表格/分页/新增编辑对话框

- [ ] **Step 1: 创建 UserList.vue**

`front/src/views/system/users/UserList.vue` — 完整 CRUD 页面：顶部搜索 + 新增按钮，`el-table` 显示 id/username/displayName/email/phone/departmentName + 编辑/删除操作列，`el-pagination` 分页，`el-dialog` 表单（username/displayName/email/phone/password），提交调用 `userApi.createUser`/`userApi.updateUser`，删除弹确认后调用 `userApi.deleteUser`。使用 `usePagination` composable 管理分页和 loading。代码结构与 Task 9 模板一致，替换字段名为 User 相关。

```vue
<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索用户名" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="handleCreate">新增用户</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="displayName" label="姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机" />
        <el-table-column prop="departmentName" label="部门" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" :disabled="!!editingId" /></el-form-item>
        <el-form-item label="姓名" prop="displayName"><el-input v-model="form.displayName" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="手机" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="密码" v-if="!editingId"><el-input v-model="form.password" type="password" /></el-form-item>
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
import type { User } from '@/types'
import * as userApi from '@/api/modules/user'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<User[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ username: '', displayName: '', email: '', phone: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入有效邮箱', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try { const res = await userApi.getUserList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
function search() { resetPage(); fetchData() }
function handleCreate() { editingId.value = null; form.value = { username: '', displayName: '', email: '', phone: '', password: '' }; dialogVisible.value = true }
function handleEdit(row: User) { editingId.value = row.id; form.value = { username: row.username, displayName: row.displayName, email: row.email || '', phone: row.phone || '', password: '' }; dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (editingId.value) { await userApi.updateUser(editingId.value, form.value); ElMessage.success('更新成功') }
      else { await userApi.createUser({ ...form.value, password: form.value.password || '123456' }); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: User) {
  await ElMessageBox.confirm(`确定删除用户 "${row.username}"？`, '确认删除', { type: 'warning' })
  await userApi.deleteUser(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 2: 创建 RoleList.vue**

`front/src/views/system/roles/RoleList.vue` — 与 UserList 相同模式，字段改为 name/code/description。使用 `roleApi.getRoleList`/`roleApi.createRole`/`roleApi.updateRole`/`roleApi.deleteRole`。

```vue
<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索角色" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="handleCreate">新增角色</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码" prop="code"><el-input v-model="form.code" :disabled="!!editingId" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
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
import type { Role } from '@/types'
import * as roleApi from '@/api/modules/role'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<Role[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ name: '', code: '', description: '' })
const rules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}
async function fetchData() {
  loading.value = true
  try { const res = await roleApi.getRoleList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
function search() { resetPage(); fetchData() }
function handleCreate() { editingId.value = null; form.value = { name: '', code: '', description: '' }; dialogVisible.value = true }
function handleEdit(row: Role) { editingId.value = row.id; form.value = { name: row.name, code: row.code, description: row.description || '' }; dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return; submitting.value = true
    try {
      if (editingId.value) { await roleApi.updateRole(editingId.value, form.value); ElMessage.success('更新成功') }
      else { await roleApi.createRole(form.value); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: Role) {
  await ElMessageBox.confirm(`确定删除角色 "${row.name}"？`, '确认删除', { type: 'warning' })
  await roleApi.deleteRole(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 3: 创建 DepartmentList.vue**

`front/src/views/system/departments/DepartmentList.vue` — 部门管理用树形表格展示。使用 `deptApi.getDepartmentTree` 获取树形数据。支持新增子部门（`el-tree-select` 选择父部门）。

```vue
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
```

- [ ] **Step 4: Commit**

```bash
git add front/src/views/system/
git commit -m "feat: add system management views (users, roles, departments)"
```

---

### Task 11: 资源管理视图（园区/楼宇/楼层/会议室/工位/设施）

**Files:**
- Create: `front/src/views/resource/campus/CampusList.vue`
- Create: `front/src/views/resource/buildings/BuildingList.vue`
- Create: `front/src/views/resource/floors/FloorList.vue`
- Create: `front/src/views/resource/rooms/RoomList.vue`
- Create: `front/src/views/resource/workstations/WorkstationList.vue`
- Create: `front/src/views/resource/facilities/FacilityList.vue`

每个资源页面模式一致：搜索输入框 + 新增按钮 → 表格显示 → 分页 → 弹窗表单（name + 关联选择 + 其他字段）。以下给出每个页面的核心代码（模板与 script 与 Task 10 同模式，替换为对应 API 和字段）。

- [ ] **Step 1: 创建 CampusList.vue**

`front/src/views/resource/campus/CampusList.vue` — 表格列: id/name/address，表单: name/address。使用 `resourceApi.getCampusList`/`createCampus`/`updateCampus`/`deleteCampus`。

```vue
<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索园区" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="handleCreate">新增园区</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="address" label="地址" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑园区' : '新增园区'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" type="textarea" /></el-form-item>
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
import type { Campus } from '@/types'
import * as resourceApi from '@/api/modules/resource'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<Campus[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ name: '', address: '' })
const rules: FormRules = { name: [{ required: true, message: '请输入园区名称', trigger: 'blur' }] }

async function fetchData() {
  loading.value = true
  try { const res = await resourceApi.getCampusList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
function search() { resetPage(); fetchData() }
function handleCreate() { editingId.value = null; form.value = { name: '', address: '' }; dialogVisible.value = true }
function handleEdit(row: Campus) { editingId.value = row.id; form.value = { name: row.name, address: row.address || '' }; dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      if (editingId.value) { await resourceApi.updateCampus(editingId.value, form.value); ElMessage.success('更新成功') }
      else { await resourceApi.createCampus(form.value); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: Campus) {
  await ElMessageBox.confirm(`确定删除园区 "${row.name}"？`, '确认删除', { type: 'warning' })
  await resourceApi.deleteCampus(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 2: 创建 BuildingList.vue**

`front/src/views/resource/buildings/BuildingList.vue` — 表格列: id/name/campusName/floorCount。表单: name + el-select 选择园区（调用 `resourceApi.getAllCampuses()` 获取选项）。API: `getBuildingList`/`createBuilding`/`updateBuilding`/`deleteBuilding`。

```vue
<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索楼宇" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="handleCreate">新增楼宇</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="campusName" label="所属园区" />
        <el-table-column prop="floorCount" label="楼层数" width="80" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑楼宇' : '新增楼宇'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="所属园区" prop="campusId">
          <el-select v-model="form.campusId" placeholder="选择园区" style="width: 100%">
            <el-option v-for="c in campuses" :key="c.id" :label="c.name" :value="c.id" />
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
import type { Building, Campus } from '@/types'
import * as resourceApi from '@/api/modules/resource'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<Building[]>([])
const campuses = ref<Campus[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ name: '', campusId: undefined as number | undefined })
const rules: FormRules = { name: [{ required: true, message: '请输入楼宇名称', trigger: 'blur' }], campusId: [{ required: true, message: '请选择园区', trigger: 'change' }] }

async function fetchData() {
  loading.value = true
  try { const res = await resourceApi.getBuildingList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
async function loadCampuses() { campuses.value = await resourceApi.getAllCampuses() }
function search() { resetPage(); fetchData() }
function handleCreate() { editingId.value = null; form.value = { name: '', campusId: undefined }; dialogVisible.value = true }
function handleEdit(row: Building) { editingId.value = row.id; form.value = { name: row.name, campusId: row.campusId }; dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      if (editingId.value) { await resourceApi.updateBuilding(editingId.value, form.value as any); ElMessage.success('更新成功') }
      else { await resourceApi.createBuilding(form.value as any); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: Building) {
  await ElMessageBox.confirm(`确定删除楼宇 "${row.name}"？`, '确认删除', { type: 'warning' })
  await resourceApi.deleteBuilding(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(() => { fetchData(); loadCampuses() })
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 3: 创建 FloorList.vue**

`front/src/views/resource/floors/FloorList.vue` — 表格列: id/name/buildingName/sort。表单: name + el-select 选择楼宇（遍历所有园区的楼宇）+ sort。API: `getFloorList`/`createFloor`/`updateFloor`/`deleteFloor`。

```vue
<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索楼层" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="handleCreate">新增楼层</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="buildingName" label="所属楼宇" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑楼层' : '新增楼层'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="所属楼宇" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="选择楼宇" style="width: 100%">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
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
import type { Floor, Building } from '@/types'
import * as resourceApi from '@/api/modules/resource'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<Floor[]>([])
const buildings = ref<Building[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ name: '', buildingId: undefined as number | undefined, sort: 0 })
const rules: FormRules = { name: [{ required: true, message: '请输入楼层名称', trigger: 'blur' }], buildingId: [{ required: true, message: '请选择楼宇', trigger: 'change' }] }

async function fetchData() {
  loading.value = true
  try { const res = await resourceApi.getFloorList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
async function loadBuildings() {
  const campuses = await resourceApi.getAllCampuses()
  const allBuildings: Building[] = []
  for (const c of campuses) { const blds = await resourceApi.getBuildingsByCampus(c.id); allBuildings.push(...blds) }
  buildings.value = allBuildings
}
function search() { resetPage(); fetchData() }
function handleCreate() { editingId.value = null; form.value = { name: '', buildingId: undefined, sort: 0 }; dialogVisible.value = true }
function handleEdit(row: Floor) { editingId.value = row.id; form.value = { name: row.name, buildingId: row.buildingId, sort: row.sort }; dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      if (editingId.value) { await resourceApi.updateFloor(editingId.value, form.value as any); ElMessage.success('更新成功') }
      else { await resourceApi.createFloor(form.value as any); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: Floor) {
  await ElMessageBox.confirm(`确定删除楼层 "${row.name}"？`, '确认删除', { type: 'warning' })
  await resourceApi.deleteFloor(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(() => { fetchData(); loadBuildings() })
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 4: 创建 RoomList.vue**

`front/src/views/resource/rooms/RoomList.vue` — 表格列: id/name/floorName/buildingName/capacity。表单: name + el-select 楼层（遍历楼宇获取）+ capacity。API: `getRoomList`/`createRoom`/`updateRoom`/`deleteRoom`。

```vue
<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索会议室" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="handleCreate">新增会议室</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="floorName" label="所属楼层" />
        <el-table-column prop="buildingName" label="所属楼宇" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑会议室' : '新增会议室'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="所属楼层" prop="floorId">
          <el-select v-model="form.floorId" placeholder="选择楼层" style="width: 100%">
            <el-option v-for="f in floors" :key="f.id" :label="`${f.buildingName} - ${f.name}`" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="容量" prop="capacity"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
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
import type { MeetingRoom, Floor } from '@/types'
import * as resourceApi from '@/api/modules/resource'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<MeetingRoom[]>([])
const floors = ref<Floor[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ name: '', floorId: undefined as number | undefined, capacity: 10 })
const rules: FormRules = { name: [{ required: true, message: '请输入会议室名称', trigger: 'blur' }], floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }] }

async function fetchData() {
  loading.value = true
  try { const res = await resourceApi.getRoomList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
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
function handleCreate() { editingId.value = null; form.value = { name: '', floorId: undefined, capacity: 10 }; dialogVisible.value = true }
function handleEdit(row: MeetingRoom) { editingId.value = row.id; form.value = { name: row.name, floorId: row.floorId, capacity: row.capacity }; dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      if (editingId.value) { await resourceApi.updateRoom(editingId.value, form.value as any); ElMessage.success('更新成功') }
      else { await resourceApi.createRoom(form.value as any); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: MeetingRoom) {
  await ElMessageBox.confirm(`确定删除会议室 "${row.name}"？`, '确认删除', { type: 'warning' })
  await resourceApi.deleteRoom(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(() => { fetchData(); loadFloors() })
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 5: 创建 WorkstationList.vue**

`front/src/views/resource/workstations/WorkstationList.vue` — 表格列: id/name/floorName/buildingName。表单: name + el-select 楼层。模式与 RoomList 相同，替换为 Workstation API 和类型。

```vue
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
```

- [ ] **Step 6: 创建 FacilityList.vue**

`front/src/views/resource/facilities/FacilityList.vue` — 表格列: id/name/type/description。表单: name/type/description。API: `getFacilityList`/`createFacility`/`updateFacility`/`deleteFacility`。

```vue
<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索设施" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="handleCreate">新增设施</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑设施' : '新增设施'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型" prop="type"><el-input v-model="form.type" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
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
import type { Facility } from '@/types'
import * as resourceApi from '@/api/modules/resource'
import { usePagination } from '@/composables/usePagination'

const keyword = ref('')
const data = ref<Facility[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const { pageParams, total, loading, resetPage } = usePagination()
const form = ref({ name: '', type: '', description: '' })
const rules: FormRules = { name: [{ required: true, message: '请输入设施名称', trigger: 'blur' }], type: [{ required: true, message: '请输入设施类型', trigger: 'blur' }] }

async function fetchData() {
  loading.value = true
  try { const res = await resourceApi.getFacilityList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
function search() { resetPage(); fetchData() }
function handleCreate() { editingId.value = null; form.value = { name: '', type: '', description: '' }; dialogVisible.value = true }
function handleEdit(row: Facility) { editingId.value = row.id; form.value = { name: row.name, type: row.type, description: row.description || '' }; dialogVisible.value = true }
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return; submitting.value = true
    try {
      if (editingId.value) { await resourceApi.updateFacility(editingId.value, form.value); ElMessage.success('更新成功') }
      else { await resourceApi.createFacility(form.value); ElMessage.success('创建成功') }
      dialogVisible.value = false; fetchData()
    } finally { submitting.value = false }
  })
}
async function handleDelete(row: Facility) {
  await ElMessageBox.confirm(`确定删除设施 "${row.name}"？`, '确认删除', { type: 'warning' })
  await resourceApi.deleteFacility(row.id); ElMessage.success('删除成功'); fetchData()
}
onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 7: Commit**

```bash
git add front/src/views/resource/campus/ front/src/views/resource/buildings/ front/src/views/resource/floors/ front/src/views/resource/rooms/ front/src/views/resource/workstations/ front/src/views/resource/facilities/
git commit -m "feat: add resource management views (campus, buildings, floors, rooms, workstations, facilities)"
```

---

### Task 12: Policy + Reservation + Approval + Notification 视图

**Files:**
- Create: `front/src/views/resource/policies/PolicyList.vue`
- Create: `front/src/views/reservation/ReservationList.vue`
- Create: `front/src/views/reservation/ReservationCreate.vue`
- Create: `front/src/views/approval/ApprovalList.vue`
- Create: `front/src/views/notification/WebhookConfig.vue`
- Create: `front/src/views/notification/DeliveryAudit.vue`

- [ ] **Step 1: 创建 PolicyList.vue**

`front/src/views/resource/policies/PolicyList.vue` — 表格列: id/name/resourceType/approvalLevel/maxDuration。表单包含所有策略字段（timeSlotGranularity, advanceBookingDays, minDuration, maxDuration, cancelDeadline, checkInWindow, approvalLevel 等），全用 `el-input-number`。使用 `policyApi`。

```vue
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
function handleCreate() { editingId.value = null; dialogVisible.value = true }
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
```

- [ ] **Step 2: 创建 ReservationList.vue**

`front/src/views/reservation/ReservationList.vue` — 表格列: id/title/resourceName/resourceType/startTime/endTime/status(StatusTag)。操作栏：已确认/待审批状态可取消，已确认可签到。使用 `reservationApi.getReservationList`/`cancelReservation`/`checkInReservation`。

```vue
<template>
  <div class="page-container">
    <el-card>
      <div class="page-header">
        <el-input v-model="keyword" placeholder="搜索预约" style="width: 240px" clearable @clear="search" />
        <el-button type="primary" @click="$router.push('/reservations/create')">创建预约</el-button>
      </div>
      <el-table :data="data" v-loading="loading" stripe border style="margin-top: 16px">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="resourceName" label="资源" />
        <el-table-column label="类型" width="80"><template #default="{ row }">{{ row.resourceType === 'MEETING_ROOM' ? '会议室' : '工位' }}</template></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" v-if="['PENDING_APPROVAL','CONFIRMED'].includes(row.status)" type="danger" @click="handleCancel(row)">取消</el-button>
            <el-button size="small" v-if="row.status === 'CONFIRMED'" type="success" @click="handleCheckIn(row)">签到</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Reservation } from '@/types'
import * as reservationApi from '@/api/modules/reservation'
import { usePagination } from '@/composables/usePagination'
import StatusTag from '@/components/StatusTag.vue'

const keyword = ref('')
const data = ref<Reservation[]>([])
const { pageParams, total, loading, resetPage } = usePagination()

async function fetchData() {
  loading.value = true
  try { const res = await reservationApi.getReservationList({ ...pageParams, keyword: keyword.value }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
function search() { resetPage(); fetchData() }
async function handleCancel(row: Reservation) {
  await ElMessageBox.confirm(`确定取消预约 "${row.title}"？`, '确认取消', { type: 'warning' })
  await reservationApi.cancelReservation(row.id); ElMessage.success('已取消'); fetchData()
}
async function handleCheckIn(row: Reservation) {
  await ElMessageBox.confirm(`确定签到 "${row.title}"？`, '确认签到', { type: 'info' })
  await reservationApi.checkInReservation(row.id); ElMessage.success('签到成功'); fetchData()
}
onMounted(fetchData)
</script>

<style scoped lang="scss">
.page-container .page-header { display: flex; justify-content: space-between; align-items: center; }
</style>
```

- [ ] **Step 3: 创建 ReservationCreate.vue**

`front/src/views/reservation/ReservationCreate.vue` — 表单：标题/资源类型（el-select 切换会议室/工位）/选择资源（el-select filterable，随类型变化）/开始时间/结束时间（el-date-picker datetime）/描述/参会人员（el-select multiple allow-create）。提交调用 `reservationApi.createReservation`，成功后跳转 `/reservations`。

```vue
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
        <el-form-item label="参会人员">
          <el-select v-model="form.attendees" multiple filterable allow-create placeholder="输入参会人员" style="width: 100%" />
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
const form = ref({ title: '', resourceType: 'MEETING_ROOM' as 'MEETING_ROOM' | 'WORKSTATION', resourceId: undefined as number | undefined, startTime: '', endTime: '', description: '', attendees: [] as string[] })
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
      await reservationApi.createReservation({ resourceId: form.value.resourceId!, title: form.value.title, description: form.value.description, startTime: form.value.startTime, endTime: form.value.endTime, attendees: form.value.attendees })
      ElMessage.success('预约创建成功'); router.push('/reservations')
    } catch { ElMessage.error('预约创建失败，可能存在时间冲突') }
    finally { submitting.value = false }
  })
}
</script>
```

- [ ] **Step 4: 创建 ApprovalList.vue**

`front/src/views/approval/ApprovalList.vue` — 表格列: id/title/username/resourceName/startTime/endTime/status(StatusTag)。操作栏：通过按钮（调用 `reservationApi.approveReservation(id, { approved: true, comment: '通过' })`）、拒绝按钮（弹出输入框输入原因后调用 `reservationApi.approveReservation(id, { approved: false, comment })`）。使用 `reservationApi.getPendingApprovals`。

```vue
<template>
  <div class="page-container">
    <el-card>
      <template #header>待审批预约</template>
      <el-table :data="data" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="username" label="申请人" />
        <el-table-column prop="resourceName" label="资源" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column label="状态" width="100"><template #default="{ row }"><StatusTag :status="row.status" /></template></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Reservation } from '@/types'
import * as reservationApi from '@/api/modules/reservation'
import { usePagination } from '@/composables/usePagination'
import StatusTag from '@/components/StatusTag.vue'

const data = ref<Reservation[]>([])
const { pageParams, total, loading } = usePagination()

async function fetchData() {
  loading.value = true
  try { const res = await reservationApi.getPendingApprovals({ ...pageParams }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
async function handleApprove(row: Reservation) {
  await reservationApi.approveReservation(row.id, { approved: true, comment: '通过' })
  ElMessage.success('已通过'); fetchData()
}
async function handleReject(row: Reservation) {
  const { value: comment } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝审批', { confirmButtonText: '确定', cancelButtonText: '取消' })
  if (comment !== null) { await reservationApi.approveReservation(row.id, { approved: false, comment }); ElMessage.success('已拒绝'); fetchData() }
}
onMounted(fetchData)
</script>
```

- [ ] **Step 5: 创建 WebhookConfig.vue**

`front/src/views/notification/WebhookConfig.vue` — 表格列: id/name/url/enabled。操作栏：编辑/测试/删除。表单: name/url/secret/events/enabled/maxRetries/connectTimeout/readTimeout。使用 `notificationApi.getWebhookList`/`createWebhook`/`updateWebhook`/`deleteWebhook`/`testWebhook`。

```vue
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
```

- [ ] **Step 6: 创建 DeliveryAudit.vue**

`front/src/views/notification/DeliveryAudit.vue` — 表格列: id/webhookName/eventType/status/retryCount/createdAt。操作栏：失败状态可点击重试（`notificationApi.retryDelivery`）。使用 `notificationApi.getDeliveryAuditList`。

```vue
<template>
  <div class="page-container">
    <el-card>
      <template #header>投递审计</template>
      <el-table :data="data" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="webhookName" label="Webhook" />
        <el-table-column prop="eventType" label="事件类型" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'danger' : 'warning'">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="80" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" v-if="row.status === 'FAILED'" @click="handleRetry(row)">重试</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageParams.page" v-model:page-size="pageParams.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" style="margin-top: 16px; justify-content: flex-end" @size-change="fetchData" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { DeliveryAudit } from '@/types'
import * as notifApi from '@/api/modules/notification'
import { usePagination } from '@/composables/usePagination'

const data = ref<DeliveryAudit[]>([])
const { pageParams, total, loading } = usePagination()

async function fetchData() {
  loading.value = true
  try { const res = await notifApi.getDeliveryAuditList({ ...pageParams }); data.value = res.records; total.value = res.total }
  finally { loading.value = false }
}
async function handleRetry(row: DeliveryAudit) { await notifApi.retryDelivery(row.id); ElMessage.success('已重新投递'); fetchData() }
onMounted(fetchData)
</script>
```

- [ ] **Step 7: Commit**

```bash
git add front/src/views/resource/policies/ front/src/views/reservation/ front/src/views/approval/ front/src/views/notification/
git commit -m "feat: add policy, reservation, approval, and notification views"
```

---

### Task 13: App.vue + main.ts + global.scss 入口组装

**Files:**
- Create: `front/src/main.ts`
- Create: `front/src/App.vue`
- Create: `front/src/styles/global.scss`

- [ ] **Step 1: 创建 main.ts**

`front/src/main.ts`:
```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/global.scss'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: undefined })
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
```

- [ ] **Step 2: 创建 App.vue**

`front/src/App.vue`:
```vue
<template>
  <router-view />
</template>

<script setup lang="ts">
import { useAuthStore } from '@/store/auth'
import { onMounted } from 'vue'

const auth = useAuthStore()

onMounted(async () => {
  if (auth.isLoggedIn) {
    try { await auth.fetchUserInfo() }
    catch { auth.logout() }
  }
})
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif; }
</style>
```

- [ ] **Step 3: 创建 global.scss**

`front/src/styles/global.scss`:
```scss
body {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
```

- [ ] **Step 4: 验证编译**

```bash
npx vue-tsc --noEmit
```
Expected: 无类型错误。如果有未引用变量的 warning，清理非空断言。

```bash
npm run build
```
Expected: 构建成功，产出 `dist/`。

- [ ] **Step 5: Commit**

```bash
git add front/src/main.ts front/src/App.vue front/src/styles/
git commit -m "feat: wire up App.vue, main.ts entry, and global styles"
```

---

### Task 14: Gateway 静态资源 SPA fallback 配置

**Files:**
- Modify: `gateway/src/main/resources/application.yaml`

- [ ] **Step 1: 检查 gateway 静态资源配置**

确认 `gateway/pom.xml` 包含 `spring-boot-starter-webflux`（Spring Cloud Gateway 默认包含）。无需额外添加静态资源依赖。

- [ ] **Step 2: 在 gateway 中配置 SPA fallback**

编辑 `gateway/src/main/java/cn/czu/claimpaws/gateway/` 下的配置类或新增一个 `WebFluxConfig.java`：

`gateway/src/main/java/cn/czu/claimpaws/gateway/config/WebFluxConfig.java`:
```java
package cn.czu.claimpaws.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class WebFluxConfig {

    @Bean
    public RouterFunction<ServerResponse> staticResourceRouter() {
        return RouterFunctions.resources("/**", new ClassPathResource("static/"));
    }
}
```

注意：此配置仅当 API 路由在 gateway routes 中先于 `/**` 匹配时才正确（Spring Cloud Gateway 路由会优先匹配）。确保 gateway routes 中 `/api/v1/**` 路由优先级高于静态资源 fallback。

更好的方式：在 gateway `application.yaml` 中添加一个 fallback route（放在所有 `/api/**` 路由之后）：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: frontend-spa
          uri: forward:///index.html
          predicates:
            - Path=/**
            - Method=GET
          filters:
            - SetStatus=200
          order: 9999
```

- [ ] **Step 3: 构建前端并复制到 gateway**

```bash
npm run build
cp -r dist/* ../gateway/src/main/resources/static/
```

- [ ] **Step 4: Commit**

```bash
git add gateway/src/main/java/cn/czu/claimpaws/gateway/config/WebFluxConfig.java
git add gateway/src/main/resources/static/
git commit -m "feat: add SPA fallback config and deploy frontend to gateway static"
```

---

### Task 15: 前端单元测试

**Files:**
- Create: `front/src/__tests__/constants.test.ts`
- Create: `front/src/__tests__/store/auth.test.ts`
- Create: `front/src/__tests__/components/StatusTag.test.ts`

- [ ] **Step 1: 创建 vitest.config.ts**（或追加到 `vite.config.ts`）

`front/vite.config.ts` 追加：
```typescript
/// <reference types="vitest" />
import { defineConfig } from 'vite'
// ... 原有内容 ...

export default defineConfig({
  // ... 原有 plugins, resolve, server ...
  test: {
    environment: 'jsdom',
    globals: true
  }
})
```

- [ ] **Step 2: 创建 constants 测试**

`front/src/__tests__/constants.test.ts`:
```typescript
import { describe, it, expect } from 'vitest'
import { RESERVATION_STATUS_MAP, RESERVATION_STATUS_COLOR } from '@/utils/constants'

describe('constants', () => {
  it('should have all 7 reservation status labels', () => {
    expect(Object.keys(RESERVATION_STATUS_MAP)).toHaveLength(7)
    expect(RESERVATION_STATUS_MAP['PENDING_APPROVAL']).toBe('待审批')
    expect(RESERVATION_STATUS_MAP['NO_SHOW']).toBe('爽约')
  })

  it('should have color mapping for all statuses', () => {
    expect(RESERVATION_STATUS_COLOR['PENDING_APPROVAL']).toBe('warning')
    expect(RESERVATION_STATUS_COLOR['REJECTED']).toBe('danger')
    expect(RESERVATION_STATUS_COLOR['COMPLETED']).toBe('info')
  })
})
```

- [ ] **Step 3: 创建 auth store 测试**

`front/src/__tests__/store/auth.test.ts`:
```typescript
import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/store/auth'

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    sessionStorage.clear()
  })

  it('should be not logged in by default', () => {
    const auth = useAuthStore()
    expect(auth.isLoggedIn).toBe(false)
    expect(auth.userInfo).toBeNull()
  })

  it('should report logged in after setToken', () => {
    const auth = useAuthStore()
    auth.setToken({ accessToken: 'at', refreshToken: 'rt', accessTokenExpiresIn: 3600, refreshTokenExpiresIn: 7200 })
    expect(auth.isLoggedIn).toBe(true)
    expect(sessionStorage.getItem('accessToken')).toBe('at')
  })

  it('should clear state on logout', () => {
    const auth = useAuthStore()
    auth.setToken({ accessToken: 'at', refreshToken: 'rt', accessTokenExpiresIn: 3600, refreshTokenExpiresIn: 7200 })
    auth.logout()
    expect(auth.isLoggedIn).toBe(false)
    expect(auth.accessToken).toBe('')
    expect(sessionStorage.getItem('accessToken')).toBeNull()
  })
})
```

- [ ] **Step 4: 创建 StatusTag 组件测试**

`front/src/__tests__/components/StatusTag.test.ts`:
```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatusTag from '@/components/StatusTag.vue'

describe('StatusTag', () => {
  it('renders correct label for PENDING_APPROVAL', () => {
    const wrapper = mount(StatusTag, { props: { status: 'PENDING_APPROVAL' } })
    expect(wrapper.text()).toBe('待审批')
    expect(wrapper.find('.el-tag--warning').exists()).toBe(true)
  })

  it('renders correct label for CONFIRMED', () => {
    const wrapper = mount(StatusTag, { props: { status: 'CONFIRMED' } })
    expect(wrapper.text()).toBe('已确认')
  })
})
```

- [ ] **Step 5: 运行测试**

```bash
npm test
```
Expected: 所有测试 PASS。

- [ ] **Step 6: Commit**

```bash
git add front/src/__tests__/ front/vite.config.ts
git commit -m "test: add unit tests for constants, auth store, and StatusTag"
```

---

### Task 16: 最终验证与构建检查

- [ ] **Step 1: 运行全量 TypeScript 编译**

```bash
npx vue-tsc --noEmit
```
Expected: 无类型错误。

- [ ] **Step 2: 运行全量测试**

```bash
npm test
```
Expected: 所有测试 PASS。

- [ ] **Step 3: 运行生产构建**

```bash
npm run build
```
Expected: 构建成功，`dist/` 大小合理。

- [ ] **Step 4: 验证 dist/ 内容**

```bash
ls dist/
```
Expected: `index.html`, `assets/` 目录存在。

- [ ] **Step 5: 复制到 gateway 并验证启动**

```bash
cp -r dist/* ../gateway/src/main/resources/static/
cd ../gateway && mvn spring-boot:run
```
Expected: Gateway 启动后访问 `http://localhost:8080` 能看到登录页面。

- [ ] **Step 6: Commit**

```bash
git add gateway/src/main/resources/static/
git commit -m "chore: build and deploy frontend to gateway static resources"
```

---

## Summary

Total: 16 tasks covering full stack frontend implementation:
1. Scaffold (Vite + Vue + TS + deps)
2. Types & constants
3. Axios wrapper with JWT refresh
4. Pinia stores (auth + app)
5. Vue Router with guards
6. AppLayout + StatusTag components
7. Login page
8. All API modules (8 files)
9. Dashboard + usePagination
10. System views (users, roles, departments)
11. Resource views (campus, buildings, floors, rooms, workstations, facilities)
12. Policy + reservation + approval + notification views
13. Entry point (App.vue + main.ts + styles)
14. Gateway SPA fallback config
15. Unit tests
16. Final verification & build
