package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.customExceptions.LimitUpdateNotAllowedException;
import com.bankingsystem.app.entity.LimitEntity;
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
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
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


    // FIXME: сделать, чтобы клиент не мог обновлять лимит, когда захочет.
    //  Сделать ограничение на единицу времени

    @Override
    @Transactional
    // @Transactional гарантирует, что вся операция
    // будет выполнена как единое целое:
    // либо все изменения будут успешно сохранены в базе данных,
    // либо, в случае ошибки,
    // ничего не будет сохранено (rollback).
    public LimitEntity setLimit(LimitRequest limit) {
        OffsetDateTime now = OffsetDateTime.now();
        LimitEntity existingLimit = limitRepository.getLimitByAccountIdAndCategory(limit.getAccountId(), limit.getCategory());
        OffsetDateTime prevUpdateTime = existingLimit.getLimitDateTime();

        // Подсчитываем сколько времени прошло с момента прошлого обновления лимита
        long monthsBetween = ChronoUnit.MONTHS.between(
                prevUpdateTime.truncatedTo(ChronoUnit.DAYS),
                now.truncatedTo(ChronoUnit.DAYS)
        );

        // Проверяем, превышает ли период 1 месяц
        if(monthsBetween <= COOLDOWN_PERIOD_TO_SET_NEW_LIMIT_IN_MONTH){
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

        // Если лимит существует - обновляем его
        if (existingLimit != null) {
            existingLimit.setLimitSum(limit.getLimit());
            existingLimit.setLimitCurrencyShortName(limit.getLimitCurrency());
            existingLimit.setLimitDateTime(now);
            return limitRepository.save(existingLimit);
        }

        // Если не существует - создаем новый
        LimitEntity newLimit = new LimitEntity();
        newLimit.setAccountId(limit.getAccountId());
        newLimit.setLimitSum(limit.getLimit());
        newLimit.setCategory(limit.getCategory());
        newLimit.setLimitDateTime(now);
        newLimit.setLimitCurrencyShortName(limit.getLimitCurrency());

        return limitRepository.save(newLimit);
    }

    @Override
    public List<LimitResponse> getLimitsByAccountId(Long accountId) {
        List<LimitEntity> limits = limitRepository.findByAccountId(accountId);

        return limits.stream()
                .map(this::convertToLimitResponse)
                .collect(Collectors.toList());
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
    @Transactional
    // @Transactional гарантирует, что вся операция
    // будет выполнена как единое целое:
    // либо все изменения будут успешно сохранены в базе данных,
    // либо, в случае ошибки,
    // ничего не будет сохранено (rollback).
    public void updateRemainder(TransactionDTO transaction) {
        LimitEntity limitEntity = limitRepository.findById(transaction.getLimitId()).orElse(null);
        BigDecimal transactionValue = transaction.getSum();

        if(limitEntity == null){
            log.error("No limit found with id: {}", transaction.getLimitId());
            return ;
        }

        limitEntity.setLimitRemainder(limitEntity.getLimitRemainder().subtract(transactionValue));

        // Грубо говоря, тут происходит перезапись объекта limitEntity в БД
        // Тут вызывается механизм "dirty checking"
        // и Hibernate автоматически обновляет только те поля, которые были изменены
        limitRepository.save(limitEntity);
    }

    // вспомогательный метод для преобразования
    // LimitEntity в LimitResponse
    private LimitResponse convertToLimitResponse(LimitEntity limitEntity) {
        LimitResponse response = new LimitResponse();
        response.setAccountId(limitEntity.getAccountId());
        response.setCategory(limitEntity.getCategory());
        response.setLimit(limitEntity.getLimitSum());
        response.setLastUpdate(limitEntity.getLimitDateTime());
        response.setRemainder(limitEntity.getLimitRemainder());

        return response;
    }
}
