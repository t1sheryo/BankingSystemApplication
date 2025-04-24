package com.bankingsystem.app.integration.controller;

import com.bankingsystem.app.model.ExchangeRateResponse;
import com.bankingsystem.app.service.impl.ExchangeRateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

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


    static{
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
        wireMockServer = new WireMockServer(8089);
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
}
