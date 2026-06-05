<template>
  <div style="min-height:100vh;display:grid;place-items:center;padding:24px;background:radial-gradient(circle at top left,#f59e0b 0%,#102542 45%,#0f172a 100%);">
    <div style="width:min(960px,100%);display:grid;grid-template-columns:1.1fr 0.9fr;gap:24px;align-items:center;">
      <div style="color:#fff;padding:24px;">
        <div style="font-size:40px;font-weight:800;line-height:1.2;">学校部门财务管理系统</div>
        <p style="font-size:16px;line-height:1.9;color:rgba(255,255,255,0.82);">
          面向校内部门的收入、支出、报表和备份管理平台。当前版本已接入真实后端接口，可直接配合数据库演示完整流程。
        </p>
        <div style="display:flex;gap:12px;flex-wrap:wrap;">
          <el-tag type="warning" size="large">管理员</el-tag>
          <el-tag type="success" size="large">财务员</el-tag>
          <el-tag type="info" size="large">报表中心</el-tag>
          <el-tag type="danger" size="large">数据备份</el-tag>
        </div>
      </div>

      <el-card shadow="always" style="border-radius:20px;">
        <template #header>
          <div>
            <div style="font-size:20px;font-weight:700;">登录系统</div>
            <div class="muted" style="margin-top:6px;">默认密码：123456</div>
          </div>
        </template>
        <el-form :model="form" label-position="top" @submit.prevent>
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-button type="primary" style="width:100%;" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginApi } from '../api/finance'
import { setAuth } from '../utils/app'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '123456'
})

const handleLogin = async () => {
  loading.value = true
  try {
    const data = await loginApi(form)
    setAuth(data)
    ElMessage.success(`欢迎你，${data.realName}`)
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>
