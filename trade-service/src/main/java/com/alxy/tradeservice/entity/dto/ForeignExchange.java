package com.alxy.tradeservice.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 李尚奇
 * @date 2025/4/16 15:29
 */
@Data
public class ForeignExchange {
    private String userId;
    private String accountId;
    private String action;        // 例如 "BUY"、"SELL"
    private BigDecimal amount;      // 总金额（单价 × 数量）
}
