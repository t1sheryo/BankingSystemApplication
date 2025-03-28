package com.bankingsystem.app.repository;

import com.bankingsystem.app.entity.ExchangeRateCompositePrimaryKey;
import com.bankingsystem.app.entity.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {
    Optional<ExchangeRateEntity> findByIdAndRateDate(ExchangeRateCompositePrimaryKey id, LocalDate rateDate);
}