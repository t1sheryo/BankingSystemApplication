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
    //FIXME:
    // изменить методы чтобы они принимали из accountEntity  
    List<TransactionEntity> getAllTransactionsByAccountFromOrAccountTo(Long accountFrom, Long accountTo);
    List<TransactionEntity> getAllTransactionsByCategory(Category category);
    List<TransactionEntity> getAllTransactionsByAccountFromOrAccountToAndLimitExceededIsTrue(Long accountFrom, Long accountTo);
}