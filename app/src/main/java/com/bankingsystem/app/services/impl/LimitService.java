package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;
import com.bankingsystem.app.repository.LimitRepository;
import com.bankingsystem.app.services.interfaces.LimitServiceInterface;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // логирование
@Service
public class LimitService implements LimitServiceInterface {
    private final LimitRepository limitRepository;

    @Autowired // говорит, что необходимо найти и внедрить
    // зависимость LimitRepository
    public LimitService(LimitRepository limitRepository) {
        this.limitRepository = limitRepository;
    }


    @Override
    // @Transactional гарантирует, что вся операция
    // будет выполнена как единое целое:
    // либо все изменения будут успешно сохранены в базе данных,
    // либо, в случае ошибки,
    // ничего не будет сохранено (rollback).
    @Transactional
    public void setLimit(LimitRequest limit) {
        // переносим все данные из LimitRequest в LimitEntity
        LimitEntity limitEntity = new LimitEntity();
        limitEntity.setAccountId(limit.getAccountId());
        limitEntity.setLimitSum(limit.getLimit());
        limitEntity.setCategory(limit.getCategory());
        limitEntity.setLimitDateTime(OffsetDateTime.now());
        limitEntity.setLimitCurrencyShortName(limit.getLimitCurrency());

        limitRepository.save(limitEntity);
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
        // FIXME: возможна неправильная работа из-за неправильной реализации метода в репозитории
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
    // @Transactional гарантирует, что вся операция
    // будет выполнена как единое целое:
    // либо все изменения будут успешно сохранены в базе данных,
    // либо, в случае ошибки,
    // ничего не будет сохранено (rollback).
    @Transactional
    public void updateRemainder(TransactionDTO transaction) {
        LimitEntity limitEntity = limitRepository.findById(transaction.getLimitId()).orElse(null);
        BigDecimal transactionValue = transaction.getSum();

        // FIXME: рассмотреть ситуацию, когда limitEntity == null
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
