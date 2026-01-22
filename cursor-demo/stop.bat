@echo off
REM Netty HTTP 服务器停止脚本 (Windows)
REM 使用方法: stop.bat [port]
REM 默认端口: 8080

setlocal

REM 设置默认端口
set PORT=8080
if not "%~1"=="" set PORT=%~1

echo ========================================
echo   Netty HTTP 服务器停止脚本
echo ========================================
echo.

REM 查找占用指定端口的进程
echo [信息] 正在查找运行在端口 %PORT% 的进程...

REM 使用 netstat 查找占用端口的进程
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do (
    set PID=%%a
    goto :found
)

echo [信息] 未找到运行在端口 %PORT% 的进程
pause
exit /b 0

:found
echo [信息] 找到进程 ID: %PID%
echo [信息] 正在停止服务器...

REM 尝试优雅停止（发送 Ctrl+C 信号）
taskkill /PID %PID% /T >nul 2>&1
if %errorlevel% equ 0 (
    echo [成功] 服务器已停止
) else (
    REM 如果优雅停止失败，强制终止
    echo [警告] 优雅停止失败，尝试强制终止...
    taskkill /F /PID %PID% /T >nul 2>&1
    if %errorlevel% equ 0 (
        echo [成功] 服务器已强制停止
    ) else (
        echo [错误] 无法停止服务器，可能需要管理员权限
    )
)

echo.
pause
