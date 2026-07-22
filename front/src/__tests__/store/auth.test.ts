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

  it('should clear state on logout', () => {
    const auth = useAuthStore()
    sessionStorage.setItem('accessToken', 'test-token')
    auth.logout()
    expect(auth.isLoggedIn).toBe(false)
    expect(auth.accessToken).toBe('')
    expect(sessionStorage.getItem('accessToken')).toBeNull()
  })
})
