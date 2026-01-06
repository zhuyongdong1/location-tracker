# 位置追踪系统Web前端

基于Vue 3 + Element Plus的轻量级Web界面，用于查看位置数据和轨迹。

## 🚀 功能特性

- 📊 **实时仪表盘**：显示最新位置、状态监控、系统信息
- 🗺️ **轨迹查看**：时间范围选择、地图展示、轨迹列表
- 🔍 **数据查询**：支持按时间范围查询历史轨迹
- 🗑️ **数据管理**：支持删除指定时间范围的数据
- 📱 **响应式设计**：适配桌面和移动端设备

## 🛠️ 技术栈

- **Vue 3**：组合式API + `<script setup>`
- **Vue Router 4**：页面路由管理
- **Pinia**：状态管理
- **Element Plus**：UI组件库
- **Axios**：HTTP客户端
- **Vite**：构建工具
- **Day.js**：时间处理

## 📦 安装和运行

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖
```bash
cd location-tracker/frontend
npm install
```

### 开发环境运行
```bash
npm run dev
```

访问 `http://localhost:3000`

### 构建生产版本
```bash
npm run build
```

### 预览构建结果
```bash
npm run preview
```

## ⚙️ 配置说明

### API配置
在 `src/api/location.js` 中修改：

```javascript
// 修改为你的域名
const API_BASE_URL = '/api/v1/location'

// 修改为你的Bearer Token
const API_TOKEN = 'your_bearer_token_here'
```

### 代理配置
开发环境通过Vite代理解决跨域问题，见 `vite.config.js`：

```javascript
proxy: {
  '/api': {
    target: 'https://yourdomain.com',
    changeOrigin: true,
    secure: true
  }
}
```

## 📁 项目结构

```
frontend/src/
├── api/                    # API接口
│   └── location.js         # 位置相关API
├── stores/                 # Pinia状态管理
│   └── location.js         # 位置数据store
├── views/                  # 页面组件
│   ├── Dashboard.vue       # 仪表盘页面
│   └── Tracks.vue          # 轨迹查看页面
├── App.vue                 # 根组件
└── main.js                 # 应用入口
```

## 🔌 API接口

### 获取最新位置
```javascript
GET /api/v1/location/latest
Authorization: Bearer <token>
```

### 获取历史轨迹
```javascript
GET /api/v1/location/list?from=<timestamp>&to=<timestamp>&limit=100
Authorization: Bearer <token>
```

### 删除位置数据
```javascript
POST /api/v1/location/delete
Authorization: Bearer <token>
Content-Type: application/json

{
  "from": <timestamp>,
  "to": <timestamp>,
  "device_id": "<device_id>"
}
```

## 🗺️ 地图集成

当前版本使用占位符显示地图。要集成真实地图：

### 高德地图集成
1. 申请高德地图Key
2. 安装高德地图JS SDK
3. 在组件中初始化地图实例

```javascript
// 安装依赖
npm install @amap/amap-jsapi-loader --save

// 在组件中使用
import AMapLoader from '@amap/amap-jsapi-loader'

const initMap = async () => {
  const AMap = await AMapLoader.load({
    key: 'your_amap_key',
    version: '2.0'
  })

  const map = new AMap.Map('map-container', {
    zoom: 15,
    center: [lng, lat]
  })
}
```

## 📊 页面说明

### 仪表盘 (Dashboard)
- **最新位置**：显示最近一次的位置信息
- **状态监控**：数据更新状态、最后更新时间
- **系统信息**：页面加载时间、API连接状态

### 轨迹查看 (Tracks)
- **时间筛选**：支持自定义时间范围、快捷选择今天/昨天
- **轨迹统计**：显示轨迹点数、时间范围、平均精度
- **地图展示**：轨迹线路和点位标记
- **列表展示**：详细的位置数据表格

## 🔧 开发指南

### 添加新页面
1. 在 `src/views/` 创建Vue组件
2. 在 `src/router/index.js` 添加路由
3. 在侧边栏菜单中添加导航项

### 添加新API
1. 在 `src/api/` 添加API方法
2. 在store中调用API并更新状态
3. 在组件中处理数据展示

### 状态管理
使用Pinia进行状态管理：

```javascript
import { useLocationStore } from '@/stores/location'

const locationStore = useLocationStore()

// 获取数据
await locationStore.fetchLatestLocation()

// 监听状态变化
watch(() => locationStore.latestLocation, (newLocation) => {
  // 处理位置更新
})
```

## 🚀 部署说明

### Nginx配置
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass https://your-backend-server;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Docker部署
```dockerfile
FROM node:16-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 🐛 故障排除

### 常见问题

1. **API请求失败**
   - 检查API_TOKEN是否正确
   - 确认后端服务是否正常运行
   - 查看浏览器网络面板的请求详情

2. **地图不显示**
   - 检查地图SDK是否正确加载
   - 确认地图Key是否有效
   - 查看浏览器控制台错误信息

3. **数据不更新**
   - 检查网络连接
   - 确认后端数据库是否有新数据
   - 尝试手动刷新页面

## 📈 性能优化

- **组件懒加载**：路由级别的代码分割
- **API缓存**：减少重复请求
- **虚拟滚动**：大数据量表格优化
- **CDN加速**：静态资源分发

## 🤝 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

---

如有问题，请检查浏览器控制台日志或联系开发者。
