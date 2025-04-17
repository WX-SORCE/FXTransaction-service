package com.alxy.notificationservice.service.impl;

import com.alxy.notificationservice.entity.Notification;
import com.alxy.notificationservice.repository.NotificationRepository;
import com.alxy.notificationservice.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * 保存通知到数据库，使用事务管理
     * @param userId 用户ID
     * @param type 通知类型（如 TRADE_STATUS）
     * @param content 通知内容
     */
    @Transactional
    @Override
    public void saveNotification(String userId, String type, String content) {
        Notification notification = new Notification();
        notification.setNotificationId("NOTIF-" + UUID.randomUUID().toString().substring(0, 8)); // 生成唯一ID
        notification.setUserId(userId);
        notification.setType(type);
        notification.setContent(content);
        notification.setCreatedAt(new Date());
        notificationRepository.save(notification);
    }

    /**
     *
     * @param userId  客户ID
     * @param type 通知类型
     * @return
     */
    @Override
    public List<Notification> getNotifications(String userId, String type) {
        if(type.equals("TRADE_STATUS")) {
            return notificationRepository.findByUserIdAndType(userId, type);
        } else {
            return notificationRepository.findByUserId(userId);
        }
    }

    /**
     * 将通知标记为已读
     * @param notificationId 通知ID
     * @return
     */
    @Transactional
    @Override
    public Notification markNotificationAsRead(String notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setRead(true);
            return notificationRepository.save(notification);
        }
        return null;
    }



}