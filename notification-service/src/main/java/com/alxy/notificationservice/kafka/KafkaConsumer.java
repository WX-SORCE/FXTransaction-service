package com.alxy.notificationservice.kafka;

import com.alxy.notificationservice.repository.NotificationRepository;
import com.alxy.notificationservice.service.NotificationService;
import com.alxy.notificationservice.service.WebSocketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * Kafka 消费者服务类，负责监听并处理来自不同主题的通知事件
 */
@Service
public class KafkaConsumer {

    @Autowired
    private WebSocketService webSocketService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // 用于 JSON 反序列化
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    public KafkaConsumer(NotificationRepository notificationRepository,
                                SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 监听交易状态变更主题（trade-notifications），处理交易相关通知
     *
     * @param message Kafka 消息内容（JSON 字符串）
     */
    @KafkaListener(topics = "trade-notifications" ,groupId = "notification-group")
    public void handleTradeNotification(String message) {
        try {
            // 1. 解析消息内容
            Map<String, Object> event = parseMessage(message);

            // 2. 提取关键字段

            String tradeId = (String) event.get("transactionId");
            String status = (String) event.get("status");
            String action = (String) event.get("action");

            // 3. 构建通知内容（示例：动态生成可读文本）
            String content = String.format("您的 %s 交易 %s 状态已更新为：%s", action, tradeId, status);

//            // 4. 调用服务层保存通知到数据库
//            notificationService.saveNotification(clientId, "TRADE_STATUS", content);

            // 可选：触发实时推送（如 WebSocket 或短信）
            // pushToClient(clientId, content);
            String messages="用户，进行的"+ action +"操作,交易编号为:"+tradeId+"的交易，现状态为:"+status;
            // 5. 通过WebSocket推送到前端
            webSocketService.sendMessageToFrontend(messages);

            System.out.println("交易状态变更通知: " + content);

        } catch (JsonProcessingException e) {
            System.err.println("交易通知消息解析失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("处理交易通知时发生异常: " + e.getMessage());
        }
    }

    /**
     * 监听审批结果主题（approval-notifications），处理审批相关通知
     *
     * @param message Kafka 消息内容（JSON 字符串）
     */
    @KafkaListener(topics = "approval-notifications",groupId = "notification-group")
    public void handleApprovalNotification(String message) {
        try {
            // 1. 解析消息内容
            Map<String, Object> event = parseMessage(message);

            // 2. 提取关键字段
            String userId = (String) event.get("userId");
            String tradeId = (String) event.get("tradeId");
            String decision = (String) event.get("decision");
            String comment = (String) event.get("comment");

            // 3. 构建通知内容（示例：包含审批意见）
            String content = String.format(
                    "交易 %s 的审批结果为【%s】。审批意见：%s",
                    tradeId, decision, comment
            );

            // 4. 调用服务层保存通知到数据库
            notificationService.saveNotification(userId, "APPROVAL_RESULT", content);

            String messages="用户编号为:"+userId+"的用户，交易编号为:"+tradeId+"的审批，结果变为:"+decision+"请处理！";

            // 5. 通过WebSocket推送到前端
            webSocketService.sendMessageToFrontend(messages);

        } catch (JsonProcessingException e) {
            System.err.println("审批通知消息解析失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("处理审批通知时发生异常: " + e.getMessage());
        }
    }

    /**
     * 解析 JSON 格式的 Kafka 消息为 Map 对象
     *
     * @param message 原始 JSON 字符串
     * @return 解析后的键值对数据
     */
    private Map<String, Object> parseMessage(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, Map.class);
    }

}
