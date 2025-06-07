package com.example.banking.service;

import java.math.BigDecimal;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.banking.dto.AccountRequest;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final Random random = new Random();

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(AccountRequest request) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setCitizenId(request.getCitizenId());
        account.setThaiName(request.getThaiName());
        account.setEnglishName(request.getEnglishName());
        if (request.getInitialDeposit() != null) {
            account.setBalance(BigDecimal.valueOf(request.getInitialDeposit()));
        }
        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.format("%07d", random.nextInt(10_000_000));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}
