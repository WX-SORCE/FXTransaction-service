package com.alxy.marketanalysisservice.Controller;

import com.alxy.marketanalysisservice.DTO.ExchangeRateView;
import com.alxy.marketanalysisservice.DTO.Result;
import com.alxy.marketanalysisservice.Entity.CurrencyHistory;
import com.alxy.marketanalysisservice.Service.CurrencyExchangeScheduledTask;
import com.alxy.marketanalysisservice.Service.CurrencyHistoryService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/currency")
public class CurrencyController {

    @Resource
    private CurrencyHistoryService historyService;
    @Resource
    private CurrencyExchangeScheduledTask currencyExchangeScheduledTask;


    // 获取历史数据（根据时间段：一周、一月、三个月）
    @GetMapping("/history")
    public Result<List<ExchangeRateView>> getHistory(
            @RequestParam String baseCurrency,
            @RequestParam String targetCurrency,
            @RequestParam String period) {
        // TODO Day

        try {
            List<ExchangeRateView> history = historyService.getHistory(baseCurrency, targetCurrency, period);
            return Result.success(history); // 成功返回数据
        } catch (Exception e) {
            return Result.error(500, "服务器内部错误：" + e.getMessage()); // 失败返回错误信息
        }
    }

    @GetMapping("/update-bid-ask-prices")
    public String updatePrices() {
        currencyExchangeScheduledTask.updateBidAndAskPrices();
        return "Bid and Ask prices updated successfully!";
    }


    @GetMapping("/currencyPairList")
    public Result<List<CurrencyHistory>> currencyPairList() {
        return Result.success(historyService.currencyPairList());
    }

    @PostMapping("/saveCurrencyOfDate")
    public Result<?> saveCurrencyOfDate(@RequestParam LocalDate date){
        currencyExchangeScheduledTask.fetchAndSaveExchangeRatesForDate(date);
        return Result.success();
    }

    @GetMapping("/getThreeMonth")
    public Result<?> getThreeMonth() {
        currencyExchangeScheduledTask.fetchLastThreeMonthsHistory();
        return Result.success();
    }
}
