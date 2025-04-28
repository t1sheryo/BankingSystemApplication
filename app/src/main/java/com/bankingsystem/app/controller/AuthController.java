package com.bankingsystem.app.controller;

import com.bankingsystem.app.model.login.LoginRequest;
import com.bankingsystem.app.model.login.LoginResponse;
import com.bankingsystem.app.security.JWTIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// FIXME : надо добавить UserEntity, UserRepository, UserService,
//  чтобы можно было как то идентифицировать уже существующих пользователей
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JWTIssuer jwtIssuer;

    // пользователь сначала должен пройти по этому адресу чтобы получить свой токен
    // а уже потом он должен используя этот токен проводить запросы на другие эндпоинты
    @PostMapping("/auth/login")
    public LoginResponse login(@RequestBody @Validated LoginRequest request) {
        var token = jwtIssuer.issue(1L, request.getUsername(), List.of("USER"));

        return LoginResponse.builder()
                .accessToken(token)
                .build();
    }
}
