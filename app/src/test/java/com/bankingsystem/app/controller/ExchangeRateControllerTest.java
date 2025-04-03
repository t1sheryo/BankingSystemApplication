package com.bankingsystem.app.controller;

import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.services.impl.ExchangeRateService;
import com.bankingsystem.app.services.interfaces.ExchangeRateServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@WebMvcTest(ExchangeRateController.class)
public class ExchangeRateControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ExchangeRateServiceInterface exchangeRateService;

    // конфигурация для добавления мока(зависимости)
    // сервиса для нашего контроллера
    @TestConfiguration
    static class ExchangeRateControllerTestContextConfiguration {
        @Bean
        public ExchangeRateServiceInterface exchangeRateService() {
            return mock(ExchangeRateServiceInterface.class);
        }
    }

    @BeforeEach
    void setUp() {
        // Сбрасываем состояние мока перед каждым тестом
        Mockito.reset(exchangeRateService);
    }

    // Тест для getExchangeRate()
    // успешный случай
    @Test
    void getExchangeRateWithCorrectParams() throws Exception {

        ExchangeRateEntity exchangeRate = new ExchangeRateEntity();
        exchangeRate.setRate(BigDecimal.valueOf(2.025));
        exchangeRate.setRateDate(LocalDate.of(2024, 1, 1));
        exchangeRate.setUpdateTime(OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        exchangeRate.setCurrencyFrom(Currency.USD);
        exchangeRate.setCurrencyTo(Currency.EUR);

        when(exchangeRateService.getExchangeRate
                (Currency.USD, Currency.EUR, LocalDate.of(2024, 1, 1)))
                .thenReturn(Optional.of(exchangeRate));

        mockMvc.perform(get("/bank/exchange-rates")
                    .param("from", "USD")
                    .param("to", "EUR")
                    .param("date", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(2.025))
                .andExpect(jsonPath("$.rateDate").value("2024-01-01"))
                .andExpect(jsonPath("$.currencyFrom").value("USD"))
                .andExpect(jsonPath("$.currencyTo").value("EUR"))
                .andExpect(jsonPath("$.updateTime").value("2024-01-01T12:00:00Z"));
    }

    // Тест для getExchangeRate()
    // курс не найден (возвращается Optional.empty())
    @Test
    void getExchangeRateWhenAppropriateRateWasNotFound() throws Exception {

        when(exchangeRateService.getExchangeRate
                (Currency.USD, Currency.EUR, LocalDate.of(2024, 1, 1)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/bank/exchange-rates")
                    .param("from", "USD")
                    .param("to", "EUR")
                    .param("date", "2024-01-01"))
                .andExpect(status().isNotFound());
    }

    // Тест для getExchangeRate()
    // неправильный параметр CurrencyFrom
    @Test
    void getExchangeRateWithIncorrectParamCurrencyFrom() throws Exception {
        mockMvc.perform(get("/bank/exchange-rates")
                    .param("from", "XYZ")
                    .param("to", "EUR")
                    .param("date", "2024-01-01"))
                .andExpect(status().isBadRequest());
    }

    // Тест для getExchangeRate()
    // неправильный параметр CurrencyTo
    @Test
    void getExchangeRateWithIncorrectParamCurrencyTo() throws Exception {
        mockMvc.perform(get("/bank/exchange-rates")
                    .param("from", "USD")
                    .param("to", "XYZ")
                    .param("date", "2024-01-01"))
                .andExpect(status().isBadRequest());
    }

    // Тест для getExchangeRate()
    // неправильный параметр Date
    @Test
    void getExchangeRateWithIncorrectParamDate() throws Exception {
        mockMvc.perform(get("/bank/exchange-rates")
                    .param("from", "USD")
                    .param("to", "EUR")
                    .param("date", "2024/01/01"))
                .andExpect(status().isBadRequest());
    }

    // Тест для getExchangeRate()
    // успешный случай, когда отсутствует необязательный параметр Date
    @Test
    void getExchangeRateSuccessfullyWithAbsenceOfDate() throws Exception {

        ExchangeRateEntity exchangeRate = new ExchangeRateEntity();
        exchangeRate.setRate(BigDecimal.valueOf(2.025));
        exchangeRate.setRateDate(LocalDate.of(2024, 1, 1));
        exchangeRate.setUpdateTime(OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        exchangeRate.setCurrencyFrom(Currency.USD);
        exchangeRate.setCurrencyTo(Currency.EUR);

        when(exchangeRateService.getExchangeRate
                (Currency.USD, Currency.EUR, LocalDate.now()))
                .thenReturn(Optional.of(exchangeRate));

        mockMvc.perform(get("/bank/exchange-rates")
                    .param("from", "USD")
                    .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(2.025))
                .andExpect(jsonPath("$.rateDate").value("2024-01-01"))
                .andExpect(jsonPath("$.currencyFrom").value("USD"))
                .andExpect(jsonPath("$.currencyTo").value("EUR"))
                .andExpect(jsonPath("$.updateTime").value("2024-01-01T12:00:00Z"));
    }

    // Тест для getExchangeRate()
    // отсутствует обязательный параметр
    @Test
    void getExchangeRateWithAbsenceOfDate() throws Exception {

        mockMvc.perform(get("/bank/exchange-rates")
                    .param("from", "USD")
                    .param("date", "2024-01-01"))
                .andExpect(status().isBadRequest());
    }

    // Тест для updateExchangeRate()
    // успешный случай
    @Test
    void updateExchangeRateSuccessfully() throws Exception {

        ExchangeRateEntity exchangeRate = new ExchangeRateEntity();
        exchangeRate.setRate(BigDecimal.valueOf(1.95));
        exchangeRate.setRateDate(LocalDate.of(2024, 1, 1));
        exchangeRate.setUpdateTime(OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        exchangeRate.setCurrencyFrom(Currency.USD);
        exchangeRate.setCurrencyTo(Currency.EUR);

        when(exchangeRateService.updateExchangeRateManually
                (Currency.USD, Currency.EUR)).
                thenReturn(exchangeRate);

        mockMvc.perform(post("/bank/exchange-rates/update")
                    .param("from", "USD")
                    .param("to", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(1.95))
                .andExpect(jsonPath("$.rateDate").value("2024-01-01"))
                .andExpect(jsonPath("$.currencyFrom").value("USD"))
                .andExpect(jsonPath("$.currencyTo").value("EUR"))
                .andExpect(jsonPath("$.updateTime").value("2024-01-01T12:00:00Z"));
    }

    // Тест для updateExchangeRate()
    // неудачный случай, когда сервис выбрасывает исключение
    @Test
    void updateExchangeRateAndServiceThrowsException() throws Exception {

        when(exchangeRateService.updateExchangeRateManually(Currency.USD, Currency.EUR))
                .thenThrow(new RuntimeException("Failed to fetch exchange rate"));

        mockMvc.perform(post("/bank/exchange-rates/update")
                    .param("from", "USD")
                    .param("to", "EUR"))
                .andExpect(status().isInternalServerError());
    }

    // Тест для updateExchangeRate()
    // некорректный параметр
    @Test
    void updateExchangeRateWithIncorrectParam() throws Exception {

        mockMvc.perform(post("/bank/exchange-rates/update")
                    .param("from", "XYZ")
                    .param("to", "EUR"))
                .andExpect(status().isBadRequest());
    }

    // Тест для updateExchangeRate()
    // отсутствует параметр
    @Test
    void updateExchangeRateWithAbsenceOfParam() throws Exception {

        mockMvc.perform(post("/bank/exchange-rates/update")
                    .param("to", "EUR"))
                .andExpect(status().isBadRequest());
    }
}
