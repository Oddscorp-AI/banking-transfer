package com.example.banking.config;

import org.springframework.stereotype.Component;

import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.UserRepository;

@Component
public class AccountSecurity {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountSecurity(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public boolean isOwner(String accountNumber, String email) {
        return accountRepository.findByAccountNumber(accountNumber)
                .flatMap(account -> userRepository.findByEmail(email)
                        .map(user -> user.getCitizenId().equals(account.getCitizenId())))
                .orElse(false);
    }
}
