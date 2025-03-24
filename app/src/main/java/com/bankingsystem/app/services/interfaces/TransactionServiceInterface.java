package com.bankingsystem.app.services.interfaces;

import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.model.TransactionDTO;

public interface TransactionServiceInterface {
     TransactionEntity createTransaction(TransactionDTO transaction);
}
