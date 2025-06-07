package com.example.banking.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionType;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountAndTimestampBetweenOrderByTimestampAsc(Account account,
            LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.account = :account and t.type = :type and t.timestamp >= :start and t.timestamp < :end")
    BigDecimal sumAmountByAccountAndTypeAndTimestampBetween(Account account,
            TransactionType type, LocalDateTime start, LocalDateTime end);
}