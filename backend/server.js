// 主服务器文件
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const config = require('./config');
const db = require('./database/db');
const logger = require('./utils/logger');
const locationRoutes = require('./routes/location');

const app = express();

// 安全中间件
app.use(helmet({
  contentSecurityPolicy: false, // 允许内联脚本（如果需要）
  crossOriginEmbedderPolicy: false
}));

// CORS配置
app.use(cors({
  origin: process.env.NODE_ENV === 'production' ?
    ['https://yourdomain.com'] : // 生产环境只允许指定域名
    true, // 开发环境允许所有
  credentials: true
}));

// 请求频率限制
const limiter = rateLimit({
  windowMs: config.api.rateLimit.windowMs,
  max: config.api.rateLimit.max,
  message: {
    code: 429,
    message: '请求过于频繁，请稍后再试',
    data: null
  },
  standardHeaders: true,
  legacyHeaders: false,
  // 跳过健康检查端点
  skip: (req) => req.path === '/health'
});

app.use('/api/', limiter);

// 请求体解析
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// 请求日志中间件
app.use((req, res, next) => {
  const startTime = Date.now();

  // 记录响应完成
  res.on('finish', () => {
    const processingTime = Date.now() - startTime;
    logger.logApiRequest(req, res, processingTime);
  });

  next();
});

// 路由
app.use('/api/v1/location', locationRoutes);

// 健康检查端点
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    version: '1.0.0'
  });
});

// 404处理
app.use('*', (req, res) => {
  res.status(404).json({
    code: 404,
    message: '接口不存在',
    data: null
  });
});

// 错误处理中间件
app.use((error, req, res, next) => {
  logger.error('未捕获的错误', {
    error: error.message,
    stack: error.stack,
    url: req.url,
    method: req.method
  });

  res.status(500).json({
    code: 500,
    message: '服务器内部错误',
    data: null
  });
});

// 启动服务器
async function startServer() {
  try {
    // 连接数据库
    const dbConnected = await db.connect();
    if (!dbConnected) {
      logger.error('数据库连接失败，服务器启动中止');
      process.exit(1);
    }

    // 启动HTTP服务器
    const server = app.listen(config.server.port, () => {
      logger.info(`🚀 服务器启动成功`, {
        port: config.server.port,
        env: config.server.env,
        api_base: `/api/v1/location`
      });
    });

    // 优雅关闭
    process.on('SIGTERM', async () => {
      logger.info('收到SIGTERM信号，开始优雅关闭...');
      server.close(async () => {
        await db.close();
        logger.info('服务器已关闭');
        process.exit(0);
      });
    });

    process.on('SIGINT', async () => {
      logger.info('收到SIGINT信号，开始优雅关闭...');
      server.close(async () => {
        await db.close();
        logger.info('服务器已关闭');
        process.exit(0);
      });
    });

  } catch (error) {
    logger.error('服务器启动失败', { error: error.message });
    process.exit(1);
  }
}

// 启动服务器
startServer();
