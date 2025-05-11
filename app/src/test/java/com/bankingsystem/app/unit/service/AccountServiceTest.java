package com.bankingsystem.app.unit.service;

import com.bankingsystem.app.entity.AccountEntity;
import com.bankingsystem.app.repository.AccountRepository;
import com.bankingsystem.app.service.impl.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    private static final Long VALID_ACCOUNT_ID = 1L;

    @Test
    @DisplayName("Should return AccountEntity by Id")
     void shouldReturnAccountEntityById() {
        AccountEntity expectedEntity = createAccount(VALID_ACCOUNT_ID);

        when(accountRepository.findById(VALID_ACCOUNT_ID)).thenReturn(Optional.of(expectedEntity));
        AccountEntity actualEntity = accountService.getAccountById(VALID_ACCOUNT_ID);

        assertThat(expectedEntity).isEqualTo(actualEntity);
        assertThat(expectedEntity.getId()).isEqualTo(VALID_ACCOUNT_ID);

        verify(accountRepository, times(1)).findById(VALID_ACCOUNT_ID);
    }

    @Test
    @DisplayName("Should return null when account not found")
    void shouldReturnNullWhenAccountNotFound() {
        when(accountRepository.findById(VALID_ACCOUNT_ID)).thenReturn(Optional.empty());
        AccountEntity actualEntity = accountService.getAccountById(VALID_ACCOUNT_ID);


        assertThat(actualEntity).isNull();
        verify(accountRepository, times(1)).findById(VALID_ACCOUNT_ID);

    }

    private AccountEntity createAccount(Long id) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(id);
        return accountEntity;
    }


}
