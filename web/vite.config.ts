import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: { tsconfigPaths: true },
  server: {
    port: 5173,
    proxy: { '/api': 'http://localhost:8080', '/uploads': 'http://localhost:8080' }
  },
  build: { sourcemap: true }
})
