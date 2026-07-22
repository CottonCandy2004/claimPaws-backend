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
