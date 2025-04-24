package com.bankingsystem.app.helper.interfaces;

import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.enums.Currency;
import com.bankingsystem.app.model.AccountPair;
import com.bankingsystem.app.model.TransactionDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionServiceHelperInterface {

    TransactionDTO convertToDTO(TransactionEntity transactionEntity);

    AccountPair validateAccounts(Long accountIdFrom, Long accountIdTo);

    LimitEntity findAndValidateLimit(Long account, Category category);

    boolean isLimitExceeded(BigDecimal sumInUsd, LimitEntity limit);

    TransactionEntity buildTransactionEntity(TransactionDTO transactionDTO, AccountPair accounts,
                                             LimitEntity limit, boolean limitExceeded);

    void updateLimitRemainder(BigDecimal sumInUsd, LimitEntity limit);

    BigDecimal convertToUSD(BigDecimal amount, Currency currency, LocalDate date);
}
