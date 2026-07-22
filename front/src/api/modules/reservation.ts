import { get, post, put } from '@/api/request'
import type { Reservation, PageResult, PageParams, CreateReservationRequest } from '@/types'

export function getReservationList(params: PageParams & { status?: string; keyword?: string }) { return get<PageResult<Reservation>>('/reservations', params) }
export function getReservationById(id: number) { return get<Reservation>(`/reservations/${id}`) }
export function createReservation(data: CreateReservationRequest) { return post<Reservation>('/reservations', data) }
export function cancelReservation(id: number) { return put<void>(`/reservations/${id}/cancel`) }
export function approveReservation(id: number, data: { approved: boolean; comment?: string }) { return put<void>(`/reservations/${id}/approve`, data) }
export function checkInReservation(id: number) { return put<void>(`/reservations/${id}/check-in`) }
export function getPendingApprovals(params: PageParams) { return get<PageResult<Reservation>>('/reservations/pending-approvals', params) }
