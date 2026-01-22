#!/bin/bash
# Netty HTTP 服务器启动脚本 (Linux/Mac)
# 使用方法: ./start.sh [port]
# 默认端口: 8080

# 设置默认端口
PORT=${1:-8080}

echo "========================================"
echo "  Netty HTTP 服务器启动脚本"
echo "========================================"
echo ""

# 检查 Java 是否安装
if ! command -v java &> /dev/null; then
    echo "[错误] 未检测到 Java，请先安装 JDK 11 或更高版本"
    exit 1
fi

# 检查 Maven 是否安装
if ! command -v mvn &> /dev/null; then
    echo "[错误] 未检测到 Maven，请先安装 Maven 3.6 或更高版本"
    exit 1
fi

# 检查 Java 版本
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ] 2>/dev/null; then
    echo "[错误] Java 版本过低，需要 JDK 11 或更高版本"
    exit 1
fi

echo "[信息] 正在编译项目..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "[错误] 项目编译失败"
    exit 1
fi

echo "[信息] 正在启动服务器，端口: $PORT"
echo "[信息] 访问地址: http://localhost:$PORT"
echo "[信息] 按 Ctrl+C 停止服务器"
echo ""

# 设置环境变量并启动服务器
export MAVEN_OPTS="-Xmx512m -Xms256m"
mvn exec:java -Dexec.mainClass="com.example.HttpServer" -Dexec.args="$PORT" -q
