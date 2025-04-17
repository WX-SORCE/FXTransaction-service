package com.alxy.tradeservice.service;

import com.alxy.tradeservice.entity.dto.BuyRequestDto;
import com.alxy.tradeservice.entity.Transaction;

/**
 * @author 李尚奇
 * @date 2025/4/15
 */
public interface ForexTradingService {
    Transaction marketBuyRequest(BuyRequestDto buyRequest) throws Exception;


    Transaction limitBuyRequest(BuyRequestDto buyRequest) throws Exception;

    Transaction stopLossBuyRequest(BuyRequestDto buyRequest) throws Exception;

    Transaction stopPorfitBuyRequest(BuyRequestDto buyRequest) throws Exception;

    Transaction processOrder(BuyRequestDto buyRequest);

}
