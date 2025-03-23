package com.bankingsystem.app.repository;

import com.bankingsystem.app.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
// создание репозитория для работы метода класса TransactionRepository с БД используя методы интерфейса
// JpaRepository которые включают в себя CRUD(create, read, update, delete)

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

}
