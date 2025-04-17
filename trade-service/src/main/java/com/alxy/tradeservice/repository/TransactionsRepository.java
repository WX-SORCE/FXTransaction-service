package com.alxy.tradeservice.repository;

import com.alxy.tradeservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @description:
 * @author: 宋枝波
 * @date: 2025-04-15 21:25
 */
public interface TransactionsRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findAllByUserId(String userid);

}