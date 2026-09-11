import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppLayout from '@/layouts/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path:'/login', component:()=>import('@/views/LoginView.vue'), meta:{ public:true } },
    { path:'/register', component:()=>import('@/views/RegisterView.vue'), meta:{ public:true } },
    { path:'/', component:AppLayout, children:[
      { path:'', redirect:'/dashboard' },
      { path:'dashboard', component:()=>import('@/views/DashboardView.vue') },
      { path:'catalog', component:()=>import('@/views/CatalogView.vue'), meta:{ roles:['ADMIN','INVENTORY_MANAGER'] } },
      { path:'suppliers', component:()=>import('@/views/SupplierView.vue'), meta:{ roles:['ADMIN','INVENTORY_MANAGER'] } },
      { path:'purchases', component:()=>import('@/views/PurchaseView.vue'), meta:{ roles:['ADMIN','INVENTORY_MANAGER'] } },
      { path:'inventory', component:()=>import('@/views/InventoryView.vue'), meta:{ roles:['ADMIN','INVENTORY_MANAGER','CASHIER'] } },
      { path:'pos', component:()=>import('@/views/PosView.vue'), meta:{ roles:['ADMIN','CASHIER'] } },
      { path:'orders', component:()=>import('@/views/OrdersView.vue') },
      { path:'system', component:()=>import('@/views/SystemView.vue'), meta:{ roles:['ADMIN'] } },
      { path:'profile', component:()=>import('@/views/ProfileView.vue') },
    ]},
    { path:'/:pathMatch(.*)*', redirect:'/dashboard' },
  ],
})

router.beforeEach((to) => {
  const auth=useAuthStore()
  if(!to.meta.public&&!auth.loggedIn)return { path:'/login',query:{redirect:to.fullPath} }
  if(to.meta.public&&auth.loggedIn)return '/dashboard'
  const roles=to.meta.roles as string[]|undefined
  if(roles&&auth.user&&!roles.includes(auth.user.role))return '/dashboard'
})
export default router

