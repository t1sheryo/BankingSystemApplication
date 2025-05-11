package com.bankingsystem.app.service.impl;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.repository.AccountRepository;
import com.bankingsystem.app.service.interfaces.AccountServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService implements AccountServiceInterface {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    @Override
    public AccountEntity getAccountById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }
    public Optional<AccountEntity> getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
}
