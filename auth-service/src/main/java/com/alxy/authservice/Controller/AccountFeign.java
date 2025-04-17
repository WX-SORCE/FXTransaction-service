package com.alxy.authservice.Controller;

import com.alxy.authservice.DTO.Account;
import com.alxy.authservice.DTO.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-service", path = "/v1/account")
public interface AccountFeign {
    @GetMapping("getAccountByUserId")
    Result<Account> getAccountByUserId(@RequestParam String userId);

}
