package com.alxy.discoveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;


@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.profiles.active", "docker");
        System.setProperty("spring.profiles.active", "dev");
        SpringApplication.run(DiscoveryServiceApplication.class, args);
    }
}
