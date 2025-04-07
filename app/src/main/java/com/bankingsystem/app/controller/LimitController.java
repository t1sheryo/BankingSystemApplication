package com.bankingsystem.app.controller;

import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;
import com.bankingsystem.app.service.interfaces.AccountServiceInterface;
import com.bankingsystem.app.service.interfaces.LimitServiceInterface;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<LimitEntity> createLimit(@Valid @RequestBody LimitRequest limitRequest) {
        if(accountService.getAccountById(limitRequest.getAccountId()) == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        LimitEntity limit = limitService.setLimit(limitRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location" ,"/bank/limits/" + limit.getId())
                .body(limit);
    }

    @GetMapping
    public ResponseEntity<List<LimitResponse>> getAllLimits() {
        List<LimitResponse> limits = limitService.getAllLimits();
        return ResponseEntity
                .ok(limits);
    }

    @GetMapping("/account")
    public ResponseEntity<List<LimitResponse>> getAllLimitsByAccountId(@RequestParam Long accountId) {
        if(accountId == null || accountId <= 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        List<LimitResponse> limitsById = limitService.getLimitsByAccountId(accountId);
        return ResponseEntity
                .ok(limitsById);
    }
}
