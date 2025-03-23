package com.bankingsystem.app.model;

import com.bankingsystem.app.enums.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.bankingsystem.app.enums.Currency;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @NotNull(message = "Account Id field must not be null")
    @Positive(message = "Account Id must be positive")
    private Long accountIdFrom;
    @NotNull(message = "Account Id field must not be null")
    @Positive(message = "Account Id must be positive")
    private Long accountIdTo;
    @NotNull(message = "Currency field must not be null")
    private Currency currency;
    @NotNull(message = "Category field must not be null")
    private Category category;
    @NotNull(message = "Amount field must not be null")
    @DecimalMin(value = "0.001", message = "Transaction value must be over 0.001 unit of currency")
    private BigDecimal amount;
    @NotNull(message = "Transaction time field must not be null")
    @PastOrPresent(message = "Transaction time must be in the past or present")
    private LocalDateTime transactionTime;
}
