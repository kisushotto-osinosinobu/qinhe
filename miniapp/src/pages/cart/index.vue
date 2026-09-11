<script setup lang="ts">
import { computed,ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCart,saveCart,type CartItem } from '@/stores/cart'
import { request,requireLogin } from '@/api/http'
const list=ref<CartItem[]>([]),submitting=ref(false),total=computed(()=>list.value.reduce((s,x)=>s+x.salePrice*x.quantity,0))
function reload(){list.value=getCart()}
function change(row:CartItem,delta:number){row.quantity=Math.min(row.availableQty,Math.max(1,row.quantity+delta));saveCart(list.value)}
function remove(index:number){list.value.splice(index,1);saveCart(list.value)}
async function submit(){if(!requireLogin()||!list.value.length)return;submitting.value=true;try{const data=await request<any>('/orders','POST',{idempotencyKey:`mini-${Date.now()}-${Math.random().toString(16).slice(2)}`,items:list.value.map(x=>({productId:x.id,quantity:x.quantity}))});list.value=[];saveCart([]);uni.showModal({title:'订单已提交',content:`订单号：${data.orderNo}\n请到店付款，本次未发生真实资金交易。`,showCancel:false,success:()=>uni.switchTab({url:'/pages/orders/index'})})}finally{submitting.value=false}}
onShow(reload)
</script>
<template><view class="page"><view class="title">购物车</view><view class="subtitle">提交时按可售库存再次校验</view><view v-if="!list.length" class="empty">购物车还是空的<view><button class="secondary" size="mini" @click="uni.switchTab({url:'/pages/products/index'})">去选商品</button></view></view><view v-for="(row,index) in list" :key="row.id" class="card product"><image v-if="row.imageUrl" class="product-image" :src="row.imageUrl"/><view v-else class="product-image" style="display:flex;align-items:center;justify-content:center;color:#0b6b57;font-size:54rpx;font-weight:800">青</view><view class="product-info"><view class="product-name">{{ row.name }}</view><view class="price">¥{{ Number(row.salePrice).toFixed(2) }}</view><view class="row" style="margin-top:16rpx"><view><button size="mini" @click="change(row,-1)">−</button><text style="padding:0 18rpx">{{ row.quantity }}</text><button size="mini" @click="change(row,1)">＋</button></view><text class="danger" style="padding:8rpx 12rpx" @click="remove(index)">删除</text></view></view></view><view v-if="list.length" class="spacer"/><view v-if="list.length" class="fixed-bottom row"><view><view class="muted">合计</view><view class="price">¥{{ total.toFixed(2) }}</view></view><button class="primary" :loading="submitting" @click="submit">提交订单</button></view></view></template>
