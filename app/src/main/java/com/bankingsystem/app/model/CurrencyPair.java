package com.bankingsystem.app.model;

import com.bankingsystem.app.enums.Currency;
import lombok.Getter;
import lombok.Setter;

// Вспомогательный класс для хранения пар
// { currencyFrom, currencyTo }
public record CurrencyPair(Currency from, Currency to) {
}