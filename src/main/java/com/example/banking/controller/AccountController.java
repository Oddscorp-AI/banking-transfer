package com.example.banking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.banking.dto.AccountRequest;
import com.example.banking.dto.DepositRequest;
import com.example.banking.dto.TransferRequest;
import com.example.banking.dto.StatementRequest;
import com.example.banking.model.Account;
import com.example.banking.dto.StatementEntry;
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
    @PreAuthorize("hasRole('TELLER')")
    @Operation(summary = "Create new account")
    public ResponseEntity<Account> createAccount(@RequestBody AccountRequest request) {
        Account account = accountService.createAccount(request);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountNumber}/deposit")
    @PreAuthorize("hasRole('TELLER')")
    @Operation(summary = "Deposit money")
    public ResponseEntity<Account> deposit(@PathVariable("accountNumber") String accountNumber,
                                           @RequestBody DepositRequest request,
                                           Authentication auth) {
        Account account = accountService.deposit(accountNumber, request.amount());
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountNumber}/transfer")
    @PreAuthorize("hasRole('CUSTOMER') and @accountSecurity.isOwner(#accountNumber, authentication.name)")
    @Operation(summary = "Transfer money")
    public ResponseEntity<Account> transfer(@PathVariable("accountNumber") String accountNumber,
                                            @RequestBody TransferRequest request,
                                            Authentication auth) {
        Account account = accountService.transfer(accountNumber,
                request.toAccount(), request.amount(), auth.getName(), request.pin());
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountNumber}/statement")
    @PreAuthorize("hasRole('CUSTOMER') and @accountSecurity.isOwner(#accountNumber, authentication.name)")
    @Operation(summary = "Get bank statement for month")
    public ResponseEntity<List<StatementEntry>> statement(@PathVariable("accountNumber") String accountNumber,
                                                          @RequestBody StatementRequest request,
                                                          Authentication auth) {
        YearMonth month = YearMonth.parse(request.month());
        List<StatementEntry> txs = accountService.getStatement(accountNumber, auth.getName(), request.pin(), month);
        return ResponseEntity.ok(txs);
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasRole('CUSTOMER') and @accountSecurity.isOwner(#accountNumber, authentication.name)")
    @Operation(summary = "Get account information")
    public ResponseEntity<Account> getAccount(@PathVariable("accountNumber") String accountNumber, Authentication auth) {
        Account account = accountService.getAccountForUser(accountNumber, auth.getName());
        return ResponseEntity.ok(account);
    }
}
