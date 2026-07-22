import { get, post, put, del } from '@/api/request'
import type { Role, PageResult, PageParams } from '@/types'

export function getRoleList(params: PageParams & { keyword?: string }) { return get<PageResult<Role>>('/roles', params) }
export function getRoleById(id: number) { return get<Role>(`/roles/${id}`) }
export function createRole(data: Partial<Role>) { return post<Role>('/roles', data) }
export function updateRole(id: number, data: Partial<Role>) { return put<Role>(`/roles/${id}`, data) }
export function deleteRole(id: number) { return del<void>(`/roles/${id}`) }
