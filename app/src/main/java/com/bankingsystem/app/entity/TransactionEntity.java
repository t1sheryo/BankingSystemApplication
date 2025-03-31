package com.bankingsystem.app.entity;

import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transactions")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //FIXME:
    // поменять поля на AccountEntity и добавить @ManyToOne
    @NotNull(message = "Account Id field must not be null")
    @Positive(message = "Account Id must be positive")
    @Column(name = "account_from", nullable = false)
    private Long accountIdFrom;

    @NotNull(message = "Account Id field must not be null")
    @Positive(message = "Account Id must be positive")
    @Column(name = "account_to", nullable = false)
    private Long accountIdTo;


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
}
