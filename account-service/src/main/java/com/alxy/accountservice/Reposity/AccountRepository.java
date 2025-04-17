package com.alxy.accountservice.Reposity;

import com.alxy.accountservice.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    /**
     * 根据用户 ID 查询账户信息
     *
     * @param userId 用户 ID
     * @return 匹配的账户信息 Optional 对象
     */
    List<Account> findByUserId(String userId);

    Optional<Account> findAccountByUserIdAndBaseCurrency(String userId, String baseCurrency);

//    Boolean
}