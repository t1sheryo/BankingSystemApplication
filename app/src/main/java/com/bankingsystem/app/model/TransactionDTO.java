package com.bankingsystem.app.model;

import com.bankingsystem.app.enums.Category;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;
import com.bankingsystem.app.enums.Currency;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

// DTO(Data Transfer Object) класс необходимый для передачи данных между слоями приложения
// в нашем случае между сервисом и контроллером чтобы туда попадали только нужные поля с сущности
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    @JsonProperty("fromAccount")
    @NotNull(message = "Account Id field must not be null")
    @Positive(message = "Account Id must be positive")
    private Long accountIdFrom;

    @JsonProperty("toAccount")
    @NotNull(message = "Account Id field must not be null")
    @Positive(message = "Account Id must be positive")
    private Long accountIdTo;

    @NotNull(message = "Currency field must not be null")
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @NotNull(message = "Category field must not be null")
    @Enumerated(EnumType.STRING)
    private Category expenseCategory;

    @NotNull(message = "Amount field must not be null")
    @DecimalMin(value = "0.001", message = "Transaction value must be over 0.001 unit of currency")
    private BigDecimal sum;

    @NotNull(message = "Limit ID is required")
    @Positive()
    private Long limitId;
}
