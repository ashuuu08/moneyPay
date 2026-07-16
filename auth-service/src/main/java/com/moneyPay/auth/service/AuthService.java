package com.moneyPay.auth.service;

import com.moneyPay.auth.domain.RefreshToken;
import com.moneyPay.auth.domain.User;
import com.moneyPay.auth.dto.*;
import com.moneyPay.auth.repository.RefreshTokenRepository;
import com.moneyPay.auth.repository.UserRepository;
import com.moneyPay.common.exception.MoneyPayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Core authentication service handling registration, login, token refresh, and logout.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-expiry-days:7}")
    private long refreshExpiryDays;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new MoneyPayException("Email already registered", HttpStatus.CONFLICT, "EMAIL_TAKEN");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new MoneyPayException("Phone already registered", HttpStatus.CONFLICT, "PHONE_TAKEN");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.valueOf(request.getRole() != null ? request.getRole() : "CUSTOMER"))
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} [{}]", user.getEmail(), user.getRole());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MoneyPayException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new MoneyPayException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new MoneyPayException("Account is not active", HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE");
        }

        // Revoke old refresh tokens and issue fresh ones
        refreshTokenRepository.revokeAllByUser(user);
        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new MoneyPayException("Invalid refresh token", HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN"));

        if (stored.isRevoked() || stored.isExpired()) {
            throw new MoneyPayException("Refresh token expired or revoked", HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID");
        }

        // Token rotation — revoke old, issue new
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(stored.getUser());
    }

    @Transactional
    public void logout(String userId) {
        userRepository.findById(UUID.fromString(userId)).ifPresent(user -> {
            refreshTokenRepository.revokeAllByUser(user);
            log.info("User logged out: {}", user.getEmail());
        });
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId().toString(), user.getEmail(), user.getRole().name());

        RefreshToken refreshToken = refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .token(UUID.randomUUID().toString())
                        .expiresAt(Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS))
                        .build());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
