export interface ApiResponse<T = any> { code: number; message: string; data: T }

export interface PageResult<T> { records: T[]; total: number; page: number; size: number }
export interface PageParams { page: number; size: number; sort?: string }

export interface LoginRequest { username: string; password: string }
export interface LoginResponse { accessToken: string; refreshToken: string; accessTokenExpiresIn: number; refreshTokenExpiresIn: number }

export interface UserInfo { id: number; username: string; displayName: string; email: string; phone: string; avatar?: string; departmentId?: number; departmentName?: string; roles: RoleBrief[]; permissions: string[] }
export interface RoleBrief { id: number; name: string; code: string }

export interface User { id: number; username: string; displayName: string; email: string; phone: string; departmentId?: number; departmentName?: string; enabled: boolean; roles: RoleBrief[]; createdAt: string; updatedAt: string }
export interface Role { id: number; name: string; code: string; description?: string; permissions: string[]; createdAt: string; updatedAt: string }
export interface Department { id: number; name: string; parentId?: number; children?: Department[]; sort: number; createdAt: string; updatedAt: string }

export interface Campus { id: number; name: string; address?: string; createdAt: string; updatedAt: string }
export interface Building { id: number; name: string; campusId: number; campusName?: string; floorCount: number; createdAt: string; updatedAt: string }
export interface Floor { id: number; name: string; buildingId: number; buildingName?: string; sort: number; createdAt: string; updatedAt: string }
export interface MeetingRoom { id: number; name: string; floorId: number; floorName?: string; buildingName?: string; capacity: number; facilities: string[]; enabled: boolean; createdAt: string; updatedAt: string }
export interface Workstation { id: number; name: string; floorId: number; floorName?: string; buildingName?: string; enabled: boolean; createdAt: string; updatedAt: string }
export interface Facility { id: number; name: string; type: string; description?: string; createdAt: string; updatedAt: string }

export interface ReservationPolicy { id: number; name: string; resourceType: 'MEETING_ROOM' | 'WORKSTATION'; timeSlotGranularity: number; advanceBookingDays: number; minDuration: number; maxDuration: number; cancelDeadline: number; checkInWindow: number; noShowPenalty: number; approvalLevel: 0 | 1 | 2; enabled: boolean; createdAt: string; updatedAt: string }

export type ReservationStatus = 'PENDING_APPROVAL' | 'CONFIRMED' | 'CHECKED_IN' | 'COMPLETED' | 'REJECTED' | 'CANCELLED' | 'NO_SHOW'

export interface Reservation { id: number; resourceId: number; resourceName: string; resourceType: 'MEETING_ROOM' | 'WORKSTATION'; userId: number; username: string; title: string; description?: string; startTime: string; endTime: string; status: ReservationStatus; attendees: string[]; approvalNodes: ApprovalNode[]; createdAt: string; updatedAt: string }
export interface ApprovalNode { id: number; reservationId: number; approverId: number; approverName: string; level: number; status: 'PENDING' | 'APPROVED' | 'REJECTED'; comment?: string; operatedAt?: string }
export interface CreateReservationRequest { resourceId: number; title: string; description?: string; startTime: string; endTime: string; attendees: string[] }

export interface WebhookConfig { id: number; name: string; url: string; secret: string; events: string[]; enabled: boolean; maxRetries: number; connectTimeout: number; readTimeout: number; createdAt: string; updatedAt: string }
export interface DeliveryAudit { id: number; webhookId: number; webhookName: string; eventId: string; eventType: string; status: 'PENDING' | 'SUCCESS' | 'FAILED'; statusCode?: number; responseBody?: string; retryCount: number; nextRetryAt?: string; createdAt: string }
