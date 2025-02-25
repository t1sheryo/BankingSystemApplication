package com.bankingsystem.app.model;

import com.bankingsystem.app.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRate {
    private Currency currencyFrom;
    private Currency currencyTo;
    private BigDecimal rate;
    private LocalDateTime timestamp;
}
