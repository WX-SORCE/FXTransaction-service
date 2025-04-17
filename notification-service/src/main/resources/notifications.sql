CREATE TABLE notifications
(
    notification_id      VARCHAR(50) PRIMARY KEY COMMENT '消息ID（主键）',
    user_id     VARCHAR(50)  NOT NULL COMMENT '用户ID',
    content        VARCHAR(255) UNIQUE COMMENT '消息内容',
    type        VARCHAR(10) UNIQUE COMMENT '消息类型',
    `read`        BOOLEAN UNIQUE COMMENT '是否已读',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT ='消息表';