<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>仪表盘</h2>
      <el-button type="primary" @click="refreshData" :loading="loading">
        <el-icon><RefreshRight /></el-icon>
        刷新数据
      </el-button>
    </div>

    <!-- 最新位置卡片 -->
    <el-row :gutter="20" class="dashboard-row">
      <el-col :span="24">
        <el-card class="location-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Location /></el-icon>
              <span>最新位置</span>
            </div>
          </template>

          <div v-if="formattedLatestLocation" class="location-info">
            <div class="info-grid">
              <div class="info-item">
                <label>坐标：</label>
                <span>{{ formattedLatestLocation.coordinates }}</span>
              </div>
              <div class="info-item">
                <label>精度：</label>
                <span>{{ formattedLatestLocation.accuracyText }}</span>
              </div>
              <div class="info-item">
                <label>更新时间：</label>
                <span>{{ formattedLatestLocation.formattedTime }}</span>
              </div>
              <div class="info-item">
                <label>设备ID：</label>
                <span>{{ formattedLatestLocation.device_id }}</span>
              </div>
              <div class="info-item" v-if="formattedLatestLocation.provider">
                <label>定位方式：</label>
                <span>{{ formattedLatestLocation.provider === 'gps' ? 'GPS定位' : '网络定位' }}</span>
              </div>
              <div class="info-item" v-if="formattedLatestLocation.battery_pct">
                <label>电量：</label>
                <span>{{ formattedLatestLocation.battery_pct }}%</span>
              </div>
            </div>

            <!-- 高德地图跳转 -->
            <div style="margin-top: 16px;">
              <el-button
                v-if="formattedLatestLocation"
                type="primary"
                size="small"
                @click="openInGaodeMap"
              >
                <el-icon><MapLocation /></el-icon>
                在高德地图查看
              </el-button>
            </div>
          </div>

          <div v-else class="no-data">
            <el-empty description="暂无位置数据" :image-size="80">
              <el-button type="primary" @click="refreshData">刷新数据</el-button>
            </el-empty>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 状态统计 -->
    <el-row :gutter="20" class="dashboard-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Clock /></el-icon>
              <span>状态监控</span>
            </div>
          </template>
          <div class="status-info">
            <div class="status-item">
              <span class="label">数据状态：</span>
              <el-tag :type="dataStatus.type">{{ dataStatus.text }}</el-tag>
            </div>
            <div class="status-item">
              <span class="label">最后更新：</span>
              <span>{{ lastUpdateText }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>系统信息</span>
            </div>
          </template>
          <div class="system-info">
            <div class="info-item">
              <label>页面加载时间：</label>
              <span>{{ pageLoadTime }}</span>
            </div>
            <div class="info-item">
              <label>API状态：</label>
              <el-tag :type="apiStatus.type">{{ apiStatus.text }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useLocationStore } from '@/stores/location'
import { RefreshRight, Location, Clock, InfoFilled } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

export default {
  name: 'Dashboard',
  components: {
    RefreshRight,
    Location,
    Clock,
    InfoFilled
  },
  setup() {
    const locationStore = useLocationStore()
    const loading = ref(false)

    // 计算属性
    const formattedLatestLocation = computed(() => locationStore.formattedLatestLocation)

    const dataStatus = computed(() => {
      if (locationStore.error) {
        return { type: 'danger', text: '数据异常' }
      }
      if (!locationStore.latestLocation) {
        return { type: 'warning', text: '暂无数据' }
      }

      const location = locationStore.latestLocation
      const now = Date.now()
      const updateTime = location.ts_server || location.ts_client
      const diffMinutes = (now - updateTime) / (1000 * 60)

      if (diffMinutes < 30) {
        return { type: 'success', text: '正常' }
      } else if (diffMinutes < 60) {
        return { type: 'warning', text: '稍旧' }
      } else {
        return { type: 'danger', text: '过时' }
      }
    })

    const lastUpdateText = computed(() => {
      if (!locationStore.latestLocation) return '无'
      const location = locationStore.latestLocation
      const updateTime = location.ts_server || location.ts_client
      return dayjs(updateTime).format('MM-DD HH:mm:ss')
    })

    const apiStatus = computed(() => {
      return locationStore.error ? { type: 'danger', text: '异常' } : { type: 'success', text: '正常' }
    })

    const pageLoadTime = computed(() => {
      return dayjs().format('MM-DD HH:mm:ss')
    })

    // 方法
    const refreshData = async () => {
      loading.value = true
      try {
        await locationStore.fetchLatestLocation()
      } finally {
        loading.value = false
      }
    }

    const openInGaodeMap = () => {
      if (!formattedLatestLocation.value) {
        ElMessage.warning('暂无位置数据')
        return
      }

      const location = locationStore.latestLocation
      const url = `https://uri.amap.com/marker?position=${location.lng},${location.lat}&name=位置标记`

      window.open(url, '_blank')
    }

    // 初始化
    onMounted(() => {
      refreshData()
    })

    return {
      loading,
      formattedLatestLocation,
      dataStatus,
      lastUpdateText,
      apiStatus,
      pageLoadTime,
      refreshData,
      openInGaodeMap
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.dashboard-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.location-info {
  width: 100%;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.info-item label {
  font-weight: bold;
  color: #666;
  min-width: 80px;
  margin-right: 8px;
}

.status-info, .system-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-item .label {
  font-weight: bold;
  color: #666;
}

</style>
