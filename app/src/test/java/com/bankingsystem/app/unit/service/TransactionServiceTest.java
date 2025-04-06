package com.bankingsystem.app.unit.service;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.AccountPair;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.repository.TransactionRepository;
import com.bankingsystem.app.services.impl.TransactionService;
import com.bankingsystem.app.services.interfaces.TransactionServiceHelperInterface;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionServiceHelperInterface transactionServiceHelper;

    private static final Long VALID_ACCOUNT_ID_FROM = 1L;
    private static final Long VALID_ACCOUNT_ID_TO = 2L;
    private static final Long INVALID_ACCOUNT_ID_FROM = -1L;
    private static final Long INVALID_ACCOUNT_ID_TO = -1L;
    private static final Currency TEST_CURRENCY = Currency.EUR;
    private static final Category TEST_CATEGORY = Category.PRODUCT;
    private static final BigDecimal TEST_SUM = BigDecimal.valueOf(1000);
    private static final BigDecimal TEST_SUM_IN_USD = BigDecimal.valueOf(1200); //курс 1.2
    private static final OffsetDateTime TEST_DATE = OffsetDateTime.now().minusDays(1);
    private static final Long LIMIT_ID  = 1L;
    private static final BigDecimal TEST_LIMIT_SUM = BigDecimal.valueOf(1000);
    private static final Long TRANSACTION_ID = 1L;

    @Test
    @DisplayName("Should create transaction successfully")
    public void shouldCreateTransactionSuccessfully() {
        TransactionDTO transactionDTO = createTransactionDTO();
        TransactionEntity expectedEntity = createTransactionEntity();
        LimitEntity limitEntity = createLimitEntity();
        AccountPair accounts = new AccountPair(new AccountEntity(), new AccountEntity());

        when(transactionServiceHelper.validateAccounts(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO))
                .thenReturn(accounts);
        when(transactionServiceHelper.findAndValidateLimit(VALID_ACCOUNT_ID_FROM, TEST_CATEGORY))
                .thenReturn(limitEntity);
        when(transactionServiceHelper.convertToUSD(TEST_SUM,TEST_CURRENCY,TEST_DATE.toLocalDate()))
                .thenReturn(TEST_SUM_IN_USD);
        when(transactionServiceHelper.isLimitExceeded(TEST_SUM_IN_USD,limitEntity))
                .thenReturn(false);
        when(transactionServiceHelper.buildTransactionEntity(transactionDTO,accounts,limitEntity,false))
                .thenReturn(expectedEntity);
        when(transactionRepository.save(expectedEntity))
                .thenReturn(expectedEntity);
        doNothing()
                .when(transactionServiceHelper).updateLimitRemainder(TEST_SUM_IN_USD,limitEntity);

        TransactionEntity actualEntity =
                transactionService.createTransaction(transactionDTO);

        assertThat(actualEntity)
                .usingRecursiveComparison()
                .isEqualTo(expectedEntity);

        verify(transactionServiceHelper).updateLimitRemainder(TEST_SUM_IN_USD,limitEntity);
        verify(transactionRepository).save(expectedEntity);
    }

    @Test
    @DisplayName("Should create transaction with exceeded limit successfully")
    public void shouldCreateTransactionWithExceededLimitSuccessfully() {
        TransactionDTO transactionDTO = createTransactionDTO();
        TransactionEntity expectedEntity = createTransactionEntity();
        LimitEntity limitEntity = createLimitEntity();
        AccountPair accounts = new AccountPair(new AccountEntity(), new AccountEntity());

        when(transactionServiceHelper.validateAccounts(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO))
                .thenReturn(accounts);
        when(transactionServiceHelper.findAndValidateLimit(VALID_ACCOUNT_ID_FROM, TEST_CATEGORY))
                .thenReturn(limitEntity);
        when(transactionServiceHelper.convertToUSD(TEST_SUM,TEST_CURRENCY,TEST_DATE.toLocalDate()))
                .thenReturn(TEST_SUM_IN_USD);
        when(transactionServiceHelper.isLimitExceeded(TEST_SUM_IN_USD,limitEntity))
                .thenReturn(true);
        when(transactionServiceHelper.buildTransactionEntity(transactionDTO,accounts,limitEntity,true))
                .thenReturn(expectedEntity);
        when(transactionRepository.save(expectedEntity))
                .thenReturn(expectedEntity);
        doNothing()
                .when(transactionServiceHelper).updateLimitRemainder(TEST_SUM_IN_USD,limitEntity);

        TransactionEntity actualEntity =
                transactionService.createTransaction(transactionDTO);

        assertThat(actualEntity)
                .usingRecursiveComparison()
                .isEqualTo(expectedEntity);

        verify(transactionServiceHelper).updateLimitRemainder(TEST_SUM_IN_USD,limitEntity);
        verify(transactionRepository).save(expectedEntity);
    }

    // FIXME : тест должен быть интеграционным
