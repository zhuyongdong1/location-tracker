-- 位置追踪数据库初始化脚本
-- 执行时间：2026年1月6日

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `location_tracker`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `location_tracker`;

-- 创建位置日志表
CREATE TABLE IF NOT EXISTS `location_logs` (
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

-- 创建API请求日志表（用于调试和监控）
CREATE TABLE IF NOT EXISTS `api_logs` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `method` VARCHAR(8) NOT NULL,
  `endpoint` VARCHAR(256) NOT NULL,
  `device_id` VARCHAR(64) DEFAULT NULL,
  `ip_address` VARCHAR(45) NOT NULL,
  `user_agent` VARCHAR(512) DEFAULT NULL,
  `request_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `response_code` INT DEFAULT NULL,
  `processing_time_ms` INT DEFAULT NULL,
  INDEX `idx_request_time` (`request_time`),
  INDEX `idx_device_ip` (`device_id`, `ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API请求日志表';

-- 数据清理存储过程（90天自动清理）
DELIMITER //

CREATE PROCEDURE IF NOT EXISTS `cleanup_old_data`()
BEGIN
  DECLARE cutoff_timestamp BIGINT;
  SET cutoff_timestamp = UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 90 DAY)) * 1000;

  -- 删除90天前的位置数据
  DELETE FROM location_logs WHERE ts_server < cutoff_timestamp;

  -- 删除30天前的API日志
  DELETE FROM api_logs WHERE request_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
END //

DELIMITER ;

-- 创建定时清理事件（每日凌晨2点执行）
CREATE EVENT IF NOT EXISTS `daily_cleanup`
ON SCHEDULE EVERY 1 DAY STARTS '2026-01-07 02:00:00'
DO
  CALL cleanup_old_data();

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;
