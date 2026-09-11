<script setup lang="ts">
import { ref } from 'vue'
import { onPullDownRefresh,onShow } from '@dcloudio/uni-app'
import { request } from '@/api/http'
const list=ref<any[]>([]),lowOnly=ref(true)
async function load(){try{const d=await request<any>('/inventory','GET',{lowOnly:lowOnly.value,pageSize:100});list.value=d.items}catch{setTimeout(()=>uni.navigateBack(),500)}finally{uni.stopPullDownRefresh()}}
onShow(load);onPullDownRefresh(load)
</script>
<template><view class="page"><view class="row"><view><view class="title">库存预警</view><view class="subtitle" style="margin:0">普通会员访问会被服务端拒绝</view></view><switch :checked="lowOnly" color="#0b6b57" @change="lowOnly=($event.detail as any).value;load()"/></view><view v-for="row in list" :key="row.productId" class="card"><view class="row"><view><view class="product-name">{{ row.name }}</view><view class="muted">{{ row.code }} · {{ row.categoryName }}</view></view><view class="tag" :style="row.lowStock?'background:#fff0ef;color:#bd4540':''">{{ row.lowStock?'库存预警':'库存正常' }}</view></view><view class="row" style="margin-top:24rpx"><view><view class="muted">当前</view><view class="price">{{ row.currentQty }}</view></view><view><view class="muted">预占</view><view class="price">{{ row.reservedQty }}</view></view><view><view class="muted">可售</view><view class="price">{{ row.availableQty }}</view></view><view><view class="muted">阈值</view><view class="price">{{ row.lowStockThreshold }}</view></view></view></view><view v-if="!list.length" class="empty">暂无库存预警</view></view></template>