//    @Test
//    @DisplayName("Should throw exception when account not found")
//    void shouldThrowExceptionWhenAccountNotFound() {
//        TransactionDTO transactionDTO = createTransactionDTO(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO);
//
//        when(transactionServiceHelper.validateAccounts(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO))
//                .thenThrow(new IllegalArgumentException("Account not found"));
//
//        assertThatThrownBy(() -> transactionService.createTransaction(transactionDTO))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("Account not found");
//
//        verify(transactionRepository, never()).save(any());
//    }

    // FIXME: тест должен быть интеграционным
//    @Test
//    @DisplayName("Should throw exception when limit for account and category not found")
//    void shouldThrowExceptionWhenLimitForAccountAndCategoryNotFound() {
//        TransactionDTO transactionDTO = createTransactionDTO(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO);
//        AccountPair accounts = new AccountPair(new AccountEntity(), new AccountEntity());
//
//        when(transactionServiceHelper.validateAccounts(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO))
//                .thenReturn(accounts);
//        when(transactionServiceHelper.findAndValidateLimit(VALID_ACCOUNT_ID_FROM, TEST_CATEGORY))
//                .thenThrow(new IllegalArgumentException("Limit for account" + VALID_ACCOUNT_ID_FROM
//                        + "and category " + TEST_CATEGORY + " not found"));
//
//        assertThatThrownBy(() -> transactionService.createTransaction(transactionDTO))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("Limit for account" + VALID_ACCOUNT_ID_FROM
//                        + "and category " + TEST_CATEGORY + " not found");

//        verify(transactionRepository, never()).save(any());
//    }

    // FIXME: тест должен быть интеграционным
//    @Test
//    @DisplayName("Should throw exception when amount is zero or negative")
//    void shouldThrowExceptionWhenAmountIsZeroOrNegative() {
//        TransactionDTO transactionDTO = createTransactionDTO(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO);
//        AccountPair accounts = new AccountPair(new AccountEntity(), new AccountEntity());
//        LimitEntity limitEntity = createLimitEntity();
//        transactionDTO.setSum(BigDecimal.ZERO);
//
//        when(transactionServiceHelper.validateAccounts(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO))
//                .thenReturn(accounts);
//        when(transactionServiceHelper.findAndValidateLimit(VALID_ACCOUNT_ID_FROM, TEST_CATEGORY))
//                .thenReturn(limitEntity);
//        when(transactionServiceHelper.convertToUSD(BigDecimal.ZERO,TEST_CURRENCY,TEST_DATE.toLocalDate()))
//                .thenThrow(new IllegalArgumentException("Amount must be greater than zero"));
//
//        assertThatThrownBy(() -> transactionService.createTransaction(transactionDTO)).
//                isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("Amount must be greater than zero");

//        verify(transactionRepository, never()).save(any());
//    }

    // FIXME: тест должен быть интеграционным
//    @Test
//    @DisplayName("Should throw exception when exchange rate is not found for currency on date")
//    void shouldThrowExceptionWhenExchangeRateIsNotFound() {
//        TransactionDTO transactionDTO = createTransactionDTO(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO);
//        AccountPair accounts = new AccountPair(new AccountEntity(), new AccountEntity());
//        LimitEntity limitEntity = createLimitEntity();
//
//        when(transactionServiceHelper.validateAccounts(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO))
//                .thenReturn(accounts);
//        when(transactionServiceHelper.findAndValidateLimit(VALID_ACCOUNT_ID_FROM, TEST_CATEGORY))
//                .thenReturn(limitEntity);
//        when(transactionServiceHelper.convertToUSD(TEST_SUM,TEST_CURRENCY,TEST_DATE.toLocalDate()))
//                .thenThrow(new IllegalStateException("Exchange rate is not found for " + TEST_CURRENCY + " on " + TEST_DATE.toLocalDate()));
//
//        assertThatThrownBy(() -> transactionService.createTransaction(transactionDTO))
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessage("Exchange rate is not found for " + TEST_CURRENCY + " on " + TEST_DATE.toLocalDate());
//
//        verify(transactionRepository, never()).save(any());
//    }

    // FIXME: тест должен быть интеграционным
