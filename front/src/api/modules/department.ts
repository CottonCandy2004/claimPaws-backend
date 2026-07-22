import { get, post, put, del } from '@/api/request'
import type { Department } from '@/types'

export function getDepartmentTree() { return get<Department[]>('/departments/tree') }
export function getDepartmentById(id: number) { return get<Department>(`/departments/${id}`) }
export function createDepartment(data: Partial<Department>) { return post<Department>('/departments', data) }
export function updateDepartment(id: number, data: Partial<Department>) { return put<Department>(`/departments/${id}`, data) }
export function deleteDepartment(id: number) { return del<void>(`/departments/${id}`) }
