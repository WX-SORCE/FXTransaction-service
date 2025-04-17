package com.alxy.tradeservice.feign;

//import com.alxy.tradeservice.entity.CurrencyHistory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 李尚奇
 * @date 2025/4/15
 */
@FeignClient(name = "marketAnalysis-service")
public interface FeignMarketAnalysisService {
//    @GetMapping("/v1/analysis/getNewRates")
//    public CurrencyHistory getNewRates(@RequestParam String baseCurrency, @RequestParam String targetCurrency);
}
