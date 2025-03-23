package com.bankingsystem.app.repository;

import com.bankingsystem.app.entity.LimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// создание репозитория для работы метода класса LimitRepository с БД используя методы интерфейса
// JpaRepository которые включают в себя CRUD(create, read, update, delete)
@Repository
public interface LimitRepository extends JpaRepository<LimitEntity, Long> {
    List<LimitEntity> findByAccountId(Long accountId);
    // FIXME: возможно неправильная работа метода getLimitById т.к. непонятна ситуация с id'шниками в классах сущностей и моделей
    LimitEntity getLimitById(Long DBId);
}
