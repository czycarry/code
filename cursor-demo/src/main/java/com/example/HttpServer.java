package com.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 Netty 的 HTTP 服务器
 *
 */
public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // 解析命令行参数
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) {
                    logger.error("端口号必须在 1-65535 之间，使用默认端口: {}", DEFAULT_PORT);
                    port = DEFAULT_PORT;
                }
            } catch (NumberFormatException e) {
                logger.error("无效的端口号: {}，使用默认端口: {}", args[0], DEFAULT_PORT);
            }
        }
        
        startServer(port);
    }
    
    private static void startServer(int port) {
        // Boss 线程组：用于接收客户端连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // Worker 线程组：用于处理 I/O 操作
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // HTTP 编解码器
                            ch.pipeline().addLast(new HttpServerCodec());
                            // HTTP 消息聚合器，将多个 HTTP 消息合并为一个完整的 FullHttpRequest 或 FullHttpResponse
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            // 自定义业务处理器
                            ch.pipeline().addLast(new HttpServerHandler());
                        }
                    });

            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("HTTP 服务器已启动，监听端口: {}", port);
            logger.info("访问地址: http://localhost:{}", port);

            // 等待服务器关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("服务器启动失败", e);
            Thread.currentThread().interrupt();
        } finally {
            // 优雅关闭线程组
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            logger.info("服务器已关闭");
        }
    }
}
