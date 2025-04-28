package com.bankingsystem.app.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

// Какую роль играет UserPrincipalAuthenticationToken?
// UserPrincipalAuthenticationToken выполняет следующие функции:
// Представление аутентифицированного пользователя:
// После успешной проверки JWT (через JwtDecoder и JwtToPrincipalConverter)
// этот класс создается для представления аутентифицированного пользователя.
// Он сохраняется в SecurityContextHolder,
// чтобы Spring Security мог использовать его для проверки
// доступа к эндпоинтам (например, /bank/limits).
// Связь UserPrincipal с Spring Security:
// UserPrincipal (реализация UserDetails) содержит данные пользователя,
// но Spring Security работает с объектами Authentication.
// UserPrincipalAuthenticationToken выступает как мост, передавая UserPrincipal в SecurityContextHolder.
// Поддержка JWT-аутентификации:
// В вашем API аутентификация основана на JWT, а не на логине/пароле для каждого запроса.
// UserPrincipalAuthenticationToken позволяет Spring Security понять,
// что пользователь уже аутентифицирован (через JWT), без проверки пароля.
// Предоставление данных контроллерам:
// Контроллеры (например, BankController) могут извлечь UserPrincipal из
// UserPrincipalAuthenticationToken через Authentication,
// чтобы использовать userId или username для бизнес-логики.
public class UserPrincipalAuthenticationToken extends AbstractAuthenticationToken {
    private final UserPrincipal principal;
    public UserPrincipalAuthenticationToken(UserPrincipal principal) {
        super(principal.getAuthorities());
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public UserPrincipal getPrincipal() {
        return principal;
    }
}
