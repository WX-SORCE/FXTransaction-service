package com.alxy.accountservice.DTO;

import lombok.Data;

import java.math.BigDecimal;


// 交换单
@Data
public class TradeVo {
    private String userId;
    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal baseBalance;
    private BigDecimal targetBalance;
    private BigDecimal marginAmount;
    private BigDecimal leverage;
}
