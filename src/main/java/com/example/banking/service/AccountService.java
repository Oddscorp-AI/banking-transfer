package com.example.banking.service;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.banking.dto.AccountRequest;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.UserRepository;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private static final int ACCOUNT_NUMBER_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Account createAccount(AccountRequest request) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setCitizenId(request.citizenId());
        account.setThaiName(request.thaiName());
        account.setEnglishName(request.englishName());
        if (request.initialDeposit() != null) {
            account.setBalance(request.initialDeposit());
        }
        return accountRepository.save(account);
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Deposit must be at least 1 THB");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    @Transactional(readOnly = true)
    public Account getAccountForUser(String accountNumber, String email) {
        Account account = getAccount(accountNumber);
        String citizenId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getCitizenId();
        if (!citizenId.equals(account.getCitizenId())) {
            throw new IllegalArgumentException("Access denied");
        }
        return account;
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
