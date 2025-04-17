package com.alxy.notificationservice.listener;

import com.alxy.notificationservice.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import java.util.Timer;
import java.util.TimerTask;

@Component
public class WebSocketEventListener {

    @Autowired
    private WebSocketService webSocketService;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        System.out.println("✅ WebSocket 已连接，sessionId: " + sessionId);

        // 延迟发送防止客户端尚未订阅
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                webSocketService.sendMessageToFrontend("🔔 通知服务已上线");
            }
        }, 1000); // 延迟 1 秒推送
    }
}
