package com.alxy.tradeservice.feign;

import com.alxy.tradeservice.entity.Result;
import com.alxy.tradeservice.entity.dto.ForeignExchange;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

/**
 * @author 李尚奇
 * @date 2025/4/15 21:41
 */
@FeignClient(name = "risk-service")
public interface RiskFeign {

    /**
     * 风险评估接口
     * @param request
     * @return

     */
    @PostMapping("/fx/monitoring")
    public int monitoring(@Valid @RequestBody ForeignExchange request);
}
