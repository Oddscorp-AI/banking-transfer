package com.example.banking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.banking.model.Account;
import com.example.banking.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountAndTimestampBetweenOrderByTimestampAsc(Account account,
            LocalDateTime start, LocalDateTime end);
}
