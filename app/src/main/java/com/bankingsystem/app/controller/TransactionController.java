package com.bankingsystem.app.controller;

import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.services.interfaces.AccountServiceInterface;
import com.bankingsystem.app.services.interfaces.TransactionServiceInterface;
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
        log.info("create Transaction for DTO: {}", transactionDTO);
        if(accountService.getAccountById(transactionDTO.getAccountIdFrom()) == null ||
        accountService.getAccountById(transactionDTO.getAccountIdTo()) == null)
        {
            log.error("One or both accounts not found: accountIdFrom={}, accountIdTo={}",
                    transactionDTO.getAccountIdFrom(),transactionDTO.getAccountIdTo());
            throw new IllegalArgumentException("One or both accounts not found");
        }
        TransactionEntity transaction= transactionService.createTransaction(transactionDTO);
        // Возвращаем полный ответ со статусом
        // - Статус-кодом 201 Created (для создания ресурса).
        // - Заголовком Location, указывающим URL новой транзакции.
        // - Телом ответа, содержащим созданный объект TransactionEntity.
        return ResponseEntity
                 .status(HttpStatus.CREATED)
                 .header("Location", "/bank/transactions/" + transaction.getId())
                 .body(transaction);
    }

    @GetMapping("/exceeded")
    public ResponseEntity<List<TransactionDTO>> getTransactionsExceededLimit(@RequestParam Long accountId) {
        log.info("get transactions exceeded limit for accountId: {}", accountId);
        if(accountId == null || accountId <= 0)
        {
            log.error("Invalid accountId={}", accountId);
            throw new IllegalArgumentException("Invalid accountId");
        }
        if(accountService.getAccountById(accountId) == null)
        {
            log.warn("Account {} not found", accountId);
        }
        List<TransactionDTO> exceededTransactions = transactionService.getTransactionsByAccountIdWhichExceedLimit(accountId);
        log.info("Exceeded transactions: {}", exceededTransactions);
        return ResponseEntity.ok(exceededTransactions);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(){
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{category}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCategory(@PathVariable Category category){
        return ResponseEntity.ok(transactionService.getTransactionsByCategory(category));
    }

<<<<<<< Updated upstream
//    @GetMapping("/{id}")
//    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountId(@PathVariable Long id){
//        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(id));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountIdWhichExceedLimit(@PathVariable Long id){
//        return ResponseEntity.ok(transactionService.getTransactionsByAccountIdWhichExceedLimit(id));
//    }


    @GetMapping("/account/{id}")
=======
    @GetMapping("/{id}")
>>>>>>> Stashed changes
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountId(
        @PathVariable Long id,
        @RequestParam(required = false) Boolean exceededOnly) {

        if (Boolean.TRUE.equals(exceededOnly)) {
            return ResponseEntity.ok(transactionService.getTransactionsByAccountIdWhichExceedLimit(id));
        }
        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(id));
    }
}
