package com.bankingsystem.app.controller;

import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.services.impl.ExchangeRateService;
import com.bankingsystem.app.services.interfaces.ExchangeRateServiceInterface;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    // Тест для getExchangeRate()
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
}
