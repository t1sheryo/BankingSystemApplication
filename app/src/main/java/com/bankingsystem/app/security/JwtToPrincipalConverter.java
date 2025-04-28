package com.bankingsystem.app.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.catalina.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtToPrincipalConverter {
    public UserPrincipal convert(DecodedJWT jwt) {
        return UserPrincipal.builder()
                .userId(Long.valueOf(jwt.getSubject()))
                .username(jwt.getClaim("username").asString())
                .authorities(extractAuthoritiesFromClaim(jwt))
                .build();
    }

    private List<SimpleGrantedAuthority> extractAuthoritiesFromClaim(DecodedJWT jwt) {
        var claim = jwt.getClaim("authorities");

        if(claim == null || claim.isMissing()) return List.of();

        return claim.asList(SimpleGrantedAuthority.class);
    }
}
