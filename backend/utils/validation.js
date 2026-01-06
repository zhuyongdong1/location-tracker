// 数据验证模块
const Joi = require('joi');

// 位置数据上报验证规则
const locationPushSchema = Joi.object({
  device_id: Joi.string()
    .min(1)
    .max(64)
    .required()
    .messages({
      'string.empty': 'device_id不能为空',
      'string.max': 'device_id长度不能超过64字符',
      'any.required': 'device_id是必需的'
    }),

  ts_client: Joi.number()
    .integer()
    .min(1609459200000) // 2021-01-01 00:00:00
    .max(Date.now() + 86400000) // 允许1天内的未来时间
    .required()
    .messages({
      'number.base': 'ts_client必须是数字',
      'number.min': 'ts_client时间戳无效（太早）',
      'number.max': 'ts_client时间戳无效（未来时间）',
      'any.required': 'ts_client是必需的'
    }),

  lat: Joi.number()
    .min(-90)
    .max(90)
    .precision(8)
    .required()
    .messages({
      'number.base': 'lat必须是数字',
      'number.min': 'lat纬度无效（太小）',
      'number.max': 'lat纬度无效（太大）',
      'any.required': 'lat是必需的'
    }),

  lng: Joi.number()
    .min(-180)
    .max(180)
    .precision(8)
    .required()
    .messages({
      'number.base': 'lng必须是数字',
      'number.min': 'lng经度无效（太小）',
      'number.max': 'lng经度无效（太大）',
      'any.required': 'lng是必需的'
    }),

  accuracy_m: Joi.number()
    .min(0)
    .max(1000)
    .precision(2)
    .required()
    .messages({
      'number.base': 'accuracy_m必须是数字',
      'number.min': 'accuracy_m不能小于0',
      'number.max': 'accuracy_m不能大于1000米',
      'any.required': 'accuracy_m是必需的'
    }),

  provider: Joi.string()
    .valid('gps', 'network')
    .optional()
    .messages({
      'any.only': 'provider必须是"gps"或"network"'
    }),

  battery_pct: Joi.number()
    .integer()
    .min(0)
    .max(100)
    .optional()
    .messages({
      'number.base': 'battery_pct必须是整数',
      'number.min': 'battery_pct不能小于0',
      'number.max': 'battery_pct不能大于100'
    }),

  remark: Joi.string()
    .max(256)
    .optional()
    .messages({
      'string.max': 'remark长度不能超过256字符'
    })
});

// 时间范围查询验证规则
const timeRangeSchema = Joi.object({
  from: Joi.number()
    .integer()
    .min(1609459200000)
    .required()
    .messages({
      'number.base': 'from必须是数字',
      'any.required': 'from是必需的'
    }),

  to: Joi.number()
    .integer()
    .min(1609459200000)
    .when('from', {
      is: Joi.exist(),
      then: Joi.number().greater(Joi.ref('from')).messages({
        'number.greater': 'to必须大于from'
      })
    })
    .required()
    .messages({
      'number.base': 'to必须是数字',
      'any.required': 'to是必需的'
    }),

  device_id: Joi.string()
    .min(1)
    .max(64)
    .optional(),

  limit: Joi.number()
    .integer()
    .min(1)
    .max(1000)
    .default(100)
    .optional()
});

// 验证函数
const validateLocationPush = (data) => {
  return locationPushSchema.validate(data, { abortEarly: false });
};

const validateTimeRange = (data) => {
  return timeRangeSchema.validate(data, { abortEarly: false });
};

module.exports = {
  validateLocationPush,
  validateTimeRange
};
