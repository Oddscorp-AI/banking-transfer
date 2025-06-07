package com.example.banking.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.banking.model.Account;
import com.example.banking.model.User;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.UserRepository;

class AccountSecurityTest {
    @Test
    void returnsTrueWhenOwnerMatches() {
        AccountRepository accRepo = mock(AccountRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        AccountSecurity security = new AccountSecurity(accRepo, userRepo);

        Account acc = new Account();
        acc.setCitizenId("111");
        when(accRepo.findByAccountNumber("A1")).thenReturn(Optional.of(acc));

        User user = new User();
        user.setCitizenId("111");
        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        assertTrue(security.isOwner("A1", "user@test.com"));
    }

    @Test
    void returnsFalseWhenNoUser() {
        AccountRepository accRepo = mock(AccountRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        AccountSecurity security = new AccountSecurity(accRepo, userRepo);

        when(accRepo.findByAccountNumber("A1")).thenReturn(Optional.empty());

        assertFalse(security.isOwner("A1", "user@test.com"));
    }

    @Test
    void returnsFalseWhenCitizenMismatch() {
        AccountRepository accRepo = mock(AccountRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        AccountSecurity security = new AccountSecurity(accRepo, userRepo);

        Account acc = new Account();
        acc.setCitizenId("111");
        when(accRepo.findByAccountNumber("A1")).thenReturn(Optional.of(acc));

        User user = new User();
        user.setCitizenId("222");
        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        assertFalse(security.isOwner("A1", "user@test.com"));
    }
}
