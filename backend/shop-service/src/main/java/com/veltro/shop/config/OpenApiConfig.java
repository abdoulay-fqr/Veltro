package com.veltro.shop.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI shopServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Veltro Shop Service API")
                .version("1.0")
                .description("Product catalog, order management, and revenue reporting"));
    }
}
