package com.alxy.tradeservice.scheduling;


import com.alxy.tradeservice.entity.Transaction;
//import com.alxy.tradeservice.repository.CurrencyHistoryRepository;
import com.alxy.tradeservice.repository.TransactionsRepository;
import com.alxy.tradeservice.service.impl.TradeServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BuyOrderScheduler {



    @Autowired
    private TradeServiceImpl tradeService;

    @Autowired
    private TransactionsRepository transactionRepository;
//@Autowired
//private CurrencyHistoryRepository currencyHistoryRepository;

    // 定义定时任务的执行时间表达式，这里示例为每 10 分钟执行一次
    // cron表达式：0 0/10 * * * ? 表示每隔 10 分钟的 0 秒执行一次
    @Scheduled(cron = "0 0/10 * * * ?")
    public void processBuyOrderTask() throws JsonProcessingException {

        // 获取所有交易记录
       List<Transaction> transactions = transactionRepository.findAll();

        for (Transaction transaction : transactions) {
            String baseCurrency = transaction.getBaseCurrency();
            String targetCurrency = transaction.getTargetCurrency();
            BigDecimal price = transaction.getPrice();
            BigDecimal pendingOrderBuy = transaction.getPendingOrderBuy();
            BigDecimal stopLoss = transaction.getStopLoss();
            BigDecimal takeProfit = transaction.getTakeProfit();
            String tradeType = transaction.getTradeType();
            if(transaction.getLeverageRatio()!=null){
            //fegin远程调用市场预测模块定时检查汇率 挂单实现
//            BigDecimal realTimeExchangeRate =currencyHistoryRepository.findLatestByBaseCurrencyAndTargetCurrency(baseCurrency,targetCurrency).get().getMarketBuy();
                BigDecimal realTimeExchangeRate =null;
            // 判断是否为限价单
            if ("限价单".equals(tradeType)) {
              //判断
                if(pendingOrderBuy!=null&&price.subtract(pendingOrderBuy).compareTo(BigDecimal.ZERO) > 0){
                    if(pendingOrderBuy!=null&&realTimeExchangeRate.subtract(pendingOrderBuy).compareTo(BigDecimal.ZERO) < 0){
                        // 触发结算逻辑，这里不具体实现
                        transaction.setPendingOrderBuy(realTimeExchangeRate);
                        tradeService.buyOrder(transaction);
                    }
                }else {
                    if(pendingOrderBuy!=null&&realTimeExchangeRate.subtract(pendingOrderBuy).compareTo(BigDecimal.ZERO) > 0){
                    // 触发结算逻辑，这里不具体实现
                        transaction.setPendingOrderBuy(realTimeExchangeRate);
                        tradeService.buyOrder(transaction);
                }

                }
            }

            // 判断是否达到止盈条件
            if (pendingOrderBuy!=null&&takeProfit != null && realTimeExchangeRate.compareTo(pendingOrderBuy) >= 0) {
                // 触发结算逻辑，这里不具体实现
                transaction.setPendingOrderBuy(realTimeExchangeRate);
                tradeService.buyOrder(transaction);
            }
            // 判断是否达到止损条件
            if (pendingOrderBuy!=null&&stopLoss != null && realTimeExchangeRate.compareTo(pendingOrderBuy) <= 0) {
                // 触发结算逻辑，这里不具体实现
                transaction.setPendingOrderBuy(realTimeExchangeRate);
                tradeService.buyOrder(transaction);
            }}
        }



    }

}