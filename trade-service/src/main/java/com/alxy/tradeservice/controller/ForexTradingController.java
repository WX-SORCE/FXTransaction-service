package com.alxy.tradeservice.controller;

import com.alxy.tradeservice.entity.dto.BuyRequestDto;

import com.alxy.tradeservice.entity.dto.Result;
import com.alxy.tradeservice.service.ForexTradingService;
import com.alxy.tradeservice.service.impl.TradeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 外汇交易控制层
 * @author: 李尚奇
 * @date: 2025-04-15
 */
@RestController
@RequestMapping("/v1/trade")
public class ForexTradingController {
    // 注入外汇交易服务
    @Autowired
    private ForexTradingService forexTradingService;


    @Autowired
    TradeServiceImpl tradeService;
    /**
     * 处理买入请求的接口
     * @param buyRequest 包含买入请求参数的对象
     * @return 处理结果信息
     */
    @PostMapping("/trade")
    public Result<?> trade(@RequestBody BuyRequestDto buyRequest){
        // 调用服务层的买入请求处理方法
        forexTradingService.processOrder(buyRequest);
        return Result.success();
    }

    @GetMapping("/fundsHistoryList")
    public com.alxy.tradeservice.entity.Result getFundsHistoryList(){
        return   com.alxy.tradeservice.entity.Result.success(tradeService.findFundsHistory());
    }
    @GetMapping("/stopLosssTakeProfitList")
    public com.alxy.tradeservice.entity.Result getStopLosssTakeProfit(){
        return   com.alxy.tradeservice.entity.Result.success(tradeService.findStopLosssTakeProfit());
    }

    @GetMapping("/transactionList")
    public com.alxy.tradeservice.entity.Result getTransactionList(){
        return   com.alxy.tradeservice.entity.Result.success(tradeService.findTransaction());
    }

}
