import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import LoginPage from '../pages/LoginPage.vue'
import DashboardPage from '../pages/DashboardPage.vue'
import UserPage from '../pages/UserPage.vue'
import DepartmentPage from '../pages/DepartmentPage.vue'
import AccountPage from '../pages/AccountPage.vue'
import IncomeCategoryPage from '../pages/IncomeCategoryPage.vue'
import ExpenseCategoryPage from '../pages/ExpenseCategoryPage.vue'
import IncomePage from '../pages/IncomePage.vue'
import ExpensePage from '../pages/ExpensePage.vue'
import ReportPage from '../pages/ReportPage.vue'
import BackupPage from '../pages/BackupPage.vue'
import { getAuth } from '../utils/app'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginPage },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: 'dashboard', component: DashboardPage, meta: { title: '首页仪表盘', roles: ['ADMIN', 'FINANCE'] } },
      { path: 'users', component: UserPage, meta: { title: '用户管理', roles: ['ADMIN'] } },
      { path: 'departments', component: DepartmentPage, meta: { title: '部门管理', roles: ['ADMIN'] } },
      { path: 'accounts', component: AccountPage, meta: { title: '账户管理', roles: ['ADMIN'] } },
      { path: 'income-categories', component: IncomeCategoryPage, meta: { title: '收入类别管理', roles: ['ADMIN'] } },
      { path: 'expense-categories', component: ExpenseCategoryPage, meta: { title: '支出类别管理', roles: ['ADMIN'] } },
      { path: 'incomes', component: IncomePage, meta: { title: '收入记录', roles: ['ADMIN', 'FINANCE'] } },
      { path: 'expenses', component: ExpensePage, meta: { title: '支出记录', roles: ['ADMIN', 'FINANCE'] } },
      { path: 'reports', component: ReportPage, meta: { title: '报表中心', roles: ['ADMIN', 'FINANCE'] } },
      { path: 'backups', component: BackupPage, meta: { title: '备份管理', roles: ['ADMIN'] } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = getAuth()
  if (to.path !== '/login' && !auth?.token) {
    return '/login'
  }
  if (to.path === '/login' && auth?.token) {
    return '/dashboard'
  }
  if (to.meta?.roles?.length) {
    const hasRole = auth?.roles?.some((role) => to.meta.roles.includes(role))
    if (!hasRole) {
      return '/dashboard'
    }
  }
  return true
})

export default router
