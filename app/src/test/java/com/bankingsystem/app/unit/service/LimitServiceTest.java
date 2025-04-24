package com.bankingsystem.app.unit.service;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;
import com.bankingsystem.app.repository.LimitRepository;
import com.bankingsystem.app.service.impl.LimitService;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private static final BigDecimal VALID_LIMIT = BigDecimal.valueOf(1000L);
    private static final BigDecimal PREV_LIMIT = BigDecimal.valueOf(700L);
    private static final BigDecimal PREV_REMAINDER = BigDecimal.valueOf(400L);
    private static final OffsetDateTime OLD_DATE = OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime NOW = OffsetDateTime.now();

    @Test
    @DisplayName("Should set limit successfully")
    void shouldSetLimitSuccessfully() {
        LimitRequest limitRequest = createLimitRequest();
        LimitEntity expectedLimitEntity = createLimitEntity();

        Mockito.when(limitRepository.getLimitByAccountIdAndCategory(VALID_ACCOUNT_ID, TEST_CATEGORY))
                .thenReturn(expectedLimitEntity);
        Mockito.when(limitRepository.save(ArgumentMatchers.any(LimitEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LimitEntity actualLimit = limitService.setLimit(limitRequest);

        AssertionsForClassTypes.assertThat(actualLimit)
                .usingRecursiveComparison()
                .isEqualTo(expectedLimitEntity);
    }

    @Test
    @DisplayName("Should return limits by account id successfully")
    void shouldReturnLimitsByAccountIdSuccessfully() {
        List<LimitEntity> limitEntities = createListOfLimitEntities();
        List<LimitResponse> limitResponses = createListOfCorrespondingLimitResponse();

        Mockito.when(limitRepository.findByAccountId(VALID_ACCOUNT_ID)).thenReturn(limitEntities);

        List<LimitResponse> actualList = limitService.getLimitsByAccountId(VALID_ACCOUNT_ID);

        AssertionsForClassTypes.assertThat(actualList)
                .usingRecursiveComparison()
                .isEqualTo(limitResponses);

        Mockito.verify(limitRepository).findByAccountId(VALID_ACCOUNT_ID);
    }

    @Test
    @DisplayName("Should return empty list of limits by account id successfully")
    void shouldReturnEmptyListOfLimitsByAccountIdSuccessfully() {
        Mockito.when(limitRepository.findByAccountId(VALID_ACCOUNT_ID)).thenReturn(Collections.emptyList());

        List<LimitResponse> actualList = limitService.getLimitsByAccountId(VALID_ACCOUNT_ID);

        AssertionsForClassTypes.assertThat(actualList).isEqualTo(Collections.emptyList());
    }

    @Test
    @DisplayName("Should return limit by account id and category successfully")
    void shouldReturnLimitByAccountIdAndCategorySuccessfully() {
        LimitEntity expectedLimitEntity = createLimitEntity();

        Mockito.when(limitRepository.findFirstByAccountIdAndCategoryOrderByLimitDateTimeDesc(VALID_ACCOUNT_ID, TEST_CATEGORY))
                .thenReturn(Optional.of(expectedLimitEntity));

        Optional<LimitEntity> actualLimitEntity = limitService.getLimitByAccountIdAndCategory(VALID_ACCOUNT_ID, TEST_CATEGORY);

        AssertionsForClassTypes.assertThat(actualLimitEntity.get())
                .usingRecursiveComparison()
                .isEqualTo(expectedLimitEntity);
    }

    @Test
    @DisplayName("Should return empty limit by account id and category successfully")
    void shouldReturnEmptyLimitByAccountIdAndCategorySuccessfully() {
        Mockito.when(limitRepository.findFirstByAccountIdAndCategoryOrderByLimitDateTimeDesc(VALID_ACCOUNT_ID, TEST_CATEGORY))
                .thenReturn(Optional.empty());

        Optional<LimitEntity> actualLimitEntity = limitService.getLimitByAccountIdAndCategory(VALID_ACCOUNT_ID, TEST_CATEGORY);

        AssertionsForClassTypes.assertThat(actualLimitEntity).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("Should return limit by db id successfully")
    void shouldReturnLimitByDbIdSuccessfully() {
        LimitEntity expectedLimitEntity = createLimitEntity();

        Mockito.when(limitRepository.findById(LIMIT_ID)).thenReturn(Optional.of(expectedLimitEntity));

        LimitEntity actualLimitEntity = limitService.getLimitByDBId(LIMIT_ID);

        AssertionsForClassTypes.assertThat(actualLimitEntity)
                .usingRecursiveComparison()
                .isEqualTo(expectedLimitEntity);
    }

    @Test
    @DisplayName("Should return null limit by db id successfully")
    void shouldReturnNullLimitByDbIdSuccessfully() {
        Mockito.when(limitRepository.findById(LIMIT_ID)).thenReturn(Optional.empty());

        LimitEntity actualLimitEntity = limitService.getLimitByDBId(LIMIT_ID);

        AssertionsForClassTypes.assertThat(actualLimitEntity).isNull();
    }

    @Test
    @DisplayName("Should return all limits successfully")
    void shouldReturnAllLimitsSuccessfully() {
        List<LimitEntity> limitEntities = createListOfLimitEntities();
        List<LimitResponse> limitResponses = createListOfCorrespondingLimitResponse();

        Mockito.when(limitRepository.findAll()).thenReturn(limitEntities);

        List<LimitResponse> actualList = limitService.getAllLimits();

        AssertionsForClassTypes.assertThat(actualList)
                .usingRecursiveComparison()
                .isEqualTo(limitResponses);

        Mockito.verify(limitRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list of limits successfully")
    void shouldReturnEmptyListOfLimitsSuccessfully() {
        Mockito.when(limitRepository.findAll()).thenReturn(Collections.emptyList());

        List<LimitResponse> actualList = limitService.getAllLimits();

        AssertionsForClassTypes.assertThat(actualList).isEqualTo(Collections.emptyList());
    }

    @Test
    @DisplayName("Should save limit successfully")
    void shouldSaveLimitSuccessfully() {
        LimitEntity limitToSave = createLimitEntity();
        LimitEntity savedLimit = createLimitEntity();
        savedLimit.setId(1L);

        Mockito.when(limitRepository.save(limitToSave)).thenReturn(savedLimit);

        LimitEntity actualLimit = limitService.saveLimit(limitToSave);

        AssertionsForClassTypes.assertThat(actualLimit)
                .usingRecursiveComparison()
                .isEqualTo(savedLimit);

        Mockito.verify(limitRepository).save(limitToSave);
    }

    @Test
    @DisplayName("Should return saved limit entity")
    void shouldReturnSavedEntity() {
        LimitEntity inputLimit = createLimitEntity();
        LimitEntity outputLimit = createLimitEntity();
        outputLimit.setId(2L);

        Mockito.when(limitRepository.save(inputLimit)).thenReturn(outputLimit);

        LimitEntity result = limitService.saveLimit(inputLimit);

        AssertionsForClassTypes.assertThat(result).isSameAs(outputLimit);
        AssertionsForClassTypes.assertThat(result.getId()).isEqualTo(2L);
    }

    private LimitEntity createLimitEntity() {
        LimitEntity limitEntity = new LimitEntity();
        limitEntity.setId(LIMIT_ID);

        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(VALID_ACCOUNT_ID);

        limitEntity.setAccount(accountEntity);
        limitEntity.setCategory(TEST_CATEGORY);
        limitEntity.setLimitDateTime(OLD_DATE);
        limitEntity.setLimitCurrencyShortName(TEST_CURRENCY);
        limitEntity.setLimitSum(PREV_LIMIT);
        limitEntity.setLimitRemainder(PREV_REMAINDER);

        return limitEntity;
    }

    private LimitRequest createLimitRequest() {
        LimitRequest limitRequest = new LimitRequest();
        limitRequest.setAccountId(VALID_ACCOUNT_ID);
        limitRequest.setCategory(TEST_CATEGORY);
        limitRequest.setLimit(VALID_LIMIT);
        limitRequest.setLimitCurrency(TEST_CURRENCY);
        return limitRequest;
    }

    private List<LimitEntity> createListOfLimitEntities() {
        LimitEntity limitEntity1 = new LimitEntity();
        AccountEntity accountEntity1 = new AccountEntity();
        accountEntity1.setId(VALID_ACCOUNT_ID);
        limitEntity1.setId(LIMIT_ID);
        limitEntity1.setLimitSum(VALID_LIMIT);
        limitEntity1.setCategory(TEST_CATEGORY);
        limitEntity1.setLimitDateTime(OLD_DATE);
        limitEntity1.setLimitCurrencyShortName(TEST_CURRENCY);
        limitEntity1.setLimitRemainder(PREV_REMAINDER);
        limitEntity1.setAccount(accountEntity1);

        LimitEntity limitEntity2 = new LimitEntity();
        AccountEntity accountEntity2 = new AccountEntity();
        accountEntity2.setId(VALID_ACCOUNT_ID);
        limitEntity2.setId(LIMIT_ID + 1L);
        limitEntity2.setLimitSum(VALID_LIMIT.add(BigDecimal.valueOf(1000L)));
        limitEntity2.setCategory(TEST_CATEGORY);
        limitEntity2.setLimitDateTime(OLD_DATE);
        limitEntity2.setLimitCurrencyShortName(TEST_CURRENCY);
        limitEntity2.setLimitRemainder(PREV_REMAINDER.add(BigDecimal.valueOf(200L)));
        limitEntity2.setAccount(accountEntity2);

        return List.of(limitEntity1, limitEntity2);
    }

    private List<LimitResponse> createListOfCorrespondingLimitResponse() {
        LimitResponse limitResponse1 = new LimitResponse();
        limitResponse1.setAccountId(VALID_ACCOUNT_ID);
        limitResponse1.setCategory(TEST_CATEGORY);
        limitResponse1.setLimit(VALID_LIMIT);
        limitResponse1.setLastUpdate(OLD_DATE);
        limitResponse1.setRemainder(PREV_REMAINDER);

        LimitResponse limitResponse2 = new LimitResponse();
        limitResponse2.setAccountId(VALID_ACCOUNT_ID);
        limitResponse2.setCategory(TEST_CATEGORY);
        limitResponse2.setLimit(VALID_LIMIT.add(BigDecimal.valueOf(1000L)));
        limitResponse2.setLastUpdate(OLD_DATE);
        limitResponse2.setRemainder(PREV_REMAINDER.add(BigDecimal.valueOf(200L)));

        return List.of(limitResponse1, limitResponse2);
    }
}
