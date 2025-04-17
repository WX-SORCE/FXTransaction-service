CREATE DATABASE account_database;

USE account_database;

-- 4.4 账户表
CREATE TABLE accounts
(
    account_id     VARCHAR(50) PRIMARY KEY COMMENT '账户ID（主键）',
    user_id        VARCHAR(50) COMMENT '用户ID',
    balance        DECIMAL(18, 6) COMMENT '账户余额',
    base_currency  VARCHAR(10) COMMENT '账户基础币种',
    margin_balance DECIMAL(18, 6) COMMENT '保证金余额',
    equity         DECIMAL(18, 6) COMMENT '净值（余额 + 保证金）',
    margin_level   DECIMAL(18, 6) COMMENT '保证金水平（如杠杆100:1）',
    created_at     DATETIME COMMENT '账户创建时间',
    updated_at     DATETIME COMMENT '账户更新时间'
) COMMENT ='账户表';