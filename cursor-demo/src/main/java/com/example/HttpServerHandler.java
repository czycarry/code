package com.example;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * HTTP 请求处理器（API 和路由处理）
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        HttpMethod method = request.method();
        
        // 移除查询参数
        if (uri.contains("?")) {
            uri = uri.substring(0, uri.indexOf("?"));
        }
        
        logger.info("收到请求: {} {}", method, uri);

        FullHttpResponse response;
        
        // 检查是否是静态资源请求
        if (isStaticResource(uri)) {
            response = handleStaticResource(uri);
        } else {
            // 处理 API 请求
            if ("/".equals(uri)) {
                // 重定向到首页
                response = createRedirectResponse("/static/index.html");
            } else if ("/hello".equals(uri) && method == HttpMethod.GET) {
                response = handleHello();
            } else if ("/api/data".equals(uri) && method == HttpMethod.GET) {
                response = handleApiData();
            } else if ("/api/echo".equals(uri) && method == HttpMethod.POST) {
                response = handleEcho(request);
            } else {
                response = handleNotFound();
            }
        }

        // 设置通用响应头
        if (!response.headers().contains(CONTENT_TYPE)) {
            response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        }
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONNECTION, "keep-alive");
        
        // 发送响应
        ctx.writeAndFlush(response);
    }

    /**
     * 判断是否是静态资源请求
     */
    private boolean isStaticResource(String uri) {
        return uri.startsWith("/static/") || 
               uri.endsWith(".html") || 
               uri.endsWith(".css") || 
               uri.endsWith(".js") || 
               uri.endsWith(".png") || 
               uri.endsWith(".jpg") || 
               uri.endsWith(".jpeg") || 
               uri.endsWith(".gif") || 
               uri.endsWith(".svg") || 
               uri.endsWith(".ico");
    }

    /**
     * 处理静态资源
     */
    private FullHttpResponse handleStaticResource(String uri) {
        // 如果请求的是根路径，返回 index.html
        if ("/".equals(uri)) {
            uri = "/static/index.html";
        } else if (!uri.startsWith("/static/")) {
            uri = "/static" + uri;
        }

        String resourcePath = uri;
        
        try {
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            
            if (inputStream == null) {
                return createNotFoundResponse();
            }
            
            // 兼容 Java 8+ 的读取方式
            byte[] content = readAllBytes(inputStream);
            inputStream.close();
            
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    OK,
                    Unpooled.copiedBuffer(content)
            );
            
            // 设置 Content-Type
            String contentType = getContentType(uri);
            response.headers().set(CONTENT_TYPE, contentType);
            
            // 设置缓存头
            if (uri.endsWith(".html")) {
                response.headers().set(CACHE_CONTROL, "no-cache");
            } else {
                response.headers().set(CACHE_CONTROL, "public, max-age=3600");
            }
            
            return response;
        } catch (IOException e) {
            logger.error("读取静态文件失败: {}", resourcePath, e);
            return createNotFoundResponse();
        }
    }

    /**
     * 根据文件扩展名获取 Content-Type
     */
    private String getContentType(String uri) {
        if (uri.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        } else if (uri.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        } else if (uri.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        } else if (uri.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        } else if (uri.endsWith(".png")) {
            return "image/png";
        } else if (uri.endsWith(".jpg") || uri.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (uri.endsWith(".gif")) {
            return "image/gif";
        } else if (uri.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (uri.endsWith(".ico")) {
            return "image/x-icon";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * 创建重定向响应
     */
    private FullHttpResponse createRedirectResponse(String location) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                FOUND,
                Unpooled.EMPTY_BUFFER
        );
        response.headers().set(LOCATION, location);
        return response;
    }

    /**
     * 处理根路径请求
     */
    private FullHttpResponse handleRoot() {
        String content = "<html><head><title>Netty HTTP Server</title></head>" +
                "<body>" +
                "<h1>欢迎使用 Netty HTTP 服务器</h1>" +
                "<ul>" +
                "<li><a href='/hello'>Hello 接口</a></li>" +
                "<li><a href='/api/data'>数据接口</a></li>" +
                "<li>POST /api/echo - 回显接口</li>" +
                "</ul>" +
                "</body></html>";
        
        return createResponse(OK, content);
    }

    /**
     * 处理 /hello 请求
     */
    private FullHttpResponse handleHello() {
        String content = "<html><head><title>Hello</title></head>" +
                "<body><h1>Hello, World!</h1>" +
                "<p>这是来自 Netty HTTP 服务器的问候</p></body></html>";
        
        return createResponse(OK, content);
    }

    /**
     * 处理 /api/data 请求
     */
    private FullHttpResponse handleApiData() {
        String json = "{\"status\":\"success\",\"data\":{\"message\":\"这是 API 数据\",\"timestamp\":" +
                System.currentTimeMillis() + "}}";
        
        FullHttpResponse response = createResponse(OK, json);
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        return response;
    }

    /**
     * 处理 /api/echo 请求（POST）
     */
    private FullHttpResponse handleEcho(FullHttpRequest request) {
        String requestBody = request.content().toString(CharsetUtil.UTF_8);
        
        // 尝试解析为 JSON，如果是 JSON 则原样返回，否则作为字符串返回
        String json;
        try {
            // 验证是否是有效的 JSON
            if (requestBody.trim().startsWith("{") || requestBody.trim().startsWith("[")) {
                // 已经是 JSON 格式，直接包装返回
                json = "{\"status\":\"success\",\"echo\":" + requestBody + "}";
            } else {
                // 普通文本，作为字符串返回
                String escaped = requestBody.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t");
                json = "{\"status\":\"success\",\"echo\":\"" + escaped + "\"}";
            }
        } catch (Exception e) {
            // 如果处理失败，作为字符串返回
            String escaped = requestBody.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
            json = "{\"status\":\"success\",\"echo\":\"" + escaped + "\"}";
        }
        
        FullHttpResponse response = createResponse(OK, json);
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        return response;
    }

    /**
     * 处理 404 错误
     */
    private FullHttpResponse handleNotFound() {
        String content = "<html><head><title>404 Not Found</title></head>" +
                "<body><h1>404 - 页面未找到</h1>" +
                "<p>请求的资源不存在</p></body></html>";
        
        return createResponse(NOT_FOUND, content);
    }

    /**
     * 创建 404 响应（用于静态资源）
     */
    private FullHttpResponse createNotFoundResponse() {
        String content = "<html><head><title>404 Not Found</title></head>" +
                "<body><h1>404 - 文件未找到</h1>" +
                "<p>请求的静态资源不存在</p></body></html>";
        
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                NOT_FOUND,
                Unpooled.copiedBuffer(content, StandardCharsets.UTF_8)
        );
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        return response;
    }

    /**
     * 读取 InputStream 的所有字节（兼容 Java 8+）
     */
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    /**
     * 创建 HTTP 响应
     */
    private FullHttpResponse createResponse(HttpResponseStatus status, String content) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(content, StandardCharsets.UTF_8)
        );
        return response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("处理请求时发生异常", cause);
        ctx.close();
    }
}
