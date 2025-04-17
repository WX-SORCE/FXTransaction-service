package com.alxy.notificationservice.controller;

import com.alxy.notificationservice.entity.Notification;
import com.alxy.notificationservice.repository.NotificationRepository;
import com.alxy.notificationservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 根据用户ID获取通知列表
     * @param userId 用户ID
     * @return 通知列表
     */
    @GetMapping("/{userId}")
    public List<Notification> getNotificationsByClientId(@PathVariable String userId, String type) {
        return notificationService.getNotifications(userId, type);
    }

    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @return 更新后的通知
     */
    @PutMapping("/{notificationId}/read")
    public Notification markNotificationAsRead(@PathVariable String notificationId) {
        return notificationService.markNotificationAsRead(notificationId);
    }
}