<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, http } from '@/api/http'
import { ElMessage } from 'element-plus'
const router=useRouter(),loading=ref(false)
const form=reactive({username:'',displayName:'',phone:'',email:'',password:'',confirm:''})
async function submit(){if(form.password!==form.confirm){ElMessage.warning('两次输入的密码不一致');return}loading.value=true;try{await api(http.post('/auth/register',form));ElMessage.success('注册成功，请登录');router.push('/login')}finally{loading.value=false}}
</script>
<template><div class="login-page"><section class="login-story"><div class="brand"><div class="brand-mark">青</div><div><strong>青禾零售</strong><span>MEMBER ACCESS</span></div></div><h1>成为会员，<br>轻松下单。</h1><p>公共注册只会创建普通会员账号。员工账号和岗位权限由管理员在运营台中分配。</p></section><section class="login-card-wrap"><el-form class="login-card" label-position="top" @submit.prevent="submit"><h2>会员注册</h2><p>创建账号后可与小程序使用同一身份</p><div class="form-grid"><el-form-item label="用户名"><el-input v-model="form.username" autocomplete="username"/></el-form-item><el-form-item label="显示名称"><el-input v-model="form.displayName"/></el-form-item><el-form-item label="手机号"><el-input v-model="form.phone"/></el-form-item><el-form-item label="邮箱"><el-input v-model="form.email"/></el-form-item><el-form-item label="密码"><el-input v-model="form.password" type="password" show-password autocomplete="new-password"/></el-form-item><el-form-item label="确认密码"><el-input v-model="form.confirm" type="password" show-password autocomplete="new-password"/></el-form-item></div><el-button type="primary" size="large" native-type="submit" :loading="loading" style="width:100%">创建会员账号</el-button><p style="text-align:center;margin-top:22px">已有账号？<router-link to="/login">返回登录</router-link></p></el-form></section></div></template>

