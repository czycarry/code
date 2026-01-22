@echo off
REM Netty HTTP 服务器重启脚本 (Windows)
REM 使用方法: restart.bat [port]
REM 默认端口: 8080

setlocal

REM 设置默认端口
set PORT=8080
if not "%~1"=="" set PORT=%~1

echo ========================================
echo   Netty HTTP 服务器重启脚本
echo ========================================
echo.

REM 停止服务器
echo [信息] 正在停止服务器...
call stop.bat %PORT% >nul 2>&1

REM 等待进程完全停止
timeout /t 2 /nobreak >nul 2>&1

REM 启动服务器
echo [信息] 正在启动服务器...
call start.bat %PORT%
