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
            log.warn("Currency parameter is null: from={}, to={}", from, to);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Currency fromCurrency = Currency.valueOf(from);
            Currency toCurrency = Currency.valueOf(to);

            log.info("getExchangeRate from: {} to: {} on date {}", fromCurrency, toCurrency, date);

             // Если дата некорректна
             if(date != null && date.isAfter(LocalDate.now())){
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
             }

             //определение текущей даты
             LocalDate targetDate = (date == null) ? LocalDate.now() : date;
             Optional<ExchangeRateEntity> exchangeRateEntity = exchangeRateService.getExchangeRate(fromCurrency, toCurrency, targetDate);
             //isPresent метод класса Optional который возращает true если внутри Optional есть значение
                //Optional != null
             if (exchangeRateEntity.isPresent()) {
                 exchangeRateService.saveExchangeRate(exchangeRateEntity.get());
                 return ResponseEntity.ok(exchangeRateEntity.get());
             }
             else {
                 log.warn("No exchange rate found for from: {} to: {} on date {}", fromCurrency, toCurrency, date);
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
             }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid currency code: from={}, to={}", from, to);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400
        } catch(Exception e){
            log.error("Error when getting ExchangeRate from: {} to: {}, exception name: {}", from, to, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //метод для обновления курса валют из внешнего API
    //и сохранения в базу данных
    @PostMapping("/update")
    public ResponseEntity<ExchangeRateEntity> updateExchangeRate(
     @RequestParam String from,
     @RequestParam String to)
    {
        if (from == null || to == null) {
            log.warn("Currency parameter is null: from={}, to={}", from, to);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Currency fromCurrency = Currency.valueOf(from);
            Currency toCurrency = Currency.valueOf(to);

            log.info("updateExchangeRate from: {} to: {}", fromCurrency, toCurrency);
            //получаем сущность через метод сервиса
            ExchangeRateEntity rateEntity = exchangeRateService.updateExchangeRateManually(fromCurrency, toCurrency);
            exchangeRateService.saveExchangeRate(rateEntity);
            return ResponseEntity.ok(rateEntity);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid currency code: from={}, to={}", from, to);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400
        } catch(Exception e){
            log.error("Failed to update exchangeRate from: {} to: {}, exception name: {}", from, to, e.getMessage());
            //обрабатываем исключение отправляя ошибку 500 Internal Server Error и пустое тело JSON
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
