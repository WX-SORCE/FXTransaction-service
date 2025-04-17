CREATE TABLE users
(
    user_id      VARCHAR(50) PRIMARY KEY COMMENT '用户ID（主键）',
    username     VARCHAR(50)  NOT NULL COMMENT '用户名',
    password     VARCHAR(255) NOT NULL COMMENT '密码',
    email        VARCHAR(100) UNIQUE COMMENT '用户邮箱',
    kyc_status   BOOLEAN  DEFAULT FALSE COMMENT '是否完成KYC',
    kyc_due_date DATETIME COMMENT 'KYC到期时间',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT ='用户表';