package com.veltro.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.veltro.auth", "com.veltro.auth_service"})
@EnableDiscoveryClient
@EntityScan(basePackages = "com.veltro.auth.entity")
@EnableJpaRepositories(basePackages = "com.veltro.auth.repository")
public class AuthServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}