package com.alxy.tradeservice.feign;

import com.alxy.tradeservice.entity.Account;

import com.alxy.tradeservice.entity.dto.MarginRequest;
import com.alxy.tradeservice.entity.dto.Result;
import com.alxy.tradeservice.entity.vo.TradeVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 李尚奇
 * @date 2025/4/15 15:05
 */
//@Component
@FeignClient(name = "account-service")
public interface FeignAccount {
    @PostMapping("/v1/account/freezeMargin")
    public Result freezeMargin(@RequestBody MarginRequest request) ;

    @PostMapping("/v1/account/exchange")
    public Result<?> exchange(@RequestBody TradeVo tradeVo);

    @GetMapping("/v1/account/getAccount")
    Result<Account> getAccount(@RequestParam String userId,@RequestParam String baseCurrency);
}
