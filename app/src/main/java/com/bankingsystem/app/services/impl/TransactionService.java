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
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByAccountFromOrAccountTo(id, id);

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
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByAccountFromOrAccountToAndLimitExceededIsTrue(accountId, accountId);

        return transactions.stream()
                .map(transactionServiceHelper::convertToDTO)
                .collect(Collectors.toList());
    }

    @Component
    private class TransactionServiceHelper {

        private final LimitServiceInterface limitService;
        private final ExchangeRateServiceInterface exchangeRateService;
        private final AccountServiceInterface accountService;

        @Autowired
        public TransactionServiceHelper(
                LimitServiceInterface limitService,
                ExchangeRateServiceInterface exchangeRateService,
                AccountServiceInterface accountService,
                LimitRepository limitRepository
        ){
            this.limitService = limitService;
            this.exchangeRateService = exchangeRateService;
            this.accountService = accountService;
        }

        private TransactionDTO convertToDTO(TransactionEntity transactionEntity) {
            TransactionDTO transactionDTO = new TransactionDTO();
            transactionDTO.setAccountIdFrom(transactionEntity.getAccountFrom().getId());
            transactionDTO.setAccountIdTo(transactionEntity.getAccountTo().getId());
            transactionDTO.setCurrency(transactionEntity.getCurrency());
            transactionDTO.setExpenseCategory(transactionEntity.getCategory());
            transactionDTO.setSum(transactionEntity.getSum());
            transactionDTO.setTransactionTime(transactionEntity.getTransactionTime());
            transactionDTO.setLimitId(transactionEntity.getLimit().getId());
            transactionDTO.setLimitSum(transactionEntity.getLimitSumAtTime());
            transactionDTO.setLimitDateTime(transactionEntity.getLimitDateTimeAtTime());
            transactionDTO.setLimitCurrency(transactionEntity.getLimitCurrencyAtTime());
            return transactionDTO;
        }

        private AccountPair validateAccounts(Long accountIdFrom, Long accountIdTo) {
            AccountEntity accountFrom = accountService.getAccountById(accountIdFrom);
            AccountEntity accountTo = accountService.getAccountById(accountIdTo);

            if (accountFrom == null || accountTo == null) {
                throw new IllegalArgumentException("Account not found");
            }
            return new AccountPair(accountFrom, accountTo);
        }

        private LimitEntity findAndValidateLimit(Long account, Category category) {
            Optional<LimitEntity> limitOptional = limitService.getLimitByAccountIdAndCategory(account, category);
            LimitEntity limit = limitOptional.orElseThrow(() -> new IllegalArgumentException("Limit for account" + account
                    + "and category " + category + " not found"));
            return limit;
        }

        private boolean isLimitExceeded(BigDecimal sumInUsd, LimitEntity limit) {
            BigDecimal limitRemainder = limit.getLimitRemainder();
            if(limitRemainder == null)
            {
                throw new IllegalStateException("Limit remainder is null for limitId: " + limit.getId());
            }
            return sumInUsd.compareTo(limitRemainder) > 0;
        }

        private TransactionEntity buildTransactionEntity(TransactionDTO transactionDTO, AccountPair accounts,
                                                         LimitEntity limit, boolean limitExceeded) {
            TransactionEntity transactionEntity = new TransactionEntity();
            transactionEntity.setAccountFrom(accounts.getAccountFrom());
            transactionEntity.setAccountTo(accounts.getAccountTo());
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

            return transactionEntity;
        }

        private void updateLimitRemainder(BigDecimal sumInUsd, LimitEntity limit) {
            BigDecimal limitRemainder = limit.getLimitRemainder();
            if(limitRemainder == null)
            {
                throw new IllegalStateException("Limit remainder is null for limitId: " + limit.getId());
            }
            limit.setLimitRemainder(limit.getLimitRemainder().subtract(sumInUsd));
            limitService.saveLimit(limit);
        }

        private BigDecimal convertToUSD(BigDecimal amount, Currency currency, LocalDate date) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
            }

            if (currency.equals(Currency.USD)) {
                return amount;
            }
            //var ExchangeRate == Optional<ExchangeRateEntity>
            //var пишем када очевидно что слева за возращаемый тип
            var exchangeRate = exchangeRateService.getExchangeRate(currency, Currency.USD, date);
            if (exchangeRate.isEmpty()) {
                log.error("Exchange rate is not found for {}/USD on {}", currency, date);
                throw new IllegalStateException("Exchange rate is not found for " + currency + " on " + date);
            }
            BigDecimal rate = exchangeRate.get().getRate();
            BigDecimal amountInUsd = amount.multiply(rate);
            log.info("Converted {} {} to {} USD using rate {}", amount, currency, amountInUsd, rate);
            return amountInUsd;

        }
    }
}
