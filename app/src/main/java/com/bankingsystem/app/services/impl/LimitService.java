package com.bankingsystem.app.services.impl;

import com.bankingsystem.app.enums.Category;
import com.bankingsystem.app.model.Transaction;
import com.bankingsystem.app.model.limits.Limit;
import com.bankingsystem.app.model.limits.LimitRequest;
import com.bankingsystem.app.model.limits.LimitResponse;
import com.bankingsystem.app.services.interfaces.LimitServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class LimitService implements LimitServiceInterface {
    private HashMap<Long, List<Limit>> clientsLimits;  // {accountId, Limits}

    LimitService() {
        clientsLimits = new HashMap<>();
    }
    @Override
    public void setLimit(LimitRequest request) {
        // TODO: implement with db
        try {
            Long accountId = request.getAccountId();
            Category category = request.getCategory();
            BigDecimal lim = request.getLimit();
            boolean isUpdatedInThisMonth = clientsLimits.getOrDefault(accountId, new ArrayList<>())
                    .stream()
                    .anyMatch(limit -> limit.getCategory().equals(category) &&
                            ChronoUnit.DAYS.between(LocalDateTime.now(), limit.getLastUpdate()) < 30); // if gap < 30(month)

            boolean isDefaultValue = true;
            Limit reference = null;  // if limit has been found
            for (Limit limit : clientsLimits.get(accountId)) {
                if (limit.getCategory().equals(category)) {
                    reference = limit;
                    isDefaultValue = limit.getIsDefault();
                    break;
                }
            }

            if (isUpdatedInThisMonth || isDefaultValue) {
                return ;
            }

            BigDecimal diff = request.getLimit().subtract(reference.getLimit());
            reference.setRemaining(reference.getRemaining().add(diff));
            if(reference.getRemaining().compareTo(BigDecimal.ZERO) == -1) {
                reference.setLimitExceeded(true);
            }
            else{
                reference.setLimitExceeded(false);
            }
            reference.setLimit(lim);
            reference.setLastUpdate(LocalDateTime.now());
            reference.setIsDefault(false);
            log.info("Limit has been successfully updated: {}", reference);

        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }

    }

    @Override
    public List<LimitResponse> getLimitsByAccount(Long accountId) {
        List<LimitResponse> responses = new ArrayList<>();

        for(Limit limit : clientsLimits.get(accountId)) {
            LimitResponse response = new LimitResponse(
                    accountId,
                    limit.getCategory(),
                    limit.getLimit(),
                    limit.getCreatedAt(),
                    limit.getLastUpdate(),
                    new BigDecimal("0")  // TODO: need to calculate this variable using TransactionService
            );
        }

        return responses;
    }

    @Override
    public List<LimitResponse> getAllLimits() {
       List<LimitResponse> responses = new ArrayList<>();

        for(final var pair : clientsLimits.entrySet()){
            Long accountId = pair.getKey();
            for(Limit limit : pair.getValue()) {
                LimitResponse response = new LimitResponse(
                        accountId,
                        limit.getCategory(),
                        limit.getLimit(),
                        limit.getCreatedAt(),
                        limit.getLastUpdate(),
                        new BigDecimal("0")  // TODO: need to calculate this variable using TransactionService
                );
                responses.add(response);
            }
        }

        return responses;
    }

    @Override
    // invoking this method after we add transaction to update remainder
    public void updateRemainder(Transaction transaction) {
        Long accountId = transaction.getAccountIdTo();
        Category category = transaction.getCategory();
        Optional<Limit> limitOptional = clientsLimits.get(accountId).stream()
                .filter(limitElem -> category.equals(limitElem.getCategory()))
                .findFirst();
        limitOptional.ifPresent(limit -> {
            limit.setRemaining(limit.getRemaining().subtract(transaction.getAmount()));
            if(limit.getRemaining().compareTo(BigDecimal.ZERO) == - 1) {
                limit.setLimitExceeded(true);
            }
        });
    }
}
