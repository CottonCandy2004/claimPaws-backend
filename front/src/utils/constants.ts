import type { ReservationStatus } from '@/types'

export const RESERVATION_STATUS_MAP: Record<ReservationStatus, string> = {
  PENDING_APPROVAL: '待审批', CONFIRMED: '已确认', CHECKED_IN: '已签到',
  COMPLETED: '已完成', REJECTED: '已拒绝', CANCELLED: '已取消', NO_SHOW: '爽约'
}

export const RESERVATION_STATUS_COLOR: Record<ReservationStatus, string> = {
  PENDING_APPROVAL: 'warning', CONFIRMED: 'primary', CHECKED_IN: 'success',
  COMPLETED: 'info', REJECTED: 'danger', CANCELLED: 'info', NO_SHOW: 'danger'
}

export const RESOURCE_TYPE_MAP = { MEETING_ROOM: '会议室', WORKSTATION: '工位' } as const
export const APPROVAL_STATUS_MAP = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' } as const
