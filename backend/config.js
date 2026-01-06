// 配置文件
require('dotenv').config();

module.exports = {
  server: {
    port: process.env.PORT || 3001,
    env: process.env.NODE_ENV || 'development'
  },

  database: {
    host: process.env.DB_HOST || 'localhost',
    port: process.env.DB_PORT || 3306,
    name: process.env.DB_NAME || 'location_tracker',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || ''
  },

  api: {
    token: process.env.API_TOKEN || 'your_bearer_token_here',
    rateLimit: {
      windowMs: parseInt(process.env.API_RATE_LIMIT_WINDOW_MS) || 60000, // 1分钟
      max: parseInt(process.env.API_RATE_LIMIT_MAX_REQUESTS) || 10 // 每分钟最多10次请求
    }
  },

  logging: {
    level: process.env.LOG_LEVEL || 'info',
    file: process.env.LOG_FILE || 'logs/app.log'
  }
};
