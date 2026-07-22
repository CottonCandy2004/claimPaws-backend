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
