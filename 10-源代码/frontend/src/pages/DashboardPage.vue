<template>
  <div class="page-grid">
    <div class="stats-grid">
      <PageCard v-for="item in stats" :key="item.label">
        <div class="muted">{{ item.label }}</div>
        <div style="font-size:28px;font-weight:700;margin-top:10px;">{{ item.value }}</div>
      </PageCard>
    </div>

    <div style="display:grid;grid-template-columns:2fr 1fr;gap:16px;">
      <PageCard title="本月财务趋势">
        <el-table :data="trendData" stripe v-loading="loading">
          <el-table-column prop="label" label="日期" />
          <el-table-column prop="income" label="收入" />
          <el-table-column prop="expense" label="支出" />
        </el-table>
      </PageCard>
      <PageCard title="使用说明">
        <el-timeline>
          <el-timeline-item timestamp="步骤 1">管理员维护部门、账户、类别</el-timeline-item>
          <el-timeline-item timestamp="步骤 2">财务员录入收入和支出</el-timeline-item>
          <el-timeline-item timestamp="步骤 3">报表中心按周月年查询</el-timeline-item>
          <el-timeline-item timestamp="步骤 4">管理员执行备份与恢复</el-timeline-item>
        </el-timeline>
      </PageCard>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageCard from '../components/PageCard.vue'
import { fetchDashboard } from '../api/finance'
import { formatMoney } from '../utils/app'

const loading = ref(false)
const summary = ref({
  totalIncome: 0,
  totalExpense: 0,
  balance: 0,
  trendStats: []
})

const stats = computed(() => [
  { label: '收入总额', value: formatMoney(summary.value.totalIncome) },
  { label: '支出总额', value: formatMoney(summary.value.totalExpense) },
  { label: '结余', value: formatMoney(summary.value.balance) }
])

const trendData = computed(() => summary.value.trendStats || [])

const loadDashboard = async () => {
  loading.value = true
  try {
    summary.value = await fetchDashboard()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>
