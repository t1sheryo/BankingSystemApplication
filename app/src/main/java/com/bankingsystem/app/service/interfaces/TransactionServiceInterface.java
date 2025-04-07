package com.bankingsystem.app.service.interfaces;

import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.TransactionDTO;

import java.util.List;

public interface TransactionServiceInterface {
     TransactionEntity createTransaction(TransactionDTO transaction);
     List<TransactionDTO> getAllTransactions();
     List<TransactionDTO> getTransactionsByAccountId(Long id);
     List<TransactionDTO> getTransactionsByCategory(Category category);
     List<TransactionDTO> getTransactionsByAccountIdWhichExceedLimit(Long accountId);
}
