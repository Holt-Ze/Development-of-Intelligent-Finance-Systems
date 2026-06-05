<template>
  <div class="page-grid">
    <PageCard title="备份说明">
      <p class="muted">
        本模块仅开放给管理员。备份采用数据库逻辑备份文件 + 备份记录表的方式管理，恢复操作需要二次确认。
      </p>
      <el-button type="danger" :loading="creating" @click="handleCreate">创建备份</el-button>
    </PageCard>

    <PageCard title="备份记录">
      <el-table :data="rows" stripe v-loading="loading">
        <el-table-column prop="backupName" label="备份名称" />
        <el-table-column prop="createdBy" label="创建人" />
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column prop="fileSize" label="文件大小(Byte)" />
        <el-table-column prop="restoredAt" label="最近恢复时间" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-space>
              <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
              <el-button type="danger" link @click="handleRestore(row)">恢复</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </PageCard>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageCard from '../components/PageCard.vue'
import { createBackup, downloadBackup, fetchBackups, restoreBackup } from '../api/finance'
import { downloadBlob } from '../utils/app'

const loading = ref(false)
const creating = ref(false)
const rows = ref([])

const loadRows = async () => {
  loading.value = true
  try {
    rows.value = await fetchBackups()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  creating.value = true
  try {
    await createBackup({ createdBy: '系统管理员', remark: '页面手动创建备份' })
    ElMessage.success('备份创建成功')
    await loadRows()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    creating.value = false
  }
}

const handleDownload = async (row) => {
  try {
    const response = await downloadBackup(row.id)
    downloadBlob(response, row.backupName)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleRestore = async (row) => {
  try {
    await ElMessageBox.confirm(`确认恢复备份 ${row.backupName} 吗？这会覆盖当前数据库数据。`, '恢复确认', {
      type: 'warning'
    })
    await restoreBackup(row.id)
    ElMessage.success('备份恢复完成')
    await loadRows()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '恢复已取消')
    }
  }
}

onMounted(loadRows)
</script>
