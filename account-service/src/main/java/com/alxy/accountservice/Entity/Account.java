package com.alxy.accountservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "account_id", length = 50, nullable = false)
    private String accountId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "balance", precision = 18, scale = 6)
    private BigDecimal balance;

    @Column(name = "base_currency", length = 10)
    private String baseCurrency;

    @Column(name = "margin_balance", precision = 18, scale = 6)
    private BigDecimal marginBalance;

    @Column(name = "equity", precision = 18, scale = 6)
    private BigDecimal equity;

    @Column(name = "margin_level", precision = 18, scale = 6)
    private BigDecimal marginLevel;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}