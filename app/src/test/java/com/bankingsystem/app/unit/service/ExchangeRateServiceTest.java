package com.bankingsystem.app.unit.service;

import com.bankingsystem.app.config.TwelveDataConfig;
import com.bankingsystem.app.entity.ExchangeRateCompositePrimaryKey;
import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.ExchangeRateDTO;
import com.bankingsystem.app.repository.ExchangeRateRepository;
import com.bankingsystem.app.service.impl.ExchangeRateService;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

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
    private static final BigDecimal RATE = new BigDecimal("1.25");
    private static final LocalDate RATE_DATE = LocalDate.now();
    private static final OffsetDateTime UPDATE_TIME = OffsetDateTime.now(ZoneOffset.UTC);
    private static final String URL = String.format("%s/exchange_rate?symbol=%s/%s&apikey=%s",
        API_URL, CURRENCY_FROM, CURRENCY_TO, API_KEY);

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(twelveDataConfig.getApiKey()).thenReturn(API_KEY);
        Mockito.lenient().when(twelveDataConfig.getApiUrl()).thenReturn(API_URL);
    }

    @Test
    @DisplayName("Should successfully update rate if it already exists")
    void shouldSuccessfullyUpdateRateIfItAlreadyExists() {
        ExchangeRateDTO response = createExchangeRateDTO();
        ExchangeRateEntity beforeUpdateExchangeRateEntity = createExchangeRateEntity();
        ExchangeRateEntity expectedEntity = createExchangeRateEntity();

        Mockito.when(restTemplate.getForObject(URL, ExchangeRateDTO.class)).thenReturn(response);
        Mockito.when(exchangeRateRepository.findByIdAndRateDate(COMPOSITE_ID, RATE_DATE))
               .thenReturn(Optional.of(beforeUpdateExchangeRateEntity));
        Mockito.when(exchangeRateRepository.save(Mockito.any(ExchangeRateEntity.class)))
               .thenReturn(expectedEntity);

        ExchangeRateEntity actualEntity = exchangeRateService.updateExchangeRateManually(CURRENCY_FROM, CURRENCY_TO);

        AssertionsForClassTypes.assertThat(actualEntity)
                .usingRecursiveComparison()
                .isEqualTo(expectedEntity);

        Mockito.verify(restTemplate).getForObject(URL, ExchangeRateDTO.class);
        Mockito.verify(exchangeRateRepository).save(Mockito.any(ExchangeRateEntity.class));
        Mockito.verify(exchangeRateRepository).findByIdAndRateDate(COMPOSITE_ID, RATE_DATE);
    }

    @Test
    @DisplayName("Should successfully update rate if it doesn't exist")
    void shouldSuccessfullyUpdateRateIfItDoesntExist() {
        ExchangeRateDTO response = createExchangeRateDTO();
        ExchangeRateEntity expectedEntity = createExchangeRateEntity();

        Mockito.when(restTemplate.getForObject(URL, ExchangeRateDTO.class)).thenReturn(response);
        Mockito.when(exchangeRateRepository.findByIdAndRateDate(COMPOSITE_ID, RATE_DATE))
               .thenReturn(Optional.empty());
        Mockito.when(exchangeRateRepository.save(Mockito.any(ExchangeRateEntity.class)))
               .thenReturn(expectedEntity);

        ExchangeRateEntity actualEntity = exchangeRateService.updateExchangeRateManually(CURRENCY_FROM, CURRENCY_TO);

        AssertionsForClassTypes.assertThat(actualEntity)
                .usingRecursiveComparison()
                .isEqualTo(expectedEntity);

        Mockito.verify(restTemplate).getForObject(URL, ExchangeRateDTO.class);
        Mockito.verify(exchangeRateRepository).save(Mockito.any(ExchangeRateEntity.class));
        Mockito.verify(exchangeRateRepository).findByIdAndRateDate(COMPOSITE_ID, RATE_DATE);
    }

    @Test
    @DisplayName("Should update many pairs of exchange rates successfully")
    void shouldSuccessfullyUpdateExchangeRates() {
        ExchangeRateEntity dummyEntity = createExchangeRateEntity();
        ExchangeRateService spiedService = Mockito.spy(
            new ExchangeRateService(twelveDataConfig, exchangeRateRepository, restTemplate));

        Mockito.doReturn(dummyEntity).when(spiedService)
               .updateExchangeRateManually(Mockito.any(Currency.class), Mockito.any(Currency.class));

        spiedService.updateExchangeRateAutomatically();

        for (Currency value1 : Currency.values()) {
            for (Currency value2 : Currency.values()) {
                if (!value1.equals(value2)) {
                    Mockito.verify(spiedService).updateExchangeRateManually(value1, value2);
                }
            }
        }
    }

    @Test
    @DisplayName("Should return exchange rate successfully")
    void shouldReturnExchangeRateSuccessfully() {
        ExchangeRateEntity expectedEntity = createExchangeRateEntity();

        Mockito.when(exchangeRateRepository.findByIdAndRateDate(COMPOSITE_ID, RATE_DATE))
               .thenReturn(Optional.of(expectedEntity));

        Optional<ExchangeRateEntity> actualEntity =
            exchangeRateService.getExchangeRate(CURRENCY_FROM, CURRENCY_TO, RATE_DATE);

        AssertionsForClassTypes.assertThat(actualEntity.get())
                .usingRecursiveComparison()
                .isEqualTo(expectedEntity);

        Mockito.verify(exchangeRateRepository).findByIdAndRateDate(COMPOSITE_ID, RATE_DATE);
    }

    @Test
    @DisplayName("Should return empty when exchange rate is not found")
    void shouldReturnEmptyWhenExchangeRateIsNotFound() {
        Mockito.when(exchangeRateRepository.findByIdAndRateDate(COMPOSITE_ID, RATE_DATE))
               .thenReturn(Optional.empty());

        Optional<ExchangeRateEntity> actualEntity =
            exchangeRateService.getExchangeRate(CURRENCY_FROM, CURRENCY_TO, RATE_DATE);

        AssertionsForClassTypes.assertThat(actualEntity).isEmpty();
        Mockito.verify(exchangeRateRepository).findByIdAndRateDate(COMPOSITE_ID, RATE_DATE);
    }

    @Test
    @DisplayName("Should use correct composite key")
    void shouldUseCorrectCompositeKey() {
        ExchangeRateEntity entity = createExchangeRateEntity();

        Mockito.when(exchangeRateRepository.findByIdAndRateDate(Mockito.any(), Mockito.eq(RATE_DATE)))
               .thenReturn(Optional.of(entity));

        exchangeRateService.getExchangeRate(CURRENCY_FROM, CURRENCY_TO, RATE_DATE);

        ArgumentCaptor<ExchangeRateCompositePrimaryKey> keyCaptor =
            ArgumentCaptor.forClass(ExchangeRateCompositePrimaryKey.class);

        Mockito.verify(exchangeRateRepository).findByIdAndRateDate(keyCaptor.capture(), Mockito.eq(RATE_DATE));

        ExchangeRateCompositePrimaryKey usedKey = keyCaptor.getValue();
        AssertionsForClassTypes.assertThat(usedKey.getCurrencyFrom()).isEqualTo(CURRENCY_FROM);
        AssertionsForClassTypes.assertThat(usedKey.getCurrencyTo()).isEqualTo(CURRENCY_TO);
    }

    @Test
    @DisplayName("Should save exchange rate successfully")
    void shouldSaveExchangeRateSuccessfully() {
        ExchangeRateEntity entity = createExchangeRateEntity();
        exchangeRateService.saveExchangeRate(entity);
        Mockito.verify(exchangeRateRepository).save(entity);
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
