package com.truecarbon.trading.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class TradingUserPrincipal implements UserDetails {

    private final UUID   userId;
    private final String email;

    public TradingUserPrincipal(UUID userId, String email) {
        this.userId = userId;
        this.email  = email;
    }

    @Override public String getUsername()                { return email; }
    @Override public String getPassword()                { return null; }
    @Override public boolean isEnabled()                 { return true; }
    @Override public boolean isAccountNonExpired()       { return true; }
    @Override public boolean isAccountNonLocked()        { return true; }
    @Override public boolean isCredentialsNonExpired()   { return true; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }
}