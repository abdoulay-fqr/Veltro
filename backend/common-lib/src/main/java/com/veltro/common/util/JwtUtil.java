package com.veltro.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private final Key key;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtUtil(String secret, long accessTokenExpiry, long refreshTokenExpiry) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    // Generate access token (15 minutes)
    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(key)
                .compact();
    }

    // Generate refresh token (1 day)
    public String generateRefreshToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
                .signWith(key)
                .compact();
    }

    // Extract all claims
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(
                        ((Key) key).getEncoded().length > 0 ? ((Key) key).getEncoded() : new byte[0]))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extract subject (userId)
    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    // Extract a specific claim
    public String extractClaim(String token, String claimKey) {
        return extractClaims(token).get(claimKey, String.class);
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}