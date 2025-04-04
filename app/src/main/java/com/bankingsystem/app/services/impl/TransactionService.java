package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.AccountPair;
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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService implements TransactionServiceInterface {
    private final TransactionRepository transactionRepository;
    private final TransactionServiceHelper transactionServiceHelper;

    //  Autowired делает автоматическую инъекцию зависимостей(dependency injection)
    //   Аннотация @Autowired говорит Spring: "Найди бины типа TransactionRepository и LimitService
    //    в контексте приложения и передай их в этот конструктор
    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              TransactionServiceHelper transactionServiceHelper) {
        this.transactionRepository = transactionRepository;
        this.transactionServiceHelper = transactionServiceHelper;
    }


    @Override
    @Transactional
    public TransactionEntity createTransaction(TransactionDTO transactionDTO) {

        log.info("Creating transaction for accountIdFrom: {}, accountIdTo: {}, category: {}, sum: {}, currency: {}",
                transactionDTO.getAccountIdFrom(), transactionDTO.getAccountIdTo(),
                transactionDTO.getExpenseCategory(), transactionDTO.getSum(), transactionDTO.getCurrency());
        //Проверка счетов отправителя и получателя
        AccountPair accounts = transactionServiceHelper.validateAccounts(transactionDTO.getAccountIdFrom(), transactionDTO.getAccountIdTo());
        //Нахождение свежего лимита
        LimitEntity limit = transactionServiceHelper.findAndValidateLimit(transactionDTO.getAccountIdFrom(), transactionDTO.getExpenseCategory());

        //Конвертация в доллары
        BigDecimal sumInUsd = transactionServiceHelper.convertToUSD(transactionDTO.getSum(), transactionDTO.getCurrency(), transactionDTO.getTransactionTime().toLocalDate());

        //Проверка превышения лимита
        boolean limitExceeded = transactionServiceHelper.isLimitExceeded(sumInUsd, limit);

        TransactionEntity transactionEntity = transactionServiceHelper.buildTransactionEntity(transactionDTO, accounts, limit, limitExceeded);

        TransactionEntity savedTransaction = transactionRepository.save(transactionEntity);

        log.info("Updating limitRemainder for limitId: {}, old value: {}, new value: {}",
                limit.getId(), limit.getLimitRemainder(), limit.getLimitRemainder().subtract(sumInUsd));
        //обновляем ремайндер
        transactionServiceHelper.updateLimitRemainder(sumInUsd, limit);

        log.info("Transaction created with id: {}", savedTransaction.getId());
        return savedTransaction;
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        List<TransactionEntity> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(transactionServiceHelper::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByAccountId(Long id) {
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByAccountFromIdOrAccountToId(id, id);

        return transactions.stream()
                .map(transactionServiceHelper::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByCategory(Category category) {
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByCategory(category);

        return transactions.stream()
                .map(transactionServiceHelper::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByAccountIdWhichExceedLimit(Long accountId) {
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByAccountFromIdOrAccountToIdAndLimitExceededIsTrue(accountId, accountId);

        return transactions.stream()
                .map(transactionServiceHelper::convertToDTO)
                .collect(Collectors.toList());
    }

}
