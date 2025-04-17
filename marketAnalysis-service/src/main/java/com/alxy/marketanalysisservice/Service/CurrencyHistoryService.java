package com.alxy.marketanalysisservice.Service;

import com.alxy.marketanalysisservice.DTO.ExchangeRateView;
import com.alxy.marketanalysisservice.Entity.CurrencyHistory;
import com.alxy.marketanalysisservice.Repository.CurrencyHistoryRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class CurrencyHistoryService {

    @Resource
    private CurrencyHistoryRepository repository;

    // 获取某货币的历史数据（过去一段时间，如一周、一月或三个月）
    public List<ExchangeRateView> getHistory(String baseCurrency, String targetCurrency, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (period.toLowerCase()) {
            case "week" -> endDate.minusDays(7);
            case "month" -> endDate.minusMonths(1);
            case "three_months" -> endDate.minusMonths(3);
            default -> throw new IllegalArgumentException("Invalid period specified: " + period);
        };
        return repository.findByBaseCurrencyAndTargetCurrencyAndDateBetweenOrderByDateAsc(baseCurrency, targetCurrency, startDate, endDate);
    }

    public List<CurrencyHistory> currencyPairList() {
        return repository.findCurrencyHistoriesByDate(LocalDate.now());
    }
}
