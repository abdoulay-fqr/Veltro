package com.veltro.subscription.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class MemberClient {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public MemberClient(@Value("${user-service.base-url}") String userServiceBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @CircuitBreaker(name = "user-service", fallbackMethod = "memberExistsFallback")
    public boolean memberExists(Long memberId) {
        String url = userServiceBaseUrl + "/api/v1/users/members/" + memberId;
        try {
            restTemplate.getForObject(url, Object.class);
            return true;
        } catch (Exception e) {
            log.warn("Failed to verify member existence for memberId={}: {}", memberId, e.getMessage());
            throw e;
        }
    }

    public boolean memberExistsFallback(Long memberId, Throwable t) {
        log.warn("Circuit breaker open for user-service. Allowing subscription creation for memberId={}", memberId);
        return true;
    }
}
