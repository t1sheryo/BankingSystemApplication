package com.bankingsystem.app.services.interfaces;

import com.bankingsystem.app.entity.AccountEntity;

public interface AccountServiceInterface {
    AccountEntity getAccountById(Long id);
}
