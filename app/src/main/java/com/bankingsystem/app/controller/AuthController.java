package com.bankingsystem.app.controller;

import com.bankingsystem.app.model.login.LoginRequest;
import com.bankingsystem.app.model.login.LoginResponse;
import com.bankingsystem.app.security.JWTIssuer;
import com.bankingsystem.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// TODO: нужно сделать контроллер и сервис для регистрации пользователя

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JWTIssuer jwtIssuer;
    private final AuthenticationManager authenticationManager;

    // пользователь сначала должен пройти по этому адресу чтобы получить свой токен,
    // а уже потом он должен используя этот токен проводить запросы на другие эндпоинты
    @PostMapping("/auth/login")
    // @Validated вместо @Valid чтобы в будущем делать группы валидаций конкретные
    public LoginResponse login(@RequestBody @Validated LoginRequest request) {
        // AuthenticationManager настраивается как ProviderManager,
        // который хранит цепочку AuthenticationProvider (в нашем случае — DaoAuthenticationProvider).
        // DaoAuthenticationProvider вызывает customUserDetailService.loadUserByUsername(username).
        // Spring-контейнер находит бим CustomUserDetailService, внедряет AccountService.
        //
        // loadUserByUsername запрашивает AccountEntity из БД (через accountService.getAccountByUsername).
        //
        // Конструирует UserPrincipal (класс, реализующий UserDetails), устанавливает:
        // - userId (собственное поле)
        // - username
        // - password (BCrypt-хеш из БД)
        // - список SimpleGrantedAuthority на основе поля role
        //
        // Проверка пароля:
        // DaoAuthenticationProvider берёт хеш из userDetails.getPassword()
        // и сравнивает его с request.getPassword() через PasswordEncoder.matches.
        // В случае несоответствия бросается BadCredentialsException.
        //При успешной проверке DaoAuthenticationProvider создаёт
        // новый UsernamePasswordAuthenticationToken с полями:
        // - Principal = UserPrincipal (из шага 3.2)
        // - Credentials = null (пароль обнуляется)
        // - Authorities = список ролей
        // - Поле authenticated = true.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // SecurityContextHolder использует ThreadLocal
        // для хранения SecurityContext на время обработки текущего HTTP-запроса.
        // Дальнейшие компоненты Spring Security (фильтры, аннотации @PreAuthorize и т. д.)
        // будут опираться на это Authentication.
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var token = jwtIssuer.issue(principal.getUserId(), request.getUsername(), roles);

        return LoginResponse.builder()
                .accessToken(token)
                .build();
    }
}
