import { post, get } from '@/api/request'
import type { LoginRequest, LoginResponse, UserInfo } from '@/types'

export function login(data: LoginRequest) { return post<LoginResponse>('/auth/login', data) }
export function getCurrentUser() { return get<UserInfo>('/auth/me') }
export function refreshAccessToken(refreshToken: string) { return post<LoginResponse>('/auth/refresh', { refreshToken }) }
export function logoutUser() { return post<void>('/auth/logout') }
