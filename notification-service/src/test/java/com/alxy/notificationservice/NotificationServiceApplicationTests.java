//package com.alxy.notificationservice;
//
//import com.alxy.notificationservice.entity.Notification;
//import com.alxy.notificationservice.service.NotificationService;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//@SpringBootTest
//class NotificationServiceApplicationTests {
//    @Autowired
//    private NotificationService notificationService;
//
//    @Test
//    public void testSaveNotification() {
//        notificationService.saveNotification("10001000000123456", "TRADE_STATUS", "交易已执行");
//        List<Notification> notifications = notificationService.getNotifications("10001000000123456", "TRADE_STATUS");
//        Assertions.assertEquals(1, notifications.size());
//    }
//}

package com.alxy.notificationservice;

import com.alxy.notificationservice.kafka.KafkaConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class NotificationServiceApplicationTests {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaConsumer kafkaConsumer;

    @Test
    public void testKafkaAndWebSocket() throws Exception {
        // 构建交易状态变更消息
        Map<String, Object> event = new HashMap<>();
        event.put("clientId", "10001000000123456");
        event.put("tradeId", "T0001");
        event.put("status", "已执行");

        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(event);

        // 发送消息到 Kafka 主题
        kafkaTemplate.send("trade-notifications", message);

        // 等待一段时间，让 Kafka 消费者有足够的时间处理消息
        Thread.sleep(5000);
    }
}
