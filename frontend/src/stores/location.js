import { defineStore } from 'pinia'
import { locationApi } from '@/api/location'
import { ElMessage } from 'element-plus'

export const useLocationStore = defineStore('location', {
  state: () => ({
    latestLocation: null,
    locationList: [],
    loading: false,
    error: null
  }),

  getters: {
    // 获取最新位置的格式化信息
    formattedLatestLocation: (state) => {
      if (!state.latestLocation) return null

      const loc = state.latestLocation
      return {
        ...loc,
        formattedTime: formatTimestamp(loc.ts_server || loc.ts_client),
        coordinates: `${loc.lat.toFixed(6)}, ${loc.lng.toFixed(6)}`,
        accuracyText: `${loc.accuracy_m.toFixed(1)}米`
      }
    },

    // 获取轨迹统计信息
    trackStats: (state) => {
      if (state.locationList.length === 0) return null

      const list = state.locationList
      const first = list[list.length - 1] // 按时间倒序，所以最后一个是最早的
      const last = list[0] // 第一个是最新的

      return {
        totalPoints: list.length,
        timeRange: {
          start: formatTimestamp(first.ts_server || first.ts_client),
          end: formatTimestamp(last.ts_server || last.ts_client)
        },
        avgAccuracy: (list.reduce((sum, item) => sum + item.accuracy_m, 0) / list.length).toFixed(1)
      }
    }
  },

  actions: {
    // 获取最新位置
    async fetchLatestLocation(deviceId = null) {
      this.loading = true
      this.error = null

      try {
        const response = await locationApi.getLatest(deviceId)
        if (response.data.code === 200) {
          this.latestLocation = response.data.data
        } else {
          throw new Error(response.data.message)
        }
      } catch (error) {
        this.error = error.message || '获取最新位置失败'
        ElMessage.error(this.error)
        console.error('Fetch latest location error:', error)
      } finally {
        this.loading = false
      }
    },

    // 获取历史轨迹
    async fetchLocationList(from, to, deviceId = null, limit = 100) {
      this.loading = true
      this.error = null

      try {
        const response = await locationApi.getList(from, to, deviceId, limit)
        if (response.data.code === 200) {
          this.locationList = response.data.data.list || []
        } else {
          throw new Error(response.data.message)
        }
      } catch (error) {
        this.error = error.message || '获取历史轨迹失败'
        ElMessage.error(this.error)
        console.error('Fetch location list error:', error)
      } finally {
        this.loading = false
      }
    },

    // 删除位置数据
    async deleteLocations(from, to, deviceId = null) {
      try {
        const response = await locationApi.deleteLocations(from, to, deviceId)
        if (response.data.code === 200) {
          ElMessage.success(`成功删除 ${response.data.data.deleted_count} 条记录`)
          // 重新获取数据
          await this.fetchLatestLocation(deviceId)
          if (this.locationList.length > 0) {
            const firstTime = this.locationList[this.locationList.length - 1].ts_server || this.locationList[this.locationList.length - 1].ts_client
            const lastTime = this.locationList[0].ts_server || this.locationList[0].ts_client
            await this.fetchLocationList(firstTime, lastTime, deviceId)
          }
        } else {
          throw new Error(response.data.message)
        }
      } catch (error) {
        const message = error.message || '删除位置数据失败'
        ElMessage.error(message)
        console.error('Delete locations error:', error)
      }
    },

    // 清空当前状态
    clearData() {
      this.latestLocation = null
      this.locationList = []
      this.error = null
    }
  }
})

// 工具函数：格式化时间戳
function formatTimestamp(timestamp) {
  if (!timestamp) return '未知时间'

  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }

  // 小于24小时
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }

  // 超过24小时显示具体时间
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
