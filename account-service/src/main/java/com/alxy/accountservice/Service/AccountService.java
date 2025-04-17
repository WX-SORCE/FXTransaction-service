package com.alxy.accountservice.Service;


import com.alxy.accountservice.DTO.Result;
import com.alxy.accountservice.Entity.Account;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface AccountService {
    /**
     * 根据账户 ID 获取账户信息
     *
     * @param accountId 账户 ID
     * @return 匹配的账户信息 Optional 对象
     */
    Result<Account> getAccountById(String accountId);

    /**
     * 根据用户 ID 获取账户信息
     *
     * @param userId 用户 ID
     * @return 匹配的账户信息 Optional 对象
     */
    List<Account> getAccountByUserId(String userId);


    // 充值方法
    Result<Account> recharge(String baseCurrency, String userId, BigDecimal amount);

    // 消费方法
    Result<Account> consume(String userId, BigDecimal amount);

    //
    Account getAccountByUserIdAndBaseCurrency(String accountId, String baseCurrency);

    Boolean updateAccount(Account baseAccount);
}