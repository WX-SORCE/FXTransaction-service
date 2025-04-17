package com.alxy.marketanalysisservice.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "currency_exchange_history")
@Data
@NoArgsConstructor
public class CurrencyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "exchange_id", columnDefinition = "varchar(255)")
    private String exchangeId;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;

    @Column(name = "exchange_rate", nullable = false, precision = 20, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "high", nullable = false, precision = 20, scale = 8)
    private BigDecimal high;

    @Column(name = "low", nullable = false, precision = 20, scale = 8)
    private BigDecimal low;

    @Column(name = "open", nullable = false, precision = 20, scale = 8)
    private BigDecimal open;

    @Column(name = "close", nullable = false, precision = 20, scale = 8)
    private BigDecimal close;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "market_buy", precision = 20, scale = 8)
    private BigDecimal marketBuy;

    @Column(name = "market_sell", precision = 20, scale = 8)
    private BigDecimal marketSell;
}