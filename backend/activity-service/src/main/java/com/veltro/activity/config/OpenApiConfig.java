package com.veltro.activity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI activityServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Veltro Activity Service API")
                .version("1.0")
                .description("Gym entry/exit tracking, machine sessions, and performance analytics"));
    }
}
