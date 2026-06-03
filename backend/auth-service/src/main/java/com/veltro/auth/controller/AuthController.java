package com.veltro.auth.controller;

import com.veltro.auth.dto.AuthResponse;
import com.veltro.auth.dto.LoginRequest;
import com.veltro.auth.dto.RefreshRequest;
import com.veltro.auth.dto.RegisterRequest;
import com.veltro.auth.service.AuthService;
import com.veltro.auth.service.LoginRateLimiterService;
import com.veltro.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiterService rateLimiter;

    public AuthController(AuthService authService, LoginRateLimiterService rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        rateLimiter.checkAndRecord(resolveClientIp(httpRequest));

        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // Rate-limit before credential check to prevent enumeration attacks
        rateLimiter.checkAndRecord(resolveClientIp(httpRequest));

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Resolves the real client IP, respecting X-Forwarded-For from the API Gateway.
     * Falls back to RemoteAddr when the header is absent (direct calls / testing).
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For may be a comma-separated chain; take the first (originating IP)
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
