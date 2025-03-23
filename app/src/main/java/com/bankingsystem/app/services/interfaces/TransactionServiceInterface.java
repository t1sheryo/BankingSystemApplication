package com.bankingsystem.app.services.interfaces;

import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.repository.TransactionRepository;

import java.util.List;

public interface TransactionServiceInterface {
     TransactionEntity createTransaction(TransactionDTO transaction);

}
