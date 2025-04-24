package com.bankingsystem.app.service.interfaces;

import com.bankingsystem.app.entity.LimitEntity;
import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;
import java.util.List;
import java.util.Optional;

public interface LimitServiceInterface {
    LimitEntity setLimit(LimitRequest limit);
    List<LimitResponse> getLimitsByAccountId(Long accountId);
    LimitEntity getLimitByDBId(Long DBId);
    Optional<LimitEntity> getLimitByAccountIdAndCategory(Long accountId, Category category);
    List<LimitResponse> getAllLimits();
    LimitEntity saveLimit(LimitEntity limit);
}
