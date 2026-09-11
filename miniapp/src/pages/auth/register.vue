<script setup lang="ts">
import { reactive } from 'vue'
import { request } from '@/api/http'
const form=reactive({username:'',displayName:'',phone:'',email:'',password:'',confirm:''})
async function submit(){if(!form.username||!form.displayName||form.password.length<8){uni.showToast({title:'请完整填写，密码至少8位',icon:'none'});return}if(form.password!==form.confirm){uni.showToast({title:'两次密码不一致',icon:'none'});return}await request('/auth/register','POST',form);uni.showToast({title:'注册成功'});setTimeout(()=>uni.navigateBack(),500)}
</script>
<template><view class="page"><view class="title">会员注册</view><view class="subtitle">公共注册不会获得任何员工权限</view><view class="card"><input v-model="form.username" class="input" placeholder="用户名（4-40位）"/><input v-model="form.displayName" class="input" placeholder="显示名称"/><input v-model="form.phone" class="input" placeholder="手机号（选填）"/><input v-model="form.email" class="input" placeholder="邮箱（选填）"/><input v-model="form.password" class="input" password placeholder="密码（至少8位）"/><input v-model="form.confirm" class="input" password placeholder="再次输入密码"/><button class="primary" @click="submit">创建账号</button></view></view></template>
