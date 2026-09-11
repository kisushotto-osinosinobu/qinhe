<script setup lang="ts">
import { reactive,ref } from 'vue'
import { request,saveAuth } from '@/api/http'
const form=reactive({username:'member',password:'Passw0rd!'}),loading=ref(false)
async function login(){if(!form.username||!form.password){uni.showToast({title:'请输入账号和密码',icon:'none'});return}loading.value=true;try{const data=await request<any>('/auth/login','POST',form);saveAuth(data);uni.showToast({title:'登录成功'});setTimeout(()=>uni.switchTab({url:'/pages/products/index'}),500)}finally{loading.value=false}}
</script>
<template><view class="page" style="padding-top:100rpx"><view class="title">欢迎回来</view><view class="subtitle">账号密码可与 Web 系统共用</view><view class="card"><input v-model="form.username" class="input" placeholder="用户名"/><input v-model="form.password" class="input" password placeholder="密码"/><button class="primary" :loading="loading" @click="login">登录</button><view class="notice" style="margin-top:24rpx">开发账号 member / Passw0rd!<br>当前为到店付款 / 模拟收款</view><button class="secondary" style="margin-top:20rpx" @click="uni.navigateTo({url:'/pages/auth/register'})">注册普通会员</button></view></view></template>
