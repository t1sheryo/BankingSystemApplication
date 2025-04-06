package com.bankingsystem.app.unit.service;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.AccountPair;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.repository.LimitRepository;
import com.bankingsystem.app.repository.TransactionRepository;
import com.bankingsystem.app.services.impl.LimitService;
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
public class LimitServiceTest {
    @InjectMocks
    private LimitService limitService;

    @Mock
    private LimitRepository limitRepository;
    private static final Long LIMIT_ID = 1L;
    private static final Long VALID_ACCOUNT_ID = 1L;
    private static final Long INVALID_ACCOUNT_ID = -1L;
    private static final Currency TEST_CURRENCY = Currency.USD;
    private static final Category TEST_CATEGORY = Category.SERVICE;
    private static final BigDecimal VALID_LIMIT = BigDecimal.valueOf(1000);

    private static final BigDecimal PREV_LIMIT = BigDecimal.valueOf(700);
    private static final BigDecimal PREV_REMAINDER = BigDecimal.valueOf(400);
    private static final OffsetDateTime OLD_DATE = OffsetDateTime.now().minusMonths(2);
    private static final OffsetDateTime NOW = OffsetDateTime.now();

    private LimitEntity createLimitEntity()
    {
        LimitEntity limitEntity = new LimitEntity();
        limitEntity.setId(LIMIT_ID);
        AccountEntity accountEntity = new AccountEntity();
        limitEntity.setAccount(accountEntity);
        accountEntity.setId(VALID_ACCOUNT_ID);
        limitEntity.setCategory(TEST_CATEGORY);
        limitEntity.setLimitDateTime(OLD_DATE);
        limitEntity.setLimitCurrencyShortName(TEST_CURRENCY);
        limitEntity.setLimitSum(PREV_LIMIT);
        limitEntity.setLimitRemainder(PREV_REMAINDER);
        return limitEntity;
    };
    private LimitRequest createLimitRequest()
    {
        LimitRequest limitRequest = new LimitRequest();
        limitRequest.setAccountId(VALID_ACCOUNT_ID);
        limitRequest.setCategory(TEST_CATEGORY);
        limitRequest.setLimit(VALID_LIMIT);
        limitRequest.setLimitCurrency(TEST_CURRENCY);
        return  limitRequest;
    }
    @Test
    @DisplayName("Schould set limit successfully")
    void shouldSetLimitSuccessfully()
    {

    }



}
