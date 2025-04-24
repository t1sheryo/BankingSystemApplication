package com.bankingsystem.app.integration.controller;

import com.bankingsystem.app.controller.TransactionController;
import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.ExchangeRateResponse;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.repository.AccountRepository;
import com.bankingsystem.app.repository.TransactionRepository;
import com.bankingsystem.app.service.impl.AccountService;
import com.bankingsystem.app.service.impl.TransactionService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

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
import java.time.ZoneOffset;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
// по умолчанию эта аннотация загружает весь spring-контекст(вообще все бины)
// однако можно в параметрах указать загружать только зависимые бины
@SpringBootTest
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
    private ObjectMapper objectMapper;
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

    private static final Long LIMIT_ID = 1L;
    private static final BigDecimal LIMIT_SUM = BigDecimal.valueOf(1000);
    private static final Category LIMIT_CATEGORY = Category.PRODUCT;
    private static final OffsetDateTime LIMIT_DATE_TIME =
            OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    private static final Currency LIMIT_CURRENCY = Currency.USD;
    private static final BigDecimal LIMIT_REMAINDER = BigDecimal.valueOf(500);
    private static final AccountEntity LIMIT_ACCOUNT =
            new AccountEntity(VALID_ACCOUNT_ID_FROM);

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

    // Это нужно, потому что Testcontainers запускает MySQL на случайном порту,
    // и мы не можем заранее знать точный URL.
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

    @Test
    @DisplayName("Should create Transaction Successfully")
    void shouldCreateTransactionSuccessfully() throws Exception {
        TransactionDTO dto = createTransactionDTO();

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
    @DisplayName("Should not create transaction with non-existing account")
    void shouldFailToCreateTransactionWithNonExistentAccount() throws Exception {
        TransactionDTO dto = createTransactionDTO();
        accountRepository.deleteAll();

        mockMvc.perform(post("/bank/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should not create transaction because of transactionDTO = null")
    void shouldReturnBadRequestStatusBecauseOfNullTransationDTO() throws Exception {
        mockMvc.perform(post("/bank/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest());
    }

    // если валидация не проходит, возвращает MethodArgumentNotValidException
    @Test
    @DisplayName("Should return BAD_REQUEST status because transactionDTO fields are not initialized")
    void shouldReturnValidationErrorForMissingRequiredFields() throws Exception {
        TransactionDTO invalidDTO = new TransactionDTO();

        mockMvc.perform(post("/bank/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors").isNotEmpty())
            .andExpect(jsonPath("$.errors").value(containsInAnyOrder(
                    "Account Id field must not be null",
                    "Account Id field must not be null",
                    "Currency field must not be null",
                    "Category field must not be null",
                    "Amount field must not be null",
                    "Transaction time must not be null"
            )));

        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for negative sum of transaction")
    void shouldReturnValidationErrorForNegativeSum() throws Exception {
        TransactionDTO invalidDTO = createTransactionDTO();
        invalidDTO.setSum(BigDecimal.valueOf(-100));

        mockMvc.perform(post("/bank/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Transaction value must be over 0.001 unit of currency"));

        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for invalid account id")
    void shouldReturnBadRequestForInvalidAccountIdFrom() throws Exception {
        TransactionDTO invalidDTO = createTransactionDTO();
        invalidDTO.setAccountIdFrom(INVALID_ACCOUNT_ID);

        mockMvc.perform(post("/bank/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Account Id must be positive"));

        assertThat(transactionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should return transactions with exceeded limit for valid account ID")
    void shouldReturnTransactionsWithExceededLimitForValidAccountId() throws Exception {
        AccountEntity accountFromEntity = createAccountFromEntity();
        AccountEntity accountToEntity = createAccountToEntity();
        TransactionEntity transaction = createTransactionEntity();
        transaction.setAccountFrom(accountFromEntity);
        transaction.setAccountTo(accountToEntity);
        transaction.setLimitExceeded(true);

        accountRepository.save(accountFromEntity);
        accountRepository.save(accountToEntity);
        transactionRepository.save(transaction);

        mockMvc.perform(get("/bank/transactions/exceeded/" + VALID_ACCOUNT_ID_FROM))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].fromAccount").value(VALID_ACCOUNT_ID_FROM))
                .andExpect(jsonPath("$[0].sum").value(TEST_SUM.doubleValue()));

        List<TransactionEntity> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getLimitExceeded()).isTrue();
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for negative account id while getting transactions with limit exceeded")
    void shouldReturnBadRequestForNegativeAccountIdWhileGettingTransactionsWithLimitExceeded() throws Exception {

        mockMvc.perform(get("/bank/transactions/exceeded/" + INVALID_ACCOUNT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid input: Invalid account Id"));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for string/invalid account id while getting transactions with limit exceeded")
    void shouldReturnBadRequestForInvalidAccountIdWhileGettingTransactionsWithLimitExceeded() throws Exception {

        mockMvc.perform(get("/bank/transactions/exceeded/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST because there is no account in repository")
    void shouldReturnBadRequestForNoAccountInRepository() throws Exception {
        AccountEntity accountFromEntity = createAccountFromEntity();
        AccountEntity accountToEntity = createAccountToEntity();
        TransactionEntity transaction = createTransactionEntity();
        transaction.setAccountFrom(accountFromEntity);
        transaction.setAccountTo(accountToEntity);
        transaction.setLimitExceeded(true);

        transactionRepository.save(transaction);
        accountRepository.delete(accountFromEntity);

        mockMvc.perform(get("/bank/transactions/exceeded/" + VALID_ACCOUNT_ID_FROM))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Resource not found: Account not found"));
    }

    @Test
    @DisplayName("Should return transactions by category successfully")
    void shouldReturnTransactionsByCategorySuccessfully() throws Exception {
        TransactionEntity transaction = createFullTransactionEntity();
        transactionRepository.save(transaction);

        mockMvc.perform(get("/bank/transactions/byCategory")
                    .param("category", TEST_CATEGORY.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fromAccount").value(VALID_ACCOUNT_ID_FROM))
                .andExpect(jsonPath("$[0].sum").value(TEST_SUM.doubleValue()))
                .andExpect(jsonPath("$[0].expenseCategory").value(TEST_CATEGORY.name()));
    }

    @Test
    @DisplayName("Should return empty list of transactions by category")
    void shouldReturnEmptyListOfTransactionsByCategory() throws Exception {
        mockMvc.perform(get("/bank/transactions/byCategory")
                    .param("category", TEST_CATEGORY.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST because of invalid category")
    void shouldReturnBadRequestBecauseOfInvalidCategory() throws Exception {
        mockMvc.perform(get("/bank/transactions/byCategory")
                    .param("category", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should return all transactions successfully")
    void shouldReturnAllTransactions() throws Exception {
        TransactionEntity transaction = createFullTransactionEntity();
        transactionRepository.save(transaction);

        mockMvc.perform(get("/bank/transactions")
                    .param("category", TEST_CATEGORY.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fromAccount").value(VALID_ACCOUNT_ID_FROM))
                .andExpect(jsonPath("$[0].sum").value(TEST_SUM.doubleValue()))
                .andExpect(jsonPath("$[0].expenseCategory").value(TEST_CATEGORY.name()));
    }

    @Test
    @DisplayName("Should return empty list of all transactions")
    void ShouldReturnEmptyListOfTransactions() throws Exception {
        mockMvc.perform(get("/bank/transactions")
                    .param("category", TEST_CATEGORY.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return all transactions by id successfully")
    void shouldReturnAllTransactionsById() throws Exception {
        TransactionEntity transaction = createFullTransactionEntity();
        transactionRepository.save(transaction);

        mockMvc.perform(get("/bank/transactions/account/" + VALID_ACCOUNT_ID_FROM)
                    .param("category", TEST_CATEGORY.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fromAccount").value(VALID_ACCOUNT_ID_FROM))
                .andExpect(jsonPath("$[0].sum").value(TEST_SUM.doubleValue()))
                .andExpect(jsonPath("$[0].expenseCategory").value(TEST_CATEGORY.name()));
    }

    @Test
    @DisplayName("Should return empty list of transactions by id successfully")
    void shouldReturnEmptyListOfTransactionsById() throws Exception {
        mockMvc.perform(get("/bank/transactions/account/" + VALID_ACCOUNT_ID_FROM)
                    .param("exceededOnly", String.valueOf(true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return all transactions by id and exceeded successfully")
    void shouldReturnAllTransactionsByIdWithExceededOnly() throws Exception {
        TransactionEntity transaction = createFullTransactionEntity();
        transaction.setLimitExceeded(true);
        transactionRepository.save(transaction);

        mockMvc.perform(get("/bank/transactions/account/" + VALID_ACCOUNT_ID_FROM)
                    .param("exceededOnly", String.valueOf(true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fromAccount").value(VALID_ACCOUNT_ID_FROM))
                .andExpect(jsonPath("$[0].sum").value(TEST_SUM.doubleValue()))
                .andExpect(jsonPath("$[0].expenseCategory").value(TEST_CATEGORY.name()));
    }

    @Test
    @DisplayName("Should return empty list of transactions by id and exceeded successfully")
    void shouldReturnEmptyListOfTransactionsByIdWithExceededOnly() throws Exception {
        mockMvc.perform(get("/bank/transactions/account/" + VALID_ACCOUNT_ID_FROM)
                    .param("exceededOnly", String.valueOf(true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for string/invalid account id")
    void shouldReturnBadRequestForInvalidAccountId() throws Exception {

        mockMvc.perform(get("/bank/transactions/account/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should return all transactions by account id with exceededOnly false")
    void shouldReturnAllTransactionsByIdWithExceededOnlyFalse() throws Exception {
        TransactionEntity transaction = createFullTransactionEntity();
        transactionRepository.save(transaction);

        mockMvc.perform(get("/bank/transactions/account/" + VALID_ACCOUNT_ID_FROM)
                .param("exceededOnly", String.valueOf(false)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].fromAccount").value(VALID_ACCOUNT_ID_FROM));
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
        return accountEntity;
    }
    private AccountEntity createAccountToEntity() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(VALID_ACCOUNT_ID_TO);
        return accountEntity;
    }
    private LimitEntity createLimitEntity() {
            LimitEntity limitEntity = new LimitEntity();

            limitEntity.setId(LIMIT_ID);
            limitEntity.setLimitSum(LIMIT_SUM);
            limitEntity.setCategory(LIMIT_CATEGORY);
            limitEntity.setLimitDateTime(LIMIT_DATE_TIME);
            limitEntity.setLimitCurrencyShortName(LIMIT_CURRENCY);
            limitEntity.setLimitRemainder(LIMIT_REMAINDER);
            limitEntity.setAccount(LIMIT_ACCOUNT);

            return limitEntity;
        }
    private TransactionEntity createTransactionEntity() {
        LimitEntity limitEntity = createLimitEntity();

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setCurrency(TEST_CURRENCY);
        transactionEntity.setCategory(TEST_CATEGORY);
        transactionEntity.setSum(TEST_SUM);
        transactionEntity.setTransactionTime(TEST_DATE);
        transactionEntity.setLimitExceeded(false);
        transactionEntity.setLimit(limitEntity);
        transactionEntity.setLimitDateTimeAtTime(LIMIT_DATE_TIME);
        transactionEntity.setLimitSumAtTime(LIMIT_SUM);
        transactionEntity.setLimitCurrencyAtTime(LIMIT_CURRENCY);

        return transactionEntity;
    }
    private TransactionEntity createFullTransactionEntity(){
        LimitEntity limitEntity = createLimitEntity();
        AccountEntity accountFrom = createAccountFromEntity();
        AccountEntity accountTo = createAccountToEntity();

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setCurrency(TEST_CURRENCY);
        transactionEntity.setCategory(TEST_CATEGORY);
        transactionEntity.setSum(TEST_SUM);
        transactionEntity.setTransactionTime(TEST_DATE);
        transactionEntity.setLimitExceeded(false);
        transactionEntity.setLimit(limitEntity);
        transactionEntity.setLimitDateTimeAtTime(LIMIT_DATE_TIME);
        transactionEntity.setLimitSumAtTime(LIMIT_SUM);
        transactionEntity.setLimitCurrencyAtTime(LIMIT_CURRENCY);
        transactionEntity.setAccountFrom(accountFrom);
        transactionEntity.setAccountTo(accountTo);

        return transactionEntity;
    }
}