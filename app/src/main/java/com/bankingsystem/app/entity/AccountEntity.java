package com.bankingsystem.app.entity;

import com.bankingsystem.app.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

// TODO:  добавить индексы для ускорения работы бд с сущностями

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "username should not be null")
    @Column(name = "user_username", unique = true, nullable = false)
    @Length(max = 100, message = "username could not be more than 100 symbols")
    private String username;

    // Эта аннотация не десериализирует поле в json
    // когда объект передается пользователю
    @JsonIgnore
    @NotBlank(message = "password should not be null")
    @Length(max = 255, message = "username could not be more than 100 symbols")
    @Column(name = "user_password", nullable = false)
    private String password;

    // FIXME: можно расширить до листа ролей у пользователя
    @NotNull(message = "user roles should not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private Role role;
}
