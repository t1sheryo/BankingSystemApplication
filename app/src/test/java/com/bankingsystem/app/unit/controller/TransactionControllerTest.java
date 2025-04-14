package com.bankingsystem.app.unit.controller;

import com.bankingsystem.app.controller.TransactionController;
import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.service.interfaces.AccountServiceInterface;
import com.bankingsystem.app.service.interfaces.TransactionServiceInterface;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {
    @InjectMocks
    private TransactionController controller;
    @Mock
    private TransactionServiceInterface transactionService;
    @Mock
    private AccountServiceInterface accountService;


    private static final Long VALID_ACCOUNT_ID_FROM = 1L;
    private static final Long VALID_ACCOUNT_ID_TO = 2L;
    private static final Long INVALID_ACCOUNT_ID = -1L;
    private static final Currency TEST_CURRENCY = Currency.EUR;
    private static final Category TEST_CATEGORY = Category.PRODUCT;
    private static final BigDecimal TEST_SUM = BigDecimal.valueOf(1000);
    private static final OffsetDateTime TEST_DATE = OffsetDateTime.now().minusDays(1);
    private static final Long TRANSACTION_ID = 1L;

    @Test
    @DisplayName("Should create transaction successfully")
    void shouldCreateTransactionSuccessfully() {
        TransactionDTO dto = createTransactionDTO();
        TransactionEntity entity = createTransactionEntity();

        when(transactionService.createTransaction(dto)).thenReturn(entity);

        ResponseEntity<TransactionEntity> response = controller.createTransaction(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/bank/transactions/" + TRANSACTION_ID);
        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(entity);

        verify(transactionService, times(1)).createTransaction(dto);
    }

   @Test
   @DisplayName("Should return exceeded transactions for valid accountId")
    void shouldReturnExceededTransactionsForValidAccountId() {
        TransactionDTO dto = createTransactionDTO();
        List<TransactionDTO> exceededTransactions = Collections.singletonList(dto);

        when(accountService.getAccountById(VALID_ACCOUNT_ID_FROM)).thenReturn(new AccountEntity());
        when(transactionService.getTransactionsByAccountIdWhichExceedLimit(VALID_ACCOUNT_ID_FROM)).
                thenReturn(exceededTransactions);

       ResponseEntity<List<TransactionDTO>> response = controller.getTransactionsExceededLimit(VALID_ACCOUNT_ID_FROM);
       assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
       assertThat(response.getBody())
               .usingRecursiveComparison()
               .isEqualTo(exceededTransactions);

       verify(transactionService, times(1)).getTransactionsByAccountIdWhichExceedLimit(VALID_ACCOUNT_ID_FROM);

   }

    @Test
    @DisplayName("Should return exception with invalid accountId")
    void shouldReturnExceptionWithInvalidAccountId() {

        assertThatThrownBy(() -> controller.getTransactionsExceededLimit(INVALID_ACCOUNT_ID)).
                isInstanceOf(IllegalArgumentException.class).
                hasMessage("Invalid account Id");

        verify(transactionService, never()).getTransactionsByAccountIdWhichExceedLimit(any());
    }

    @Test
    @DisplayName("Should return all transactions")
    void shouldReturnAllTransactions() {
        TransactionDTO dto = createTransactionDTO();
        List<TransactionDTO> transactions = Collections.singletonList(dto);

        when(transactionService.getAllTransactions()).thenReturn(transactions);
        ResponseEntity<List<TransactionDTO>> response = controller.getAllTransactions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(transactions);

        verify(transactionService, times(1)).getAllTransactions();
    }

    @Test
    @DisplayName("Should return empty list when no transactions")
    void shouldReturnEmptyListWhenNoTransactions() {
        when(transactionService.getAllTransactions()).thenReturn(Collections.emptyList());
        ResponseEntity<List<TransactionDTO>> response = controller.getAllTransactions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();

        verify(transactionService, times(1)).getAllTransactions();
    }

    @Test
    @DisplayName("Should return transactions by category")
    void shouldReturnTransactionsByCategory() {
        TransactionDTO transactionDTO = createTransactionDTO();
        List<TransactionDTO> transactions = Collections.singletonList(transactionDTO);

        when(transactionService.getTransactionsByCategory(TEST_CATEGORY)).thenReturn(transactions);
        ResponseEntity<List<TransactionDTO>> response = controller.getTransactionsByCategory(TEST_CATEGORY);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(transactions);

        verify(transactionService, times(1)).getTransactionsByCategory(TEST_CATEGORY);
    }

    @Test
    @DisplayName("Should return transactions by Account Id")
    void shouldReturnTransactionsByAccountId() {
        TransactionDTO transactionDTO = createTransactionDTO();
        List<TransactionDTO> transactions = Collections.singletonList(transactionDTO);

        when(transactionService.getTransactionsByAccountId(VALID_ACCOUNT_ID_FROM)).thenReturn(transactions);

        ResponseEntity<List<TransactionDTO>> response = controller.getTransactionsByAccountId(VALID_ACCOUNT_ID_FROM,false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(transactions);

        verify(transactionService, times(1)).getTransactionsByAccountId(VALID_ACCOUNT_ID_FROM);
    }

    @Test
    @DisplayName("Should return exceeded transactions when exceeded is true")
    void shouldReturnExceededTransactionsWhenExceededIsTrue() {
        TransactionDTO transactionDTO = createTransactionDTO();
        List<TransactionDTO> exceededTransactions = Collections.singletonList(transactionDTO);

        when(transactionService.getTransactionsByAccountIdWhichExceedLimit(VALID_ACCOUNT_ID_FROM)).thenReturn(exceededTransactions);

        ResponseEntity<List<TransactionDTO>> response = controller.getTransactionsByAccountId(VALID_ACCOUNT_ID_FROM,true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(exceededTransactions);

        verify(transactionService, times(1)).getTransactionsByAccountIdWhichExceedLimit(VALID_ACCOUNT_ID_FROM);
    }
    private TransactionDTO createTransactionDTO()
    {
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
    private TransactionEntity createTransactionEntity()
    {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(TRANSACTION_ID);
        return transactionEntity;
    }
}