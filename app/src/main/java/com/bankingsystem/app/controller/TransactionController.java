package com.bankingsystem.app.controller;

import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.services.impl.TransactionService;
import com.bankingsystem.app.services.interfaces.TransactionServiceInterface;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import com.bankingsystem.app.enums.Currency;

// TODO
@Slf4j
@RestController
@RequestMapping("/bank")
public class TransactionController {

    private final TransactionServiceInterface transactionService;
    // Выносим все справочные данные как константы
    private static final List<Category> categories = Arrays.asList(Category.values());
    private static final List<Currency> currencies = Arrays.asList(Currency.values());

    TransactionController(TransactionServiceInterface transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionEntity> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO)
    {
        log.info("create Transaction for DTO: {}", transactionDTO);
        TransactionEntity transaction= transactionService.createTransaction(transactionDTO);
        // Возвращаем полный ответ со статусом
        // - Статус-кодом 201 Created (для создания ресурса).
        // - Заголовком Location, указывающим URL новой транзакции.
        // - Телом ответа, содержащим созданный объект TransactionEntity.
        return ResponseEntity
                 .status(HttpStatus.CREATED)
                 .header("Location", "/bank" + transaction.getId())
                 .body(transaction);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(){
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountId(@PathVariable Long id){
        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(id));
    }

    @GetMapping("/{category}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCategory(@PathVariable Category category){
        return ResponseEntity.ok(transactionService.getTransactionsByCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByAccountIdWhichExceedLimit(@PathVariable Long id){
        return ResponseEntity.ok(transactionService.getTransactionsByAccountIdWhichExceedLimit(id));
    }
}
