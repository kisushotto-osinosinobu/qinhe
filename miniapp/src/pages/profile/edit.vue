<script setup lang="ts">
import { reactive,ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '@/api/http'

const form=reactive({displayName:'',phone:'',email:'',avatarUrl:''}),saving=ref(false)
onLoad(async()=>{const profile:any=await request('/auth/profile');Object.assign(form,profile)})
async function save(){if(!form.displayName.trim()){uni.showToast({title:'请填写显示名称',icon:'none'});return}saving.value=true;try{const profile:any=await request('/auth/profile','PUT',form);uni.setStorageSync('user',profile);uni.showToast({title:'资料已保存'});setTimeout(()=>uni.navigateBack(),500)}finally{saving.value=false}}
</script>
<template><view class="page"><view class="title">编辑个人资料</view><view class="subtitle">保存后 Web 与小程序读取同一份账号资料</view><view class="card"><view class="muted">显示名称</view><input v-model="form.displayName" class="input" maxlength="80" placeholder="显示名称"/><view class="muted">手机号</view><input v-model="form.phone" class="input" maxlength="30" type="number" placeholder="选填"/><view class="muted">邮箱</view><input v-model="form.email" class="input" maxlength="120" placeholder="选填"/><button class="primary" :loading="saving" @click="save">保存资料</button></view></view></template>
