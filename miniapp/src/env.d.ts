/// <reference types="@dcloudio/types" />
declare module '*.vue' { import type { DefineComponent } from 'vue'; const component: DefineComponent; export default component }
declare module '@vue/runtime-core' { interface ComponentCustomProperties { uni: any } }
declare module 'vue' { interface ComponentCustomProperties { uni: any } }
declare global { interface Event { detail: any } }
export {}
