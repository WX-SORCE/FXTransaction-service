package com.alxy.configservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.ConfigServerApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@EnableConfigServer
@Slf4j
public class ConfigServiceApplication {

	public static void main(String[] args) {
		// System.setProperty("spring.profiles.active", "docker");
		System.setProperty("spring.profiles.active", "dev");
		SpringApplication.run(ConfigServiceApplication.class, args);
		log.info("配置中心启动成功.");
	}
}
