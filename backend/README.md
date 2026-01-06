# 位置追踪系统后端

基于 Node.js + Express + MySQL 的轻量级位置追踪API服务。

## 🚀 快速开始

### 1. 环境要求

- Node.js >= 16.0.0
- MySQL >= 8.0
- Nginx (生产环境)

### 2. 安装依赖

```bash
cd location-tracker/backend
npm install
```

### 3. 数据库配置

```bash
# 登录MySQL
mysql -u root -p

# 执行数据库初始化脚本
source database/init.sql
```

### 4. 环境配置

复制并修改配置文件：

```bash
cp .env.example .env
```

编辑 `.env` 文件：

```env
# 服务器配置
PORT=3001
NODE_ENV=development

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=location_tracker
DB_USER=root
DB_PASSWORD=your_password

# API配置
API_TOKEN=your_bearer_token_here
API_RATE_LIMIT_WINDOW_MS=60000
API_RATE_LIMIT_MAX_REQUESTS=10

# 日志配置
LOG_LEVEL=info
LOG_FILE=logs/app.log
```

### 5. 启动服务

```bash
# 开发模式
npm run dev

# 生产模式
npm start
```

## 📡 API文档

### 认证方式

所有API接口都需要Bearer Token认证：

```
Authorization: Bearer your_token_here
```

### 接口列表

#### 1. 上报位置数据
```
POST /api/v1/location/push
```

请求体示例：
```json
{
  "device_id": "android_abc123",
  "ts_client": 1704518400000,
  "lat": 39.90420000,
  "lng": 116.40740000,
  "accuracy_m": 15.50,
  "provider": "gps",
  "battery_pct": 85
}
```

#### 2. 获取最新位置
```
GET /api/v1/location/latest?device_id=device123
```

#### 3. 获取历史轨迹
```
GET /api/v1/location/list?from=1704432000000&to=1704518400000&limit=100
```

#### 4. 删除位置数据
```
POST /api/v1/location/delete
```

请求体：
```json
{
  "from": 1704432000000,
  "to": 1704518400000,
  "device_id": "device123"
}
```

## 🏗️ 项目结构

```
backend/
├── config.js              # 配置文件
├── server.js              # 主服务器文件
├── database/
│   ├── db.js             # 数据库连接
│   └── init.sql          # 数据库初始化脚本
├── routes/
│   └── location.js       # 位置相关路由
├── services/
│   └── locationService.js # 位置服务逻辑
├── utils/
│   ├── logger.js         # 日志模块
│   └── validation.js     # 数据验证
└── package.json
```

## 🔧 配置说明

### 数据库配置

- **自动清理**：90天前的数据自动删除
- **索引优化**：时间戳和设备ID索引
- **字符集**：UTF8MB4，支持 emoji

### 安全配置

- **HTTPS强制**：生产环境必须使用HTTPS
- **请求频率限制**：每分钟最多10次请求
- **参数验证**：严格的输入验证
- **日志记录**：所有API请求记录IP、UA等信息

### 性能优化

- **连接池**：数据库连接池复用
- **异步处理**：位置上报异步写入
- **分页查询**：历史数据分页返回

## 🚀 部署指南

### 1. PM2部署

```bash
# 全局安装PM2
npm install -g pm2

# 创建PM2配置文件
cp ecosystem.config.js.example ecosystem.config.js

# 启动服务
pm2 start ecosystem.config.js

# 设置开机自启
pm2 startup
pm2 save
```

### 2. Nginx配置

参考 `nginx.conf` 文件配置反向代理。

### 3. SSL证书

使用 Let's Encrypt 或其他CA获取免费SSL证书：

```bash
certbot --nginx -d yourdomain.com
```

## 📊 监控和维护

### 日志查看

```bash
# 查看应用日志
pm2 logs location-tracker

# 查看Nginx日志
tail -f /var/log/nginx/location_access.log
```

### 数据库维护

```bash
# 手动清理过期数据
mysql -u root -p location_tracker -e "CALL cleanup_old_data();"

# 备份数据库
mysqldump -u root -p location_tracker > backup_$(date +%Y%m%d).sql
```

## 🐛 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查MySQL服务是否运行
   - 验证数据库配置是否正确

2. **API请求失败**
   - 确认Bearer Token是否正确
   - 检查请求参数格式

3. **性能问题**
   - 监控数据库查询性能
   - 检查服务器资源使用情况

## 📈 扩展计划

- [ ] 添加Redis缓存层
- [ ] 实现位置数据压缩存储
- [ ] 添加地理围栏功能
- [ ] 支持批量位置数据导入

## 📝 更新日志

### v1.0.0 (2026-01-06)
- ✅ 基础位置上报和查询功能
- ✅ Bearer Token认证
- ✅ 数据验证和错误处理
- ✅ 自动数据清理
- ✅ API请求日志记录

---

如有问题，请检查日志文件或联系管理员。
