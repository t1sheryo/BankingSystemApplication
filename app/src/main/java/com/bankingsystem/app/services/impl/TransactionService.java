package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.repository.LimitRepository;
import com.bankingsystem.app.repository.TransactionRepository;
import com.bankingsystem.app.services.interfaces.AccountServiceInterface;
import com.bankingsystem.app.services.interfaces.ExchangeRateServiceInterface;
import com.bankingsystem.app.services.interfaces.LimitServiceInterface;
import com.bankingsystem.app.services.interfaces.TransactionServiceInterface;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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
    private final ExchangeRateServiceInterface exchangeRateService;
    private final AccountServiceInterface accountService;
    private final LimitRepository limitRepository;

    //  Autowired делает автоматическую инъекцию зависимостей(dependency injection)
    //   Аннотация @Autowired говорит Spring: "Найди бины типа TransactionRepository и LimitService
    //    в контексте приложения и передай их в этот конструктор
    @Autowired
    public TransactionService(TransactionRepository transactionRepository, LimitServiceInterface limitService,
                              ExchangeRateServiceInterface exchangeRateService, AccountServiceInterface accountService,
                              LimitRepository limitRepository) {
        this.transactionRepository = transactionRepository;
        this.limitService = limitService;
        this.exchangeRateService = exchangeRateService;
        this.accountService = accountService;
        this.limitRepository = limitRepository;
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

    // FIXME: Удалить метод updateRemainder и перенести его логику
    // TODO: 1.Перенести логику обновления limitRemainder в TransactionService.createTransaction и удалить из текущего класса
    // причина: обновление остатка должно происходить атомарно с созданием транзакции
    // (что значит операция пройдет успешно либо откатиться)
    // сейчас метод в LimitService разделяет операции, что может привести к несогласованности (транзакция создана, а лимит не обновлен)
    // если мы создаем транзакцию то она должна сразу учитывать ее влияние на лимит
    // лимит не превышен -- мы открываем транзакцию и меняем значение лимита
    @Override
    @Transactional
    public TransactionEntity createTransaction(TransactionDTO transactionDTO) {
        AccountEntity accountFrom = accountService.getAccountById(transactionDTO.getAccountIdFrom());
        AccountEntity accountTo = accountService.getAccountById(transactionDTO.getAccountIdTo());

        if(accountFrom == null || accountTo == null) {
            throw new IllegalStateException("Account not found");
        }
        //Нахождение свежего лимита
        Optional<LimitEntity> limitOptional = limitService.getLimitByAccountIdAndCategory(transactionDTO.getAccountIdFrom(), transactionDTO.getExpenseCategory());
        LimitEntity limit = limitOptional.orElseThrow(() -> new IllegalArgumentException("Limit for account" +  transactionDTO.getAccountIdFrom()
                + "and category " + transactionDTO.getExpenseCategory() + " not found"));
        //Конвертация в доллары
        BigDecimal sumInUsd = convertToUSD(transactionDTO.getSum(), transactionDTO.getCurrency(), transactionDTO.getTransactionDate().toLocalDate());

        //Проверка превышения лимита
        boolean limitExceeded = sumInUsd.compareTo(limit.getLimitRemainder()) > 0;

        //Создаем транзакцию
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAccountFrom(accountFrom);
        transactionEntity.setAccountTo(accountTo);
        transactionEntity.setCurrency(transactionDTO.getCurrency());
        transactionEntity.setSum(transactionDTO.getSum());
        transactionEntity.setCategory(transactionDTO.getExpenseCategory());
        transactionEntity.setTransactionTime(OffsetDateTime.now());
        transactionEntity.setLimitExceeded(limitExceeded);
        transactionEntity.setLimit(limit);

        //сохраняем данные об лимите на момент транзакции
        transactionEntity.setLimitSumAtTime(limit.getLimitSum());
        transactionEntity.setLimitDateTimeAtTime(limit.getLimitDateTime());
        transactionEntity.setLimitCurrencyAtTime(limit.getLimitCurrencyShortName());

        TransactionEntity savedTransaction = transactionRepository.save(transactionEntity);

        //обновляем ремайндер
        limit.setLimitRemainder(limit.getLimitRemainder().subtract(sumInUsd));
        limitRepository.save(limit);

    //TODO доделать
        return transactionRepository.save(
                convertDTOToEntity(transactionDTO, limit)
        );
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

    private TransactionEntity convertDTOToEntity(TransactionDTO transactionDTO, LimitEntity limit){

        boolean limitExceeded = (limit.getLimitRemainder().compareTo(BigDecimal.ZERO) > 0);

        TransactionEntity transactionEntity = new TransactionEntity();

        transactionEntity.setAccountFrom(
                accountService.getAccountById(transactionDTO.getAccountIdFrom()));
        transactionEntity.setAccountTo(
                accountService.getAccountById(transactionDTO.getAccountIdTo()));
        transactionEntity.setCurrency(transactionDTO.getCurrency());
        transactionEntity.setCategory(transactionDTO.getExpenseCategory());
        transactionEntity.setSum(transactionDTO.getSum());
        transactionEntity.setTransactionTime(OffsetDateTime.now());
        transactionEntity.setLimitExceeded(limitExceeded);
        transactionEntity.setLimit(limit);
        transactionEntity.setLimitDateTimeAtTime(limit.getLimitDateTime());
        transactionEntity.setLimitSumAtTime(limit.getLimitSum());
        transactionEntity.setLimitCurrencyAtTime(limit.getLimitCurrencyShortName());

        return transactionEntity;
    }

    private BigDecimal convertToUSD(BigDecimal amount, Currency currency, LocalDate date) {
        if(currency.equals(Currency.USD)) {
            return amount;
        }
        //var ExchangeRate == Optional<ExchangeRateEntity>
        //var пишем када очевидно что слева за возращаемый тип
        var exchangeRate = exchangeRateService.getExchangeRate(currency, Currency.USD, date);
        if(exchangeRate.isEmpty()) {
            log.error("Exchange rate is not found for {}/USD on {}", currency, date);
            throw new IllegalStateException("Exchange rate is not found for " + currency + " on " + date);
        }
        BigDecimal rate = exchangeRate.get().getRate();
        BigDecimal amountInUsd =amount.multiply(rate);
        log.info("Converted {} {} to {} USD using rate {}", amount, currency,amountInUsd, rate);
        return amountInUsd;

    }
}
