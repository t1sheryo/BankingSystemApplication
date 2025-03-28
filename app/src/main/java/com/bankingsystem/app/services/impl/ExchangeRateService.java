package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.config.TwelveDataConfig;
import com.bankingsystem.app.entity.ExchangeRateCompositePrimaryKey;
import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.ExchangeRateDTO;
import com.bankingsystem.app.repository.ExchangeRateRepository;
import com.bankingsystem.app.services.interfaces.ExchangeRateServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.bankingsystem.app.model.CurrencyPair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// для чего нужен RestTemplate
// Пример без RestTemplate:
// URL url = new URL("https://api.twelvedata.com/price?symbol=USD/EUR&apikey=your_api_key");
// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
// conn.setRequestMethod("GET");
// BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
// StringBuilder response = new StringBuilder();
// String line;
// while ((line = in.readLine()) != null) {
//      response.append(line);
// }
// in.close();
// Еще нужно вручную парсить JSON
// С RestTemplate:
// String url = "https://api.twelvedata.com/price?symbol=USD/EUR&apikey=your_api_key";
// String response = restTemplate.getForObject(url, String.class);

@Slf4j
@Service
public class ExchangeRateService implements ExchangeRateServiceInterface {
    private final TwelveDataConfig twelveDataConfig;
    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate;
    // это время необходимо предоставить в мс
    private final static int FIXED_UPDATE_RATE_TIME = 1000 * 60 * 60 * 6; // 6 часов
    private static final List<CurrencyPair> CURRENCY_PAIRS;

    // лист пар для перевода из валюты в другую валюту
    // будет заполняться в статическом блоке,
    // что точно говорит о том, что лист будет заполнен один раз
    // и больше изменяться не будет
    static {
        // т.к. список обьявлен неизменяемым и ссылка на него
        // тоже неизменяема, то создадим обычный список,
        // который заполним нужными парами,
        // а потом уже инициализируем наш оригинальный список

        List<CurrencyPair> tmp = new ArrayList<>();
        for (final var value1 : Currency.values()) {
            for (final var value2 : Currency.values()) {
                if (value1.equals(value2)) continue;
                tmp.add(new CurrencyPair(value1, value2));
            }
        }

        CURRENCY_PAIRS = List.copyOf(tmp); // Неизменяемый список
    }

    @Autowired // для автоматического внедрения зависимостей
    public ExchangeRateService(TwelveDataConfig twelveDataConfig,
                               ExchangeRateRepository exchangeRateRepository,
                               RestTemplate restTemplate) {
        this.twelveDataConfig = twelveDataConfig;
        this.exchangeRateRepository = exchangeRateRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public ExchangeRateEntity updateExchangeRateManually(Currency currencyFrom, Currency currencyTo) {
        log.info("Method called for {}/{}", currencyFrom, currencyTo);

        String url = String.format("%s/exchange_rate?symbol=%s/%s&apikey=%s",
            twelveDataConfig.getApiUrl(), currencyFrom, currencyTo, twelveDataConfig.getApiKey());

        // этот класс сделан для упрощения работы с десериализацией данных
        // т.к. RestTemplate ожидает, что обьект JSON будет преобразован
        // в обьект существующего класса. Иначе, будет требоваться вручную
        // парсить данные ответа.
        try {
        log.debug("Requesting URL: {}", url);
        ExchangeRateDTO response = restTemplate.getForObject(url, ExchangeRateDTO.class);
        log.debug("API Response: {}", response);
        } catch (RestClientException e) {
            log.error("REST call failed: ", e);  // <- Логируем ошибку
            throw e;
        }
//        if(response == null || response.getValue() == null) {
//            throw new RuntimeException("Failed to fetch exchange rate from Twelve Data API");
//        }

        BigDecimal rate = new BigDecimal(response.getValue());
        LocalDate today = LocalDate.now();
        ExchangeRateCompositePrimaryKey primaryKey
                = new ExchangeRateCompositePrimaryKey(currencyFrom, currencyTo);

        // если запись существует в БД, то корректируем ее
        // иначе создаем новую запись
        Optional<ExchangeRateEntity> existingRate
                = exchangeRateRepository.findByIdAndRateDate(primaryKey, today);
        ExchangeRateEntity entity = existingRate.orElse(new ExchangeRateEntity());
        // поле updateTime не трогаем, т.к. оно обновляется через методы
        // с аннотацией @PreUpdate и @PrePersist
        entity.setId(primaryKey);
        entity.setRateDate(today);
        entity.setRate(rate);

        // если в БД существовала эта запись, тогда
        // некоторые поля просто перезапишутся.
        // если же такой записи не было, тогда добавиться новая
        return exchangeRateRepository.save(entity);
    }

    // автоматическое обновление курсов раз в указанную единицу времени
    @Scheduled(fixedRate = FIXED_UPDATE_RATE_TIME)
    @Override
    public void updateExchangeRateAutomatically() {
        for (CurrencyPair pair : CURRENCY_PAIRS) {
            log.info("Processing pair: {}/{}", pair.from(), pair.to());
            try {
                updateExchangeRateManually(pair.from(), pair.to());
                log.info("Updated exchange rate for {}/{}", pair.from(), pair.to());
            } catch (Exception e) {
                log.error("Failed to update {}/{}: ", pair.from(), pair.to(), e);
            }
        }
    }

    // Метод для получения курса из базы
    @Override
    public Optional<ExchangeRateEntity> getExchangeRate(Currency from, Currency to, LocalDate date) {
        ExchangeRateCompositePrimaryKey key = new ExchangeRateCompositePrimaryKey(from, to);
        return exchangeRateRepository.findByIdAndRateDate(key, date);
    }
}