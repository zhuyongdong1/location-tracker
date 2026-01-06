# 🚀 部署到 location.ulbooks.cn 指南

**域名**: `location.ulbooks.cn`
**API Token**: `733385e53ac29e11b3f1a9f5fe59e0485af2e47fbd0411ba7c815cfee9864bea`

---

## 📋 部署前准备

### ✅ 已完成的配置
- [x] API Token: `733385e53ac29e11b3f1a9f5fe59e0485af2e47fbd0411ba7c815cfee9864bea`
- [x] 数据库: `location_tracker` + 表结构
- [x] 前端构建: dist目录已生成
- [x] Android APK: 可构建

### 🔧 需要在服务器上做的

#### 1. 上传后端代码
```bash
# 将 location-tracker/backend/ 目录上传到服务器
scp -r location-tracker/backend/ user@your-server:/path/to/app/
```

#### 2. 安装依赖并启动后端
```bash
cd /path/to/app/backend
npm install
npm start

# 或者用PM2 (推荐生产环境)
npm install -g pm2
pm2 start ecosystem.config.js --name location-tracker
pm2 save
pm2 startup
```

#### 3. 配置Nginx反向代理
```nginx
# /etc/nginx/sites-available/location-tracker
server {
    listen 80;
    server_name location.ulbooks.cn;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name location.ulbooks.cn;

    # SSL证书
    ssl_certificate /path/to/ssl/location.ulbooks.cn.crt;
    ssl_certificate_key /path/to/ssl/location.ulbooks.cn.key;

    # API代理到后端
    location /api/ {
        proxy_pass http://localhost:3001;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
}
```

#### 4. 启用Nginx配置
```bash
sudo ln -s /etc/nginx/sites-available/location-tracker /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## 🧪 部署后验证

### 1. 后端API测试
```bash
# 健康检查
curl https://location.ulbooks.cn/health

# API接口测试
curl -H "Authorization: Bearer 733385e53ac29e11b3f1a9f5fe59e0485af2e47fbd0411ba7c815cfee9864bea" \
     https://location.ulbooks.cn/api/v1/location/latest
```

### 2. 前端页面测试
打开浏览器访问: `https://location.ulbooks.cn`

应该能看到:
- 位置追踪系统界面
- 仪表盘和轨迹查看页面

### 3. Android App测试
- 重新构建APK (配置已更新为域名)
- 安装到手机
- 测试位置上报功能

---

## 🔧 故障排除

### 如果API调用失败
1. 检查后端服务是否运行: `ps aux | grep node`
2. 检查Nginx配置: `sudo nginx -t`
3. 查看后端日志: `pm2 logs location-tracker`

### 如果前端页面空白
1. 检查dist文件是否正确部署
2. 检查Nginx静态文件配置
3. 浏览器F12查看控制台错误

### 如果Android连接失败
1. 检查域名SSL证书是否有效
2. 确认网络能访问域名
3. 查看Android Logcat日志

---

## 📊 部署状态检查清单

- [ ] 域名DNS解析正确
- [ ] SSL证书有效
- [ ] 后端服务运行正常
- [ ] Nginx配置正确
- [ ] 前端文件部署完成
- [ ] API接口可访问
- [ ] 网页能正常加载
- [ ] Android能连接API

**部署完成后就可以开始24小时测试了！** 🎉
