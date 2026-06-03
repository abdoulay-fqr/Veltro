package com.veltro.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS configuration for the API Gateway.
 *
 * Allowed origins are driven by the `cors.allowed-origins` property so they
 * can be overridden per environment without code changes:
 *
 *   development:  cors.allowed-origins=http://localhost:3000
 *   staging:      cors.allowed-origins=https://staging.veltro.gym
 *   production:   cors.allowed-origins=https://app.veltro.gym
 *
 * Default is localhost:3000 (the Next.js web frontend) only.
 * The mobile app (Flutter) does NOT use browsers, so it is unaffected by CORS.
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOriginsConfig;

    @Bean
    public CorsFilter corsFilter() {
        List<String> origins = List.of(allowedOriginsConfig.split(","));

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(origins);
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
