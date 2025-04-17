package com.alxy.marketanalysisservice.Controller;

//import com.example.demo.service.ExchangeRateService;


import com.alxy.marketanalysisservice.Entity.CurrencyHistory;
import com.alxy.marketanalysisservice.Service.MarketAnalysisService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController("/v1/analysis")
public class ExchangeRateController {


    @Autowired
    MarketAnalysisService marketAnalysisService;
    //获取预测汇率
    @GetMapping("/forecast")
    public  Map<String,Double> getExchangeRateForecast(@RequestParam String baseCurrency, @RequestParam String targetCurrency) {
        return marketAnalysisService.predictExchangeRate(baseCurrency, targetCurrency);
    }
    //获取最新汇率
    @GetMapping("/getNewRates")
    public CurrencyHistory getNewRates(@RequestParam String baseCurrency, @RequestParam String targetCurrency){
       return marketAnalysisService.getNewRates(baseCurrency,targetCurrency);
    };


}