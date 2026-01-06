# 📋 位置追踪系统 - 精简版准备清单

**当前状态**: 已完成大幅优化，系统从2000行代码精简到800行
**目标**: 1天内完成部署，1天内完成测试

---

## 🚀 精简版 - 今天1小时内完成

### 1. 🔑 生成固定Token（5分钟）
```bash
# 生成32位随机token
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```
**保存这个token，后端、前端、Android都要用这个**

### 2. 🗄️ 数据库准备（10分钟）
```sql
-- 登录MySQL
mysql -u root -p

-- 执行以下命令
CREATE DATABASE location_tracker;
-- 如果需要创建用户（可选）
-- GRANT ALL ON location_tracker.* TO 'user'@'localhost';
```

### 3. 🌐 域名检查（5分钟）
- ✅ 域名已备案
- ✅ SSL证书有效
- ✅ DNS解析正确
- ✅ 可以访问 https://yourdomain.com

---

## 🛠️ 开发环境检查（今天30分钟）

### 4. 📱 Android Studio
- ✅ Android Studio 2022+ 已安装
- ✅ 可以打开项目并构建

### 5. 🔧 Node.js环境
- ✅ Node.js 16+ 已安装
- ✅ npm 8+ 已安装
- ✅ 可以运行 `node -e "console.log('test')"`

---

## 📱 测试手机确认（今天完成）

### 6. 🎯 手机信息
**告诉我你妹妹的手机：**
- **品牌**: [小米/华为/OPPO/vivo/三星/其他]
- **型号**: [例如: 小米14, OPPO Reno10]
- **Android版本**: [例如: Android 13]

**精简版优势**: 不需要复杂的省电策略适配，WorkManager会自动处理

---

## ⚙️ 配置文件设置（今天1小时）

### 7. 🔧 配置三个地方
**1. 后端配置** (`backend/config.js`):
```javascript
module.exports = {
  database: {
    host: 'localhost',
    name: 'location_tracker',
    user: 'root',  // 或你创建的用户
    password: 'your_password'
  },
  api: {
    token: '刚才生成的32位token'
  }
};
```

**2. 前端配置** (`frontend/src/api/location.js`):
```javascript
const API_BASE_URL = 'https://yourdomain.com/api/v1/location'
const API_TOKEN = '刚才生成的32位token'
```

**3. Android配置** (`android/app/src/main/java/.../ApiClient.kt`):
```kotlin
private const val BASE_URL = "https://yourdomain.com/"
```

---

## 🚀 部署和测试（明天完成）

### 8. 🔄 一键部署脚本
```bash
# 1. 初始化数据库表
cd location-tracker/backend
mysql -u root -p location_tracker < database/init.sql

# 2. 启动后端（开发模式）
npm install
npm start

# 3. 部署前端
cd ../frontend
npm install
npm run build

# 4. 复制前端文件到Web目录
# cp -r dist/* /path/to/your/web/root/
```

### 9. 📱 APK构建
```bash
# Android Studio中：
# 1. 打开 location-tracker/android
# 2. 等待Gradle同步
# 3. Build → Build Bundle(s)/APK(s) → Build APK(s)
# 4. 安装到手机测试
```

### 10. 🧪 快速测试清单
- [ ] 后端启动成功 (访问 http://localhost:3001/health)
- [ ] 前端页面正常 (https://yourdomain.com)
- [ ] Android App安装成功
- [ ] 隐私协议显示
- [ ] 位置上报开关工作

---

## 📊 24小时测试计划（后天完成）

### 11. 🎯 精简测试场景
- **正常使用**: 手机日常使用12小时
- **后台运行**: 锁屏状态12小时
- **网络切换**: WiFi和移动网络切换
- **重启测试**: 手机重启后App自动恢复

### 12. 📈 验收标准（家庭用）
- [ ] **位置上报**: 24小时内至少15-20次成功上报
- [ ] **后台稳定**: 锁屏状态下不被系统杀死
- [ ] **网络恢复**: 网络断开后恢复时能继续上报
- [ ] **电池消耗**: 控制在合理范围内（<25%/天）
- [ ] **用户体验**: 开关简单，信息清晰

### 13. 📝 测试记录
```
时间: YYYY-MM-DD HH:mm
状态: 正常/后台/重启
网络: WiFi/4G/断网
结果: 成功上报/失败/无响应
备注: 任何异常情况
```

---

## 🚨 如果出问题

### 14. 🔧 快速排查
- **后端不启动**: 检查数据库连接和端口占用
- **前端空白**: 检查API_BASE_URL和Token配置
- **App崩溃**: 查看Android Studio的Logcat日志
- **定位失败**: 检查手机GPS权限和网络状态

### 15. 📞 技术支持
- **后端日志**: `backend/logs/app.log`
- **前端调试**: 浏览器F12 → Console
- **Android调试**: Android Studio → Logcat

---

## ✅ 完成检查清单

- [ ] Token已生成并记录
- [ ] 数据库已创建
- [ ] 域名SSL正常
- [ ] 配置文件已更新
- [ ] 后端可启动
- [ ] 前端可构建
- [ ] APK可安装

**检查通过就开始24小时测试！**

---

*精简版时间表: 今天准备(2小时) + 明天部署(2小时) + 后天测试(24小时) = 总计26小时*
