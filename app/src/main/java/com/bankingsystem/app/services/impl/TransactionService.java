package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.repository.TransactionRepository;
import com.bankingsystem.app.services.interfaces.LimitServiceInterface;
import com.bankingsystem.app.services.interfaces.TransactionServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TransactionService implements TransactionServiceInterface {
    private final TransactionRepository transactionRepository;
    private final LimitServiceInterface limitService;

    //  Autowired делает автоматическую инъекцию зависимостей(dependency injection)
    //   Аннотация @Autowired говорит Spring: "Найди бины типа TransactionRepository и LimitService
    //    в контексте приложения и передай их в этот конструктор
    @Autowired
    public TransactionService(TransactionRepository transactionRepository, LimitServiceInterface limitService) {
        this.transactionRepository = transactionRepository;
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

    @Override
    public List<TransactionDTO> getAllTransactions() {
        List<TransactionEntity> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByAccountId(Long id) {
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByAccountIdFromOrAccountIdTo(id, id);

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByCategory(Category category) {
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByCategory(category);

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getTransactionsByAccountIdWhichExceedLimit(Long accountId) {
        List<TransactionEntity> transactions = transactionRepository.getAllTransactionsByAccountIdFromOrAccountIdToAndLimitExceededIsTrue(accountId, accountId);

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TransactionDTO convertToDTO(TransactionEntity transactionEntity) {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAccountIdFrom(transactionEntity.getAccountIdFrom());
        transactionDTO.setAccountIdTo(transactionEntity.getAccountIdTo());
        transactionDTO.setCurrency(transactionEntity.getCurrency());
        transactionDTO.setExpenseCategory(transactionEntity.getCategory());
        transactionDTO.setSum(transactionEntity.getSum());
        transactionDTO.setTransactionTime(transactionEntity.getTransactionTime());
        transactionDTO.setLimitId(transactionEntity.getLimit().getId());

        return transactionDTO;
    }
}
