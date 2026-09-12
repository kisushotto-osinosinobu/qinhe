<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { clearAuth,request,token } from '@/api/http'
const profile=ref<any>()
async function load(){if(!token()){profile.value=null;return}try{profile.value=await request('/auth/profile');uni.setStorageSync('user',profile.value)}catch{profile.value=null}}
async function logout(){try{await request('/auth/logout','POST')}finally{clearAuth();profile.value=null;uni.showToast({title:'已退出'})}}
const staff=()=>profile.value&&profile.value.role!=='MEMBER'
onShow(load)
</script>
<template><view class="page"><view class="title">个人中心</view><view class="subtitle">同一账号可登录 Web 与小程序</view><view v-if="profile" class="card"><view class="row"><view><view class="product-name">{{ profile.displayName }}</view><view class="muted">@{{ profile.username }}</view></view><view class="tag">{{ profile.role }}</view></view><view class="muted" style="margin-top:24rpx">{{ profile.phone||'未填写手机号' }} · {{ profile.email||'未填写邮箱' }}</view></view><view v-if="profile" class="card"><view class="row" style="padding:12rpx 0" @click="uni.navigateTo({url:'/pages/profile/edit'})"><view><view class="product-name">编辑个人资料</view><view class="muted">姓名、手机、邮箱与 Web 共用</view></view><text>›</text></view><view class="row" style="padding:28rpx 0 12rpx;border-top:1px solid #e2e9e7" @click="uni.navigateTo({url:'/pages/profile/password'})"><view><view class="product-name">修改登录密码</view><view class="muted">成功后全部设备重新登录</view></view><text>›</text></view></view><view v-if="staff()" class="card row" @click="uni.navigateTo({url:'/pages/staff/inventory'})"><view><view class="product-name">员工库存与预警</view><view class="muted">仅员工角色可访问，服务端再次鉴权</view></view><text>›</text></view><view class="card"><button v-if="!profile" class="primary" @click="uni.navigateTo({url:'/pages/auth/login'})">登录</button><button v-else class="danger" @click="logout">退出登录</button></view><view class="notice">微信身份登录未启用。当前使用账号密码联调；若接入微信登录，必须配置真实 AppID、AppSecret 和 code2Session 服务端流程。</view></view></template>
