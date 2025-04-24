package com.bankingsystem.app.controller;

import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.service.interfaces.ExchangeRateServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/bank/exchange-rates")
public class ExchangeRateController {
    private final ExchangeRateServiceInterface exchangeRateService;

    public ExchangeRateController(ExchangeRateServiceInterface exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public ResponseEntity<ExchangeRateEntity> getExchangeRate(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) LocalDate date
            )
    {
        if (from == null || to == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Currency fromCurrency = Currency.valueOf(from);
        Currency toCurrency = Currency.valueOf(to);

         if(date != null && date.isAfter(LocalDate.now())){
             return ResponseEntity
                     .status(HttpStatus.BAD_REQUEST)
                     .body(null);
         }

         LocalDate targetDate = (date == null) ? LocalDate.now() : date;
         Optional<ExchangeRateEntity> exchangeRateEntity = exchangeRateService.getExchangeRate(fromCurrency, toCurrency, targetDate);

         return exchangeRateEntity.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(null));
    }

    @PostMapping("/update")
    public ResponseEntity<ExchangeRateEntity> updateExchangeRate(
            @RequestParam String from,
            @RequestParam String to)
    {
        if (from == null || to == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        Currency fromCurrency = Currency.valueOf(from);
        Currency toCurrency = Currency.valueOf(to);

        ExchangeRateEntity rateEntity = exchangeRateService.updateExchangeRateManually(fromCurrency, toCurrency);

        return ResponseEntity
                .ok(rateEntity);
    }
}
