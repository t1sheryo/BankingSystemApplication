package com.bankingsystem.app.entity;

import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transactions",indexes = {
        @Index(name = "idx_account_from_category", columnList = "account_from, expense_category"),
        @Index(name = "idx_transaction_time",columnList = "datetime")
})
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Account Id field must not be null")
    @JoinColumn(name = "account_from", nullable = false)
    @ManyToOne
    private AccountEntity accountFrom;

    @NotNull(message = "Account Id field must not be null")
    @JoinColumn(name = "account_to", nullable = false)
    @ManyToOne
    private AccountEntity accountTo;

    @NotNull(message = "Currency field must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_shortname", nullable = false)
    private Currency currency;

    @NotNull(message = "Category field must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_category", nullable = false)
    private Category category;

    @NotNull(message = "Amount field must not be null")
    @DecimalMin(value = "0.001", message = "Transaction value must be over 0.001 unit of currency")
    @Column(name = "sum", nullable = false)
    private BigDecimal sum;

    @NotNull(message = "Transaction time field must not be null")
    @PastOrPresent(message = "Transaction time must be in the past or present")
    @Column(name = "datetime", nullable = false)
    private OffsetDateTime transactionTime;

    @Column(name = "limit_exceeded", nullable = false)
    private boolean limitExceeded;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "limit_id", nullable = false)
    private LimitEntity limit;


    // эти поля нужны для фиксации даты и времени, суммы на ремайндере и валюты лимита в рамках текущей транзакции
    // необходимо для независимости транзакции от изменений в таблице limit
    @NotNull
    @Column(name = "limit_datatime_at_time", nullable = false)
    private OffsetDateTime limitDateTimeAtTime;

    @NotNull
    @Column(name = "limit_sum_at_time", nullable = false)
    private BigDecimal limitSumAtTime;

    @NotNull
    @Column(name = "limit_currency_at_time", nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency limitCurrencyAtTime;
}
