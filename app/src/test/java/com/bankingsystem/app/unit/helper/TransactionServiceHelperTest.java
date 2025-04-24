package com.bankingsystem.app.unit.helper;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.ExchangeRateEntity;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.helper.impl.TransactionServiceHelper;
import com.bankingsystem.app.model.AccountPair;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.service.interfaces.AccountServiceInterface;
import com.bankingsystem.app.service.interfaces.ExchangeRateServiceInterface;
import com.bankingsystem.app.service.interfaces.LimitServiceInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TransactionServiceHelperTest {
    @InjectMocks
    private TransactionServiceHelper transactionServiceHelper;

    @Mock
    private LimitServiceInterface limitService;

    @Mock
    private AccountServiceInterface accountService;

    @Mock
    private ExchangeRateServiceInterface exchangeRateService;

    private static final Long ACCOUNT_FROM = 1L;
    private static final Long ACCOUNT_TO = 2L;
    private static final Currency TEST_CURRENCY = Currency.USD;
    private static final Category TEST_CATEGORY = Category.SERVICE;
    private static final BigDecimal TEST_SUM = new BigDecimal("1000.00");
    private static final OffsetDateTime TEST_DATE =
            OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);;
    private static final Boolean LIMIT_EXCEEDED_FALSE = false;
    private static final Boolean LIMIT_EXCEEDED_TRUE = true;
    private static final Long LIMIT_ID = 1L;

    private static final BigDecimal LIMIT_SUM_VALUE = new BigDecimal("1000.00");
    private static final Category LIMIT_CATEGORY = Category.SERVICE;
    private static final OffsetDateTime LIMIT_DATETIME =
            OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);;
    private static final Currency LIMIT_CURRENCY_USD = Currency.USD;
    private static final BigDecimal LIMIT_REMAINDER = new BigDecimal("1000.00");
    private static final BigDecimal SUM_IN_USD = new BigDecimal("1000.00");
    private static final BigDecimal RATE = new BigDecimal("1.2");


    @Test
    @DisplayName("Should convert TransactionDTO from TransactionEntity successfully")
    void shouldConvertTransactionDTOFromTransactionEntitySuccessfully() {
        TransactionEntity transactionEntity = createTransactionEntity();
        TransactionDTO dto = transactionServiceHelper.convertToDTO(transactionEntity);

        assertThat(dto.getAccountIdFrom()).isEqualTo(ACCOUNT_FROM);
        assertThat(dto.getAccountIdTo()).isEqualTo(ACCOUNT_TO);
        assertThat(dto.getCurrency()).isEqualTo(TEST_CURRENCY);
        assertThat(dto.getExpenseCategory()).isEqualTo(TEST_CATEGORY);
        assertThat(dto.getSum()).isEqualTo(TEST_SUM);
        assertThat(dto.getTransactionTime()).isEqualTo(TEST_DATE);
        assertThat(dto.getLimitId()).isEqualTo(LIMIT_ID);
        assertThat(dto.getLimitSum()).isEqualTo(LIMIT_SUM_VALUE);
        assertThat(dto.getLimitDateTime()).isEqualTo(LIMIT_DATETIME);
        assertThat(dto.getLimitCurrency()).isEqualTo(LIMIT_CURRENCY_USD);
    }
    @Test
    @DisplayName("Should validate accounts successfully")
    void shouldValidateAccountsSuccessfully() {
        AccountEntity expectedAccountFrom = createAccountEntity(ACCOUNT_FROM);
        AccountEntity expectedAccountTo = createAccountEntity(ACCOUNT_TO);

        when(accountService.getAccountById(ACCOUNT_FROM)).thenReturn(expectedAccountFrom);
        when(accountService.getAccountById(ACCOUNT_TO)).thenReturn(expectedAccountTo);

        AccountPair actualPair = transactionServiceHelper.validateAccounts(ACCOUNT_FROM, ACCOUNT_TO);

        assertThat(actualPair.getAccountFrom()).isEqualTo(expectedAccountFrom);
        assertThat(actualPair.getAccountTo()).isEqualTo(expectedAccountTo);
    }

    @Test
    @DisplayName("Should return exception when account not found")
    void shouldReturnExceptionWhenAccountNotFound() {
        when(accountService.getAccountById(ACCOUNT_FROM)).thenReturn(null);

        assertThatThrownBy(() -> transactionServiceHelper.validateAccounts(ACCOUNT_FROM, ACCOUNT_TO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Account not found");
    }

    @Test
    @DisplayName("Should find and validate limit successfully")
    void shouldValidateLimitSuccessfully() {
        LimitEntity expectedLimit = createLimitEntity();

        when(limitService.getLimitByAccountIdAndCategory(ACCOUNT_FROM,TEST_CATEGORY)).thenReturn(Optional.of(expectedLimit));

        LimitEntity actualLimit = transactionServiceHelper.findAndValidateLimit(ACCOUNT_FROM, TEST_CATEGORY);

        assertThat(expectedLimit).isEqualTo(actualLimit);
        verify(limitService,times(1)).getLimitByAccountIdAndCategory(ACCOUNT_FROM,TEST_CATEGORY);

    }
    //не
    @Test
    @DisplayName("Should return exception when limit not found")
    void shouldReturnExceptionWhenLimitNotFound() {
        when(limitService.getLimitByAccountIdAndCategory(ACCOUNT_FROM,TEST_CATEGORY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionServiceHelper.findAndValidateLimit(ACCOUNT_FROM, TEST_CATEGORY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Limit for account" + ACCOUNT_FROM
                        + "and category " + TEST_CATEGORY + " not found");
    }

    @Test
    @DisplayName("Should return false when Limit is not Exceeded ")
    void shouldReturnFalseWhenLimitIsNotExceeded() {
        //limit remainder equals 1000
        LimitEntity expectedLimit = createLimitEntity();
        BigDecimal sumInUSD = new BigDecimal("100.00");

        boolean actualResult = transactionServiceHelper.isLimitExceeded(sumInUSD, expectedLimit);

        assertThat(actualResult).isEqualTo(LIMIT_EXCEEDED_FALSE);
    }
    @Test
    @DisplayName("Should return true when limit exceeded")
    void shouldReturnTrueWhenLimitExceeded() {
        LimitEntity expectedLimit = createLimitEntity();
        BigDecimal sumInUSD = new BigDecimal("1100.00");

        boolean actualResult = transactionServiceHelper.isLimitExceeded(sumInUSD, expectedLimit);

        assertThat(actualResult).isEqualTo(LIMIT_EXCEEDED_TRUE);
    }

    @Test
    @DisplayName("Should return exception when limit remainder is null")
    void shouldReturnExceptionWhenLimitRemainderIsNull() {
        LimitEntity expectedLimit = createLimitEntity();
        expectedLimit.setLimitRemainder(null);

        assertThatThrownBy(() -> transactionServiceHelper.isLimitExceeded(SUM_IN_USD,expectedLimit))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Limit remainder is null for limitId: " + LIMIT_ID);
    }
    @Test
    @DisplayName("Should build transaction Entity successfully")
    void shouldBuildTransactionEntitySuccessfully() {
             TransactionDTO dto = createTransactionDTO();
             LimitEntity limitEntity = createLimitEntity();
             AccountPair accounts = new AccountPair(createAccountEntity(ACCOUNT_FROM), createAccountEntity(ACCOUNT_TO));

             TransactionEntity  actualEntity = transactionServiceHelper.buildTransactionEntity(dto,accounts,limitEntity,LIMIT_EXCEEDED_FALSE);

             assertThat(actualEntity.getAccountFrom().getId()).isEqualTo(ACCOUNT_FROM);
             assertThat(actualEntity.getAccountTo().getId()).isEqualTo(ACCOUNT_TO);
             assertThat(actualEntity.getCurrency()).isEqualTo(TEST_CURRENCY);
             assertThat(actualEntity.getSum()).isEqualTo(TEST_SUM);
             assertThat(actualEntity.getCategory()).isEqualTo(TEST_CATEGORY);
             assertThat(actualEntity.getLimitExceeded()).isEqualTo(LIMIT_EXCEEDED_FALSE);
             assertThat(actualEntity.getLimitSumAtTime()).isEqualTo(LIMIT_SUM_VALUE);
             assertThat(actualEntity.getLimitCurrencyAtTime()).isEqualTo(LIMIT_CURRENCY_USD);
             assertThat(actualEntity.getLimitDateTimeAtTime()).isEqualTo(TEST_DATE);
    }
    @Test
    @DisplayName("Should return update limit remainder")
    void shouldReturnUpdateLimitRemainder() {
        LimitEntity limitEntity = createLimitEntity();
        BigDecimal expectedRemainder = LIMIT_REMAINDER.subtract(SUM_IN_USD);

        when(limitService.saveLimit(limitEntity)).thenReturn(limitEntity);

        transactionServiceHelper.updateLimitRemainder(SUM_IN_USD,limitEntity);

        assertThat(limitEntity.getLimitRemainder()).isEqualTo(expectedRemainder);
        verify(limitService,times(1)).saveLimit(limitEntity);
    }
    @Test
    @DisplayName("Should return exception when remainder null")
    void shouldReturnExceptionWhenRemainderIsNull() {
        LimitEntity limitEntity = createLimitEntity();
        limitEntity.setLimitRemainder(null);

        assertThatThrownBy(() ->transactionServiceHelper.updateLimitRemainder(SUM_IN_USD,limitEntity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Limit remainder is null for limitId: " + limitEntity.getId());
    }
    @Test
    @DisplayName("Should return amount in USD")
    void shouldReturnAmountInUSD() {
        Currency currency = Currency.EUR;
        LocalDate date = TEST_DATE.toLocalDate();
        BigDecimal expectedAmountInUSD = SUM_IN_USD.multiply(RATE);

        ExchangeRateEntity entity = new ExchangeRateEntity();
        entity.setRate(RATE);
        when(exchangeRateService.getExchangeRate(currency, Currency.USD,date)).thenReturn(Optional.of(entity));

        BigDecimal actualResult = transactionServiceHelper.convertToUSD(SUM_IN_USD,currency,date);

        assertThat(actualResult).isEqualTo(expectedAmountInUSD);
        verify(exchangeRateService,times(1)).getExchangeRate(currency, Currency.USD,date);

        }
    @Test
    @DisplayName("Should throw exception when amount is zero")
    void shouldThrowExceptionWhenAmountIsZero() {
        BigDecimal amount = BigDecimal.ZERO;
        Currency currency = Currency.EUR;
        LocalDate date = TEST_DATE.toLocalDate();

        assertThatThrownBy(() -> transactionServiceHelper.convertToUSD(amount, currency, date))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than zero");

        verify(exchangeRateService, never()).getExchangeRate(any(), any(), any());
    }
    private LimitEntity createLimitEntity() {
        LimitEntity limitEntity = new LimitEntity();
        limitEntity.setId(LIMIT_ID);
        limitEntity.setCategory(TEST_CATEGORY);
        limitEntity.setLimitSum(LIMIT_SUM_VALUE);
        limitEntity.setCategory(LIMIT_CATEGORY);
        limitEntity.setLimitCurrencyShortName(LIMIT_CURRENCY_USD);
        limitEntity.setLimitRemainder(LIMIT_REMAINDER);
        limitEntity.setLimitDateTime(LIMIT_DATETIME);
        return limitEntity;
    }

    private AccountEntity createAccountEntity(Long accountId) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(accountId);
        return accountEntity;
    }
    private TransactionEntity createTransactionEntity()
    {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setId(1L);
        transaction.setAccountFrom(createAccountEntity(ACCOUNT_FROM));
        transaction.setAccountTo(createAccountEntity(ACCOUNT_TO));
        transaction.setCurrency(TEST_CURRENCY);
        transaction.setCategory(TEST_CATEGORY);
        transaction.setSum(TEST_SUM);
        transaction.setTransactionTime(TEST_DATE);
        transaction.setLimitExceeded(LIMIT_EXCEEDED_FALSE);
        transaction.setLimit(createLimitEntity());
        transaction.setLimitDateTimeAtTime(LIMIT_DATETIME);
        transaction.setLimitSumAtTime(LIMIT_SUM_VALUE);
        transaction.setLimitCurrencyAtTime(LIMIT_CURRENCY_USD);
        return transaction;
    }
    private TransactionDTO createTransactionDTO() {
        TransactionDTO dto = new TransactionDTO();
        dto.setAccountIdFrom(ACCOUNT_FROM);
        dto.setAccountIdTo(ACCOUNT_TO);
        dto.setCurrency(TEST_CURRENCY);
        dto.setExpenseCategory(TEST_CATEGORY);
        dto.setSum(TEST_SUM);
        dto.setTransactionTime(TEST_DATE);
        dto.setLimitId(LIMIT_ID);
        dto.setLimitSum(LIMIT_SUM_VALUE);
        dto.setLimitCurrency(LIMIT_CURRENCY_USD);
        dto.setLimitDateTime(LIMIT_DATETIME);
        return dto;
    }
}
