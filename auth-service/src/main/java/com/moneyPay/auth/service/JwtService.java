package com.moneyPay.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * JWT utility service — issues access tokens and validates them.
 * Access tokens are short-lived (default 15 min).
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiry-minutes:15}")
    private long expiryMinutes;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /** Generate a signed access token for the given user. */
    public String generateAccessToken(String userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claims(Map.of("email", email, "role", role))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiryMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey())
                .compact();
    }

    /** Parse and return all claims from a token. Throws JwtException if invalid. */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
