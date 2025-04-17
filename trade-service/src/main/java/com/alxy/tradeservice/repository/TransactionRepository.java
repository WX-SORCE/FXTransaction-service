package com.alxy.tradeservice.repository;

import com.alxy.tradeservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author 李尚奇
 * @date 2025/4/15
 * 交易数据访问接口，定义了保存和更新交易记录的方法
 */

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     * 保存、更新交易记录的方法
     *
     * @param transaction 交易对象
     * @return
     */
    Transaction save(Transaction transaction);

}
