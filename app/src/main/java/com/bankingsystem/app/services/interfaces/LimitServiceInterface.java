package com.bankingsystem.app.services.interfaces;

import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.Transaction;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;

import java.util.List;

public interface LimitServiceInterface {
    void setLimit(LimitRequest limit);
    List<LimitResponse> getLimitsByAccount(Long accountId);
    List<LimitResponse> getAllLimits();
    void updateRemainder(Transaction transaction);
}
