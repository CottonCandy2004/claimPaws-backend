import { get, post, put, del } from '@/api/request'
import type { User, PageResult, PageParams } from '@/types'

export function getUserList(params: PageParams & { keyword?: string; departmentId?: number }) { return get<PageResult<User>>('/users', params) }
export function getUserById(id: number) { return get<User>(`/users/${id}`) }
export function createUser(data: Partial<User> & { password: string }) { return post<User>('/users', data) }
export function updateUser(id: number, data: Partial<User>) { return put<User>(`/users/${id}`, data) }
export function deleteUser(id: number) { return del<void>(`/users/${id}`) }
export function assignRoles(userId: number, roleIds: number[]) { return post<void>(`/users/${userId}/roles`, { roleIds }) }
