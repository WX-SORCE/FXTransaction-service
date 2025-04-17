package com.alxy.tradeservice.repository;

import com.alxy.tradeservice.entity.FundsHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @description:
 * @author: 宋枝波
 * @date: 2025-04-15 21:24
 */
public interface FundsHistoryRepository extends JpaRepository<FundsHistory, String> {


    List<FundsHistory> findByUserId(String u12345);
}