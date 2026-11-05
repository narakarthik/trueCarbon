package com.truecarbon.user.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public String generateAccessToken(UserDetails user, Map<String, Object> extra) {
        return build(user, extra, accessExpirationMs);
    }

    public String generateRefreshToken(UserDetails user) {
        return build(user, Map.of(), refreshExpirationMs);
    }

    private String build(UserDetails user, Map<String, Object> extra, long ttl) {
        return Jwts.builder()
            .claims(extra)
            .subject(user.getUsername())
            .id(UUID.randomUUID().toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + ttl))
            .signWith(key())
            .compact();
    }

    public String extractUsername(String token) {
        return claim(token, Claims::getSubject);
    }

    public boolean isValid(String token, UserDetails user) {
        try {
            return extractUsername(token).equals(user.getUsername()) && !isExpired(token);
        } catch (JwtException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    private boolean isExpired(String token) {
        return claim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T claim(String token, Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parser().verifyWith(key()).build()
            .parseSignedClaims(token).getPayload());
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
