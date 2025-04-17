package com.alxy.authservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {


    private String accountId;

    private String userId;

    private BigDecimal balance;

    private String baseCurrency;

    private BigDecimal marginBalance;

    private BigDecimal equity;

    private BigDecimal marginLevel;

    private Date createdAt;

    private Date updatedAt;
}