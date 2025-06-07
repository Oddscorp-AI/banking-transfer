package com.example.banking.service;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.banking.dto.AccountRequest;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private static final int ACCOUNT_NUMBER_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(AccountRequest request) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setCitizenId(request.citizenId());
        account.setThaiName(request.thaiName());
        account.setEnglishName(request.englishName());
        if (request.initialDeposit() != null) {
            account.setBalance(BigDecimal.valueOf(request.initialDeposit()));
        }
        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        String number;
        int limit = (int) Math.pow(10, ACCOUNT_NUMBER_LENGTH);
        do {
            number = String.format("%0" + ACCOUNT_NUMBER_LENGTH + "d", random.nextInt(limit));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}
