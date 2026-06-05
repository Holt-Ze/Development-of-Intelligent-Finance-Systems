import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000
})

http.interceptors.request.use((config) => {
  const raw = localStorage.getItem('finance-auth')
  if (raw) {
    try {
      const auth = JSON.parse(raw)
      if (auth?.token) {
        config.headers.Authorization = `Bearer ${auth.token}`
      }
    } catch (error) {
      localStorage.removeItem('finance-auth')
    }
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response
    }
    const payload = response.data
    if (payload && typeof payload.code !== 'undefined') {
      if (payload.code === 200) {
        return payload.data
      }
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload
  },
  (error) => Promise.reject(new Error(error.response?.data?.message || error.message || '请求失败'))
)

export default http
