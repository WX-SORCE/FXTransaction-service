package com.alxy.accountservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 杠杆交易
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeRequest {
    private String userId;
    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal marginAmount;   // 保证金金额
    private BigDecimal targetProfit;   // 平仓后获得的目标资产
    private String action;             // "OPEN" 或 "CLOSE"
}
