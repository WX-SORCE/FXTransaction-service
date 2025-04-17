package com.alxy.tradeservice.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * 交易实体类，用于表示交易记录的相关信息
 */

@Entity
@Table(name = "transactions")
@Builder
public class Transaction {

    // 交易 ID（主键）
    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    // 用户 ID
    @Column(name = "user_id")
    private String userId;

    // 基准货币
    @Column(name = "base_currency")
    private String baseCurrency;

    // 目标货币
    @Column(name = "target_currency")
    private String targetCurrency;

    // 交易动作（BUY 或 SELL）
    @Column(name = "action")
    private String action;

    // 交易金额
    @Column(name = "amount", precision = 18, scale = 6)
    private BigDecimal amount;

    // 交易时的汇率价格
    @Column(name = "price", precision = 18, scale = 6)
    private BigDecimal price;

    // 交易状态（SUCCESS、FAILED、PENDING等）
    @Column(name = "status")
    private String status;

    // 止损金额
    @Column(name = "stop_loss", precision = 18, scale = 6)
    private BigDecimal stopLoss;

    // 止盈金额
    @Column(name = "take_profit", precision = 18, scale = 6)
    private BigDecimal takeProfit;

    // 交易时间
    @Column(name = "transaction_time")
    private Date transactionTime;

    // 交易完成时间
    @Column(name = "completed_time")
    private Date completedTime;

    // 交易类型（manual 或 automated）
    @Column(name = "transaction_type")
    private String transactionType;

    // 杠杆比率
    @Column(name = "leverageRatio")
    private Long leverageRatio;

    // 挂单买入价
    @Column(name = "pendingOrderBuy", precision = 19, scale = 4)
    private BigDecimal pendingOrderBuy;

    // 货币数量
    @Column(name = "size")
    private Long size;

    // 保证金
    @Column(name = "margin", precision = 19, scale = 4)
    private BigDecimal margin;

    // 交易类型（例如可能的更多分类）
    @Column(name = "tradeType")
    private String tradeType;

    // 总计
    @Column(name = "total", precision = 19, scale = 4)
    private BigDecimal total;

    public Transaction(String transactionId, String userId, String baseCurrency, String targetCurrency, String action, BigDecimal amount, BigDecimal price, String status, BigDecimal stopLoss, BigDecimal takeProfit, Date transactionTime, Date completedTime, String transactionType, Long leverageRatio, BigDecimal pendingOrderBuy, Long size, BigDecimal margin, String tradeType, BigDecimal total) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.action = action;
        this.amount = amount;
        this.price = price;
        this.status = status;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.transactionTime = transactionTime;
        this.completedTime = completedTime;
        this.transactionType = transactionType;
        this.leverageRatio = leverageRatio;
        this.pendingOrderBuy = pendingOrderBuy;
        this.size = size;
        this.margin = margin;
        this.tradeType = tradeType;
        this.total = total;
    }

    @PrePersist
    protected void onCreate() {

        if (transactionId == null) {
            transactionId = UUID.randomUUID().toString().replaceAll("-","").substring(12);
        }
    }

    // 无参构造函数
    public Transaction() {
    }

    // 以下是各字段的 get 和 set 方法

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(BigDecimal stopLoss) {
        this.stopLoss = stopLoss;
    }

    public BigDecimal getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(BigDecimal takeProfit) {
        this.takeProfit = takeProfit;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public Date getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Date completedTime) {
        this.completedTime = completedTime;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Long getLeverageRatio() {
        return leverageRatio;
    }

    public void setLeverageRatio(Long leverageRatio) {
        this.leverageRatio = leverageRatio;
    }

    public BigDecimal getPendingOrderBuy() {
        return pendingOrderBuy;
    }

    public void setPendingOrderBuy(BigDecimal pendingOrderBuy) {
        this.pendingOrderBuy = pendingOrderBuy;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}