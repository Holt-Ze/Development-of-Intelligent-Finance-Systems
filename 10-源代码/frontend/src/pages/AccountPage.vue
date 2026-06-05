<template>
  <PageCard title="账户管理">
    <template #extra>
      <el-button type="primary" @click="openCreate">新增账户</el-button>
    </template>
    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="accountCode" label="账户编号" />
      <el-table-column prop="accountName" label="账户名称" />
      <el-table-column prop="ownerUnit" label="开户单位" />
      <el-table-column prop="description" label="说明" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <div class="page-actions">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="changeStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </PageCard>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑账户' : '新增账户'" width="520px">
    <el-form :model="form" label-width="88px">
      <el-form-item label="账户编号"><el-input v-model="form.accountCode" /></el-form-item>
      <el-form-item label="账户名称"><el-input v-model="form.accountName" /></el-form-item>
      <el-form-item label="开户单位"><el-input v-model="form.ownerUnit" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageCard from '../components/PageCard.vue'
import { createItem, endpointMap, fetchList, updateItem, updateItemStatus } from '../api/finance'
import { statusLabel, statusType } from '../utils/app'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const rows = ref([])
const form = reactive({
  accountCode: '',
  accountName: '',
  ownerUnit: '',
  description: '',
  status: 1
})

const resetForm = () => {
  form.accountCode = ''
  form.accountName = ''
  form.ownerUnit = ''
  form.description = ''
  form.status = 1
}

const loadAccounts = async () => {
  loading.value = true
  try {
    rows.value = await fetchList(endpointMap.accounts)
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.accountCode = row.accountCode
  form.accountName = row.accountName
  form.ownerUnit = row.ownerUnit
  form.description = row.description
  form.status = row.status
  dialogVisible.value = true
}

const submitForm = async () => {
  submitting.value = true
  try {
    if (editingId.value) {
      await updateItem(endpointMap.accounts, editingId.value, form)
    } else {
      await createItem(endpointMap.accounts, form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadAccounts()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

const changeStatus = async (row) => {
  try {
    await updateItemStatus(endpointMap.accounts, row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态已更新')
    await loadAccounts()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadAccounts)
</script>
