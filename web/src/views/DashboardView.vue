<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api, http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'
import { Refresh } from '@element-plus/icons-vue'
import { orderStatus } from '@/utils/orderStatus'

const auth = useAuthStore()
const loading = ref(false)
const data = ref<any>({ salesAmount: 0, orderCount: 0, productCount: 0, lowStockCount: 0 })
const inventory = ref<any[]>([])
const orders = ref<any[]>([])
const trend = ref<any[]>([])
const hotProducts = ref<any[]>([])
const member = computed(() => auth.user?.role === 'MEMBER')
const maxSales = computed(() => Math.max(1, ...trend.value.map((item) => Number(item.netSales || 0))))
const maxSold = computed(() => Math.max(1, ...hotProducts.value.map((item) => Number(item.soldQty || 0))))

function dateText(date: Date) { return date.toISOString().slice(0, 10) }

async function load() {
  loading.value = true
  try {
    if (member.value) {
      const result = await api<any>(http.get('/orders', { params: { pageSize: 5 } }))
      orders.value = result.items
      return
    }
    const to = new Date(), from = new Date()
    from.setDate(to.getDate() - 6)
    const params = { from: dateText(from), to: dateText(to) }
    const [summary, stock, sales, hot] = await Promise.all([
      api<any>(http.get('/stats/dashboard')),
      api<any>(http.get('/inventory', { params: { lowOnly: true, pageSize: 6 } })),
      api<any[]>(http.get('/stats/sales-trend', { params })),
      api<any[]>(http.get('/stats/hot-products', { params: { ...params, limit: 5 } })),
    ])
    data.value = summary
    inventory.value = stock.items
    trend.value = sales
    hotProducts.value = hot
  } finally { loading.value = false }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <div class="page-head">
      <div><h2>{{ member ? '我的消费概览' : '今日经营概览' }}</h2><p>{{ member ? '查看最近订单，数据与微信小程序实时共用。' : '截至当前的已付款订单与可售库存状态。' }}</p></div>
      <el-button :icon="Refresh" @click="load">刷新数据</el-button>
    </div>
    <template v-if="!member">
      <div class="metric-grid">
        <article class="metric"><span class="label">今日净销售额</span><strong>¥ {{ Number(data.salesAmount || 0).toFixed(2) }}</strong><small>已扣除退货金额</small></article>
        <article class="metric"><span class="label">今日有效订单</span><strong>{{ data.orderCount || 0 }}</strong><small>不含待付款和已取消</small></article>
        <article class="metric"><span class="label">商品档案总数</span><strong>{{ data.productCount || 0 }}</strong><small>包含在售和下架商品</small></article>
        <article class="metric"><span class="label">库存预警</span><strong :class="{ 'danger-text': data.lowStockCount > 0 }">{{ data.lowStockCount || 0 }}</strong><small>可售库存低于阈值</small></article>
      </div>
      <div class="grid-2 dashboard-row">
        <section class="panel">
          <div class="page-head"><div><h2>近 7 日销售趋势</h2><p>仅统计已收款订单，金额已扣除退货</p></div></div>
          <div v-if="trend.length" class="bar-list">
            <div v-for="item in trend" :key="item.saleDate" class="bar-row"><span>{{ item.saleDate }}</span><div class="bar-track"><i :style="{ width: `${Math.max(3, Number(item.netSales || 0) / maxSales * 100)}%` }"></i></div><strong>¥{{ Number(item.netSales || 0).toFixed(2) }}</strong><small>{{ item.orderCount }} 单</small></div>
          </div>
          <el-empty v-else description="近 7 日暂无已收款销售" :image-size="72" />
        </section>
        <section class="panel">
          <div class="page-head"><div><h2>热销商品</h2><p>按净售出数量排序</p></div></div>
          <div v-if="hotProducts.length" class="rank-list">
            <div v-for="(item, index) in hotProducts" :key="item.productId" class="rank-row"><b>{{ index + 1 }}</b><div><strong>{{ item.productName }}</strong><span>{{ item.productCode }}</span><i><em :style="{ width: `${Number(item.soldQty || 0) / maxSold * 100}%` }"></em></i></div><span>{{ item.soldQty }} 件</span></div>
          </div>
          <el-empty v-else description="暂无热销数据" :image-size="72" />
        </section>
      </div>
      <div class="grid-2">
        <section class="panel">
          <div class="page-head"><div><h2>低库存待处理</h2><p>可售库存 = 当前库存 − 预占库存</p></div><router-link to="/inventory">查看全部</router-link></div>
          <el-table :data="inventory" stripe><el-table-column prop="code" label="商品编码" width="120"/><el-table-column prop="name" label="商品"/><el-table-column prop="currentQty" label="当前" width="80"/><el-table-column prop="reservedQty" label="预占" width="80"/><el-table-column prop="availableQty" label="可售" width="80"><template #default="scope"><strong class="danger-text">{{ scope.row.availableQty }}</strong></template></el-table-column><el-table-column prop="lowStockThreshold" label="阈值" width="80"/></el-table>
        </section>
        <aside class="panel"><h2 style="margin-top:0">数据口径</h2><p style="line-height:1.75;color:var(--muted)">{{ data.salesDefinition || '净销售额按已付款订单减去退货金额计算。' }}</p><div class="notice">到店付款 / 模拟收款<br/>本系统不接入真实资金交易</div></aside>
      </div>
    </template>
    <section v-else class="panel"><h2 style="margin-top:0">最近订单</h2><el-table :data="orders"><el-table-column prop="orderNo" label="订单号" min-width="190"/><el-table-column label="状态" width="130"><template #default="scope"><el-tag :type="orderStatus[scope.row.status]?.type||'info'">{{ orderStatus[scope.row.status]?.label||scope.row.status }}</el-tag></template></el-table-column><el-table-column prop="totalAmount" label="金额" width="110"><template #default="scope">¥{{ Number(scope.row.totalAmount).toFixed(2) }}</template></el-table-column><el-table-column prop="createdAt" label="下单时间" min-width="170"/></el-table></section>
  </div>
</template>

<style scoped>
.dashboard-row{margin-bottom:18px}.bar-list{display:grid;gap:13px}.bar-row{display:grid;grid-template-columns:92px minmax(100px,1fr) 90px 46px;align-items:center;gap:10px;font-size:13px}.bar-track{height:9px;background:#edf2f0;border-radius:99px;overflow:hidden}.bar-track i{display:block;height:100%;background:linear-gradient(90deg,var(--green),#44a78e);border-radius:inherit}.bar-row strong{text-align:right}.bar-row small{color:var(--muted);text-align:right}.rank-list{display:grid;gap:14px}.rank-row{display:grid;grid-template-columns:28px 1fr 56px;align-items:center;gap:10px}.rank-row>b{width:25px;height:25px;border-radius:8px;background:var(--mint);color:var(--deep);display:grid;place-items:center}.rank-row>div{display:grid;grid-template-columns:1fr auto;gap:3px 10px}.rank-row>div>span{font-size:12px;color:var(--muted)}.rank-row>div>i{grid-column:1/-1;height:5px;background:#edf2f0;border-radius:99px;overflow:hidden}.rank-row>div>i>em{display:block;height:100%;background:var(--amber);border-radius:inherit}.rank-row>span{text-align:right;font-size:13px;color:var(--muted)}
@media(max-width:720px){.bar-row{grid-template-columns:78px 1fr 78px}.bar-row small{display:none}}
</style>
