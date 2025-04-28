package com.bankingsystem.app.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JWTIssuer {
    private final JwtProperties properties;

    // метод который возвращает готовый токен авторизации пользователю
    // используя в качестве идентификатора userId
    // и храня в себе также некоторые другие параметры пользователя
    public String issue(long userId, String username, List<String> roles){
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withExpiresAt(Instant.now().plus(Duration.of(1, ChronoUnit.DAYS)))
                .withClaim("username", username)
                .withClaim("roles", roles)
                .sign(Algorithm.HMAC256(properties.getSecretKey()));
    }
}
