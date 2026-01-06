// 位置相关路由
const express = require('express');
const router = express.Router();
const locationService = require('../services/locationService');
const { validateLocationPush, validateTimeRange } = require('../utils/validation');
const config = require('../config');
const logger = require('../utils/logger');

// 中间件：Bearer Token验证
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

  if (!token) {
    return res.status(401).json({
      code: 401,
      message: '缺少访问令牌',
      data: null
    });
  }

  if (token !== config.api.token) {
    return res.status(401).json({
      code: 401,
      message: '无效的访问令牌',
      data: null
    });
  }

  next();
};

// POST /api/v1/location/push - 上报位置数据
router.post('/push', authenticateToken, async (req, res) => {
  const startTime = Date.now();

  try {
    // 数据验证
    const { error, value } = validateLocationPush(req.body);
    if (error) {
      logger.warn('位置数据验证失败', {
        errors: error.details.map(d => d.message),
        device_id: req.body.device_id
      });

      return res.status(400).json({
        code: 400,
        message: '请求参数错误: ' + error.details.map(d => d.message).join(', '),
        data: null
      });
    }

    // 插入数据库
    const result = await locationService.insertLocation(value);

    if (result.success) {
      res.json({
        code: 200,
        message: 'success',
        data: {
          id: result.id,
          ts_server: result.ts_server
        }
      });
    } else {
      logger.error('位置数据插入失败', { error: result.error });
      res.status(500).json({
        code: 500,
        message: '服务器内部错误',
        data: null
      });
    }

  } catch (error) {
    logger.error('位置上报处理异常', { error: error.message });
    res.status(500).json({
      code: 500,
      message: '服务器内部错误',
      data: null
    });
  } finally {
    // 记录API请求日志
    const processingTime = Date.now() - startTime;
    logger.logApiRequest(req, res, processingTime);
  }
});

// GET /api/v1/location/latest - 获取最新位置
router.get('/latest', authenticateToken, async (req, res) => {
  const startTime = Date.now();

  try {
    const { device_id } = req.query;

    const result = await locationService.getLatestLocation(device_id);

    if (result.success) {
      res.json({
        code: 200,
        message: 'success',
        data: result.data
      });
    } else {
      res.status(500).json({
        code: 500,
        message: '服务器内部错误',
        data: null
      });
    }

  } catch (error) {
    logger.error('获取最新位置异常', { error: error.message });
    res.status(500).json({
      code: 500,
      message: '服务器内部错误',
      data: null
    });
  } finally {
    const processingTime = Date.now() - startTime;
    logger.logApiRequest(req, res, processingTime);
  }
});

// GET /api/v1/location/list - 获取历史轨迹
router.get('/list', authenticateToken, async (req, res) => {
  const startTime = Date.now();

  try {
    const queryData = {
      from: parseInt(req.query.from),
      to: parseInt(req.query.to),
      device_id: req.query.device_id,
      limit: req.query.limit ? parseInt(req.query.limit) : 100
    };

    // 数据验证
    const { error } = validateTimeRange(queryData);
    if (error) {
      return res.status(400).json({
        code: 400,
        message: '请求参数错误: ' + error.details.map(d => d.message).join(', '),
        data: null
      });
    }

    const result = await locationService.getLocationList(
      queryData.from,
      queryData.to,
      queryData.device_id,
      queryData.limit
    );

    if (result.success) {
      res.json({
        code: 200,
        message: 'success',
        data: result.data
      });
    } else {
      res.status(500).json({
        code: 500,
        message: '服务器内部错误',
        data: null
      });
    }

  } catch (error) {
    logger.error('获取历史轨迹异常', { error: error.message });
    res.status(500).json({
      code: 500,
      message: '服务器内部错误',
      data: null
    });
  } finally {
    const processingTime = Date.now() - startTime;
    logger.logApiRequest(req, res, processingTime);
  }
});

module.exports = router;
