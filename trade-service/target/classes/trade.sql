CREATE DATABASE trade_database;

USE trade_database;

-- 4.2 交易记录表
CREATE TABLE transactions
(
    transaction_id   VARCHAR(50) PRIMARY KEY COMMENT '交易ID（主键）',
    user_id          VARCHAR(50) COMMENT '用户ID',
    base_currency    VARCHAR(3) NOT NULL, -- 基准货币（如 "USD"）
    target_currency  VARCHAR(3) NOT NULL, -- 目标货币（如 "EUR"）
    action           VARCHAR(10) COMMENT '交易动作（BUY 或 SELL）',
    amount           DECIMAL(18, 6) COMMENT '交易金额',
    price            DECIMAL(18, 6) COMMENT '交易时的汇率价格',
    status           VARCHAR(20) COMMENT '交易状态（SUCCESS、FAILED、PENDING等）',
    stop_loss        DECIMAL(18, 6) COMMENT '止损金额',
    take_profit      DECIMAL(18, 6) COMMENT '止盈金额',
    transaction_time DATETIME COMMENT '交易时间',
    completed_time   DATETIME COMMENT '交易完成时间',
    transaction_type VARCHAR(20) COMMENT '交易类型（manual 或 automated）'
) COMMENT ='交易记录表';


-- 4.3 用户资金流水表
CREATE TABLE funds_history
(
    transaction_id VARCHAR(50) PRIMARY KEY COMMENT '流水ID（主键）',
    user_id        VARCHAR(50) COMMENT '用户ID',
    amount         DECIMAL(18, 6) COMMENT '交易金额',
    type           VARCHAR(20) COMMENT '资金变动类型（deposit 或 withdrawal）',
    status         VARCHAR(20) COMMENT '资金变动状态（SUCCESS 或 FAILED）',
    balance        DECIMAL(18, 6) COMMENT '变动后的账户余额',
    created_at     DATETIME COMMENT '资金变动时间',
    description    VARCHAR(255) COMMENT '资金变动描述（例如“充值”或“提现”）'
) COMMENT ='用户资金流水表';


-- 4.6 止损止盈记录表
CREATE TABLE stop_loss_take_profit
(
    record_id      VARCHAR(50) PRIMARY KEY COMMENT '止损止盈记录ID（主键）',
    user_id        VARCHAR(50) COMMENT '用户ID',
    transaction_id VARCHAR(50) COMMENT '交易ID',
    stop_loss      DECIMAL(18, 6) COMMENT '止损价格',
    take_profit    DECIMAL(18, 6) COMMENT '止盈价格',
    trigger_time   DATETIME COMMENT '触发时间',
    triggered      BOOLEAN DEFAULT FALSE COMMENT '是否已触发'
) COMMENT ='止损止盈记录表';