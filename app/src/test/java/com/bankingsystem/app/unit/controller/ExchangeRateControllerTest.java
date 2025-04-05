package com.bankingsystem.app.unit.controller;

import com.bankingsystem.app.controller.ExchangeRateController;
import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.services.interfaces.ExchangeRateServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

// TODO: заменить комментарии на @DisplayName

// с этой аннотацией лучше делать юнит-тесты,
// т.к. она не подгружает spring
// и чисто работает с контроллером и проверяет бизнес-логику
// MockitoExtension автоматически обеспечивает корректную инициализацию моков и их сброс между тестами. Это означает, что:
// Все моки (@Mock) будут созданы заново перед каждым тестом.
// Состояние моков (например, заданные стабы when(...).thenReturn(...)) не будет сохраняться между тестами.
@ExtendWith(MockitoExtension.class)
public class ExchangeRateControllerTest {
    // Указывает, что в этот объект нужно внедрить (инжектировать) все созданные моки
    @InjectMocks
    private ExchangeRateController exchangeRateController;
    @Mock
    private ExchangeRateServiceInterface exchangeRateService;
    private static final LocalDate VALID_DATE = LocalDate.now().minusYears(1);
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusYears(1);
    private static final String USD = "USD";
    private static final String EUR = "EUR";
    private static final String INVALID_CURRENCY = "XYZ";
    private static final BigDecimal RATE_VALUE = BigDecimal.valueOf(2.025);
    private static final OffsetDateTime UPDATE_TIME =
            OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

    @Test
    @DisplayName("Should return exchange rate with valid params")
    void shouldReturnExchangeRateWhenParamsAreValid() throws Exception {
        ExchangeRateEntity expectedRate = createExpectedRate(USD, EUR, LocalDate.now());

        when(exchangeRateService.getExchangeRate(
                Currency.USD,
                Currency.EUR,
                VALID_DATE))
                .thenReturn(Optional.of(expectedRate));

        ResponseEntity<ExchangeRateEntity> actualRate =
                exchangeRateController.getExchangeRate(USD, EUR, VALID_DATE);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualRate.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedRate);

