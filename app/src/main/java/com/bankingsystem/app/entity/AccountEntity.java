package com.bankingsystem.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
    @Column(unique = true, nullable = false)
    private String username;

    // Эта аннотация не десериализирует поле в json
    // когда объект передается пользователю
    @JsonIgnore
    @NotBlank(message = "password should not be null")
    @Column(nullable = false)
    private String password;

    // FIXME: можно расширить до листа ролей у пользователя
    @NotBlank(message = "user roles should not be null")
    @Column(nullable = false)
    private String role;
}
