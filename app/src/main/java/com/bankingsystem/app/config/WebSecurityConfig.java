package com.bankingsystem.app.config;

import com.bankingsystem.app.security.JwtAuthenticationFilter;
import com.bankingsystem.app.service.impl.CustomUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailService customUserDetailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Эта строка добавляет кастомный фильтр JwtAuthenticationFilter
        // в цепочку фильтров Spring Security перед стандартным фильтром UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // http - это основной объект конфигурации безопасности Spring Security.
        // тут задается так называемая security filter chain, где последовательно
        // выполняются проверки условий авторизации и другие различные параметры.
        // cors - это то что позволяет делать запросы с других доменов.
        // нам тут это не надо потому что пока что это лишнее
        // csrf - это дополнительная проверка того что пользователь не был взломан и т.д.
        // нам тут это не надо потому что мы используем stateless состояние и
        // jwt токены которые пользователь должен вставлять при каждом запросе
        // stateless - это состояние когда отсутствует сохранение данных в сессии пользователя
        // и он каждый раз должен передавать jwt токен для доступа к эндпоинтам
        // permitAll() разрешает доступ к указанным адресам без авторизации
        // anyRequest().authenticated() говорит что для доступа ко всем остальным адресам
        // необходима авторизация пользователя
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/auth/login").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Этот метод создает и возвращает бин AuthenticationManager,
    // который отвечает за аутентификацию пользователей в Spring Security.
    // Ключевой элемент здесь — объект AuthenticationConfiguration,
    // который является частью инфраструктуры Spring Security и
    // автоматически настраивает AuthenticationManager на основе доступных компонентов
    // в контексте приложения.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
