@echo off
REM Netty HTTP 服务器启动脚本 (Windows) - 使用 JAR 文件
REM 使用方法: start-jar.bat [port]
REM 默认端口: 8080

setlocal

REM 设置默认端口
set PORT=8080
if not "%~1"=="" set PORT=%~1

echo ========================================
echo   Netty HTTP 服务器启动脚本 (JAR)
echo ========================================
echo.

REM 检查 Java 是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java，请先安装 JDK 11 或更高版本
    pause
    exit /b 1
)

REM 检查 JAR 文件是否存在
if not exist "target\netty-http-server-1.0.0.jar" (
    echo [信息] JAR 文件不存在，正在打包...
    call mvn clean package -q
    if %errorlevel% neq 0 (
        echo [错误] 项目打包失败
        pause
        exit /b 1
    )
)

echo [信息] 正在启动服务器，端口: %PORT%
echo [信息] 访问地址: http://localhost:%PORT%
echo [信息] 按 Ctrl+C 停止服务器
echo.

REM 启动服务器
java -jar target\netty-http-server-1.0.0.jar %PORT%

pause
