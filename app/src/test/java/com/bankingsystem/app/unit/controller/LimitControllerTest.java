package com.bankingsystem.app.unit.controller;

import com.bankingsystem.app.controller.LimitController;
import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;
import com.bankingsystem.app.service.interfaces.AccountServiceInterface;
import com.bankingsystem.app.service.interfaces.LimitServiceInterface;
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
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LimitControllerTest {
    @InjectMocks
    LimitController limitController;
    @Mock
    LimitServiceInterface limitService;
    @Mock
    AccountServiceInterface accountService;
    private static final Long ACCOUNT_ID = 2L;
    private static final Long LIMIT_ID = 1L;
    private static final BigDecimal LIMIT_SUM = BigDecimal.valueOf(1000);
    private static final Category LIMIT_CATEGORY = Category.PRODUCT;
    private static final OffsetDateTime LIMIT_DATE_TIME =
            OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    private static final Currency LIMIT_CURRENCY = Currency.USD;
    private static final BigDecimal LIMIT_REMAINDER = BigDecimal.valueOf(500);
    private static final AccountEntity LIMIT_ACCOUNT =
            new AccountEntity(ACCOUNT_ID);

    private static final String RESPONSE_HEADER = "Location";
    private static final String RESPONSE_HEADER_VALUE = "/bank/limits/" + LIMIT_ID;

    @Test
    @DisplayName("Should create limit successfully")
    void shouldCreateLimitSuccessfully() {
        LimitRequest limitRequest = createLimitRequest();
        LimitEntity expectedLimitEntity = createLimitEntity();
        AccountEntity accountEntity = LIMIT_ACCOUNT;

        when(accountService.getAccountById(ACCOUNT_ID)).thenReturn(accountEntity);
        when(limitService.setLimit(limitRequest)).thenReturn(expectedLimitEntity);

        ResponseEntity<LimitEntity> actualResponse =
                limitController.createLimit(limitRequest);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actualResponse.getHeaders().getFirst(RESPONSE_HEADER))
                .isEqualTo(RESPONSE_HEADER_VALUE);
        assertThat(actualResponse.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedLimitEntity);

        verify(accountService).getAccountById(ACCOUNT_ID);
        verify(limitService).setLimit(limitRequest);
    }

    @Test
    @DisplayName("Should return NOT_FOUND status with limit")
    void shouldReturnNotFoundStatus() {
        LimitRequest limitRequest = createLimitRequest();

        when(accountService.getAccountById(ACCOUNT_ID)).thenReturn(null);

        ResponseEntity<LimitEntity> actualResponse =
                limitController.createLimit(limitRequest);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(actualResponse.getHeaders().isEmpty()).isTrue();
        assertThat(actualResponse.getBody()).isNull();

        verify(accountService).getAccountById(ACCOUNT_ID);
        verify(limitService, never()).setLimit(any());
    }

    @Test
    @DisplayName("Should return all limits by id successfully")
    void shouldReturnAllLimitsByAccountIdSuccessfully() {
        List<LimitResponse> expectedList = createListOfLimitResponses();

        when(limitService.getLimitsByAccountId(ACCOUNT_ID)).thenReturn(expectedList);

        ResponseEntity<List<LimitResponse>> actualList =
                limitController.getAllLimitsByAccountId(ACCOUNT_ID);

        assertThat(actualList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(expectedList)
                .usingRecursiveComparison()
                .isEqualTo(actualList.getBody());

        verify(limitService).getLimitsByAccountId(ACCOUNT_ID);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status when account id is null")
    void shouldReturnBadRequestStatusWhenAccountIdEqualsNull() {
        Long accountId = null;

        ResponseEntity<List<LimitResponse>> actualResponse =
                limitController.getAllLimitsByAccountId(accountId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(actualResponse.getBody()).isNull();

        verify(accountService, never()).getAccountById(accountId);
        verify(limitService, never()).setLimit(any());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status when account id is less than 0")
    void shouldReturnBadRequestStatusWhenAccountIdIsLessThanZero() {
        Long accountId = -1L;

        ResponseEntity<List<LimitResponse>> actualResponse =
                limitController.getAllLimitsByAccountId(accountId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(actualResponse.getBody()).isNull();

        verify(accountService, never()).getAccountById(accountId);
        verify(limitService, never()).setLimit(any());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST status when account id is 0")
    void shouldReturnBadRequestStatusWhenAccountIdEqualsZero() {
        Long accountId = 0L;

        ResponseEntity<List<LimitResponse>> actualResponse =
                limitController.getAllLimitsByAccountId(accountId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(actualResponse.getBody()).isNull();

        verify(accountService, never()).getAccountById(accountId);
        verify(limitService, never()).setLimit(any());
    }

    @Test
    @DisplayName("Should return empty list of limits because no limits by id were found")
    void shouldReturnEmptyListBecauseLimitsWereNotFound() {
        List<LimitResponse> expectedList = Collections.emptyList();

        when(limitService.getLimitsByAccountId(ACCOUNT_ID))
                .thenReturn(expectedList);

        ResponseEntity<List<LimitResponse>> actualList =
                limitController.getAllLimitsByAccountId(ACCOUNT_ID);

        assertThat(actualList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualList.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedList);

        verify(limitService).getLimitsByAccountId(ACCOUNT_ID);
    }

    @Test
    @DisplayName("Should return all limits")
    void shouldReturnListOfLimitsSuccessfully() {
        List<LimitResponse> expectedList = createListOfLimitResponses();

        when(limitService.getAllLimits())
                .thenReturn(expectedList);

        ResponseEntity<List<LimitResponse>> actualList =
                limitController.getAllLimits();

        assertThat(actualList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualList.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedList);

        verify(limitService).getAllLimits();
    }

    @Test
    @DisplayName("Should return empty list because no limits were found")
    void shouldReturnEmptyListOfLimits() {
        List<LimitResponse> expectedList = Collections.emptyList();

        when(limitService.getAllLimits())
                .thenReturn(expectedList);

        ResponseEntity<List<LimitResponse>> actualList =
                limitController.getAllLimits();

        assertThat(actualList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualList.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedList);

        verify(limitService).getAllLimits();
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

    private LimitRequest createLimitRequest() {
        LimitRequest limitRequest = new LimitRequest();

        limitRequest.setAccountId(ACCOUNT_ID);
        limitRequest.setLimit(LIMIT_SUM);
        limitRequest.setCategory(LIMIT_CATEGORY);
        limitRequest.setLimitCurrency(LIMIT_CURRENCY);

        return limitRequest;
    }

    private List<LimitResponse> createListOfLimitResponses() {
        LimitResponse limitResponse1 = new LimitResponse();
        limitResponse1.setAccountId(ACCOUNT_ID);
        limitResponse1.setCategory(LIMIT_CATEGORY);
        limitResponse1.setLimit(LIMIT_SUM);
        limitResponse1.setLastUpdate(
                OffsetDateTime.of(2023, 10, 1, 12, 0, 0, 0, ZoneOffset.UTC)
        );
        limitResponse1.setRemainder(LIMIT_REMAINDER);

        LimitResponse limitResponse2 = new LimitResponse();
        limitResponse2.setAccountId(ACCOUNT_ID + 1);
        limitResponse2.setCategory(LIMIT_CATEGORY);
        limitResponse2.setLimit(LIMIT_SUM);
        limitResponse2.setLastUpdate(
                OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        );
        limitResponse2.setRemainder(LIMIT_REMAINDER);

        return List.of(limitResponse1, limitResponse2);
    }
}
