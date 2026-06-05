<template>
  <div class="page-grid">
    <PageCard title="报表筛选">
      <el-form :model="query" inline>
        <el-form-item label="报表类型">
          <el-select v-model="query.type" style="width:180px;">
            <el-option label="周报表" value="weekly" />
            <el-option label="月报表" value="monthly" />
            <el-option label="年报表" value="yearly" />
            <el-option label="自定义区间" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="query.deptId" style="width:180px;" clearable placeholder="全部部门">
            <el-option v-for="item in departments" :key="item.id" :label="item.deptName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围" v-if="query.type === 'custom'">
          <el-date-picker v-model="query.range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="loadReport">查询</el-button>
      </el-form>
    </PageCard>

    <div class="stats-grid">
      <PageCard title="收入总额"><div style="font-size:26px;font-weight:700;">{{ formatMoney(summary.totalIncome) }}</div></PageCard>
      <PageCard title="支出总额"><div style="font-size:26px;font-weight:700;">{{ formatMoney(summary.totalExpense) }}</div></PageCard>
      <PageCard title="结余"><div style="font-size:26px;font-weight:700;">{{ formatMoney(summary.balance) }}</div></PageCard>
    </div>

    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">
      <PageCard title="类别汇总">
        <el-table :data="summary.categoryStats" stripe v-loading="loading">
          <el-table-column prop="name" label="类别" />
          <el-table-column prop="amount" label="金额" />
        </el-table>
      </PageCard>
      <PageCard title="部门汇总">
        <el-table :data="summary.departmentStats" stripe v-loading="loading">
          <el-table-column prop="name" label="部门" />
          <el-table-column prop="amount" label="金额" />
        </el-table>
      </PageCard>
    </div>

    <PageCard title="趋势汇总">
      <el-table :data="summary.trendStats" stripe v-loading="loading">
        <el-table-column prop="label" label="时间" />
        <el-table-column prop="income" label="收入" />
        <el-table-column prop="expense" label="支出" />
      </el-table>
    </PageCard>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageCard from '../components/PageCard.vue'
import { endpointMap, fetchList, fetchReport } from '../api/finance'
import { formatMoney } from '../utils/app'

const loading = ref(false)
const departments = ref([])
const summary = ref({
  totalIncome: 0,
  totalExpense: 0,
  balance: 0,
  categoryStats: [],
  departmentStats: [],
  trendStats: []
})
const query = reactive({
  type: 'monthly',
  deptId: null,
  range: []
})

const loadDepartments = async () => {
  try {
    departments.value = await fetchList(endpointMap.departments)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const loadReport = async () => {
  loading.value = true
  try {
    const params = {}
    if (query.deptId) {
      params.deptId = query.deptId
    }
    if (query.type === 'custom') {
      params.startDate = query.range?.[0]
      params.endDate = query.range?.[1]
    }
    summary.value = await fetchReport(query.type, params)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadDepartments()
  await loadReport()
})
</script>
