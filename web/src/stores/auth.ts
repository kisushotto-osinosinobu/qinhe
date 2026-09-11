import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api, http } from '@/api/http'
import type { Role, User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('supermarket_token') || '')
  const user = ref<User | null>(JSON.parse(localStorage.getItem('supermarket_user') || 'null'))
  const loggedIn = computed(() => Boolean(token.value && user.value))
  const isStaff = computed(() => user.value?.role !== 'MEMBER')
  const can = (...roles: Role[]) => Boolean(user.value && roles.includes(user.value.role))

  async function login(username: string, password: string) {
    const data = await api<{ token: string; user: User }>(http.post('/auth/login', { username, password }))
    token.value = data.token; user.value = data.user
    localStorage.setItem('supermarket_token', data.token)
    localStorage.setItem('supermarket_user', JSON.stringify(data.user))
  }
  async function logout() {
    try { await api(http.post('/auth/logout')) } finally { clear() }
  }
  async function refresh() {
    user.value = await api<User>(http.get('/auth/profile'))
    localStorage.setItem('supermarket_user', JSON.stringify(user.value))
  }
  function clear() { token.value='';user.value=null;localStorage.removeItem('supermarket_token');localStorage.removeItem('supermarket_user') }
  return { token, user, loggedIn, isStaff, can, login, logout, refresh, clear }
})

