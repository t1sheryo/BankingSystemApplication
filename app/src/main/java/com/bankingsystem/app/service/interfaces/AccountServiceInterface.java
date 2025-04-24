package com.bankingsystem.app.service.interfaces;

import com.bankingsystem.app.entity.AccountEntity;

public interface AccountServiceInterface {
    AccountEntity getAccountById(Long id);
}
