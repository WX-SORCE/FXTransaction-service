package com.alxy.accountservice.DTO;

import lombok.Data;

import java.math.BigDecimal;

// 保证金
@Data
public class MarginRequest {
    private String userId;
    private String currency;
    private BigDecimal amount;
}