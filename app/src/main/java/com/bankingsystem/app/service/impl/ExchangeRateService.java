package com.bankingsystem.app.service.impl;

import com.bankingsystem.app.config.TwelveDataConfig;
import com.bankingsystem.app.entity.ExchangeRateCompositePrimaryKey;
import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.ExchangeRateDTO;
import com.bankingsystem.app.repository.ExchangeRateRepository;
import com.bankingsystem.app.service.interfaces.ExchangeRateServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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

// FIXME : я тут сделал, чтобы при какой либо ошибке выбрасывало исключение
//  однако при таком подходе если произойдет одна ошибка или неуспешное действие
//  то все приложение ложиться. можно продумать, если надо, как сделать
//  чтобы при ошибке приложение продолжало работать и как то уведомляло об ошибке

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
        String url = String.format("%s/exchange_rate?symbol=%s/%s&apikey=%s",
                twelveDataConfig.getApiUrl(), currencyFrom, currencyTo, twelveDataConfig.getApiKey());

        log.debug("Making request to URL: {}", url);

        // Делаем запрос и автоматически преобразуем JSON в DTO
        ExchangeRateDTO response = restTemplate.getForObject(url, ExchangeRateDTO.class);

        if (response == null) {
            throw new RuntimeException("Empty response from API");
        }

        log.debug("Received rate: {}", response.getRate());

        // Получаем курс (используем value если есть, иначе rate)
        BigDecimal rate = response.getRate();

        // Создаем или обновляем запись в БД
        LocalDate today = LocalDate.now();
        ExchangeRateEntity entity = exchangeRateRepository
                .findByIdAndRateDate(
                    new ExchangeRateCompositePrimaryKey(currencyFrom, currencyTo),
                    today)
                .orElseGet(ExchangeRateEntity::new);

        entity.setId(new ExchangeRateCompositePrimaryKey(currencyFrom, currencyTo));
        entity.setRateDate(today);
        entity.setRate(rate);

        return exchangeRateRepository.save(entity);
    }

    // автоматическое обновление курсов раз в указанную единицу времени
    @Scheduled(fixedRate = FIXED_UPDATE_RATE_TIME)
    @Override
    public void updateExchangeRateAutomatically() {
        for (CurrencyPair pair : CURRENCY_PAIRS) {
            updateExchangeRateManually(pair.getCurrencyFrom(), pair.getCurrencyTo());
        }
    }

    @Override
    public Optional<ExchangeRateEntity> getExchangeRate(Currency from, Currency to, LocalDate date) {
        ExchangeRateCompositePrimaryKey key = new ExchangeRateCompositePrimaryKey(from, to);
        Optional<ExchangeRateEntity> exchangeRateEntity =
                exchangeRateRepository.findByIdAndRateDate(key, date);
        log.info("Exchange rate from {} to {} : {}", from, to, exchangeRateEntity);

        return exchangeRateEntity;
    }

    @Override
    public void saveExchangeRate(ExchangeRateEntity exchangeRate) {
        log.info("Saving exchange rate : {}", exchangeRate);
        exchangeRateRepository.save(exchangeRate);
    }
}