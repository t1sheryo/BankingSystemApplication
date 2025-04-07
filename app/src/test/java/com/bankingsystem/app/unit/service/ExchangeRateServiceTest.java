package com.bankingsystem.app.unit.service;

import com.bankingsystem.app.config.TwelveDataConfig;
import com.bankingsystem.app.entity.ExchangeRateCompositePrimaryKey;
import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.ExchangeRateDTO;
import com.bankingsystem.app.repository.ExchangeRateRepository;
import com.bankingsystem.app.service.impl.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {
    @InjectMocks
    private ExchangeRateService exchangeRateService;
    @Mock
    private TwelveDataConfig twelveDataConfig;
    @Mock
    private ExchangeRateRepository exchangeRateRepository;
    @Mock
    private RestTemplate restTemplate;

    private static final String API_KEY = "7a79c306727443819a002da0398f5ce7";
    private static final String API_URL = "https://api.twelvedata.com";

    private static final Currency CURRENCY_FROM = Currency.USD;
    private static final Currency CURRENCY_TO = Currency.EUR;
    private static final ExchangeRateCompositePrimaryKey COMPOSITE_ID =
            new ExchangeRateCompositePrimaryKey(CURRENCY_FROM, CURRENCY_TO);
    private static final BigDecimal RATE =
            new BigDecimal("1.25");
    private static final LocalDate RATE_DATE =
            LocalDate.now();
    private static final OffsetDateTime UPDATE_TIME =
            OffsetDateTime.now(ZoneOffset.UTC);
    private static final String URL =
            String.format("%s/exchange_rate?symbol=%s/%s&apikey=%s",
                API_URL, CURRENCY_FROM, CURRENCY_TO, API_KEY);

    @BeforeEach
    void setUp() {
        when(twelveDataConfig.getApiKey())
                .thenReturn(API_KEY);
        when(twelveDataConfig.getApiUrl())
                .thenReturn(API_URL);
    }

    @Test
    @DisplayName("Should successfully update rate if it already exists")
    void shouldSuccessfullyUpdateRateIfItAlreadyExists() {
        ExchangeRateDTO response = createExchangeRateDTO();
        ExchangeRateEntity beforeUpdateExchangeRateEntity = createExchangeRateEntity();
        ExchangeRateEntity expectedEntity = createExchangeRateEntity();

        when(restTemplate.getForObject(URL, ExchangeRateDTO.class))
                .thenReturn(response);
        when(exchangeRateRepository.findByIdAndRateDate(COMPOSITE_ID, RATE_DATE))
                .thenReturn(Optional.of(beforeUpdateExchangeRateEntity));
        when(exchangeRateRepository.save(any(ExchangeRateEntity.class)))
                .thenReturn(expectedEntity);

        ExchangeRateEntity actualEntity =
                exchangeRateService.updateExchangeRateManually(CURRENCY_FROM, CURRENCY_TO);

        assertThat(actualEntity)
                .usingRecursiveComparison()
                .isEqualTo(expectedEntity);

        verify(restTemplate).getForObject(URL, ExchangeRateDTO.class);
        verify(exchangeRateRepository).save(any(ExchangeRateEntity.class));
        verify(exchangeRateRepository).findByIdAndRateDate(COMPOSITE_ID, RATE_DATE);
    }

    private ExchangeRateDTO createExchangeRateDTO() {
        ExchangeRateDTO exchangeRateDTO = new ExchangeRateDTO();
        exchangeRateDTO.setRate(RATE);

        return exchangeRateDTO;
    }

    private ExchangeRateEntity createExchangeRateEntity() {
        ExchangeRateEntity exchangeRateEntity = new ExchangeRateEntity();
        exchangeRateEntity.setId(COMPOSITE_ID);
        exchangeRateEntity.setRate(RATE);
        exchangeRateEntity.setRateDate(RATE_DATE);
        exchangeRateEntity.setUpdateTime(UPDATE_TIME);

        return exchangeRateEntity;
    }
}
