<template>
  <div id="app">
    <el-container style="height: 100vh">
      <!-- 侧边栏 -->
      <el-aside width="200px" style="background-color: #f0f2f5">
        <div style="padding: 20px; text-align: center; border-bottom: 1px solid #e6e6e6">
          <h3>位置追踪系统</h3>
        </div>
        <el-menu
          :default-active="activeIndex"
          class="el-menu-vertical-demo"
          @select="handleSelect"
          style="border-right: none"
        >
        <el-menu-item index="dashboard">
          <el-icon><Monitor /></el-icon>
          <span>最新位置</span>
        </el-menu-item>
        <el-menu-item index="tracks">
          <el-icon><List /></el-icon>
          <span>位置记录</span>
        </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { Monitor, MapLocation } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'App',
  components: {
    Monitor,
    MapLocation
  },
  setup() {
    const router = useRouter()
    const activeIndex = ref('dashboard')

    const handleSelect = (key) => {
      router.push(`/${key}`)
    }

    return {
      activeIndex,
      handleSelect
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.el-menu-vertical-demo {
  border-right: none;
}

.el-menu-item {
  height: 48px;
  line-height: 48px;
}
</style>
