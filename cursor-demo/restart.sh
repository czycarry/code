#!/bin/bash
# Netty HTTP 服务器重启脚本 (Linux/Mac)
# 使用方法: ./restart.sh [port]
# 默认端口: 8080

# 设置默认端口
PORT=${1:-8080}

echo "========================================"
echo "  Netty HTTP 服务器重启脚本"
echo "========================================"
echo ""

# 停止服务器
echo "[信息] 正在停止服务器..."
./stop.sh $PORT >/dev/null 2>&1

# 等待进程完全停止
sleep 2

# 启动服务器
echo "[信息] 正在启动服务器..."
./start.sh $PORT
