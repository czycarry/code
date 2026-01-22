@echo off
REM Netty HTTP 服务器启动脚本 (Windows)
REM 使用方法: start.bat [port]
REM 默认端口: 8080

setlocal

REM 设置默认端口
set PORT=8080
if not "%~1"=="" set PORT=%~1

echo ========================================
echo   Netty HTTP 服务器启动脚本
echo ========================================
echo.

REM 检查 Java 是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java，请先安装 JDK 11 或更高版本
    pause
    exit /b 1
)

REM 检查 Maven 是否安装
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Maven，请先安装 Maven 3.6 或更高版本
    pause
    exit /b 1
)

echo [信息] 正在编译项目...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo [错误] 项目编译失败
    pause
    exit /b 1
)

echo [信息] 正在启动服务器，端口: %PORT%
echo [信息] 访问地址: http://localhost:%PORT%
echo [信息] 按 Ctrl+C 停止服务器
echo.

REM 设置环境变量并启动服务器
set MAVEN_OPTS=-Xmx512m -Xms256m
mvn exec:java -Dexec.mainClass="com.example.HttpServer" -Dexec.args="%PORT%" -q

pause
