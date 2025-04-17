package com.alxy.notificationservice.repository;

import com.alxy.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    /**
     *
     * @param userId
     * @return
     */
    List<Notification> findByUserId(String userId);

    /**
     *
     * @param userId
     * @param type
     * @return
     */
    List<Notification> findByUserIdAndType(String userId, String type);

}