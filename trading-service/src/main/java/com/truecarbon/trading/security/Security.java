package com.truecarbon.trading.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.*;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@Getter
public class Security implements UserDetails {
    private final UUID userId;
    private final String email;

    public Security(UUID userId, String email) {
        this.userId = userId;
        this.email  = email;
    }

    @Override public String getUsername()  { return email; }
    @Override public String getPassword()  { return null; }
    @Override public boolean isEnabled()   { return true; }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }
}

@Service
@Slf4j
class JwtService {
    @Value("${jwt.secret}") private String secret;

    String extractEmail(String token)  { return claim(token, Claims::getSubject); }
    String extractUserId(String token) { return claim(token, c -> c.get("userId", String.class)); }

    boolean isValid(String token) {
        try { Jwts.parser().verifyWith(key()).build().parseSignedClaims(token); return true; }
        catch (JwtException e) { log.debug("Invalid JWT: {}", e.getMessage()); return false; }
    }

    private <T> T claim(String token, Function<Claims, T> r) {
        return r.apply(Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload());
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
