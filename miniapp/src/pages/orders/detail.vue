<script setup lang="ts">
import { ref } from 'vue'
import { onLoad,onPullDownRefresh } from '@dcloudio/uni-app'
import { request } from '@/api/http'
const order=ref<({items:any[]}&Record<string,any>)|null>(null),id=ref(''),labels:any={PENDING_PAYMENT:'待付款',PAID:'已付款',CANCELLED:'已取消',PARTIALLY_RETURNED:'部分退货',RETURNED:'已退货'}
async function load(){order.value=await request(`/orders/${id.value}`);uni.stopPullDownRefresh()}
async function cancel(){const r=await uni.showModal({title:'取消订单',content:'确认取消？已预占库存将立即释放。'});if(!r.confirm)return;await request(`/orders/${id.value}/cancel`,'POST');uni.showToast({title:'已取消'});load()}
onLoad(q=>{id.value=String(q?.id||'');load()});onPullDownRefresh(load)
</script>
<template><view v-if="order" class="page"><view class="card"><view class="row"><view><view class="muted">订单状态</view><view class="title" style="margin-top:10rpx">{{ labels[order.status] }}</view></view><view class="tag">{{ order.channel==='MINIAPP'?'小程序订单':'门店订单' }}</view></view><view class="muted">{{ order.orderNo }}</view></view><view v-for="item in order.items" :key="item.id" class="card row"><view><view class="product-name">{{ item.productName }}</view><view class="muted">{{ item.specification }} · ¥{{ item.unitPrice }} × {{ item.quantity }}</view><view v-if="item.returnedQty" class="muted">已退 {{ item.returnedQty }}</view></view><view>¥{{ Number(item.amount).toFixed(2) }}</view></view><view class="card"><view class="row"><text>订单金额</text><text class="price">¥{{ Number(order.totalAmount).toFixed(2) }}</text></view><view class="row muted" style="margin-top:14rpx"><text>退款金额</text><text>¥{{ Number(order.refundAmount).toFixed(2) }}</text></view></view><view class="notice">到店付款 / 模拟收款，不接入真实资金交易。</view><button v-if="order.status==='PENDING_PAYMENT'" class="danger" style="margin-top:24rpx" @click="cancel">取消订单</button></view></template>
