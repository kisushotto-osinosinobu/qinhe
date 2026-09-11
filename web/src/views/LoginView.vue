<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth=useAuthStore(),router=useRouter(),route=useRoute(),loading=ref(false)
const form=reactive({username:'admin',password:'Passw0rd!'})
async function submit(){loading.value=true;try{await auth.login(form.username,form.password);await router.push(String(route.query.redirect||'/dashboard'))}finally{loading.value=false}}
</script>
<template>
  <div class="login-page">
    <section class="login-story">
      <div class="brand"><div class="brand-mark">青</div><div><strong>青禾零售</strong><span>SUPERMARKET OPS</span></div></div>
      <h1>每一笔进销存，<br>都有迹可循。</h1>
      <p>统一管理商品、采购、库存、收银和会员订单。Web 与微信小程序共享同一套业务数据，让门店操作清楚、可靠、可追溯。</p>
      <div class="login-features"><span>库存实时预占</span><span>关键操作幂等</span><span>全链路审计</span></div>
    </section>
    <section class="login-card-wrap">
      <el-form class="login-card" label-position="top" @submit.prevent="submit">
        <h2>登录运营台</h2><p>请输入账号密码继续</p>
        <el-form-item label="账号"><el-input v-model="form.username" size="large" autocomplete="username" placeholder="用户名" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" size="large" type="password" autocomplete="current-password" show-password placeholder="至少 8 位" @keyup.enter="submit" /></el-form-item>
        <el-button type="primary" size="large" native-type="submit" :loading="loading" style="width:100%">登录</el-button>
        <div class="notice" style="margin-top:18px">开发演示账号：admin / Passw0rd!。生产环境不会加载此账号。</div>
        <p style="text-align:center;margin-top:22px">还没有会员账号？<router-link to="/register">立即注册</router-link></p>
      </el-form>
    </section>
  </div>
</template>
