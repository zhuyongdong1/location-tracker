<template>
  <div class="tracks">
    <div class="page-header">
      <h2>位置记录</h2>
      <el-button type="primary" @click="refreshData" :loading="loading">
        <el-icon><RefreshRight /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 快速时间选择 -->
    <el-card class="filter-card" shadow="never">
      <el-space wrap>
        <el-button @click="setTodayRange" :type="timeFilter === 'today' ? 'primary' : ''">
          今天
        </el-button>
        <el-button @click="setYesterdayRange" :type="timeFilter === 'yesterday' ? 'primary' : ''">
          昨天
        </el-button>
        <el-button @click="setLast24hRange" :type="timeFilter === '24h' ? 'primary' : ''">
          最近24小时
        </el-button>
        <el-button @click="setLast7dRange" :type="timeFilter === '7d' ? 'primary' : ''">
          最近7天
        </el-button>
      </el-space>
    </el-card>

    <!-- 位置列表 -->
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><List /></el-icon>
          <span>位置记录 ({{ locationList.length }}条)</span>
          <el-button
            v-if="locationList.length > 0"
            type="primary"
            size="small"
            @click="openInGaodeMap"
            style="margin-left: auto"
          >
            <el-icon><MapLocation /></el-icon>
            在高德地图查看
          </el-button>
        </div>
      </template>

      <el-table
        :data="locationList"
        style="width: 100%;"
        stripe
        :max-height="600"
      >
        <el-table-column
          prop="formattedTime"
          label="时间"
          width="160"
          sortable
        />
        <el-table-column
          label="坐标"
          width="180"
        >
          <template #default="scope">
            <div class="coordinate-cell">
              <div>纬度: {{ scope.row.lat.toFixed(6) }}</div>
              <div>经度: {{ scope.row.lng.toFixed(6) }}</div>
              <el-button
                size="mini"
                type="text"
                @click="copyCoordinate(scope.row)"
              >
                复制
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="accuracy_m"
          label="精度(米)"
          width="100"
          sortable
        >
          <template #default="scope">
            {{ scope.row.accuracy_m.toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="provider"
          label="来源"
          width="80"
        >
          <template #default="scope">
            {{ scope.row.provider === 'gps' ? 'GPS' : scope.row.provider === 'network' ? '网络' : '-' }}
          </template>
        </el-table-column>
        <el-table-column
          label="状态"
          width="80"
        >
          <template #default="scope">
            <el-tag
              :type="scope.row.uploadStatus === 1 ? 'success' : scope.row.uploadStatus === 2 ? 'danger' : 'warning'"
              size="small"
            >
              {{ scope.row.uploadStatus === 1 ? '成功' : scope.row.uploadStatus === 2 ? '失败' : '待上传' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="locationList.length === 0" class="no-data">
        <el-empty description="暂无位置记录" :image-size="80">
          <el-button type="primary" @click="refreshData">刷新数据</el-button>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useLocationStore } from '@/stores/location'
import { RefreshRight, MapLocation, List } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

export default {
  name: 'Tracks',
  components: {
    RefreshRight,
    MapLocation,
    List
  },
  setup() {
    const locationStore = useLocationStore()

    // 响应式数据
    const loading = ref(false)
    const timeFilter = ref('today')
    const currentRange = ref([0, 0])

    // 计算属性
    const locationList = computed(() => {
      return locationStore.locationList
        .slice() // 创建副本避免修改原数组
        .sort((a, b) => (b.ts_server || b.ts_client) - (a.ts_server || a.ts_client)) // 按时间倒序
        .map(item => ({
          ...item,
          formattedTime: dayjs(item.ts_server || item.ts_client).format('MM-DD HH:mm:ss')
        }))
    })

    // 方法
    const setTodayRange = () => {
      timeFilter.value = 'today'
      const today = new Date()
      const startOfDay = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0).getTime()
      const endOfDay = Math.min(
        new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 59, 59).getTime(),
        Date.now()
      )
      currentRange.value = [startOfDay, endOfDay]
      loadTracks()
    }

    const setYesterdayRange = () => {
      timeFilter.value = 'yesterday'
      const yesterday = new Date(Date.now() - 24 * 60 * 60 * 1000)
      const startOfDay = new Date(yesterday.getFullYear(), yesterday.getMonth(), yesterday.getDate(), 0, 0, 0).getTime()
      const endOfDay = new Date(yesterday.getFullYear(), yesterday.getMonth(), yesterday.getMonth(), yesterday.getDate(), 23, 59, 59).getTime()
      currentRange.value = [startOfDay, endOfDay]
      loadTracks()
    }

    const setLast24hRange = () => {
      timeFilter.value = '24h'
      const endTime = Date.now()
      const startTime = endTime - 24 * 60 * 60 * 1000
      currentRange.value = [startTime, endTime]
      loadTracks()
    }

    const setLast7dRange = () => {
      timeFilter.value = '7d'
      const endTime = Date.now()
      const startTime = endTime - 7 * 24 * 60 * 60 * 1000
      currentRange.value = [startTime, endTime]
      loadTracks()
    }

    const loadTracks = async () => {
      const [from, to] = currentRange.value
      if (!from || !to) return

      loading.value = true
      try {
        await locationStore.fetchLocationList(from, to, null, 200) // 最多200条记录
      } catch (error) {
        console.error('Load tracks error:', error)
      } finally {
        loading.value = false
      }
    }

    const refreshData = async () => {
      await loadTracks()
    }

    const copyCoordinate = (location) => {
      const text = `${location.lat.toFixed(6)},${location.lng.toFixed(6)}`
      navigator.clipboard?.writeText(text).then(() => {
        ElMessage.success('坐标已复制到剪贴板')
      }).catch(() => {
        ElMessage.warning('复制失败，请手动复制')
      })
    }

    const openInGaodeMap = () => {
      if (locationList.value.length === 0) {
        ElMessage.warning('暂无位置数据')
        return
      }

      // 使用最新的位置点
      const latestLocation = locationList.value[0]
      const url = `https://uri.amap.com/marker?position=${latestLocation.lng},${latestLocation.lat}&name=位置标记`

      window.open(url, '_blank')
    }

    // 初始化
    onMounted(() => {
      setTodayRange() // 默认显示今天的数据
    })

    return {
      loading,
      timeFilter,
      locationList,
      setTodayRange,
      setYesterdayRange,
      setLast24hRange,
      setLast7dRange,
      refreshData,
      copyCoordinate,
      openInGaodeMap
    }
  }
}
</script>

<style scoped>
.tracks {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.coordinate-cell {
  line-height: 1.4;
}

.coordinate-cell .el-button {
  padding: 2px 6px;
  font-size: 12px;
}

.no-data {
  text-align: center;
  padding: 40px 0;
}
</style>
