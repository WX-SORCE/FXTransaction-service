package com.alxy.tradeservice.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 交易请求参数
 * @author: 李尚奇
 * @date: 2025-04-15
 */
@Data
public class BuyRequestDto {
    private String userId;             //用户ID
    private String baseCurrency;       //基准货币
    private String targetCurrency;     //目标货币
    private BigDecimal total;         //交易金额(单价)
    private BigDecimal price;          //交易时的汇率价格
    private Date transactionTime;       //交易时间
    private String action;                //交易行为   buy/sell
    private  Long size;                   //货币数量
    private BigDecimal pendingOrderBuy;    //挂单买入价
    private BigDecimal pendingOrderSell;   //挂单卖出价
    private Long leverageRatio;   //杠杆比例

    // 新增交易类型    0 MARKET_ORDER 市价单, 1 LIMIT_ORDER 限价单, 2 STOP_LOSS_ORDER 止损单, 3 STOP_PROFIT_ORDER 止盈单
    private Long tradeType;

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

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public BigDecimal getPendingOrderBuy() {
        return pendingOrderBuy;
    }

    public void setPendingOrderBuy(BigDecimal pendingOrderBuy) {
        this.pendingOrderBuy = pendingOrderBuy;
    }

    public BigDecimal getPendingOrderSell() {
        return pendingOrderSell;
    }

    public void setPendingOrderSell(BigDecimal pendingOrderSell) {
        this.pendingOrderSell = pendingOrderSell;
    }

    public Long getLeverageRatio() {
        return leverageRatio;
    }

    public void setLeverageRatio(Long leverageRatio) {
        this.leverageRatio = leverageRatio;
    }

    public Long getTradeType() {
        return tradeType;
    }

    public void setTradeType(Long tradeType) {
        this.tradeType = tradeType;
    }
}
