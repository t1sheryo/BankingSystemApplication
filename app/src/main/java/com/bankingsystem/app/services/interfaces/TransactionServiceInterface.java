package com.bankingsystem.app.services.interfaces;

import com.bankingsystem.app.model.Transaction;

import java.util.List;

public interface TransactionServiceInterface {
    void addTransaction(Transaction transaction);
    List<Transaction> getAllTransactions();
}
