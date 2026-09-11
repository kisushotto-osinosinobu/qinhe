import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiEnvelope } from '@/types'

export const http = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || '/api', timeout: 12000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('supermarket_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || (status ? `请求失败（${status}）` : '无法连接服务器')
    if (status === 401) {
      localStorage.removeItem('supermarket_token')
      localStorage.removeItem('supermarket_user')
      if (location.pathname !== '/login') location.assign('/login')
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export async function api<T>(promise: Promise<{ data: ApiEnvelope<T> }>): Promise<T> {
  const response = await promise
  if (response.data.code !== 0) throw new Error(response.data.message)
  return response.data.data
}

