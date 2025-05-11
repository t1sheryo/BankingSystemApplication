package com.bankingsystem.app.repository;

import com.bankingsystem.app.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    public Optional<AccountEntity> findByUsername(String username);
}
