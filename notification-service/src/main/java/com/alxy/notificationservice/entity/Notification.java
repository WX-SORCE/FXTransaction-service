
package com.alxy.notificationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @Column(name = "notification_id")
    private String notificationId; // 通知ID

    @Column(name = "user_id")
    private String userId; // 接收人
    private String type; // 通知类型
    private String content; // 通知正文

    @Column(name = "`read`")
    private boolean read; // 是否已读

    @Column(name = "create_At")
    private Date createdAt; // 推送时间
}
