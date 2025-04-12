package com.bankingsystem.app.integration.controller;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.ExchangeRateResponse;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.repository.AccountRepository;
import com.bankingsystem.app.repository.TransactionRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

//TODO:
@Slf4j
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yaml")
@Testcontainers
@AutoConfigureMockMvc
// для того чтобы после каждого теста изменения откатывались
// данное поведения отличается от обычного, т.к.
// сейчас аннотация используется в сочетании с аннотацией @SpringBootTest
@Transactional
//JUnit создает один экземпляр тестового класса для всех тестов в этом классе
//экземпляр используется для всех тестов и поля класса сохраняют своё состояние
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionControllerIT {
    //создает контейнер внутри теста
    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    //класс из библиотеки WireMock, который представляет собой локальный HTTP-server
    //Способный мокать из внешнего API
    private static WireMockServer wireMockServer;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private  ObjectMapper objectMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    private static final Long VALID_ACCOUNT_ID_FROM = 1L;
    private static final Long VALID_ACCOUNT_ID_TO = 2L;
    private static final Long INVALID_ACCOUNT_ID = -1L;
    private static final Currency TEST_CURRENCY = Currency.EUR;
    private static final Category TEST_CATEGORY = Category.PRODUCT;
    private static final BigDecimal TEST_SUM = BigDecimal.valueOf(1000);
    private static final OffsetDateTime TEST_DATE = OffsetDateTime.now();

    // вынес запуск контейнера, т.к. загружаются тут обьекты так:
    // 1) статические блоки и инициализация полей
    // 2) аннотации на уровне класса(например, @SpringBootTest, @Testcontainers, @DynamicPropertySource)
    // 3) spring-контекст
    // 4) @BeforeAll
    // 5) @BeforeEach
    // 6) @Test
    // 7) @AfterEach/@AfterAll
    // Следовательно для @DynamicPropertySource необходимо запустить контейнер в статическом блоке
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

        wireMockServer.stubFor(get(urlPathMatching("/exchange_rate"))
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
    @DisplayName("Should create Transaction Successfully")
    void shouldCreateTransactionSuccessfully() throws Exception {
        TransactionDTO dto = createTransactionDTO();

        log.info("flagflag");

        mockMvc.perform(post("/bank/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.limitExceeded").value(false));

        List<TransactionEntity> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
    }

    @Test
    @DisplayName("Should not create transaction because of transactionDTO = null")
    void shouldReturnBadRequestStatusBecauseOfNullTransationDTO() throws Exception {
        mockMvc.perform(post("/bank/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());
    }

    private TransactionDTO createTransactionDTO() {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountIdFrom(VALID_ACCOUNT_ID_FROM);
        transactionDTO.setAccountIdTo(VALID_ACCOUNT_ID_TO);
        transactionDTO.setCurrency(TEST_CURRENCY);
        transactionDTO.setExpenseCategory(TEST_CATEGORY);
        transactionDTO.setSum(TEST_SUM);
        transactionDTO.setTransactionTime(TEST_DATE);
        transactionDTO.setLimitSum(null);
        transactionDTO.setLimitId(null);
        transactionDTO.setLimitDateTime(null);
        transactionDTO.setLimitCurrency(null);
        return transactionDTO;
    }
    private AccountEntity createAccountFromEntity() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(VALID_ACCOUNT_ID_FROM);
        return accountRepository.save(accountEntity);
    }
    private AccountEntity createAccountToEntity() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(VALID_ACCOUNT_ID_TO);
        return accountRepository.save(accountEntity);
    }
}
