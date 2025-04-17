package com.alxy.notificationservice.controller;


import com.alxy.notificationservice.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 声明这是一个 RESTful 控制器，处理 HTTP 请求
@RestController
public class MessageController {

    // 自动注入 WebSocketService
    @Autowired
    private WebSocketService webSocketService;

    /**
     * 触发向后端发送消息的接口
     * @return 消息发送结果
     */
    @GetMapping("/send-message")
    public String sendMessage() {
        // 调用 WebSocketService 的方法发送消息
        webSocketService.sendMessageToFrontend("这是来自后端的消息！");
        return "消息已发送";
    }
}