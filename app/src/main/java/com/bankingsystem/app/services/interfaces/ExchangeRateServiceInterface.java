package com.bankingsystem.app.services.interfaces;

import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.enums.Currency;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateServiceInterface {
    public ExchangeRateEntity updateExchangeRateManually(Currency currencyFrom, Currency currencyTo);
    public void updateExchangeRateAutomatically();
    public Optional<ExchangeRateEntity> getExchangeRate(Currency from, Currency to, LocalDate date);
}
