package com.alxy.tradeservice.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 李尚奇
 * @date 2025/4/15 14:05
 */
@Data
public class TradeVo {
    String userId;
    String baseCurrency;
    BigDecimal baseBalance;
    String targetCurrency;
    BigDecimal targetBalance;
    private BigDecimal marginAmount;
    private BigDecimal leverage;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public BigDecimal getBaseBalance() {
        return baseBalance;
    }

    public void setBaseBalance(BigDecimal baseBalance) {
        this.baseBalance = baseBalance;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getTargetBalance() {
        return targetBalance;
    }

    public void setTargetBalance(BigDecimal targetBalance) {
        this.targetBalance = targetBalance;
    }
}
