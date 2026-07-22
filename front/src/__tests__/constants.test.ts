import { describe, it, expect } from 'vitest'
import { RESERVATION_STATUS_MAP, RESERVATION_STATUS_COLOR } from '@/utils/constants'

describe('constants', () => {
  it('should have all 7 reservation status labels', () => {
    expect(Object.keys(RESERVATION_STATUS_MAP)).toHaveLength(7)
    expect(RESERVATION_STATUS_MAP['PENDING_APPROVAL']).toBe('待审批')
    expect(RESERVATION_STATUS_MAP['NO_SHOW']).toBe('爽约')
  })

  it('should have color mapping for all statuses', () => {
    expect(RESERVATION_STATUS_COLOR['PENDING_APPROVAL']).toBe('warning')
    expect(RESERVATION_STATUS_COLOR['REJECTED']).toBe('danger')
    expect(RESERVATION_STATUS_COLOR['COMPLETED']).toBe('info')
  })
})
