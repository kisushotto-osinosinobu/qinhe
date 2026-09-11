<script setup lang="ts">
import { ref } from 'vue'
import { onPullDownRefresh,onShow } from '@dcloudio/uni-app'
import { request,requireLogin } from '@/api/http'
const list=ref<any[]>([]),labels:any={PENDING_PAYMENT:'待付款',PAID:'已付款',CANCELLED:'已取消',PARTIALLY_RETURNED:'部分退货',RETURNED:'已退货'}
async function load(){if(!requireLogin())return;try{const d=await request<any>('/orders','GET',{pageSize:50});list.value=d.items}finally{uni.stopPullDownRefresh()}}
onShow(load);onPullDownRefresh(load)
</script>
<template><view class="page"><view class="title">我的订单</view><view class="subtitle">下拉刷新可获取后台最新状态</view><view v-if="!list.length" class="empty">暂无订单</view><view v-for="o in list" :key="o.id" class="card" @click="uni.navigateTo({url:`/pages/orders/detail?id=${o.id}`})"><view class="row"><view class="product-name">{{ o.orderNo }}</view><view class="tag">{{ labels[o.status]||o.status }}</view></view><view class="row" style="margin-top:20rpx"><view class="muted">{{ o.createdAt }}</view><view class="price">¥{{ Number(o.totalAmount).toFixed(2) }}</view></view></view></view></template>

