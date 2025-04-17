package com.alxy.tradeservice.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

// 保证金
@Data
public class MarginRequest {
    private String userId;
    private String currency;
    private BigDecimal amount;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}