package com.example.banking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.banking.dto.AccountRequest;
import com.example.banking.dto.StatementEntry;
import com.example.banking.mapper.AccountMapperImpl;
import com.example.banking.mapper.TransactionMapperImpl;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionChannel;
import com.example.banking.model.TransactionType;
import com.example.banking.model.User;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.repository.SettingRepository;
import com.example.banking.model.Setting;

class AccountServiceTest {
    private AccountRepository accountRepository;
    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private SettingRepository settingRepository;
    private PasswordEncoder passwordEncoder;
    private AccountService service;

    @BeforeEach
    void setup() {
        accountRepository = mock(AccountRepository.class);
        userRepository = mock(UserRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        settingRepository = mock(SettingRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(transactionRepository.sumAmountByAccountAndTypeAndTimestampBetween(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(settingRepository.findById("DAILY_TRANSFER_LIMIT")).thenReturn(Optional.of(limit(50000)));
        service = new AccountService(accountRepository, userRepository, transactionRepository,
                passwordEncoder, new AccountMapperImpl(), new TransactionMapperImpl(), settingRepository);
    }

    private Setting limit(double amount) {
        Setting s = new Setting();
        s.setName("DAILY_TRANSFER_LIMIT");
        s.setValue(BigDecimal.valueOf(amount));
        return s;
    }

    @Test
    void createAccountGeneratesNumber() {
        AccountRequest req = new AccountRequest("111", "t", "e", BigDecimal.ZERO);
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        Account acc = service.createAccount(req);
        assertEquals(7, acc.getAccountNumber().length());
    }

    @Test
    void depositLessThanOneThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deposit("123", new BigDecimal("0")));
    }

    @Test
    void depositUpdatesBalanceAndLogsTx() {
        Account acc = new Account();
        acc.setBalance(new BigDecimal("10"));
        when(accountRepository.findByAccountNumberForUpdate("123"))
                .thenReturn(Optional.of(acc));
        when(accountRepository.save(any())).thenReturn(acc);
        ArgumentCaptor<Transaction> txCap = ArgumentCaptor.forClass(Transaction.class);

        service.deposit("123", new BigDecimal("5"));
        assertEquals(new BigDecimal("15"), acc.getBalance());
        verify(transactionRepository).save(txCap.capture());
        assertEquals(TransactionType.DEPOSIT, txCap.getValue().getType());
    }

    @Test
    void getStatementFiltersByMonth() {
        Account acc = new Account();
        acc.setCitizenId("111");
        when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(acc));
        User user = new User();
        user.setCitizenId("111");
        user.setPinHash("pin");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "pin")).thenReturn(true);
        when(transactionRepository.findByAccountAndTimestampBetweenOrderByTimestampAsc(any(), any(), any()))
                .thenReturn(List.of());
        List<StatementEntry> list = service.getStatement("123", "a@b.com", "123456", YearMonth.now());
        assertTrue(list.isEmpty());
    }

    @Test
    void transferMovesFundsAndLogs() {
        Account from = new Account();
        from.setCitizenId("111");
        from.setBalance(new BigDecimal("100"));
        Account to = new Account();
        when(accountRepository.findByAccountNumberForUpdate("A1"))
                .thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumberForUpdate("A2"))
                .thenReturn(Optional.of(to));
        User usr = new User();
        usr.setEmail("user@ex.com");
        usr.setPassword("pass");
        usr.setCitizenId("111");
        usr.setThaiName("t");
        usr.setEnglishName("e");
        usr.setPinHash("pin");
        when(userRepository.findByEmail("user@ex.com"))
                .thenReturn(Optional.of(usr));
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.matches("123456", "pin")).thenReturn(true);

        service.transfer("A1", "A2", new BigDecimal("20"), "user@ex.com", "123456");

        assertEquals(new BigDecimal("80"), from.getBalance());
        assertEquals(new BigDecimal("20"), to.getBalance());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void transferInvalidPinThrows() {
        Account from = new Account();
        from.setCitizenId("111");
        from.setBalance(new BigDecimal("100"));
        Account to = new Account();
        when(accountRepository.findByAccountNumberForUpdate(any()))
                .thenReturn(Optional.of(from))
                .thenReturn(Optional.of(to));
        User user = new User();
        user.setCitizenId("111");
        user.setPinHash("pin");
        when(userRepository.findByEmail("user@ex.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "pin")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("A1", "A2", new BigDecimal("10"), "user@ex.com", "bad"));
    }

    @Test
    void transferOverDailyLimitThrows() {
        Account from = new Account();
        from.setCitizenId("111");
        from.setBalance(new BigDecimal("1000"));
        Account to = new Account();
        when(accountRepository.findByAccountNumberForUpdate(any()))
                .thenReturn(Optional.of(from))
                .thenReturn(Optional.of(to));
        User user = new User();
        user.setCitizenId("111");
        user.setPinHash("pin");
        when(userRepository.findByEmail("user@ex.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "pin")).thenReturn(true);
        when(transactionRepository.sumAmountByAccountAndTypeAndTimestampBetween(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("49999"));

        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("A1", "A2", new BigDecimal("100"), "user@ex.com", "123456"));
    }

    @Test
    void getAccountForUserValidatesOwner() {
        Account acc = new Account();
        acc.setCitizenId("111");
        when(accountRepository.findByAccountNumber("A1")).thenReturn(Optional.of(acc));
        User user = new User();
        user.setCitizenId("222");
        when(userRepository.findByEmail("user@ex.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> service.getAccountForUser("A1", "user@ex.com"));
    }
}