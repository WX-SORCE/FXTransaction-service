CREATE DATABASE marketAnalysis_database;

USE marketAnalysis_database;
CREATE TABLE currency_exchange_realtime
(
    id              VARCHAR(64) PRIMARY KEY,
    base_currency   VARCHAR(10)    NOT NULL,
    target_currency VARCHAR(10)    NOT NULL,
    latest          DECIMAL(19, 6) NOT NULL,
    date            DATETIME       NOT NULL
);


CREATE TABLE `currency_exchange_history`
(
    `exchange_id`     varchar(255)   NOT NULL,
    `base_currency`   varchar(3)     NOT NULL,
    `target_currency` varchar(3)     NOT NULL,
    `exchange_rate`   decimal(20, 8) NOT NULL,
    `high`            decimal(20, 8) NOT NULL,
    `low`             decimal(20, 8) NOT NULL,
    `open`            decimal(20, 8) NOT NULL,
    `close`           decimal(20, 8) NOT NULL,
    `date`            date           NOT NULL,
    `market_buy`      decimal(20,8) NOT NULL ,
    `market_sell`     decimal(20,8) NOT NULL,
    PRIMARY KEY (`exchange_id`),
    KEY `idx_base_target_currency` (`base_currency`, `target_currency`),
    KEY `idx_date` (`date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
