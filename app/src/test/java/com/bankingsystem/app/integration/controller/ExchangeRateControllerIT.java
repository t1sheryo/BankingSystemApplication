package com.bankingsystem.app.integration.controller;

import com.bankingsystem.app.model.ExchangeRateResponse;
import com.bankingsystem.app.service.impl.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// FIXME : переделать тесты с security

@Slf4j
@SpringBootTest
@Transactional
@Testcontainers
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExchangeRateControllerIT {
    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    private static WireMockServer wireMockServer;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ExchangeRateService exchangeRateService;
    @Autowired
    ObjectMapper objectMapper;
    private static final LocalDate CURRENT_TIME = LocalDate.now();
    private static final LocalDate FUTURE_TIME = LocalDate.now().plusDays(1);
    private static final String CURRENT_TIME_STRING = CURRENT_TIME.toString();
    private static final String FUTURE_TIME_STRING = FUTURE_TIME.toString();

    static {
        mysqlContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = mysqlContainer.getJdbcUrl();
        String username = mysqlContainer.getUsername();
        String password = mysqlContainer.getPassword();

        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> username);
        registry.add("spring.datasource.password", () -> password);

        registry.add("spring.flyway.url", () -> jdbcUrl);
        registry.add("spring.flyway.user", () -> username);
        registry.add("spring.flyway.password", () -> password);
    }

    @BeforeAll
    void beforeAll() throws Exception{
        wireMockServer = new WireMockServer(8090);
        wireMockServer.start();

        wireMockServer.resetAll();
        // Настройка заглушки для EUR/USD
        ExchangeRateResponse exchangeRateUSDtoEUR = new ExchangeRateResponse(1.10825);
        String usdEurBody = objectMapper.writeValueAsString(exchangeRateUSDtoEUR);

        wireMockServer.stubFor(WireMock.get(urlPathMatching("/exchange_rate"))
                .withQueryParam("symbol", equalTo("EUR/USD"))
                .withQueryParam("apikey", equalTo("7a79c306727443819a002da0398f5ce7"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(usdEurBody)));
    }

    @AfterAll
    void afterAll() {
        wireMockServer.stop();
        mysqlContainer.stop();
    }

    @Test
    @DisplayName("Should return exchange rate successfully without date param")
    void shouldReturnExchangeRateSuccessfullyWithoutDate() throws Exception {
        mockMvc.perform(get("/bank/exchange-rates")
                        .param("from", "EUR")
                        .param("to", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyFrom").value("EUR"))
                .andExpect(jsonPath("$.currencyTo").value("USD"));
    }

    @Test
    @DisplayName("Should return exchange rate successfully with date param")
    void shouldReturnExchangeRateSuccessfullyWithDate() throws Exception {
        mockMvc.perform(get("/bank/exchange-rates")
                        .param("from", "EUR")
                        .param("to", "USD")
                        .param("date", CURRENT_TIME_STRING))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyFrom").value("EUR"))
                .andExpect(jsonPath("$.currencyTo").value("USD"));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for invalid currency")
    void shouldReturnBadRequestForInvalidCurrency() throws Exception {
        mockMvc.perform(get("/bank/exchange-rates")
                        .param("from", "XYZ")
                        .param("to", "USD"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status because of null param")
    void shouldReturnBadRequestStatusCodeBecauseOfNullParam() throws Exception {
        mockMvc.perform(get("/bank/exchange-rates"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value(containsString("from")));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status because of future date")
    void shouldReturnBadRequestStatusCodeBecauseOfFutureDate() throws Exception {
        mockMvc.perform(get("/bank/exchange-rates")
                        .param("from", "EUR")
                        .param("to", "USD")
                        .param("date", FUTURE_TIME_STRING))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("date")));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for invalid date format")
    void shouldReturnBadRequestForInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/bank/exchange-rates")
                        .param("from", "EUR")
                        .param("to", "USD")
                        .param("date", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("date")));
    }

    @Test
    @DisplayName("Should update exchange rate successfully")
    void shouldReturnExchangeRateSuccessfully() throws Exception {
        mockMvc.perform(post("/bank/exchange-rates/update")
                        .param("from", "EUR")
                        .param("to", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyFrom").value("EUR"))
                .andExpect(jsonPath("$.currencyTo").value("USD"));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status because of null param while updating rate")
    void shouldReturnBadRequestStatusCodeBecauseOfNullParamWhileUpdatingRate() throws Exception {
        mockMvc.perform(post("/bank/exchange-rates/update"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value(containsString("from")));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for invalid currency while updating")
    void shouldReturnBadRequestForInvalidCurrencyWhileUpdating() throws Exception {
        mockMvc.perform(post("/bank/exchange-rates/update")
                        .param("from", "XYZ")
                        .param("to", "USD"))
                .andExpect(status().isBadRequest());
    }
}
