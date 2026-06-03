package com.veltro.chatbot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI chatbotOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Veltro Chatbot API")
                .version("1.0")
                .description("AI-powered assistant using Google Gemini 1.5 Flash with member context enrichment"));
    }
}
