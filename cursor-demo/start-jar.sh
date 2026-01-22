#!/bin/bash
# Netty HTTP 服务器启动脚本 (Linux/Mac) - 使用 JAR 文件
# 使用方法: ./start-jar.sh [port]
# 默认端口: 8080

# 设置默认端口
PORT=${1:-8080}

echo "========================================"
echo "  Netty HTTP 服务器启动脚本 (JAR)"
echo "========================================"
echo ""

# 检查 Java 是否安装
if ! command -v java &> /dev/null; then
    echo "[错误] 未检测到 Java，请先安装 JDK 11 或更高版本"
    exit 1
fi

# 检查 Java 版本
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ] 2>/dev/null; then
    echo "[错误] Java 版本过低，需要 JDK 11 或更高版本"
    exit 1
fi

# 检查 JAR 文件是否存在
if [ ! -f "target/netty-http-server-1.0.0.jar" ]; then
    echo "[信息] JAR 文件不存在，正在打包..."
    mvn clean package -q
    if [ $? -ne 0 ]; then
        echo "[错误] 项目打包失败"
        exit 1
    fi
fi

echo "[信息] 正在启动服务器，端口: $PORT"
echo "[信息] 访问地址: http://localhost:$PORT"
echo "[信息] 按 Ctrl+C 停止服务器"
echo ""

# 启动服务器
java -jar target/netty-http-server-1.0.0.jar "$PORT"
