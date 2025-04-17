package com.alxy.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class AccountServiceApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.profiles.active", "docker");
        System.setProperty("spring.profiles.active", "dev");
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}