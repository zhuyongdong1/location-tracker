// PM2配置文件
module.exports = {
  apps: [{
    name: 'location-tracker-backend',
    script: 'server.js',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '1G',
    env: {
      NODE_ENV: 'production',
      PORT: 3001
    },
    error_file: './logs/pm2-error.log',
    out_file: './logs/pm2-out.log',
    log_file: './logs/pm2-combined.log',
    time: true,
    // 优雅关闭超时时间
    kill_timeout: 5000,
    // 重启延迟
    restart_delay: 4000,
    // 最大重启次数
    max_restarts: 10,
    // 重启时间窗口
    min_uptime: '10s'
  }]
};
