package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.model.Transaction;
import com.bankingsystem.app.services.interfaces.TransactionServiceInterface;

import java.util.List;

public class TransactionService implements TransactionServiceInterface {
    @Override
    public void addTransaction(Transaction transaction) {

    }

    @Override
    public List<Transaction> getAllTransactions() {
        return List.of();
    }
}
