# 位置追踪API设计文档

## 📋 API概览

**基础信息：**
- **域名**：使用您已备案的域名
- **协议**：HTTPS（强制）
- **版本**：v1
- **基础路径**：`/api/v1/location`
- **鉴权方式**：Bearer Token

## 🔐 鉴权方案

### 方案选择：固定Bearer Token（推荐）

**原因：**
- 自用场景，无需复杂动态token管理
- 安全性足够（HTTPS传输 + 固定token）
- 实现简单，维护成本低

**Token生成规则：**
```javascript
// 生成32位随机token
const token = crypto.randomBytes(32).toString('hex');
// 示例：a1b2c3d4e5f678901234567890abcdef1234567890abcdef
```

**使用方式：**
```
Authorization: Bearer a1b2c3d4e5f678901234567890abcdef1234567890abcdef
```

## 📡 API接口清单

### 1. 位置数据上报 `POST /api/v1/location/push`

**用途：** Android客户端上报位置数据

**请求头：**
```
Content-Type: application/json
Authorization: Bearer <TOKEN>
```

**请求体：**
```json
{
  "device_id": "android_abc123",
  "ts_client": 1704518400000,
  "lat": 39.90420000,
  "lng": 116.40740000,
  "accuracy_m": 15.50,
  "provider": "gps",
  "battery_pct": 85,
  "remark": "正常定位"
}
```

**字段说明：**
- `device_id`: 设备唯一标识（必需）
- `ts_client`: 客户端采集时间戳，毫秒（必需）
- `lat`: 纬度，WGS84坐标系（必需）
- `lng`: 经度，WGS84坐标系（必需）
- `accuracy_m`: 定位精度，米（必需）
- `provider`: 定位提供者，"gps"或"network"（可选）
- `battery_pct`: 设备电量百分比，0-100（可选）
- `remark`: 备注信息（可选）

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 12345,
    "ts_server": 1704518400500
  }
}
```

**错误响应：**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "data": null
}
```

---

### 2. 获取最新位置 `GET /api/v1/location/latest`

**用途：** 获取最新一条位置记录

**请求头：**
```
Authorization: Bearer <TOKEN>
```

**查询参数：**
- `device_id`: 可选，指定设备ID，不传则返回所有设备的最新位置

**示例请求：**
```
GET /api/v1/location/latest?device_id=android_abc123
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 12345,
    "user_id": 1,
    "device_id": "android_abc123",
    "ts_client": 1704518400000,
    "ts_server": 1704518400500,
    "lat": 39.90420000,
    "lng": 116.40740000,
    "accuracy_m": 15.50,
    "provider": "gps",
    "battery_pct": 85,
    "remark": null
  }
}
```

---

### 3. 获取历史轨迹 `GET /api/v1/location/list`

**用途：** 获取指定时间范围内的位置记录列表

**请求头：**
```
Authorization: Bearer <TOKEN>
```

**查询参数：**
- `from`: 开始时间戳，毫秒（必需）
- `to`: 结束时间戳，毫秒（必需）
- `device_id`: 设备ID（可选，默认所有设备）
- `limit`: 返回记录数量上限，默认100，最大1000

**示例请求：**
```
GET /api/v1/location/list?from=1704432000000&to=1704518400000&limit=500
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 48,
    "list": [
      {
        "id": 12345,
        "user_id": 1,
        "device_id": "android_abc123",
        "ts_client": 1704518400000,
        "ts_server": 1704518400500,
        "lat": 39.90420000,
        "lng": 116.40740000,
        "accuracy_m": 15.50,
        "provider": "gps",
        "battery_pct": 85
      }
      // ... 更多记录
    ]
  }
}
```

---

### 4. 删除位置数据 `POST /api/v1/location/delete`

**用途：** 删除指定时间范围内的位置数据

**请求头：**
```
Content-Type: application/json
Authorization: Bearer <TOKEN>
```

**请求体：**
```json
{
  "from": 1704432000000,
  "to": 1704518400000,
  "device_id": "android_abc123"  // 可选，不传则删除所有设备
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deleted_count": 48
  }
}
```

## 🚦 错误码定义

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式 |
| 401 | 未授权 | 检查token是否正确 |
| 403 | 权限不足 | 确认用户权限 |
| 429 | 请求过于频繁 | 等待后重试 |
| 500 | 服务器内部错误 | 联系管理员 |

## 🔒 安全措施

### 传输安全
- **HTTPS强制**：所有接口必须通过HTTPS访问
- **证书验证**：使用有效SSL证书

### 访问控制
- **Token验证**：每个请求都验证Bearer Token
- **IP白名单**：可选，仅允许指定IP段访问
- **请求频率限制**：每分钟最多10次请求（可配置）

### 数据安全
- **参数验证**：严格验证输入参数类型和范围
- **SQL注入防护**：使用参数化查询
- **敏感信息脱敏**：日志中不记录完整token

## 📊 性能考虑

### 查询优化
- **分页查询**：list接口支持limit参数，避免一次性返回过多数据
- **时间索引**：数据库使用时间戳索引，提升查询性能
- **缓存策略**：可考虑缓存最新位置数据

### 并发处理
- **数据库连接池**：避免连接耗尽
- **异步处理**：位置上报采用异步写入，提升响应速度

## 🧪 测试用例

### Push接口测试
```bash
# 成功上报
curl -X POST "https://yourdomain.com/api/v1/location/push" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "device_id": "test_device",
    "ts_client": 1704518400000,
    "lat": 39.90420000,
    "lng": 116.40740000,
    "accuracy_m": 10.00
  }'
```

### 查询接口测试
```bash
# 获取最新位置
curl -H "Authorization: Bearer YOUR_TOKEN" \
  "https://yourdomain.com/api/v1/location/latest"
```

---

*API版本：v1.0 | 更新日期：2026年1月6日*
