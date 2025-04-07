package com.bankingsystem.app.controller;

import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.service.interfaces.AccountServiceInterface;
import com.bankingsystem.app.service.interfaces.TransactionServiceInterface;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bank/transactions")
public class TransactionController {

    private final TransactionServiceInterface transactionService;
    private final AccountServiceInterface accountService;


    TransactionController(TransactionServiceInterface transactionService, AccountServiceInterface accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<TransactionEntity> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO)
    {
        if (transactionDTO == null) {
            throw new IllegalArgumentException("Transaction DTO cannot be null");
        }

        log.info("create Transaction for DTO: {}", transactionDTO);

        if(accountService.getAccountById(transactionDTO.getAccountIdFrom()) == null ||
            accountService.getAccountById(transactionDTO.getAccountIdTo()) == null) {
            throw new IllegalArgumentException("One or both accounts not found");
        }

        TransactionEntity transaction= transactionService.createTransaction(transactionDTO);

        return ResponseEntity
                 .status(HttpStatus.CREATED)
                 .header("Location", "/bank/transactions/" + transaction.getId())
                 .body(transaction);
    }

    @GetMapping("/exceeded")
    public ResponseEntity<List<TransactionDTO>> getTransactionsExceededLimit(@RequestParam Long accountId) {
        if(accountId == null || accountId <= 0) {
            throw new IllegalArgumentException("Invalid accountId");
        }

        if(accountService.getAccountById(accountId) == null) {
            log.warn("Account {} not found", accountId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        List<TransactionDTO> exceededTransactions = transactionService.getTransactionsByAccountIdWhichExceedLimit(accountId);
        log.info("Exceeded transactions: {}", exceededTransactions);

        return ResponseEntity
                .ok(exceededTransactions);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(){
        return ResponseEntity
                .ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{category}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCategory(@PathVariable Category category){
        return ResponseEntity
                .ok(transactionService.getTransactionsByCategory(category));
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountId(
        @PathVariable Long id,
        @RequestParam(required = false) Boolean exceededOnly) {

        if (Boolean.TRUE.equals(exceededOnly)) {
            return ResponseEntity
                    .ok(transactionService.getTransactionsByAccountIdWhichExceedLimit(id));
        }
        return ResponseEntity
                .ok(transactionService.getTransactionsByAccountId(id));
    }
}
