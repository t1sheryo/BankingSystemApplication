package com.bankingsystem.app.model;

import com.bankingsystem.app.entity.AccountEntity;
import lombok.Data;

@Data
public class AccountPair {
    private final AccountEntity accountFrom;
    private final AccountEntity accountTo;
}
