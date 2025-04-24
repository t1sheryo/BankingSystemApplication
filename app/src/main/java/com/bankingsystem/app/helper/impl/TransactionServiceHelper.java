package com.bankingsystem.app.helper.impl;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.helper.interfaces.TransactionServiceHelperInterface;
import com.bankingsystem.app.model.AccountPair;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.service.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Component
public class TransactionServiceHelper implements TransactionServiceHelperInterface {

    private final LimitServiceInterface limitService;
    private final ExchangeRateServiceInterface exchangeRateService;
    private final AccountServiceInterface accountService;

    @Autowired
    public TransactionServiceHelper(
            LimitServiceInterface limitService,
            ExchangeRateServiceInterface exchangeRateService,
            AccountServiceInterface accountService
    ){
        this.limitService = limitService;
        this.exchangeRateService = exchangeRateService;
        this.accountService = accountService;
    }

    @Override
    public TransactionDTO convertToDTO(TransactionEntity transactionEntity) {
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
    @Override
    public AccountPair validateAccounts(Long accountIdFrom, Long accountIdTo) {
        AccountEntity accountFrom = accountService.getAccountById(accountIdFrom);
        AccountEntity accountTo = accountService.getAccountById(accountIdTo);

        if (accountFrom == null || accountTo == null) {
            throw new IllegalStateException("Account not found");
        }

        return new AccountPair(accountFrom, accountTo);
    }
    @Override
    public LimitEntity findAndValidateLimit(Long account, Category category) {
        Optional<LimitEntity> limitOptional = limitService.getLimitByAccountIdAndCategory(account, category);
        LimitEntity limit = limitOptional.orElseThrow(() -> new IllegalArgumentException("Limit for account" + account
                + "and category " + category + " not found"));
        return limit;
    }
    @Override
    public boolean isLimitExceeded(BigDecimal sumInUsd, LimitEntity limit) {
        BigDecimal limitRemainder = limit.getLimitRemainder();
        if(limitRemainder == null) {
            throw new IllegalStateException("Limit remainder is null for limitId: " + limit.getId());
        }
        return sumInUsd.compareTo(limitRemainder) > 0;
    }
    @Override
    public TransactionEntity buildTransactionEntity(TransactionDTO transactionDTO, AccountPair accounts,
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
    @Override
    public void updateLimitRemainder(BigDecimal sumInUsd, LimitEntity limit) {
        BigDecimal limitRemainder = limit.getLimitRemainder();
        if(limitRemainder == null) {
            throw new IllegalStateException("Limit remainder is null for limitId: " + limit.getId());
        }
        limit.setLimitRemainder(limit.getLimitRemainder().subtract(sumInUsd));
        limitService.saveLimit(limit);
    }
    @Override
    public BigDecimal convertToUSD(BigDecimal amount, Currency currency, LocalDate date) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (currency.equals(Currency.USD)) {
            return amount;
        }

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