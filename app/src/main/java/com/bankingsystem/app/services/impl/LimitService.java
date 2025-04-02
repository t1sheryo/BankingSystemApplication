package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.customExceptions.LimitUpdateNotAllowedException;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;
import com.bankingsystem.app.repository.LimitRepository;
import com.bankingsystem.app.services.interfaces.LimitServiceInterface;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j // логирование
@Service
public class LimitService implements LimitServiceInterface {

    private final LimitRepository limitRepository;
    private static final int COOLDOWN_PERIOD_TO_SET_NEW_LIMIT_IN_MONTH = 1;

    @Autowired // говорит, что необходимо найти и внедрить
    // зависимость LimitRepository
    public LimitService(LimitRepository limitRepository) {
        this.limitRepository = limitRepository;
    }


    // FIXME: добавить user table, изменить структуру, чтобы адаптировать под это


    @Override
    @Transactional
    // @Transactional гарантирует, что вся операция
    // будет выполнена как единое целое:
    // либо все изменения будут успешно сохранены в базе данных,
    // либо, в случае ошибки,
    // ничего не будет сохранено (rollback).
    public LimitEntity setLimit(LimitRequest limit) {
        if(limit.getLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Limit cannot be less than 0");
        }

        OffsetDateTime now = OffsetDateTime.now();
        LimitEntity existingLimit = limitRepository.getLimitByAccountIdAndCategory(limit.getAccountId(), limit.getCategory());
        if(existingLimit == null) {
            throw new IllegalStateException("Limit not found for account id " + limit.getAccountId());
        }

        OffsetDateTime prevUpdateTime = existingLimit.getLimitDateTime();
        BigDecimal prevRemainder = existingLimit.getLimitRemainder();

        // Подсчитываем сколько времени прошло с момента прошлого обновления лимита
        long monthsBetween = ChronoUnit.MONTHS.between(
                prevUpdateTime.truncatedTo(ChronoUnit.DAYS),
                now.truncatedTo(ChronoUnit.DAYS)
        );

        // Проверяем, превышает ли период 1 месяц
        if (monthsBetween <= COOLDOWN_PERIOD_TO_SET_NEW_LIMIT_IN_MONTH) {
            // Логирование того, что пользователь слишком рано хочет обновить лимит
            log.warn("Attempt to update limit too soon. Account: {}, Category: {}, Last update: {}",
                    limit.getAccountId(),
                    limit.getCategory(),
                    existingLimit.getLimitDateTime());

            throw new LimitUpdateNotAllowedException(
                    String.format("Limit can be updated only once per %d month(s). " +
                                    "Last update was at %s",
                            COOLDOWN_PERIOD_TO_SET_NEW_LIMIT_IN_MONTH,
                            prevUpdateTime.format(DateTimeFormatter.ISO_DATE))
            );
        }

        existingLimit.setLimitSum(limit.getLimit());
        existingLimit.setLimitRemainder(prevRemainder.add(limit.getLimit().subtract(existingLimit.getLimitSum())));
        existingLimit.setLimitCurrencyShortName(Currency.USD);
        existingLimit.setLimitDateTime(now);
        return limitRepository.save(existingLimit);
    }

    @Override
    public List<LimitResponse> getLimitsByAccountId(Long accountId) {
        List<LimitEntity> limits = limitRepository.findByAccountId(accountId);

        return limits.stream()
                .map(this::convertToLimitResponse)
                .collect(Collectors.toList());
    }
    @Override
    public Optional<LimitEntity> getLimitByAccountIdAndCategory(Long accountId, Category category) {
        return limitRepository.findFirstByAccountIdAndCategoryOrderByLimitDateTimeDesc(accountId,category);
    }

    @Override
    public LimitEntity getLimitByDBId(Long DBId){
        return limitRepository.findById(DBId).orElse(null);
    }

    @Override
    public List<LimitResponse> getAllLimits() {
        List<LimitEntity> limits = limitRepository.findAll();
        return limits.stream()
                .map(this::convertToLimitResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LimitEntity saveLimit(LimitEntity limit) {
        return limitRepository.save(limit);
    }

    // вспомогательный метод для преобразования
    // LimitEntity в LimitResponse
    private LimitResponse convertToLimitResponse(LimitEntity limitEntity) {
        if(limitEntity.getAccount() == null) {
            throw new IllegalArgumentException("Account is not set for limitId " + limitEntity.getId());
        }
        LimitResponse response = new LimitResponse();
        response.setAccountId(limitEntity.getAccount().getId());
        response.setCategory(limitEntity.getCategory());
        response.setLimit(limitEntity.getLimitSum());
        response.setLastUpdate(limitEntity.getLimitDateTime());
        response.setRemainder(limitEntity.getLimitRemainder());

        return response;
    }
}