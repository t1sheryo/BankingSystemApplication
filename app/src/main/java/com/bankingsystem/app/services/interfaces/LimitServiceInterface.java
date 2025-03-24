package com.bankingsystem.app.services.interfaces;

import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.model.TransactionDTO;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;
import java.util.List;

public interface LimitServiceInterface {
    void setLimit(LimitRequest limit);
    List<LimitResponse> getLimitsByAccountId(Long accountId);
    LimitEntity getLimitByDBId(Long DBId);
    List<LimitResponse> getAllLimits();
    void updateRemainder(TransactionDTO transaction);
}
