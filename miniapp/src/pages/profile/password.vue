<script setup lang="ts">
import { reactive,ref } from 'vue'
import { clearAuth,request } from '@/api/http'

const form=reactive({oldPassword:'',newPassword:'',confirm:''}),saving=ref(false)
async function save(){if(form.newPassword.length<8){uni.showToast({title:'新密码至少 8 位',icon:'none'});return}if(form.newPassword!==form.confirm){uni.showToast({title:'两次新密码不一致',icon:'none'});return}saving.value=true;try{await request('/auth/password','PUT',{oldPassword:form.oldPassword,newPassword:form.newPassword});clearAuth();await uni.showModal({title:'密码已修改',content:'全部登录会话已失效，请使用新密码重新登录。',showCancel:false});uni.redirectTo({url:'/pages/auth/login'})}finally{saving.value=false}}
</script>
<template><view class="page"><view class="title">修改密码</view><view class="subtitle">密码修改后会撤销 Web 与小程序的全部会话</view><view class="card"><input v-model="form.oldPassword" class="input" password placeholder="原密码"/><input v-model="form.newPassword" class="input" password placeholder="新密码（至少 8 位）"/><input v-model="form.confirm" class="input" password placeholder="再次输入新密码"/><button class="primary" :loading="saving" @click="save">确认修改</button></view><view class="notice">请勿在公共设备保存密码。修改成功后需要重新登录。</view></view></template>
