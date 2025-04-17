package com.alxy.tradeservice.controller;

import com.alxy.tradeservice.entity.Result;
import com.alxy.tradeservice.entity.dto.BuyRequestDto;
import com.alxy.tradeservice.service.impl.TradeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @author: 宋枝波
 * @date: 2025-04-15 21:27
 */
@RestController
@RequestMapping("/trade")
@CrossOrigin
public class TransactionController {
//    @Autowired
//    TransactionService transactionService;

    @Autowired
    TradeServiceImpl tradeService;

    @PostMapping
    public Result transaction(@RequestBody BuyRequestDto buyRequestDto){
         tradeService.pendingTransaction(buyRequestDto);
    return Result.success(tradeService);
    }

}
