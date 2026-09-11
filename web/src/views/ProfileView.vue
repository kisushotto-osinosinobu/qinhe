<script setup lang="ts">
import { onMounted,reactive,ref } from 'vue'
import { api,http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
const auth=useAuthStore(),router=useRouter(),saving=ref(false),profile=reactive({displayName:'',phone:'',email:'',avatarUrl:''}),password=reactive({oldPassword:'',newPassword:'',confirm:''})
onMounted(()=>Object.assign(profile,auth.user||{}))
async function save(){saving.value=true;try{await api(http.put('/auth/profile',profile));await auth.refresh();ElMessage.success('个人资料已更新')}finally{saving.value=false}}
async function change(){if(password.newPassword!==password.confirm){ElMessage.warning('两次输入的新密码不一致');return}await api(http.put('/auth/password',{oldPassword:password.oldPassword,newPassword:password.newPassword}));ElMessage.success('密码已修改，请重新登录');auth.clear();router.push('/login')}
</script>
<template><div><div class="page-head"><div><h2>个人中心</h2><p>维护联系方式和登录密码。</p></div></div><div class="grid-2"><section class="panel"><h3 style="margin-top:0">个人资料</h3><el-form label-position="top"><el-form-item label="用户名"><el-input :model-value="auth.user?.username" disabled/></el-form-item><el-form-item label="显示名称" required><el-input v-model="profile.displayName"/></el-form-item><div class="form-grid"><el-form-item label="手机号"><el-input v-model="profile.phone"/></el-form-item><el-form-item label="邮箱"><el-input v-model="profile.email"/></el-form-item></div><el-button type="primary" :loading="saving" @click="save">保存资料</el-button></el-form></section><section class="panel"><h3 style="margin-top:0">修改密码</h3><div class="notice" style="margin-bottom:16px">密码修改成功后，所有已登录会话都会立即失效。</div><el-form label-position="top"><el-form-item label="原密码" required><el-input v-model="password.oldPassword" type="password" show-password/></el-form-item><el-form-item label="新密码" required><el-input v-model="password.newPassword" type="password" show-password/></el-form-item><el-form-item label="确认新密码" required><el-input v-model="password.confirm" type="password" show-password/></el-form-item><el-button type="danger" @click="change">修改密码并重新登录</el-button></el-form></section></div></div></template>
