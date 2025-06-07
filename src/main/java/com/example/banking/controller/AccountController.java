package com.example.banking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.banking.dto.AccountRequest;
import com.example.banking.dto.DepositRequest;
import com.example.banking.model.Account;
import com.example.banking.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Create new account")
    public ResponseEntity<Account> createAccount(@RequestBody AccountRequest request) {
        Account account = accountService.createAccount(request);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountNumber}/deposit")
    @Operation(summary = "Deposit money")
    public ResponseEntity<Account> deposit(@PathVariable("accountNumber") String accountNumber,
                                           @RequestBody DepositRequest request) {
        Account account = accountService.deposit(accountNumber, request.amount());
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account information")
    public ResponseEntity<Account> getAccount(@PathVariable("accountNumber") String accountNumber, Authentication auth) {
        Account account = accountService.getAccountForUser(accountNumber, auth.getName());
        return ResponseEntity.ok(account);
    }
}