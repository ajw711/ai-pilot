package com.mcp.mcp_pilot.common.security;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.authorities = Collections.singleton(new SimpleGrantedAuthority(role));
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public @NonNull String  getUsername() {
        return username;
    }


}
