export const AUTH_KEY = 'finance-auth'

export const getAuth = () => {
  const raw = localStorage.getItem(AUTH_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw)
  } catch (error) {
    localStorage.removeItem(AUTH_KEY)
    return null
  }
}

export const setAuth = (payload) => {
  localStorage.setItem(AUTH_KEY, JSON.stringify(payload))
}

export const clearAuth = () => {
  localStorage.removeItem(AUTH_KEY)
}

export const statusLabel = (value) => (value === 1 ? '启用' : '停用')

export const statusType = (value) => (value === 1 ? 'success' : 'info')

export const formatMoney = (value) => Number(value || 0).toFixed(2)

export const downloadBlob = (response, fileName) => {
  const blob = new Blob([response.data])
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = fileName
  link.click()
  URL.revokeObjectURL(link.href)
}
