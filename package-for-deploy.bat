@echo off
echo ========================================
echo 位置追踪系统 - 部署包打包脚本
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] 打包后端代码...
if exist backend.zip del backend.zip
powershell -Command "Compress-Archive -Path 'backend\*' -DestinationPath 'backend.zip' -Force" 2>nul
if %errorlevel% neq 0 (
    echo 后端打包失败，尝试手动复制...
    if not exist backend-manual (
        mkdir backend-manual
        xcopy backend\* backend-manual\ /E /I /H /Y /EXCLUDE:exclude.txt
    )
)

echo [2/3] 打包前端代码...
if exist frontend-dist.zip del frontend-dist.zip
if exist frontend\dist (
    powershell -Command "Compress-Archive -Path 'frontend\dist\*' -DestinationPath 'frontend-dist.zip' -Force" 2>nul
) else (
    echo 前端dist目录不存在，请先运行: cd frontend && npm run build
)

echo [3/3] 创建部署说明...
echo 部署包已准备完成！
echo.
echo 文件列表:
if exist backend.zip echo   ✅ backend.zip - 后端代码
if exist frontend-dist.zip echo   ✅ frontend-dist.zip - 前端代码
if exist backend-manual echo   ✅ backend-manual\ - 后端代码(手动复制)
echo.
echo 请将这些文件上传到服务器:
echo   后端 → /root/
echo   前端 → /www/wwwroot/location.ulbooks.cn/
echo.
echo 上传完成后运行服务器部署命令！
echo.

pause
