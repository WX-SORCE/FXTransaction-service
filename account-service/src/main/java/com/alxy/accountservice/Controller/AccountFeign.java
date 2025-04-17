package com.alxy.accountservice.Controller;

import com.alxy.accountservice.DTO.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-service", path = "/v1/account")
public interface AccountFeign {

    @GetMapping("/getAccount")
    Result<?> getAccount(@RequestParam String baseCurrency, @RequestParam String userId);

}
