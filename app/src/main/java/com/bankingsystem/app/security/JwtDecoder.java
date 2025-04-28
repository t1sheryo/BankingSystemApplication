package com.bankingsystem.app.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// когда jwt строка извлекается из текста запроса этот класс декодирует и валидирует токен
// потом передается в конвертер из jwt в userprincipal
@Component
@RequiredArgsConstructor
public class JwtDecoder {
    private final JwtProperties properties;
    public DecodedJWT decode(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(properties.getSecretKey()))
                .build();
        return verifier.verify(token);
    }
}
