package com.alxy.tradeservice.repository;

import com.alxy.tradeservice.entity.Account;

/**
 * @author 李尚奇
 * @date 2025/4/15 10:47
 * 账户数据访问接口，定义了根据用户 ID 查询账户信息和更新账户信息的方法
 */
public interface AccountRepository {
    /**
     * 根据用户 ID 查询账户信息的方法
     * @param userId 用户 ID
     * @return 账户对象
     */
    Account findByUserId(String userId);

    /**
     * 更新账户信息的方法
     * @param account 账户对象
     */
    void update(Account account);
}
