// 数据库连接模块
const mysql = require('mysql2/promise');
const config = require('../config');

class Database {
  constructor() {
    this.pool = null;
  }

  async connect() {
    try {
      this.pool = mysql.createPool({
        host: config.database.host,
        port: config.database.port,
        database: config.database.name,
        user: config.database.user,
        password: config.database.password,
        waitForConnections: true,
        connectionLimit: 10,
        queueLimit: 0,
        timezone: '+08:00', // 东八区
        dateStrings: true
      });

      // 测试连接
      const connection = await this.pool.getConnection();
      console.log('✅ 数据库连接成功');
      connection.release();
      return true;
    } catch (error) {
      console.error('❌ 数据库连接失败:', error.message);
      return false;
    }
  }

  async query(sql, params = []) {
    try {
      const [rows, fields] = await this.pool.execute(sql, params);
      return { success: true, data: rows, fields };
    } catch (error) {
      console.error('数据库查询错误:', error);
      return { success: false, error: error.message };
    }
  }

  async close() {
    if (this.pool) {
      await this.pool.end();
      console.log('数据库连接已关闭');
    }
  }
}

module.exports = new Database();
