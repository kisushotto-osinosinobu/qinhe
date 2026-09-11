export type Role = 'ADMIN' | 'INVENTORY_MANAGER' | 'CASHIER' | 'MEMBER'
export interface User { id:number; username:string; displayName:string; phone?:string; email?:string; avatarUrl?:string; role:Role }
export interface ApiEnvelope<T> { code:number; message:string; data:T }
export interface PageData<T> { items:T[]; total:number; page:number; pageSize:number }
export interface Product { id:number; code:string; barcode:string; name:string; categoryId:number; categoryName:string; specification?:string; unit:string; purchasePrice:number; salePrice:number; imageUrl?:string; status:string; lowStockThreshold:number; currentQty:number; reservedQty:number; availableQty:number }
export interface Category { id:number; name:string; code:string; sortOrder:number; status:string; productCount:number }
export interface Supplier { id:number; code:string; name:string; contactName?:string; phone?:string; email?:string; address?:string; status:string; remark?:string }
export interface SaleOrder { id:number; orderNo:string; channel:string; status:string; totalAmount:number; paidAmount:number; refundAmount:number; memberName?:string; cashierName?:string; createdAt:string; items?:SaleItem[] }
export interface SaleItem { id:number; productId:number; productName:string; specification?:string; unit:string; quantity:number; returnedQty:number; unitPrice:number; amount:number }

