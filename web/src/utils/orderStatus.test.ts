import { describe, expect, it } from 'vitest'
import { canCancel, canReturn } from './orderStatus'

describe('order state actions', () => {
  it('only permits pending orders to cancel', () => {
    expect(canCancel('PENDING_PAYMENT')).toBe(true)
    expect(canCancel('PAID')).toBe(false)
    expect(canCancel('CANCELLED')).toBe(false)
  })
  it('permits returns only after payment', () => {
    expect(canReturn('PAID')).toBe(true)
    expect(canReturn('PARTIALLY_RETURNED')).toBe(true)
    expect(canReturn('PENDING_PAYMENT')).toBe(false)
  })
})

