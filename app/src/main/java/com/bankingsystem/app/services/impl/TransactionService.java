package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.repository.TransactionRepository;
import com.bankingsystem.app.services.interfaces.LimitServiceInterface;
import com.bankingsystem.app.services.interfaces.TransactionServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

//этот фикс ми написал чатик я не разбирался пока, я спать короче покич
// FIXME: Исправить логику создания транзакции для соответствия ТЗ
// TODO: 1. Добавить конвертацию суммы транзакции в USD с использованием ExchangeRateService
//       - Добавить зависимость ExchangeRateServiceInterface через @Autowired
//       - Создать метод convertToUsd для получения курса из ExchangeRateService и конвертации суммы
//       - Причина: по ТЗ лимиты в USD, а транзакции могут быть в любой валюте (KZT, RUB и т.д.), нужно сравнивать в USD
// TODO: 2. Перенести логику обновления limitRemainder из LimitService.updateRemainder
//       - Добавить зависимость LimitRepository через @Autowired
//       - Обновлять limit.setLimitRemainder(limit.getLimitRemainder().subtract(sumInUsd)), если лимит не превышен
//       - Причина: обновление остатка должно происходить атомарно с созданием транзакции для целостности данных
// TODO: 3. Сделать метод транзакционным с аннотацией @Transactional
//       - Добавить @Transactional над методом
//       - Причина: создание транзакции и обновление лимита должны быть одним атомарным действием
// TODO: 4. Добавить проверку на null для limit с выбросом исключения
//       - Использовать if (limit == null) с IllegalArgumentException
//       - Причина: сейчас при limit == null будет NullPointerException, нужно явное сообщение об ошибке

@Service
@Slf4j
public class TransactionService implements TransactionServiceInterface {
    private final TransactionRepository transactionRepository;
    private final LimitServiceInterface limitService;

    //  Autowired делает автоматическую инъекцию зависимостей(dependency injection)
    //   Аннотация @Autowired говорит Spring: "Найди бины типа TransactionRepository и LimitService
    //    в контексте приложения и передай их в этот конструктор
    @Autowired
    public TransactionService(TransactionRepository transactionRepository, LimitServiceInterface limitService) {
        this.transactionRepository = transactionRepository;
        this.limitService = limitService;
    }
    //FIXME:
    // Переделать логику транзакции согласно схеме
    // 1.Находим счет отправителя и получателя через AccountRepository(Этот пункт можно делать через AccountService
    // Который будет содержать нужную бизнес логику, но его надо написать)
    // Нужно это чтобы не было зависимостей между репозиториями
    // 2.Находим лимит для AccountFrom и Category
    // 3. Конвертируем сумму транзакции в доллары
    // 4.Проверяем превышает ли лимит транзакции(флажок limit_exeeded)
    // 5.Создаем транзакцию
    // 6.Сохраняем данные о лимите
    // 7. Сохраняем транзакцию
    // 8. Обновляем значения LimitRemainder

    @Override
    public TransactionEntity createTransaction(TransactionDTO transactionDTO) {
        LimitEntity limit = limitService.getLimitByDBId(transactionDTO.getLimitId());
        boolean limitExceeded = transactionDTO.getSum().compareTo(limit.getLimitSum()) > 0;

        //создаем сущность для передачи ее в репозиторий и сетаем в нее из transactionDTO нужную информацию
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAccountIdFrom(transactionDTO.getAccountIdFrom());
        transactionEntity.setAccountIdTo(transactionDTO.getAccountIdTo());
        transactionEntity.setCurrency(transactionDTO.getCurrency());
        transactionEntity.setSum(transactionDTO.getSum());
        transactionEntity.setCategory(transactionDTO.getExpenseCategory());
        transactionEntity.setTransactionTime(OffsetDateTime.now());
        transactionEntity.setLimitExceeded(limitExceeded);
        transactionEntity.setLimit(limit);
        return transactionRepository.save(transactionEntity);
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        List<TransactionEntity> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByAccountId(Long id) {
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByAccountIdFromOrAccountIdTo(id, id);

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByCategory(Category category) {
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByCategory(category);

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByAccountIdWhichExceedLimit(Long accountId) {
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByAccountIdFromOrAccountIdToAndLimitExceededIsTrue(accountId, accountId);

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TransactionDTO convertToDTO(TransactionEntity transactionEntity) {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountIdFrom(transactionEntity.getAccountIdFrom());
        transactionDTO.setAccountIdTo(transactionEntity.getAccountIdTo());
        transactionDTO.setCurrency(transactionEntity.getCurrency());
        transactionDTO.setExpenseCategory(transactionEntity.getCategory());
        transactionDTO.setSum(transactionEntity.getSum());
        transactionDTO.setLimitId(transactionEntity.getLimit().getId());

        return transactionDTO;
    }
}
