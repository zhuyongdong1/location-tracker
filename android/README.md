# 位置追踪Android应用

基于Kotlin开发的轻量级位置追踪应用，支持后台定时上报位置信息。

## 🚀 功能特性

- 📍 **自动定位**：每30分钟自动采集位置信息
- 🔄 **后台上报**：使用WorkManager实现稳定的后台任务
- 💾 **本地缓存**：支持离线缓存，网络恢复后自动补发
- 🔒 **隐私保护**：严格的权限控制和数据最小化
- ⚙️ **控制面板**：一键开启/关闭上报，查看统计信息
- 📊 **状态监控**：实时显示上报状态和统计数据

## 🛠️ 技术栈

- **语言**：Kotlin
- **架构**：MVVM + Repository
- **数据库**：Room
- **网络**：Retrofit + OkHttp
- **后台任务**：WorkManager
- **UI**：Material Design 3
- **依赖注入**：手动依赖注入

## 📱 安装要求

- **Android版本**：API 21+ (Android 5.0)
- **权限**：
  - 精确定位权限 (ACCESS_FINE_LOCATION)
  - 粗略定位权限 (ACCESS_COARSE_LOCATION)
  - 后台定位权限 (ACCESS_BACKGROUND_LOCATION)

## 🔧 配置说明

### 1. API配置

在 `ApiClient.kt` 中修改服务器地址：

```kotlin
private const val BASE_URL = "https://yourdomain.com/" // 替换为你的域名
```

### 2. Token配置

在 `LocationRepository.kt` 中设置Bearer Token：

```kotlin
// 替换为你的实际token
val response = apiService.pushLocation("Bearer your_token_here", request)
```

## 📁 项目结构

```
app/src/main/java/com/locationtracker/
├── data/
│   ├── api/           # 网络层
│   ├── database/      # 数据库层
│   ├── model/         # 数据模型
│   └── repository/    # 仓库层
├── service/           # 定位服务
├── ui/                # UI层
│   ├── main/         # 主界面
│   ├── privacy/      # 隐私协议
│   └── permission/   # 权限引导
├── utils/            # 工具类
└── worker/           # WorkManager
```

## 🔐 隐私与安全

### 数据采集
- 仅采集经纬度、精度、时间戳等必要信息
- 不收集通讯录、照片、设备IMEI等敏感数据
- 首次运行强制显示隐私协议

### 权限控制
- 分步申请权限，清晰说明用途
- 支持用户随时开启/关闭位置上报
- 提供一键清空本地数据功能

### 数据传输
- 使用HTTPS加密传输
- Bearer Token认证
- API请求日志记录（仅用于调试）

## 🔄 工作流程

1. **首次启动**：显示隐私协议 → 用户同意 → 权限申请
2. **权限检查**：精确定位 + 后台定位权限
3. **定位采集**：WorkManager定时任务 → 获取位置 → 保存本地
4. **数据上报**：检查网络 → 上传到服务器 → 更新状态
5. **离线处理**：网络恢复后自动补发失败的数据

## ⚙️ 自定义配置

### 定位间隔
在 `LocationReportWorker.kt` 中修改：

```kotlin
PeriodicWorkRequestBuilder<LocationReportWorker>(
    30, TimeUnit.MINUTES // 修改间隔时间
)
```

### 数据清理
在 `LocationRepository.kt` 中修改清理周期：

```kotlin
val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
```

## 🐛 故障排除

### 常见问题

1. **定位失败**
   - 检查GPS是否开启
   - 确认定位权限已授权
   - 查看设备是否在室内环境

2. **上报失败**
   - 检查网络连接
   - 验证服务器地址和Token
   - 查看应用是否被系统杀死

3. **后台不工作**
   - 检查电池优化设置
   - 确认自启动权限
   - 查看WorkManager状态

## 📊 性能优化

- **省电设计**：使用平衡精度模式
- **批量处理**：支持批量上报减少网络请求
- **智能重试**：指数退避策略避免频繁重试
- **数据压缩**：最小化数据字段减少存储和传输

## 🚀 构建和部署

### 开发环境
```bash
# 克隆项目
git clone <repository-url>
cd location-tracker/android

# 打开Android Studio
# 导入项目，等待Gradle同步完成
```

### 构建APK
1. 在Android Studio中选择 `Build` → `Build Bundle(s)/APK(s)` → `Build APK(s)`
2. 等待构建完成，APK文件位于 `app/build/outputs/apk/debug/`

### 安装到设备
```bash
# 通过ADB安装
adb install app/build/outputs/apk/debug/app-debug.apk

# 或直接在Android Studio中运行
```

## 📝 更新日志

### v1.0.0 (2026-01-06)
- ✅ 基础定位和上报功能
- ✅ WorkManager后台任务
- ✅ Room本地数据缓存
- ✅ 隐私协议和权限引导
- ✅ 统计信息和控制面板

## 🤝 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 📄 许可证

本项目仅供个人学习和家庭使用，请勿用于商业用途。

## 📞 联系方式

如有问题，请检查日志文件或联系开发者。
