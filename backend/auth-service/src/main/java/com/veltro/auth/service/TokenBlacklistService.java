package com.veltro.auth.service;

import com.veltro.auth.entity.TokenBlacklist;
import com.veltro.auth.repository.TokenBlacklistRepository;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    public void blacklist(String token) {
        TokenBlacklist entry = new TokenBlacklist();
        entry.setToken(token);
        tokenBlacklistRepository.save(entry);
    }

    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
}