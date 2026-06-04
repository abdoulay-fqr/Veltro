package com.veltro.auth.service;

import com.veltro.auth.dto.AuthResponse;
import com.veltro.auth.dto.LoginRequest;
import com.veltro.auth.dto.RefreshRequest;
import com.veltro.auth.dto.RegisterRequest;
import com.veltro.auth.entity.AppUser;
import com.veltro.auth.entity.Role;
import com.veltro.auth.repository.AppUserRepository;
import com.veltro.common.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(AppUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByIdentifier(request.getIdentifier())) {
            throw new RuntimeException("Identifier already in use");
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());

        AppUser user = new AppUser();
        user.setIdentifier(request.getIdentifier());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        userRepository.save(user);

        return generateTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return generateTokens(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new RuntimeException("Refresh token is blacklisted");
        }

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String identifier = jwtUtil.extractSubject(refreshToken);
        AppUser user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return generateTokens(user);
    }

    public void logout(String accessToken) {
        tokenBlacklistService.blacklist(accessToken);
    }

    public void deleteUser(Long userId) {
        userRepository.findById(userId).ifPresent(userRepository::delete);
    }

    public void changePassword(String identifier, String currentPassword, String newPassword) {
        AppUser user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private AuthResponse generateTokens(AppUser user) {
        Map<String, Object> claims = Map.of(
                "role",   user.getRole().name(),
                "userId", String.valueOf(user.getId())
        );
        String accessToken = jwtUtil.generateAccessToken(user.getIdentifier(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getIdentifier());
        return new AuthResponse(accessToken, refreshToken, user.getRole().name(), user.getId());
    }
}