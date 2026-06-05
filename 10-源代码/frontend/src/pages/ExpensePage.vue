<template>
  <div class="page-grid">
    <PageCard title="支出录入">
      <el-form :model="form" label-width="88px">
        <el-row :gutter="16">
          <el-col :md="8">
            <el-form-item label="部门">
              <el-select v-model="form.deptId" placeholder="请选择部门" style="width:100%;">
                <el-option v-for="item in departmentOptions" :key="item.id" :label="item.deptName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="8">
            <el-form-item label="账户">
              <el-select v-model="form.accountId" placeholder="请选择账户" style="width:100%;">
                <el-option v-for="item in accountOptions" :key="item.id" :label="item.accountName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="8">
            <el-form-item label="类别">
              <el-select v-model="form.categoryId" placeholder="请选择类别" style="width:100%;">
                <el-option v-for="item in categoryOptions" :key="item.id" :label="item.categoryName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :md="8">
            <el-form-item label="金额">
              <el-input v-model="form.amount" />
            </el-form-item>
          </el-col>
          <el-col :md="8">
            <el-form-item label="日期">
              <el-date-picker v-model="form.occurredOn" type="date" value-format="YYYY-MM-DD" style="width:100%;" />
            </el-form-item>
          </el-col>
          <el-col :md="8">
            <el-form-item label="经办人">
              <el-input v-model="form.operatorName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
        <div class="page-actions">
          <el-button type="primary" :loading="submitting" @click="submitForm">{{ editingId ? '更新支出记录' : '保存支出记录' }}</el-button>
          <el-button @click="resetForm">重置</el-button>
        </div>
      </el-form>
    </PageCard>

    <PageCard title="支出记录列表">
      <el-table :data="rows" stripe v-loading="loading">
        <el-table-column prop="recordNo" label="编号" />
        <el-table-column prop="deptName" label="部门" />
        <el-table-column prop="accountName" label="账户" />
        <el-table-column prop="categoryName" label="类别" />
        <el-table-column prop="amount" label="金额" />
        <el-table-column prop="occurredOn" label="日期" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <div class="page-actions">
              <el-button link type="primary" @click="editRow(row)">编辑</el-button>
              <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="changeStatus(row)">
                {{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </PageCard>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageCard from '../components/PageCard.vue'
import { createItem, endpointMap, fetchList, updateItem, updateItemStatus } from '../api/finance'
import { statusLabel, statusType } from '../utils/app'

const loading = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const rows = ref([])
const departmentOptions = ref([])
const accountOptions = ref([])
const categoryOptions = ref([])
const form = reactive({
  deptId: null,
  accountId: null,
  categoryId: null,
  amount: '3200.00',
  occurredOn: '',
  operatorName: '张会计',
  remark: '',
  status: 1
})

const resetForm = () => {
  editingId.value = null
  form.deptId = departmentOptions.value[0]?.id ?? null
  form.accountId = accountOptions.value[0]?.id ?? null
  form.categoryId = categoryOptions.value[0]?.id ?? null
  form.amount = ''
  form.occurredOn = ''
  form.operatorName = '张会计'
  form.remark = ''
  form.status = 1
}

const loadPageData = async () => {
  loading.value = true
  try {
    const [departments, accounts, categories, expenseResult] = await Promise.all([
      fetchList(endpointMap.departments),
      fetchList(endpointMap.accounts),
      fetchList(endpointMap.expenseCategories),
      fetchList(endpointMap.expenses)
    ])
    departmentOptions.value = departments.filter((item) => item.status === 1)
    accountOptions.value = accounts.filter((item) => item.status === 1)
    categoryOptions.value = categories.filter((item) => item.status === 1)
    rows.value = expenseResult.records || []
    if (!form.deptId && departmentOptions.value.length) {
      resetForm()
    }
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  submitting.value = true
  try {
    const payload = {
      ...form,
      amount: Number(form.amount)
    }
    if (editingId.value) {
      await updateItem(endpointMap.expenses, editingId.value, payload)
    } else {
      await createItem(endpointMap.expenses, payload)
    }
    ElMessage.success('保存成功')
    resetForm()
    await loadPageData()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

const editRow = (row) => {
  editingId.value = row.id
  form.deptId = row.deptId
  form.accountId = row.accountId
  form.categoryId = row.categoryId
  form.amount = row.amount
  form.occurredOn = row.occurredOn
  form.operatorName = row.operatorName
  form.remark = row.remark
  form.status = row.status
}

const changeStatus = async (row) => {
  try {
    await updateItemStatus(endpointMap.expenses, row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态已更新')
    await loadPageData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadPageData)
</script>
