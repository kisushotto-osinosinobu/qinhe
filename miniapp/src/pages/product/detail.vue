<script setup lang="ts">
import { ref } from 'vue'
import { onLoad,onPullDownRefresh } from '@dcloudio/uni-app'
import { assetUrl,request,token,user } from '@/api/http'
import { addCart } from '@/stores/cart'
const product=ref<any>(),id=ref(''),canShop=ref(true)
async function load(){canShop.value=!token()||user()?.role==='MEMBER';const item:any=await request(`/catalog/public/products/${id.value}`);product.value={...item,imageUrl:assetUrl(item.imageUrl)};uni.stopPullDownRefresh()}
onLoad((q)=>{id.value=String(q?.id||'');load()});onPullDownRefresh(load)
</script>
<template><view v-if="product" class="page"><image v-if="product.imageUrl" :src="product.imageUrl" mode="aspectFill" style="width:100%;height:600rpx;border-radius:28rpx;background:#e7efec"/><view v-else style="width:100%;height:600rpx;border-radius:28rpx;background:#e7efec;display:flex;align-items:center;justify-content:center;color:#0b6b57;font-size:140rpx;font-weight:800">青</view><view class="card" style="margin-top:24rpx"><view class="row"><view class="title" style="margin:0">{{ product.name }}</view><view class="tag">{{ product.categoryName }}</view></view><view class="subtitle">{{ product.code }} · {{ product.specification||'标准规格' }}</view><view class="price">¥{{ Number(product.salePrice).toFixed(2) }} / {{ product.unit }}</view><view class="muted" style="margin-top:18rpx">当前 {{ product.currentQty }} · 已预占 {{ product.reservedQty }} · 可售 {{ product.availableQty }}</view></view><view class="notice">{{ canShop?'提交订单后会预占库存；到店模拟付款后才从当前库存扣减。':'员工账号仅用于业务查询，不能在小程序购买。' }}</view><view v-if="canShop" class="spacer"/><view v-if="canShop" class="fixed-bottom"><button class="primary" :disabled="product.availableQty<1" @click="addCart(product)">{{ product.availableQty>0?'加入购物车':'暂时缺货' }}</button></view></view></template>
