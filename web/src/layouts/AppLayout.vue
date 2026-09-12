<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route=useRoute(),router=useRouter(),auth=useAuthStore(),collapsed=ref(false)
const menu=computed(()=>[
  {path:'/dashboard',label:'经营概览',icon:'DataAnalysis'},
  ...(auth.can('ADMIN','INVENTORY_MANAGER')?[{path:'/catalog',label:'商品与分类',icon:'Goods'},{path:'/suppliers',label:'供应商',icon:'OfficeBuilding'},{path:'/purchases',label:'采购入库',icon:'Van'}]:[]),
  ...(auth.isStaff?[{path:'/inventory',label:'库存中心',icon:'Box'}]:[]),
  ...(auth.can('ADMIN','CASHIER')?[{path:'/pos',label:'收银台',icon:'ShoppingCartFull'}]:[]),
  ...(auth.can('ADMIN','CASHIER','MEMBER')?[{path:'/orders',label:auth.user?.role==='MEMBER'?'我的订单':'销售订单',icon:'Tickets'}]:[]),
  ...(auth.can('ADMIN')?[{path:'/system',label:'系统管理',icon:'Setting'}]:[]),
])
async function logout(){await auth.logout();router.push('/login')}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar" :class="{collapsed}">
      <div class="brand"><div class="brand-mark">青</div><div v-if="!collapsed"><strong>青禾零售</strong><span>SUPERMARKET OPS</span></div></div>
      <nav>
        <button v-for="item in menu" :key="item.path" :class="{active:route.path===item.path}" @click="router.push(item.path)">
          <el-icon><component :is="item.icon" /></el-icon><span v-if="!collapsed">{{ item.label }}</span>
        </button>
      </nav>
      <button class="collapse-button" @click="collapsed=!collapsed"><el-icon><Fold v-if="!collapsed"/><Expand v-else/></el-icon><span v-if="!collapsed">收起菜单</span></button>
    </aside>
    <main class="main-area">
      <header class="topbar">
        <div><p class="eyebrow">青禾生活超市</p><h1>{{ menu.find(m=>m.path===route.path)?.label || '个人中心' }}</h1></div>
        <div class="user-block"><span class="role-chip">{{ {'ADMIN':'管理员','INVENTORY_MANAGER':'库存管理员','CASHIER':'收银员','MEMBER':'普通会员'}[auth.user?.role||'MEMBER'] }}</span><button class="user-button" @click="router.push('/profile')"><span class="avatar">{{ auth.user?.displayName?.slice(0,1) }}</span><span>{{ auth.user?.displayName }}</span></button><el-button text @click="logout">退出</el-button></div>
      </header>
      <section class="content"><router-view /></section>
    </main>
  </div>
</template>