//    @Test
//    @DisplayName("Should throw exception when Limit remainder is null for limitId")
//    void shouldThrowExceptionWhenLimitRemainderIsNullForLimitId() {
//        TransactionDTO transactionDTO = createTransactionDTO(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO);
//        AccountPair accounts = new AccountPair(new AccountEntity(), new AccountEntity());LimitEntity limitEntity = createLimitEntity();
//
//        when(transactionServiceHelper.validateAccounts(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_TO))
//                .thenReturn(accounts);
//        when(transactionServiceHelper.findAndValidateLimit(VALID_ACCOUNT_ID_FROM, TEST_CATEGORY))
//                .thenReturn(limitEntity);
//        when(transactionServiceHelper.convertToUSD(TEST_SUM,TEST_CURRENCY,TEST_DATE.toLocalDate()))
//                .thenReturn(TEST_SUM_IN_USD);
//        when(transactionServiceHelper.isLimitExceeded(TEST_SUM_IN_USD,limitEntity))
//                .thenThrow(new IllegalStateException("Limit remainder is null for limitId: " + limitEntity.getId()));
//
//        assertThatThrownBy(() -> transactionService.createTransaction(transactionDTO))
//                        .isInstanceOf(IllegalStateException.class)
//                        .hasMessage("Limit remainder is null for limitId: " + limitEntity.getId());
//
//        verify(transactionRepository, never()).save(any());
//    }

    @Test
    @DisplayName("Should return all transactions successfully")
    public void shouldReturnAllTransactionsSuccessfully() {
        TransactionDTO transactionDTO = createTransactionDTO();
        TransactionEntity entity = createTransactionEntity();
        List<TransactionEntity> expectedList = Collections.singletonList(entity);

        when(transactionRepository.findAll()).thenReturn(expectedList);
        when(transactionServiceHelper.convertToDTO(entity)).thenReturn(transactionDTO);

        List<TransactionDTO> actualList = transactionService.getAllTransactions();

        assertThat(actualList).hasSize(1);
        assertThat(actualList)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(transactionDTO);

        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list of all transaction ssuccessfully")
    void shouldReturnEmptyListOfAllTransactionsSuccessfully() {
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        List<TransactionDTO> actualList = transactionService.getAllTransactions();

        assertThat(actualList).isEmpty();

        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return transactions by accountId successfully")
    void shouldReturnTransactionsByAccountIdSuccessfully() {
        TransactionDTO transactionDTO = createTransactionDTO();
        TransactionEntity entity = createTransactionEntity();
        List<TransactionEntity> expectedList = Collections.singletonList(entity);

        when(transactionRepository.
                getAllTransactionsByAccountFromIdOrAccountToId(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_FROM)).
                thenReturn(expectedList);
        when(transactionServiceHelper.convertToDTO(entity)).thenReturn(transactionDTO);

        List<TransactionDTO> actualList = transactionService.getTransactionsByAccountId(VALID_ACCOUNT_ID_FROM);

        assertThat(actualList).hasSize(1);
        assertThat(actualList)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(transactionDTO);

        verify(transactionRepository, times(1)).getAllTransactionsByAccountFromIdOrAccountToId(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_FROM);

    }

    @Test
    @DisplayName("Should return empty list when no transactions by accountId")
    void shouldReturnEmptyListOfTransactionsByAccountIdSuccessfully() {
        when(transactionRepository.getAllTransactionsByAccountFromIdOrAccountToId(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_FROM)).thenReturn(Collections.emptyList());

        List<TransactionDTO> actualList = transactionService.getTransactionsByAccountId(VALID_ACCOUNT_ID_FROM);

        assertThat(actualList).isEmpty();

        verify(transactionRepository, times(1)).getAllTransactionsByAccountFromIdOrAccountToId(VALID_ACCOUNT_ID_FROM,VALID_ACCOUNT_ID_FROM);
    }


    @Test
    @DisplayName("Should return all transactions by category successfully")
    public void shouldReturnAllTransactionsByCategorySuccessfully() {
        TransactionEntity transactionEntity = createTransactionEntity();
        TransactionDTO transactionDTO = createTransactionDTO();
        List<TransactionEntity> entities = Collections.singletonList(transactionEntity);
        List<TransactionDTO> expectedList = createTransactionDTOList();

        when(transactionRepository.getAllTransactionsByCategory(TEST_CATEGORY))
                .thenReturn(entities);
        when(transactionServiceHelper.convertToDTO(transactionEntity))
                .thenReturn(transactionDTO);

        List<TransactionDTO> actualList =
                transactionService.getTransactionsByCategory(TEST_CATEGORY);

        assertThat(actualList)
                .usingRecursiveComparison()
                .isEqualTo(expectedList);

        verify(transactionRepository).getAllTransactionsByCategory(TEST_CATEGORY);
        verify(transactionServiceHelper).convertToDTO(transactionEntity);
    }

    @Test
    @DisplayName("Should return empty list of transaction by category successfully")
    void shouldReturnEmptyListOfAllTransactionsByCategorySuccessfully() {
        List<TransactionEntity> entities = Collections.emptyList();

        when(transactionRepository.getAllTransactionsByCategory(TEST_CATEGORY))
                .thenReturn(entities);

        List<TransactionDTO> actualList =
                transactionService.getTransactionsByCategory(TEST_CATEGORY);

        assertThat(actualList).isEmpty();

        verify(transactionRepository).getAllTransactionsByCategory(TEST_CATEGORY);
        verify(transactionServiceHelper, never()).convertToDTO(any());
    }

    @Test
    @DisplayName("Should return all transactions by account id which exceed limit successfully")
    public void shouldReturnAllTransactionsByAccountIdExceedLimitSuccessfully() {
        TransactionEntity transactionEntity = createTransactionEntity();
        TransactionDTO transactionDTO = createTransactionDTO();
        List<TransactionEntity> entities = Collections.singletonList(transactionEntity);
        List<TransactionDTO> expectedList = createTransactionDTOList();

        when(transactionRepository.getAllTransactionsByAccountFromIdOrAccountToIdAndLimitExceededIsTrue(VALID_ACCOUNT_ID_FROM, VALID_ACCOUNT_ID_FROM))
                .thenReturn(entities);
        when(transactionServiceHelper.convertToDTO(transactionEntity))
                .thenReturn(transactionDTO);

        List<TransactionDTO> actualList =
                transactionService.getTransactionsByAccountIdWhichExceedLimit(VALID_ACCOUNT_ID_FROM);

        assertThat(actualList)
                .usingRecursiveComparison()
                .isEqualTo(expectedList);

        verify(transactionRepository).getAllTransactionsByAccountFromIdOrAccountToIdAndLimitExceededIsTrue(VALID_ACCOUNT_ID_FROM, VALID_ACCOUNT_ID_FROM);
        verify(transactionServiceHelper).convertToDTO(transactionEntity);
    }

    @Test
    @DisplayName("Should return empty list of transaction by account id which exceed limit successfully")
    void shouldReturnEmptyListOfTransactionsByAccountIdExceedLimitSuccessfully() {
        List<TransactionEntity> entities = Collections.emptyList();

        when(transactionRepository.getAllTransactionsByAccountFromIdOrAccountToIdAndLimitExceededIsTrue(VALID_ACCOUNT_ID_FROM, VALID_ACCOUNT_ID_FROM))
                .thenReturn(entities);

        List<TransactionDTO> actualList =
                transactionService.getTransactionsByAccountIdWhichExceedLimit(VALID_ACCOUNT_ID_FROM);

        assertThat(actualList).isEmpty();

        verify(transactionRepository).getAllTransactionsByAccountFromIdOrAccountToIdAndLimitExceededIsTrue(VALID_ACCOUNT_ID_FROM, VALID_ACCOUNT_ID_FROM);
        verify(transactionServiceHelper, never()).convertToDTO(any());
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
    private List<TransactionDTO> createTransactionDTOList() {
        TransactionDTO transactionDTO1 = createTransactionDTO();

        return List.of(transactionDTO1);
    }
    private TransactionEntity createTransactionEntity() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(TRANSACTION_ID);
        return transactionEntity;
    }
    private LimitEntity createLimitEntity() {
        LimitEntity limitEntity = new LimitEntity();
        limitEntity.setId(LIMIT_ID);
        limitEntity.setLimitSum(TEST_LIMIT_SUM);
        limitEntity.setCategory(TEST_CATEGORY);
        limitEntity.setLimitCurrencyShortName(Currency.USD);
        limitEntity.setLimitRemainder(TEST_SUM);
        limitEntity.setLimitDateTime(TEST_DATE);
        return limitEntity;
    }
}