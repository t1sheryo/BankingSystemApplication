package com.bankingsystem.app.service.impl;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountEntity user = accountService.getAccountByUsername(username).orElseThrow(
                () -> new IllegalStateException("User not found with username: " + username)
                );

        return UserPrincipal.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .password(user.getPassword())
                .build();
    }
}
