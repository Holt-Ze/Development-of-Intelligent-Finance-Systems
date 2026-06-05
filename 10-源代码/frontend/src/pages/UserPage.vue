<template>
  <PageCard title="用户管理">
    <template #extra>
      <el-button type="primary" @click="openCreate">新增用户</el-button>
    </template>
    <el-table :data="rows" stripe v-loading="loading">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column prop="roleName" label="角色" />
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

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="520px">
    <el-form :model="form" label-width="88px">
      <el-form-item label="用户名">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item :label="editingId ? '重置密码' : '密码'">
        <el-input v-model="form.password" type="password" show-password placeholder="编辑时留空表示不修改" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="form.realName" />
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="form.roleCode" style="width:100%;">
          <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
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
import { createItem, endpointMap, fetchList, roleOptions, updateItem, updateItemStatus } from '../api/finance'
import { statusLabel, statusType } from '../utils/app'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const rows = ref([])
const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  roleCode: 'FINANCE',
  status: 1
})

const resetForm = () => {
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  form.roleCode = 'FINANCE'
  form.status = 1
}

const loadUsers = async () => {
  loading.value = true
  try {
    rows.value = await fetchList(endpointMap.users)
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
  form.username = row.username
  form.password = ''
  form.realName = row.realName
  form.phone = row.phone
  form.roleCode = row.roleCode
  form.status = row.status
  dialogVisible.value = true
}

const submitForm = async () => {
  submitting.value = true
  try {
    if (editingId.value) {
      await updateItem(endpointMap.users, editingId.value, form)
    } else {
      await createItem(endpointMap.users, form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

const changeStatus = async (row) => {
  try {
    await updateItemStatus(endpointMap.users, row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态已更新')
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

onMounted(loadUsers)
</script>
