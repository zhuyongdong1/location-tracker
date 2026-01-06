import axios from 'axios'

// 配置API基础设置
const API_BASE_URL = 'https://location.ulbooks.cn/api/v1/location'
const API_TOKEN = '733385e53ac29e11b3f1a9f5fe59e0485af2e47fbd0411ba7c815cfee9864bea'

// 创建axios实例
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Authorization': `Bearer ${API_TOKEN}`,
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    console.log('API Request:', config.method?.toUpperCase(), config.url)
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    return response
  },
  error => {
    console.error('API Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

// API方法
export const locationApi = {
  // 获取最新位置
  getLatest(deviceId = null) {
    const params = deviceId ? { device_id: deviceId } : {}
    return apiClient.get('/latest', { params })
  },

  // 获取历史轨迹
  getList(from, to, deviceId = null, limit = 100) {
    const params = {
      from,
      to,
      limit
    }
    if (deviceId) {
      params.device_id = deviceId
    }
    return apiClient.get('/list', { params })
  },

  // 删除位置数据
  deleteLocations(from, to, deviceId = null) {
    return apiClient.post('/delete', {
      from,
      to,
      device_id: deviceId
    })
  }
}

export default apiClient
