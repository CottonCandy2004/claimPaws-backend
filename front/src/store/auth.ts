import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginRequest } from '@/types'
import { post, get } from '@/api/request'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(sessionStorage.getItem('accessToken') || '')
  const userInfo = ref<{ id: number; username: string; displayName: string; roles: string[] } | null>(null)
  const permissions = ref<string[]>([])

  const isLoggedIn = computed(() => !!accessToken.value)

  async function login(req: LoginRequest) {
    const res = await post<{ accessToken: string }>('/auth/login', req)
    accessToken.value = res.accessToken
    sessionStorage.setItem('accessToken', res.accessToken)
  }

  async function fetchUserInfo() {
    const info = await get<{ id: number; username: string; displayName: string; roles: string[] }>('/auth/me')
    userInfo.value = info
  }

  function hasPermission(perm: string): boolean { return permissions.value.includes(perm) }
  function hasRole(code: string): boolean { return userInfo.value?.roles?.includes(code) ?? false }

  function logout() {
    accessToken.value = ''; userInfo.value = null
    permissions.value = []; sessionStorage.clear()
  }

  return { accessToken, userInfo, permissions, isLoggedIn, login, fetchUserInfo, hasPermission, hasRole, logout }
})
