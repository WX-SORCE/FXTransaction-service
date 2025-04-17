package com.alxy.accountservice.DTO;

import lombok.Data;

import java.math.BigDecimal;


// 市价单
@Data
public class TradeSettleRequest {
    private String userId;
    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal baseAmount;
    private BigDecimal targetAmount;
}