package com.bankingsystem.app.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// UserPrincipal создается на основе данных из JWT,
// чтобы Spring Security мог аутентифицировать пользователя без проверки пароля
// (так как пароль уже проверен при выдаче JWT через /auth/login)
// В отличие от стандартного User из Spring Security, UserPrincipal включает поле userId,
// что позволяет хранить уникальный идентификатор пользователя (например, для связи с базой данных)
@Getter
@Builder
public class UserPrincipal implements UserDetails {
    private final Long userId;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
