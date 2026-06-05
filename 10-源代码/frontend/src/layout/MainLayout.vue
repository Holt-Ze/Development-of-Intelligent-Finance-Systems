<template>
  <el-container style="min-height: 100vh;">
    <el-aside width="240px" style="background:#102542;color:#fff;">
      <div style="padding:24px 20px;font-size:18px;font-weight:700;">
        学校部门财务管理系统
      </div>
      <el-menu
        router
        :default-active="$route.path"
        background-color="#102542"
        text-color="#dbeafe"
        active-text-color="#f59e0b"
      >
        <el-menu-item v-for="item in visibleMenus" :key="item.path" :index="item.path">{{ item.label }}</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:rgba(255,255,255,0.78);backdrop-filter: blur(12px);display:flex;align-items:center;justify-content:space-between;">
        <div>
          <strong>{{ $route.meta.title || '财务管理系统' }}</strong>
          <div class="muted" style="font-size:12px;margin-top:4px;">基于真实接口的课程设计实现</div>
        </div>
        <div style="display:flex;align-items:center;gap:12px;">
          <el-tag type="warning">{{ authLabel }}</el-tag>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main style="padding:24px;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { clearAuth, getAuth } from '../utils/app'

const router = useRouter()
const menus = [
  { path: '/dashboard', label: '首页仪表盘', roles: ['ADMIN', 'FINANCE'] },
  { path: '/users', label: '用户管理', roles: ['ADMIN'] },
  { path: '/departments', label: '部门管理', roles: ['ADMIN'] },
  { path: '/accounts', label: '账户管理', roles: ['ADMIN'] },
  { path: '/income-categories', label: '收入类别管理', roles: ['ADMIN'] },
  { path: '/expense-categories', label: '支出类别管理', roles: ['ADMIN'] },
  { path: '/incomes', label: '收入记录', roles: ['ADMIN', 'FINANCE'] },
  { path: '/expenses', label: '支出记录', roles: ['ADMIN', 'FINANCE'] },
  { path: '/reports', label: '报表中心', roles: ['ADMIN', 'FINANCE'] },
  { path: '/backups', label: '备份管理', roles: ['ADMIN'] }
]

const auth = computed(() => getAuth() || {})
const visibleMenus = computed(() => menus.filter((item) => item.roles.some((role) => auth.value.roles?.includes(role))))
const authLabel = computed(() => {
  const role = auth.value.roles?.[0]
  return role === 'FINANCE' ? '财务员' : '管理员'
})

const handleLogout = () => {
  clearAuth()
  router.push('/login')
}
</script>
