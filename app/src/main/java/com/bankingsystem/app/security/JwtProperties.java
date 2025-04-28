package com.bankingsystem.app.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// этот класс создан для того чтобы подтягивать из .yaml файла
// секретный ключ для токена
@Setter
@Getter
@Configuration
@ConfigurationProperties("security.jwt")
public class JwtProperties {
    private String secretKey;
}
