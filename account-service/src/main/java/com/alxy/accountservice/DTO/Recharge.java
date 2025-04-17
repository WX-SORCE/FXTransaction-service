package com.alxy.accountservice.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Recharge {
    private String baseCurrency;
    private BigDecimal amount;
    private String faceToken;
}
