package com.example.banking.service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.example.banking.ErrorMessages;
import com.example.banking.dto.AccountRequest;
import com.example.banking.dto.StatementEntry;
import com.example.banking.mapper.AccountMapper;
import com.example.banking.mapper.TransactionMapper;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionChannel;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.repository.SettingRepository;
import com.example.banking.model.Setting;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final SettingRepository settingRepository;
    private static final int ACCOUNT_NUMBER_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();

    public AccountService(AccountRepository accountRepository,
                         UserRepository userRepository,
                         TransactionRepository transactionRepository,
                         PasswordEncoder passwordEncoder,
                         AccountMapper accountMapper,
                         TransactionMapper transactionMapper,
                         SettingRepository settingRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountMapper = accountMapper;
        this.transactionMapper = transactionMapper;
        this.settingRepository = settingRepository;
    }

    private com.example.banking.model.User loadUserAndVerifyPin(String email, String pin) {
        com.example.banking.model.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));
        if (!passwordEncoder.matches(pin, user.getPinHash())) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_PIN);
        }
        return user;
    }

    @Transactional
    public Account createAccount(AccountRequest request) {
        Account account = accountMapper.toEntity(request);
        account.setAccountNumber(generateAccountNumber());
        return accountRepository.save(account);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Account deposit(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Deposit must be at least 1 THB");
        }
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ACCOUNT_NOT_FOUND));
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setTimestamp(LocalDateTime.now());
        tx.setType(TransactionType.DEPOSIT);
        tx.setChannel(TransactionChannel.TELLER);
        tx.setAmount(amount);
        tx.setBalance(account.getBalance());
        tx.setRemark("Deposit");
        transactionRepository.save(tx);

        return account;
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ACCOUNT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Account getAccountForUser(String accountNumber, String email) {
        Account account = getAccount(accountNumber);
        String citizenId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND))
                .getCitizenId();
        if (!citizenId.equals(account.getCitizenId())) {
            throw new IllegalArgumentException(ErrorMessages.ACCESS_DENIED);
        }
        return account;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Account transfer(String fromAccountNumber, String toAccountNumber,
                            BigDecimal amount, String email, String pin) {
        if (amount.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Transfer must be at least 1 THB");
        }

        Account from = accountRepository.findByAccountNumberForUpdate(fromAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ACCOUNT_NOT_FOUND));
        Account to = accountRepository.findByAccountNumberForUpdate(toAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.ACCOUNT_NOT_FOUND));

        com.example.banking.model.User user = loadUserAndVerifyPin(email, pin);

        if (!from.getCitizenId().equals(user.getCitizenId())) {
            throw new IllegalArgumentException(ErrorMessages.ACCESS_DENIED);
        }
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        BigDecimal totalToday = transactionRepository
                .sumAmountByAccountAndTypeAndTimestampBetween(from, TransactionType.TRANSFER_OUT,
                        startOfDay, endOfDay);
        BigDecimal dailyLimit = settingRepository.findById("DAILY_TRANSFER_LIMIT")
                .map(Setting::getValue)
                .orElse(new BigDecimal("50000"));
        if (totalToday.add(amount).compareTo(dailyLimit) > 0) {
            throw new IllegalArgumentException(ErrorMessages.DAILY_LIMIT_EXCEEDED);
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        accountRepository.save(to);
        accountRepository.save(from);

        LocalDateTime now = LocalDateTime.now();

        Transaction outTx = new Transaction();
        outTx.setAccount(from);
        outTx.setTimestamp(now);
        outTx.setType(TransactionType.TRANSFER_OUT);
        outTx.setChannel(TransactionChannel.ONLINE);
        outTx.setAmount(amount);
        outTx.setBalance(from.getBalance());
        outTx.setRemark("To " + toAccountNumber);
        transactionRepository.save(outTx);

        Transaction inTx = new Transaction();
        inTx.setAccount(to);
        inTx.setTimestamp(now);
        inTx.setType(TransactionType.TRANSFER_IN);
        inTx.setChannel(TransactionChannel.ONLINE);
        inTx.setAmount(amount);
        inTx.setBalance(to.getBalance());
        inTx.setRemark("From " + fromAccountNumber);
        transactionRepository.save(inTx);

        return from;
    }

    @Transactional(readOnly = true)
    public List<StatementEntry> getStatement(String accountNumber, String email, String pin, YearMonth month) {
        Account account = getAccountForUser(accountNumber, email);
        loadUserAndVerifyPin(email, pin);
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
        List<Transaction> txs = transactionRepository
                .findByAccountAndTimestampBetweenOrderByTimestampAsc(account, start, end);
        return txs.stream().map(transactionMapper::toDto).toList();
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