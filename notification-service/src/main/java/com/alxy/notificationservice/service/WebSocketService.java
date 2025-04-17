package com.alxy.notificationservice.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendMessageToFrontend(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // 将消息封装成 JSON 对象
            String jsonMessage = objectMapper.writeValueAsString(new MessageWrapper(message));
            messagingTemplate.convertAndSend("/topic/messages", jsonMessage);
            System.out.println("将消息封装成 JSON 对象" + jsonMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // 定义一个简单的消息包装类
    private static class MessageWrapper {
        private String message;

        public MessageWrapper(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}