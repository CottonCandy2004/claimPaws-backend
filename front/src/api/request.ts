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
