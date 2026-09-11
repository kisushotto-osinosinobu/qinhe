import { defineConfig } from 'vite'
import * as uniModule from '@dcloudio/vite-plugin-uni'
const uni = (uniModule as any).default?.default || (uniModule as any).default || uniModule
export default defineConfig({ plugins: [uni()] })
