package com.alxy.tradeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
public class TradeServiceApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.profiles.active", "docker");
        System.setProperty("spring.profiles.active", "dev");
        SpringApplication.run(TradeServiceApplication.class, args);
    }
}
