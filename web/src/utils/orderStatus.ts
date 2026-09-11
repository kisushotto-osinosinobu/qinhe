export const orderStatus: Record<string, { label:string; type:'info'|'warning'|'success'|'danger' }> = {
  PENDING_PAYMENT: { label:'待付款', type:'warning' },
  PAID: { label:'已付款', type:'success' },
  CANCELLED: { label:'已取消', type:'info' },
  PARTIALLY_RETURNED: { label:'部分退货', type:'warning' },
  RETURNED: { label:'已退货', type:'danger' },
}
export function canCancel(status:string){ return status === 'PENDING_PAYMENT' }
export function canReturn(status:string){ return status === 'PAID' || status === 'PARTIALLY_RETURNED' }

