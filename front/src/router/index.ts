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
