import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatusTag from '@/components/StatusTag.vue'

describe('StatusTag', () => {
  it('renders correct label for PENDING_APPROVAL', () => {
    const wrapper = mount(StatusTag, { props: { status: 'PENDING_APPROVAL' } })
    expect(wrapper.text()).toBe('待审批')
    expect(wrapper.find('.el-tag--warning').exists()).toBe(true)
  })

  it('renders correct label for CONFIRMED', () => {
    const wrapper = mount(StatusTag, { props: { status: 'CONFIRMED' } })
    expect(wrapper.text()).toBe('已确认')
  })
})
