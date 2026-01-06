// 位置服务模块
const db = require('../database/db');
const logger = require('../utils/logger');

class LocationService {
  // 插入位置数据
  async insertLocation(data) {
    const sql = `
      INSERT INTO location_logs
      (user_id, device_id, ts_client, ts_server, lat, lng, accuracy_m, provider, battery_pct, remark)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;

    const ts_server = Date.now();
    const params = [
      data.user_id || 1,
      data.device_id,
      data.ts_client,
      ts_server,
      data.lat,
      data.lng,
      data.accuracy_m,
      data.provider || null,
      data.battery_pct || null,
      data.remark || null
    ];

    const result = await db.query(sql, params);

    if (result.success) {
      const insertId = result.data.insertId;
      logger.info('位置数据插入成功', { id: insertId, device_id: data.device_id });
      return { success: true, id: insertId, ts_server };
    } else {
      logger.error('位置数据插入失败', { error: result.error, device_id: data.device_id });
      return { success: false, error: result.error };
    }
  }

  // 获取最新位置
  async getLatestLocation(deviceId = null) {
    let sql, params;

    if (deviceId) {
      sql = `
        SELECT * FROM location_logs
        WHERE device_id = ?
        ORDER BY ts_server DESC
        LIMIT 1
      `;
      params = [deviceId];
    } else {
      sql = `
        SELECT * FROM location_logs
        ORDER BY ts_server DESC
        LIMIT 1
      `;
      params = [];
    }

    const result = await db.query(sql, params);

    if (result.success && result.data.length > 0) {
      return { success: true, data: result.data[0] };
    } else if (result.success && result.data.length === 0) {
      return { success: true, data: null };
    } else {
      logger.error('获取最新位置失败', { error: result.error, device_id: deviceId });
      return { success: false, error: result.error };
    }
  }

  // 获取历史轨迹
  async getLocationList(from, to, deviceId = null, limit = 100) {
    let sql, params;

    if (deviceId) {
      sql = `
        SELECT * FROM location_logs
        WHERE ts_server BETWEEN ? AND ? AND device_id = ?
        ORDER BY ts_server DESC
        LIMIT ?
      `;
      params = [from, to, deviceId, limit];
    } else {
      sql = `
        SELECT * FROM location_logs
        WHERE ts_server BETWEEN ? AND ?
        ORDER BY ts_server DESC
        LIMIT ?
      `;
      params = [from, to, limit];
    }

    const result = await db.query(sql, params);

    if (result.success) {
      // 获取总数
      const countSql = deviceId ?
        `SELECT COUNT(*) as total FROM location_logs WHERE ts_server BETWEEN ? AND ? AND device_id = ?` :
        `SELECT COUNT(*) as total FROM location_logs WHERE ts_server BETWEEN ? AND ?`;

      const countParams = deviceId ? [from, to, deviceId] : [from, to];
      const countResult = await db.query(countSql, countParams);

      const total = countResult.success ? countResult.data[0].total : 0;

      return {
        success: true,
        data: {
          total,
          list: result.data
        }
      };
    } else {
      logger.error('获取历史轨迹失败', { error: result.error, device_id: deviceId, from, to });
      return { success: false, error: result.error };
    }
  }


  // 获取设备统计信息
  async getDeviceStats(deviceId, hours = 24) {
    const cutoffTime = Date.now() - (hours * 60 * 60 * 1000);

    const sql = `
      SELECT
        COUNT(*) as total_count,
        AVG(accuracy_m) as avg_accuracy,
        MIN(ts_server) as first_report,
        MAX(ts_server) as last_report,
        COUNT(DISTINCT DATE(FROM_UNIXTIME(ts_server/1000))) as active_days
      FROM location_logs
      WHERE device_id = ? AND ts_server >= ?
    `;

    const result = await db.query(sql, [deviceId, cutoffTime]);

    if (result.success && result.data.length > 0) {
      return { success: true, data: result.data[0] };
    } else {
      return { success: false, error: result.error || '设备无数据' };
    }
  }
}

module.exports = new LocationService();
