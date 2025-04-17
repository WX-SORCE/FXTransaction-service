package com.alxy.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;

/**
 * WebSocket 配置类，用于配置 Spring WebSocket 和 STOMP 消息代理，
 * 同时处理跨域资源共享（CORS）问题。
 */
@Configuration
// 启用 WebSocket 消息代理功能
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理，定义消息的发送和接收规则。
     * @param config 消息代理配置对象
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的内存消息代理，客户端可以订阅以 /topic 开头的目的地来接收广播消息
        config.enableSimpleBroker("/topic");
        // 设置应用程序的目的地前缀，客户端发送消息到以 /app 开头的目的地
        // 这些消息会被路由到相应的控制器方法进行处理
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 注册 STOMP 端点，客户端可以通过这些端点建立 WebSocket 连接。
     * @param registry STOMP 端点注册对象
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 添加一个 STOMP 端点，客户端可以通过该端点建立 WebSocket 连接
        registry.addEndpoint("/ws")
                // 设置允许跨域访问的源，这里允许来自 http://localhost:3000 和 http://127.0.0.1:3000 的请求
                .setAllowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
                // 使用 SockJS 作为后备方案，以支持不支持 WebSocket 的浏览器
                .withSockJS();
    }

    /**
     * 创建并配置 CORS 过滤器，用于处理跨域请求。
     * @return 配置好的 CORS 过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建一个新的 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 设置允许访问的源，这里允许来自 http://localhost:3000 和 http://127.0.0.1:3000 的请求
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        // 允许所有请求头，即客户端可以携带任何请求头进行请求
        config.addAllowedHeader("*");
        // 允许所有请求方法，如 GET、POST、PUT、DELETE 等
        config.addAllowedMethod("*");
        // 允许客户端在跨域请求中携带凭证（如 cookies、HTTP 认证等）
        config.setAllowCredentials(true);

        // 创建一个基于 URL 的 CORS 配置源，将 CORS 配置应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 注册 CORS 配置，将其应用到所有路径
        source.registerCorsConfiguration("/**", config);

        // 创建并返回一个 CORS 过滤器，用于处理跨域请求
        return new CorsFilter(source);
    }
}