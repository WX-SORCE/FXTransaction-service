package com.alxy.tradeservice.repository;

import com.alxy.tradeservice.entity.StopLossTakeProfit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @description:
 * @author: 宋枝波
 * @date: 2025-04-15 21:24
 */

public interface StopLossTakeProfitRepository extends JpaRepository<StopLossTakeProfit, String> {

    List<StopLossTakeProfit> findByUserId(String u12345);
}