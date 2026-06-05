import http from './http'

export const endpointMap = {
  login: '/auth/login',
  logout: '/auth/logout',
  users: '/users',
  departments: '/departments',
  accounts: '/accounts',
  incomeCategories: '/income-categories',
  expenseCategories: '/expense-categories',
  incomes: '/incomes',
  expenses: '/expenses',
  reportsDashboard: '/reports/dashboard',
  reportsWeekly: '/reports/weekly',
  reportsMonthly: '/reports/monthly',
  reportsYearly: '/reports/yearly',
  reportsCustom: '/reports/custom',
  backups: '/backups'
}

export const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '财务员', value: 'FINANCE' }
]

export const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

export const loginApi = (payload) => http.post(endpointMap.login, payload)
export const logoutApi = () => http.post(endpointMap.logout)

export const fetchList = (endpoint, params) => http.get(endpoint, { params })
export const createItem = (endpoint, payload) => http.post(endpoint, payload)
export const updateItem = (endpoint, id, payload) => http.put(`${endpoint}/${id}`, payload)
export const updateItemStatus = (endpoint, id, status) => http.patch(`${endpoint}/${id}/status`, { status })

export const fetchDashboard = () => http.get(endpointMap.reportsDashboard)
export const fetchReport = (type, params) => {
  const mapping = {
    weekly: endpointMap.reportsWeekly,
    monthly: endpointMap.reportsMonthly,
    yearly: endpointMap.reportsYearly,
    custom: endpointMap.reportsCustom
  }
  return http.get(mapping[type], { params })
}

export const createBackup = (payload = {}) => http.post(endpointMap.backups, payload)
export const fetchBackups = () => http.get(endpointMap.backups)
export const restoreBackup = (id) => http.post(`${endpointMap.backups}/${id}/restore`)
export const downloadBackup = (id) => http.get(`${endpointMap.backups}/${id}/download`, { responseType: 'blob' })
