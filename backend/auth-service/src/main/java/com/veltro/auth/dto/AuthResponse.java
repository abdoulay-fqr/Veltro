package com.veltro.auth.dto;

public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String role;
    private Long userId;

    public AuthResponse(String accessToken, String refreshToken, String role, Long userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.userId = userId;
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getRole() { return role; }
    public Long getUserId() { return userId; }
}