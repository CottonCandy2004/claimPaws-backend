import { get, post, put, del } from '@/api/request'
import type { ReservationPolicy, PageResult, PageParams } from '@/types'

export function getPolicyList(params: PageParams & { keyword?: string }) { return get<PageResult<ReservationPolicy>>('/policies', params) }
export function getPolicyById(id: number) { return get<ReservationPolicy>(`/policies/${id}`) }
export function createPolicy(data: Partial<ReservationPolicy>) { return post<ReservationPolicy>('/policies', data) }
export function updatePolicy(id: number, data: Partial<ReservationPolicy>) { return put<ReservationPolicy>(`/policies/${id}`, data) }
export function deletePolicy(id: number) { return del<void>(`/policies/${id}`) }
