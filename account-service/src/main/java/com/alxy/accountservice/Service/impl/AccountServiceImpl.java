package com.alxy.accountservice.Service.impl;

import com.alxy.accountservice.DTO.Result;
import com.alxy.accountservice.Entity.Account;
import com.alxy.accountservice.Reposity.AccountRepository;
import com.alxy.accountservice.Service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountRepository accountRepository;

    @Override
    public Result<Account> getAccountById(String accountId) {
        Optional<Account> accountOptional = accountRepository.findById(accountId);
        return accountOptional.map(Result::success).orElseGet(() -> Result.error(404, "未找到该账户信息"));
    }

    @Override
    public List<Account> getAccountByUserId(String userId) {
        return accountRepository.findByUserId(userId);
    }

    // 充值 -
    @Override
    public Result<Account> recharge(String baseCurrency, String userId, BigDecimal amount) {
        Account account = accountRepository.findAccountByUserIdAndBaseCurrency(userId, baseCurrency)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(new Date());
        accountRepository.save(account);
        return Result.success(account);
    }

    @Override
    public Result<Account> consume(String userId, BigDecimal amount) {

        return null;
    }


    @Override
    public Account getAccountByUserIdAndBaseCurrency(String userId, String baseCurrency) {
        return accountRepository.findAccountByUserIdAndBaseCurrency(userId, baseCurrency)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
    }

    @Override
    public Boolean updateAccount(Account baseAccount) {
        try {
            accountRepository.save(baseAccount);
            return true;
        }catch (Exception e) {
            return false;
        }
    }
}