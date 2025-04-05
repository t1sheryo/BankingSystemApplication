package com.bankingsystem.app.unit.service;

import com.bankingsystem.app.controller.TransactionController;
import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.repository.TransactionRepository;
import com.bankingsystem.app.services.impl.TransactionService;
import com.bankingsystem.app.services.interfaces.AccountServiceInterface;
import com.bankingsystem.app.services.interfaces.TransactionServiceHelperInterface;
import com.bankingsystem.app.services.interfaces.TransactionServiceInterface;

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
    private static final Long VALID_ACCOUNT_ID_TO= 2L;
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

    private TransactionDTO createTransactionDTO(Long accountIdFrom,Long accountIdTo) {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountIdFrom(accountIdFrom);
        transactionDTO.setAccountIdTo(accountIdTo);
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
    private TransactionEntity createTransactionEntity(Long id) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(id);
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
    //TODO:
    //тестики

}
