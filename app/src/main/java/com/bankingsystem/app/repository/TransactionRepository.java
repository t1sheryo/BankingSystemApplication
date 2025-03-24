package com.bankingsystem.app.repository;

import com.bankingsystem.app.entity.TransactionEntity;
import com.bankingsystem.app.enums.Category;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// создание репозитория для работы метода класса TransactionRepository с БД используя методы интерфейса
// JpaRepository которые включают в себя CRUD(create, read, update, delete)
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> getAllTransactionsByAccountIdFromOrAccountIdTo(Long accountIdFrom, Long accountIdTo);
    List<TransactionEntity> getAllTransactionsByCategory(Category category);
    List<TransactionEntity> getAllTransactionsByAccountIdFromOrAccountIdToAndLimitExceededIsTrue(Long accountIdFrom, Long accountIdTo);
}