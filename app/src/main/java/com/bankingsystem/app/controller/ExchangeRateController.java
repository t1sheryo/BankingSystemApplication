package com.bankingsystem.app.controller;

import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.services.interfaces.ExchangeRateServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Optional;

// TODO: разобраться что тут к чему с сохранением в бд
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
            //тут параметр false для того чтобы пользователь мог узнать курс на текущее время
            // и запрос по времени тогда будет пустой
            //то есть позволяем либо запросить курс на конкретный день(с параметрами)
            // либо получить на текущий(без параметров)
            @RequestParam(required = false) LocalDate date
            )
    {
        if (from == null || to == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Currency fromCurrency = Currency.valueOf(from);
            Currency toCurrency = Currency.valueOf(to);

             if(date != null && date.isAfter(LocalDate.now())){
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
             }

             LocalDate targetDate = (date == null) ? LocalDate.now() : date;
             Optional<ExchangeRateEntity> exchangeRateEntity = exchangeRateService.getExchangeRate(fromCurrency, toCurrency, targetDate);
             log.info("Exchange rate from {} to {} : {}", fromCurrency, toCurrency, exchangeRateEntity);

             if (exchangeRateEntity.isPresent()) {
                 // FIXME : зачем эта строка
                 exchangeRateService.saveExchangeRate(exchangeRateEntity.get());
                 return ResponseEntity.ok(exchangeRateEntity.get());
             }
             else {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
             }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ExchangeRateEntity> updateExchangeRate(
     @RequestParam String from,
     @RequestParam String to)
    {
        if (from == null || to == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Currency fromCurrency = Currency.valueOf(from);
            Currency toCurrency = Currency.valueOf(to);

            // FIXME : написать тут логи с тем что обновилось. лень щас разбираться как тут работает
            ExchangeRateEntity rateEntity = exchangeRateService.updateExchangeRateManually(fromCurrency, toCurrency);
            exchangeRateService.saveExchangeRate(rateEntity);
            return ResponseEntity.ok(rateEntity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
