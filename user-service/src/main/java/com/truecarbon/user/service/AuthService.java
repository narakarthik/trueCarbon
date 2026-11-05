package com.truecarbon.user.service;

import com.truecarbon.user.api.auth.AuthDtos.*;
import com.truecarbon.user.domain.*;
import com.truecarbon.user.repository.*;
import com.truecarbon.user.security.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository       userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder      encoder;
    private final JwtService           jwtService;
    private final AuthenticationManager authManager;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
            .name(req.name())
            .email(req.email())
            .passwordHash(encoder.encode(req.password()))
            .oauthProvider("LOCAL")
            .build();
        userRepo.save(user);
        log.info("New user registered: {}", user.getEmail());
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userRepo.findByEmail(req.email())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String rawToken) {
        RefreshToken token = refreshRepo.findByTokenAndRevokedFalse(rawToken)
            .orElseThrow(() -> new SecurityException("Invalid or expired refresh token"));

        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            token.setRevoked(true);
            refreshRepo.save(token);
            throw new SecurityException("Refresh token expired — please log in again");
        }

        // Rotate
        token.setRevoked(true);
        refreshRepo.save(token);
        return buildResponse(token.getUser());
    }

    @Transactional
    public void logout(String rawToken) {
        refreshRepo.revokeByToken(rawToken);
    }

    // Called from OAuth2 flow (future)
    @Transactional
    public User upsertOAuthUser(String email, String name, String oauthId) {
        return userRepo.findByEmail(email).orElseGet(() ->
            userRepo.save(User.builder()
                .name(name).email(email)
                .oauthProvider("GOOGLE").oauthId(oauthId)
                .build()));
    }

    private AuthResponse buildResponse(User user) {
        var principal   = new UserPrincipal(user);
        String access   = jwtService.generateAccessToken(principal,
            Map.of("userId", user.getId().toString()));
        String refresh  = createRefreshToken(user);
        return new AuthResponse(
            access, refresh, AuthResponse.BEARER, 900,
            new AuthResponse.UserInfo(
                user.getId().toString(), user.getName(), user.getEmail())
        );
    }

    private String createRefreshToken(User user) {
        var principal = new UserPrincipal(user);
        String raw    = jwtService.generateRefreshToken(principal);
        refreshRepo.save(RefreshToken.builder()
            .user(user).token(raw)
            .expiresAt(OffsetDateTime.now().plusSeconds(refreshExpirationMs / 1000))
            .build());
        return raw;
    }
}
