package com.alxy.marketanalysisservice.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "currency_exchange_realtime")
public class CurrencyRealtime {

    @Id
    private String id;
    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal latest;
    private LocalDateTime date;
}
