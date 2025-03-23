package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.repository.LimitRepository;
import com.bankingsystem.app.repository.TransactionRepository;
import com.bankingsystem.app.services.interfaces.TransactionServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class TransactionService implements TransactionServiceInterface {
    private final TransactionRepository transactionRepository;
    private final LimitRepository limitRepository;
    private final LimitService limitService;

    //  Autowired делает автоматическую инъекцию зависимостей(dependency injection)
   //   Аннотация @Autowired говорит Spring: "Найди бины типа TransactionRepository и LimitService
  //    в контексте приложения и передай их в этот конструктор
    @Autowired
    public TransactionService(TransactionRepository transactionRepository, LimitRepository limitRepository, LimitService limitService) {
        this.transactionRepository = transactionRepository;
        this.limitRepository = limitRepository;
        this.limitService = limitService;
    }

    @Override
    public TransactionEntity createTransaction(TransactionDTO transactionDTO) {
        LimitEntity limit = limitService.getLimitByDBId(transactionDTO.getLimitId());
        boolean limitExceeded = transactionDTO.getSum().compareTo(limit.getLimitSum()) > 0;

        //создаем сущность для передачи ее в репозиторий и сетаем в нее из transactionDTO нужную информацию
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAccountIdFrom(transactionDTO.getAccountIdFrom());
        transactionEntity.setAccountIdTo(transactionDTO.getAccountIdTo());
        transactionEntity.setCurrency(transactionDTO.getCurrency());
        transactionEntity.setSum(transactionDTO.getSum());
        transactionEntity.setCategory(transactionDTO.getExpenseCategory());
        transactionEntity.setTransactionTime(transactionDTO.getTransactionTime());
        transactionEntity.setLimitExceeded(limitExceeded);
        transactionEntity.setLimit(limit);
        return transactionRepository.save(transactionEntity);
    }


}