        verify(exchangeRateService).getExchangeRate(Currency.valueOf(USD), Currency.valueOf(EUR), VALID_DATE);
    }

    @Test
    @DisplayName("Should return NOT_FOUND status when exchange rate is missing")
    void shouldReturnNotFoundStatusWhenRateIsMissing() throws Exception {
        when(exchangeRateService.getExchangeRate
                (Currency.USD, Currency.EUR, VALID_DATE))
                .thenReturn(Optional.empty());

        ResponseEntity<ExchangeRateEntity> actualRate =
                exchangeRateController.getExchangeRate(USD, EUR, VALID_DATE);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(actualRate.getBody()).isNull();

        verify(exchangeRateService).getExchangeRate(Currency.valueOf(USD), Currency.valueOf(EUR), VALID_DATE);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status when currency param is incorrect")
    void shouldReturnBadRequestStatusWhenCurrencyParamIsIncorrect() throws Exception {
        ResponseEntity<ExchangeRateEntity> actualRate =
                exchangeRateController.getExchangeRate(INVALID_CURRENCY, EUR, VALID_DATE);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status when currency param is null")
    void shouldReturnBadRequestStatusWhenCurrencyParamIsNull() throws Exception {
        ResponseEntity<ExchangeRateEntity> actualRate =
                exchangeRateController.getExchangeRate(null, EUR, VALID_DATE);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return exchange rate when date is today")
    void shouldReturnExchangeRateWhenDateIsToday() {
        ExchangeRateEntity expectedRate = createExpectedRate(USD, EUR, LocalDate.now());

        when(exchangeRateService.getExchangeRate(Currency.valueOf(USD), Currency.valueOf(EUR), LocalDate.now()))
            .thenReturn(Optional.of(expectedRate));

        ResponseEntity<ExchangeRateEntity> actualRate =
            exchangeRateController.getExchangeRate(USD, EUR, LocalDate.now());

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualRate.getBody()).usingRecursiveComparison().isEqualTo(expectedRate);

        verify(exchangeRateService).getExchangeRate(Currency.valueOf(USD), Currency.valueOf(EUR), LocalDate.now());
    }

    @Test
    @DisplayName("Should return INTERNAL_SERVER_ERROR from service")
    void shouldReturnInternalServerErrorWhenServiceThrowsException() {
        when(exchangeRateService.getExchangeRate(Currency.valueOf(USD), Currency.valueOf(EUR), VALID_DATE))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<ExchangeRateEntity> actualRate =
            exchangeRateController.getExchangeRate(USD, EUR, VALID_DATE);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should return exchange rate when date param is missing")
    void shouldReturnExchangeRateWhenDateParamIsMissing() throws Exception {
        ExchangeRateEntity expectedRate = createExpectedRate(USD, EUR, LocalDate.now());

        when(exchangeRateService
                .getExchangeRate(Currency.USD, Currency.EUR, LocalDate.now()))
                .thenReturn(Optional.of(expectedRate));

        ResponseEntity<ExchangeRateEntity> actualRate =
                exchangeRateController.getExchangeRate(USD, EUR, null);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualRate.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedRate);

        verify(exchangeRateService).getExchangeRate(Currency.valueOf(USD), Currency.valueOf(EUR), LocalDate.now());
    }

    @Test
    @DisplayName("Should return exchange rate when date is from future")
    void shouldReturnBadRequestWhenDateIsFromFuture() throws Exception {
        ResponseEntity<ExchangeRateEntity> actualRate =
                exchangeRateController.getExchangeRate(USD, EUR, FUTURE_DATE);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should successfully update exchange rate")
    void shouldSuccessfullyUpdateExchangeRate() throws Exception {
        ExchangeRateEntity expectedRate = createExpectedRate(USD, EUR, LocalDate.now());

        when(exchangeRateService.updateExchangeRateManually
                (Currency.USD, Currency.EUR)).
                thenReturn(expectedRate);

        ResponseEntity<ExchangeRateEntity> actualRate =
                exchangeRateController.updateExchangeRate(USD, EUR);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualRate.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedRate);

        verify(exchangeRateService).updateExchangeRateManually(Currency.valueOf(USD), Currency.valueOf(EUR));
    }

    @Test
    @DisplayName("Should throw exception while updating exchange rate")
    void shouldThrowAnExceptionWhileUpdatingExchangeRate() throws Exception {
        when(exchangeRateService.updateExchangeRateManually(Currency.USD, Currency.EUR))
                .thenThrow(new RuntimeException("Failed to fetch exchange rate"));

        ResponseEntity<ExchangeRateEntity> actualRate =
                exchangeRateController.updateExchangeRate(USD, EUR);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status while updating exchange rate")
    void shouldReturnBadRequestStatusWhileUpdatingExchangeRate() throws Exception {
        ResponseEntity<ExchangeRateEntity> actualRate =
                exchangeRateController.updateExchangeRate(INVALID_CURRENCY, EUR);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status while updating exchange rate and currency param equals null")
    void shouldReturnBadRequestStatusWhileUpdatingExchangeRateAndCurrencyParamIsNull() throws Exception {
        ResponseEntity<ExchangeRateEntity> actualRate =
                exchangeRateController.updateExchangeRate(null, EUR);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return INTERNAL_SERVER_ERROR while updating exchange rate and service throws exception")
    void shouldReturnInternalServerErrorWhileUpdatingExchangeRateAndWhenServiceThrowsException() {
        when(exchangeRateService.updateExchangeRateManually(Currency.valueOf(USD), Currency.valueOf(EUR)))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<ExchangeRateEntity> actualRate =
            exchangeRateController.updateExchangeRate(USD, EUR);

        assertThat(actualRate.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ExchangeRateEntity createExpectedRate(String currencyFrom, String currencyTo, LocalDate rateDate) {
        ExchangeRateEntity rate = new ExchangeRateEntity();
        rate.setRate(RATE_VALUE);
        rate.setRateDate(rateDate);
        rate.setUpdateTime(UPDATE_TIME);
        rate.setCurrencyFrom(Currency.valueOf(currencyFrom));
        rate.setCurrencyTo(Currency.valueOf(currencyTo));
        return rate;
    }
}
