<template>
  <PageCard title="部门管理">
    <template #extra>
      <el-button type="primary" @click="openCreate">新增部门</el-button>
    </template>
    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="deptCode" label="部门编号" />
      <el-table-column prop="deptName" label="部门名称" />
      <el-table-column prop="leaderName" label="负责人" />
      <el-table-column prop="phone" label="联系电话" />
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

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑部门' : '新增部门'" width="520px">
    <el-form :model="form" label-width="88px">
      <el-form-item label="部门编号"><el-input v-model="form.deptCode" /></el-form-item>
      <el-form-item label="部门名称"><el-input v-model="form.deptName" /></el-form-item>
      <el-form-item label="负责人"><el-input v-model="form.leaderName" /></el-form-item>
      <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
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
  deptCode: '',
  deptName: '',
  leaderName: '',
  phone: '',
  status: 1
})

const resetForm = () => {
  form.deptCode = ''
  form.deptName = ''
  form.leaderName = ''
  form.phone = ''
  form.status = 1
}

const loadDepartments = async () => {
  loading.value = true
  try {
    rows.value = await fetchList(endpointMap.departments)
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
  form.deptCode = row.deptCode
  form.deptName = row.deptName
  form.leaderName = row.leaderName
  form.phone = row.phone
  form.status = row.status
  dialogVisible.value = true
}

const submitForm = async () => {
  submitting.value = true
  try {
    if (editingId.value) {
      await updateItem(endpointMap.departments, editingId.value, form)
    } else {
      await createItem(endpointMap.departments, form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadDepartments()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

const changeStatus = async (row) => {
  try {
    await updateItemStatus(endpointMap.departments, row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态已更新')
    await loadDepartments()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadDepartments)
</script>
