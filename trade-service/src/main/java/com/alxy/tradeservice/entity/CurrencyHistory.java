//package com.alxy.tradeservice.entity;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.joda.time.DateTime;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//@Entity
//@Table(name = "currency_exchange_history")
//@Data
//@NoArgsConstructor
//public class CurrencyHistory {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    @Column(name = "exchange_id", columnDefinition = "varchar(255)")
//    private String exchangeId;
//
//    @Column(name = "base_currency", nullable = false, length = 3)
//    private String baseCurrency;
//
//    @Column(name = "target_currency", nullable = false, length = 3)
//    private String targetCurrency;
//
//    @Column(name = "exchange_rate", nullable = false, precision = 20, scale = 8)
//    private BigDecimal exchangeRate;
//
//    @Column(name = "high", nullable = false, precision = 20, scale = 8)
//    private BigDecimal high;
//
//    @Column(name = "low", nullable = false, precision = 20, scale = 8)
//    private BigDecimal low;
//
//    @Column(name = "open", nullable = false, precision = 20, scale = 8)
//    private BigDecimal open;
//
//    @Column(name = "close", nullable = false, precision = 20, scale = 8)
//    private BigDecimal close;
//
//    @Column(name = "date", nullable = false)
//    private DateTime date;
//
//    @Column(name = "market_buy", precision = 20, scale = 8)
//    private BigDecimal marketBuy;
//
//    @Column(name = "market_sell", precision = 20, scale = 8)
//    private BigDecimal marketSell;
//
//
//    public String getExchangeId() {
//        return exchangeId;
//    }
//
//    public void setExchangeId(String exchangeId) {
//        this.exchangeId = exchangeId;
//    }
//
//    public String getBaseCurrency() {
//        return baseCurrency;
//    }
//
//    public void setBaseCurrency(String baseCurrency) {
//        this.baseCurrency = baseCurrency;
//    }
//
//    public String getTargetCurrency() {
//        return targetCurrency;
//    }
//
//    public void setTargetCurrency(String targetCurrency) {
//        this.targetCurrency = targetCurrency;
//    }
//
//    public BigDecimal getExchangeRate() {
//        return exchangeRate;
//    }
//
//    public void setExchangeRate(BigDecimal exchangeRate) {
//        this.exchangeRate = exchangeRate;
//    }
//
//    public BigDecimal getHigh() {
//        return high;
//    }
//
//    public void setHigh(BigDecimal high) {
//        this.high = high;
//    }
//
//    public BigDecimal getLow() {
//        return low;
//    }
//
//    public void setLow(BigDecimal low) {
//        this.low = low;
//    }
//
//    public BigDecimal getOpen() {
//        return open;
//    }
//
//    public void setOpen(BigDecimal open) {
//        this.open = open;
//    }
//
//    public BigDecimal getClose() {
//        return close;
//    }
//
//    public void setClose(BigDecimal close) {
//        this.close = close;
//    }
//
//    public DateTime getDate() {
//        return date;
//    }
//
//    public void setDate(DateTime date) {
//        this.date = date;
//    }
//
//    public BigDecimal getMarketBuy() {
//        return marketBuy;
//    }
//
//    public void setMarketBuy(BigDecimal marketBuy) {
//        this.marketBuy = marketBuy;
//    }
//
//    public BigDecimal getMarketSell() {
//        return marketSell;
//    }
//
//    public void setMarketSell(BigDecimal marketSell) {
//        this.marketSell = marketSell;
//    }
//}