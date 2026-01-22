#!/bin/bash
# Netty HTTP 服务器停止脚本 (Linux/Mac)
# 使用方法: ./stop.sh [port]
# 默认端口: 8080

# 设置默认端口
PORT=${1:-8080}

echo "========================================"
echo "  Netty HTTP 服务器停止脚本"
echo "========================================"
echo ""

# 查找占用指定端口的进程
echo "[信息] 正在查找运行在端口 $PORT 的进程..."

# 使用 lsof 或 netstat 查找占用端口的进程
PID=""

# 优先使用 lsof（Mac 和部分 Linux 系统）
if command -v lsof &> /dev/null; then
    PID=$(lsof -ti:$PORT 2>/dev/null)
fi

# 如果 lsof 不可用，使用 netstat + awk（Linux）
if [ -z "$PID" ] && command -v netstat &> /dev/null; then
    PID=$(netstat -tlnp 2>/dev/null | grep ":$PORT " | awk '{print $7}' | cut -d'/' -f1 | head -n1)
fi

# 如果 netstat 不可用，使用 ss（现代 Linux 系统）
if [ -z "$PID" ] && command -v ss &> /dev/null; then
    PID=$(ss -tlnp 2>/dev/null | grep ":$PORT " | grep -oP 'pid=\K[0-9]+' | head -n1)
fi

if [ -z "$PID" ]; then
    echo "[信息] 未找到运行在端口 $PORT 的进程"
    exit 0
fi

echo "[信息] 找到进程 ID: $PID"
echo "[信息] 正在停止服务器..."

# 尝试优雅停止（发送 SIGTERM 信号）
kill -TERM $PID 2>/dev/null

# 等待进程结束（最多等待 5 秒）
for i in {1..5}; do
    if ! kill -0 $PID 2>/dev/null; then
        echo "[成功] 服务器已优雅停止"
        exit 0
    fi
    sleep 1
done

# 如果进程仍在运行，强制终止
if kill -0 $PID 2>/dev/null; then
    echo "[警告] 优雅停止超时，尝试强制终止..."
    kill -KILL $PID 2>/dev/null
    sleep 1
    
    if ! kill -0 $PID 2>/dev/null; then
        echo "[成功] 服务器已强制停止"
    else
        echo "[错误] 无法停止服务器，可能需要 root 权限"
        exit 1
    fi
else
    echo "[成功] 服务器已停止"
fi

exit 0
