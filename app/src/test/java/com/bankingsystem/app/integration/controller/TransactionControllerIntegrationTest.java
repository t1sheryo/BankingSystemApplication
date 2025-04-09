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
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

//TODO:
@SpringBootTest(    )
@Testcontainers
@AutoConfigureMockMvc
// для того чтобы после каждого теста изменения откатывались
// данное поведения отличается от обычного, т.к.
// сейчас аннотация используется в сочетании с аннотацией @SpringBootTest
@Transactional
//JUnit создает один экземпляр тестового класса для всех тестов в этом классе
//экземпляр используется для всех тестов и поля класса сохраняют своё состояние
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TransactionControllerIntegrationTest {
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

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);;

        registry.add("spring.flyway.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.flyway.username", mysqlContainer::getUsername);
        registry.add("spring.flyway.password", mysqlContainer::getPassword);
    }

    private static final Long VALID_ACCOUNT_ID_FROM = 1L;
    private static final Long VALID_ACCOUNT_ID_TO = 2L;
    private static final Long INVALID_ACCOUNT_ID = -1L;
    private static final Currency TEST_CURRENCY = Currency.EUR;
    private static final Category TEST_CATEGORY = Category.PRODUCT;
    private static final BigDecimal TEST_SUM = BigDecimal.valueOf(1000);
    private static final OffsetDateTime TEST_DATE = OffsetDateTime.now().minusDays(1);
    private static final Long TRANSACTION_ID = 1L;
    @BeforeAll
      void beforeAll() throws Exception{
        mysqlContainer.start();

        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();

    }
    @BeforeEach
    void setUp() throws Exception
    {
        wireMockServer.resetAll();
        //Настройка заглушки для USD/EUR
        ExchangeRateResponse exchangeRateUSDtoEUR = new ExchangeRateResponse(0.915542);
        String usdEurBody = objectMapper.writeValueAsString(exchangeRateUSDtoEUR);

        wireMockServer.stubFor(get(urlPathMatching("/exchange_rate"))
                .withQueryParam("symbol", equalTo("USD/EUR"))
                .withQueryParam("apikey", equalTo("7a79c306727443819a002da0398f5ce7"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(usdEurBody)));

        ExchangeRateResponse exchangeRateEURtoRUS = new ExchangeRateResponse(93.5);
        String usdRubBody = objectMapper.writeValueAsString(exchangeRateEURtoRUS);

        wireMockServer.stubFor(get(urlPathMatching("/exchange_rate"))
                .withQueryParam("symbol", equalTo("USD/RUB"))
                .withQueryParam("apikey", equalTo("7a79c306727443819a002da0398f5ce7"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(usdRubBody)));


    }

    @AfterAll
     void afterAll() {
        wireMockServer.stop();
        mysqlContainer.stop();
    }

    @Test
    @DisplayName("Should create Transaction Successfully")
    void shouldCreateTransactionSuccessfully() throws Exception {
        AccountEntity accountFrom = createAccountEntity();
        AccountEntity accountTo = createAccountEntity();
        TransactionDTO dto = createTransactionDTO(accountFrom.getId(), accountTo.getId());

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transaction DTO cannot be null"));
    }

    private TransactionDTO createTransactionDTO(Long accountFrom, Long accountTo)
    {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountIdFrom(accountFrom);
        transactionDTO.setAccountIdTo(accountTo);
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
    private AccountEntity createAccountEntity() {
        AccountEntity accountEntity = new AccountEntity();
        return accountRepository.save(accountEntity);
    }
}
