# 数据字段最小化清单

## 📊 位置数据字段定义

### 核心必需字段

| 字段名 | 类型 | 说明 | 示例值 | 必要性 |
|--------|------|------|--------|--------|
| `user_id` | INT | 用户ID（固定为1） | `1` | ✅ 必需 |
| `device_id` | VARCHAR(64) | 设备唯一标识 | `android_abc123` | ✅ 必需 |
| `ts_client` | BIGINT | 客户端采集时间戳（毫秒） | `1704518400000` | ✅ 必需 |
| `ts_server` | BIGINT | 服务端入库时间戳（毫秒） | `1704518400500` | ✅ 必需 |
| `lat` | DECIMAL(10,8) | 纬度（WGS84坐标系） | `39.90420000` | ✅ 必需 |
| `lng` | DECIMAL(11,8) | 经度（WGS84坐标系） | `116.40740000` | ✅ 必需 |
| `accuracy_m` | DECIMAL(6,2) | 定位精度（米） | `15.50` | ✅ 必需 |

### 可选辅助字段

| 字段名 | 类型 | 说明 | 示例值 | 必要性 |
|--------|------|------|--------|--------|
| `provider` | VARCHAR(16) | 定位提供者 | `gps` 或 `network` | ⚠️ 可选 |
| `battery_pct` | TINYINT | 设备电量百分比 | `85` | ⚠️ 可选 |
| `remark` | VARCHAR(256) | 备注信息 | `室内定位` | ⚠️ 可选 |

## 🗄️ 数据库表结构

### location_logs 表

```sql
CREATE TABLE `location_logs` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `user_id` INT NOT NULL DEFAULT 1 COMMENT '用户ID（固定为1）',
  `device_id` VARCHAR(64) NOT NULL COMMENT '设备唯一标识',
  `ts_client` BIGINT NOT NULL COMMENT '客户端采集时间戳（毫秒）',
  `ts_server` BIGINT NOT NULL COMMENT '服务端入库时间戳（毫秒）',
  `lat` DECIMAL(10,8) NOT NULL COMMENT '纬度（WGS84）',
  `lng` DECIMAL(11,8) NOT NULL COMMENT '经度（WGS84）',
  `accuracy_m` DECIMAL(6,2) NOT NULL COMMENT '定位精度（米）',
  `provider` VARCHAR(16) DEFAULT NULL COMMENT '定位提供者',
  `battery_pct` TINYINT DEFAULT NULL COMMENT '电量百分比',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_ts_server` (`ts_server`),
  INDEX `idx_user_device_ts` (`user_id`, `device_id`, `ts_server`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='位置日志表';
```

## 🔍 数据查询索引说明

### 主要查询场景
1. **最新位置查询**：`SELECT * FROM location_logs WHERE user_id=? ORDER BY ts_server DESC LIMIT 1`
2. **时间范围查询**：`SELECT * FROM location_logs WHERE user_id=? AND ts_server BETWEEN ? AND ? ORDER BY ts_server DESC`
3. **设备状态检查**：`SELECT * FROM location_logs WHERE device_id=? ORDER BY ts_server DESC LIMIT 1`

### 索引设计原则
- **时间索引**：`idx_ts_server` 用于时间范围查询
- **复合索引**：`idx_user_device_ts` 用于用户+设备+时间的组合查询
- **避免过度索引**：仅为实际查询场景创建索引

## 📏 数据约束

### 数值范围限制
- **纬度**：-90.0 ~ 90.0
- **经度**：-180.0 ~ 180.0
- **精度**：0.0 ~ 1000.0米（超过1000米的数据视为无效）
- **电量**：0 ~ 100

### 数据验证规则
```javascript
// 客户端数据验证
const validateLocationData = (data) => {
  return (
    data.lat >= -90 && data.lat <= 90 &&
    data.lng >= -180 && data.lng <= 180 &&
    data.accuracy_m >= 0 && data.accuracy_m <= 1000 &&
    data.ts_client > 0 &&
    data.device_id && data.device_id.length <= 64
  );
};
```

## 🧹 数据清理策略

### 自动清理
- **保留期限**：90天
- **清理频率**：每日凌晨2点执行
- **清理SQL**：
  ```sql
  DELETE FROM location_logs
  WHERE ts_server < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 90 DAY)) * 1000;
  ```

### 手动清理接口
- 支持按时间范围删除
- 支持按设备ID删除
- 支持清空所有数据

---

*字段定义版本：v1.0 | 更新日期：2026年1月6日*
