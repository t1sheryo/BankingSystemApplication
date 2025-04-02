package com.bankingsystem.app.controller;

import com.bankingsystem.app.customException.LimitUpdateNotAllowedException;
import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;
import com.bankingsystem.app.services.interfaces.AccountServiceInterface;
import com.bankingsystem.app.services.interfaces.LimitServiceInterface;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bank/limits")
public class LimitController {
    private final LimitServiceInterface limitService;
    private final AccountServiceInterface accountService;

    public LimitController(LimitServiceInterface limitService, AccountServiceInterface accountService) {
        this.limitService = limitService;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<LimitEntity> createLimit(@Valid  @RequestBody LimitRequest limitRequest) {
        log.info("Create Limit Request: {}", limitRequest);
        if(accountService.getAccountById(limitRequest.getAccountId()) == null) {
            log.error("Account with id {} not found", limitRequest.getAccountId());
            throw new IllegalArgumentException("Account with id " + limitRequest.getAccountId() + " not found");
        }
        LimitEntity limit = limitService.setLimit(limitRequest);
        // Возращаем ответ в виде ResponseEntity со
        // Статус-кодом 201 Created (для создания ресурса).
        // - Заголовком Location, указывающим URL новой транзакции.
        // - Телом ответа, содержащим созданный объект LimitEntity
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location" ,"/bank/limits/" + limit.getId())
                .body(limit);
    }

    @GetMapping
    public ResponseEntity<List<LimitResponse>> getAllLimits() {
        log.info("Get All Limits");
        List<LimitResponse> limits = limitService.getAllLimits();
        log.info("Found {} limits", limits.size());
        return ResponseEntity.ok(limits);
    }

    @GetMapping("/account")
    public ResponseEntity<List<LimitResponse>> getAllLimitsByAccountId(@RequestParam Long accountId) {
        log.info("Get All Limits By Account Id: {}", accountId);
        //проверка валидности id при некорректном бросаем exception
        if(accountId == null || accountId <= 0) {
            log.error("Account Id is not valid");
           throw new IllegalArgumentException("Account Id is not valid");
        }
        List<LimitResponse> limitsById = limitService.getLimitsByAccountId(accountId);
        log.info("Found {} Limits", limitsById.size());
        return ResponseEntity.ok(limitsById);
    }
}
