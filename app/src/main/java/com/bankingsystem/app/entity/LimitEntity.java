package com.bankingsystem.app.entity;

import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "limits")
public class LimitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "limit_sum") // сумма установленного лимита
    private BigDecimal limitSum;

    @NotNull
    @Column(name = "expense_category")
    @Enumerated(EnumType.STRING)
    private Category category;

    @NotNull
    @Column(name = "limit_datetime")
    private OffsetDateTime limitDateTime;

    @NotNull
    @Column(name = "limit_currency_shortname")
    private Currency limitCurrencyShortName;

    @NotNull
    @Column(name = "limit_remainder")
    private BigDecimal limitRemainder;

    @NotNull
    @Column(name = "account_id")
    private Long accountId;

}
