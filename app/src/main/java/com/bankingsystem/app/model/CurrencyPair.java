package com.bankingsystem.app.model;

import com.bankingsystem.app.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class CurrencyPair {
    private Currency currencyFrom;
    private Currency currencyTo;
}