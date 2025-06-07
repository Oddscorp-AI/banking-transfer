package com.example.banking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.banking.controller.AccountController;
import com.example.banking.dto.AccountRequest;
import com.example.banking.dto.StatementRequest;
import com.example.banking.model.Account;
import com.example.banking.dto.StatementEntry;
import com.example.banking.service.AccountService;
import com.example.banking.config.AccountSecurity;

import static org.mockito.Mockito.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AccountSecurity accountSecurity;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createAccount() throws Exception {
        String json = "{\"citizenId\":\"987654321\",\"thaiName\":\"Thai\",\"englishName\":\"English\",\"initialDeposit\":100.0}";

        Account account = new Account();
        account.setAccountNumber("1234567");
        when(accountService.createAccount(any(AccountRequest.class))).thenReturn(account);
        when(userDetailsService.loadUserByUsername("teller@example.com"))
            .thenReturn(User.withUsername("teller@example.com")
                    .password(passwordEncoder.encode("secret"))
                    .roles("TELLER")
                    .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller@example.com", "secret"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567"));
    }

    @Test
    void customerCannotCreateAccount() throws Exception {
        String json = "{\"citizenId\":\"987654321\",\"thaiName\":\"Thai\",\"englishName\":\"English\",\"initialDeposit\":100.0}";

        when(userDetailsService.loadUserByUsername("customer@example.com"))
            .thenReturn(User.withUsername("customer@example.com")
                    .password(passwordEncoder.encode("secret"))
                    .roles("CUSTOMER")
                    .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("customer@example.com", "secret"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void depositMoney() throws Exception {
        String json = "{\"amount\":50.0}";

        Account account = new Account();
        account.setAccountNumber("1234567");
        account.setBalance(new java.math.BigDecimal("150.0"));
        when(accountService.deposit(eq("1234567"), any())).thenReturn(account);
        when(userDetailsService.loadUserByUsername("teller@example.com"))
            .thenReturn(User.withUsername("teller@example.com")
                    .password(passwordEncoder.encode("secret"))
                    .roles("TELLER")
                    .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/deposit")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller@example.com", "secret"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.0));
    }

    @Test
    void customerCannotDeposit() throws Exception {
        String json = "{\"amount\":50.0}";

        when(userDetailsService.loadUserByUsername("customer@example.com"))
            .thenReturn(User.withUsername("customer@example.com")
                    .password(passwordEncoder.encode("secret"))
                    .roles("CUSTOMER")
                    .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/deposit")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("customer@example.com", "secret"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewOwnAccount() throws Exception {
        Account account = new Account();
        account.setAccountNumber("1234567");
        account.setCitizenId("987654321");
        account.setBalance(new java.math.BigDecimal("100"));
        when(accountService.getAccountForUser("1234567", "test@example.com"))
            .thenReturn(account);
        when(accountSecurity.isOwner("1234567", "test@example.com")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("test@example.com"))
            .thenReturn(User.withUsername("test@example.com")
                    .password(passwordEncoder.encode("secret"))
                    .roles("CUSTOMER")
                    .build());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/1234567")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test@example.com", "secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567"));
    }

    @Test
    void tellerCannotViewAccount() throws Exception {
        when(userDetailsService.loadUserByUsername("teller@example.com"))
            .thenReturn(User.withUsername("teller@example.com")
                    .password(passwordEncoder.encode("secret"))
                    .roles("TELLER")
                    .build());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/1234567")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller@example.com", "secret")))
                .andExpect(status().isForbidden());
    }

    @Test
    void transferMoney() throws Exception {
        String json = "{\"toAccount\":\"7654321\",\"amount\":50.0,\"pin\":\"123456\"}";

        Account account = new Account();
        account.setAccountNumber("1234567");
        when(accountService.transfer(eq("1234567"), eq("7654321"), any(), eq("test@example.com"), eq("123456")))
            .thenReturn(account);
        when(accountSecurity.isOwner("1234567", "test@example.com")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("test@example.com"))
            .thenReturn(User.withUsername("test@example.com")
                    .password(passwordEncoder.encode("secret"))
                    .roles("CUSTOMER")
                    .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/transfer")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test@example.com", "secret"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567"));
    }

    @Test
    void tellerCannotTransfer() throws Exception {
        String json = "{\"toAccount\":\"7654321\",\"amount\":50.0,\"pin\":\"123456\"}";

        when(userDetailsService.loadUserByUsername("teller@example.com"))
            .thenReturn(User.withUsername("teller@example.com")
                    .password(passwordEncoder.encode("secret"))
                    .roles("TELLER")
                    .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/transfer")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller@example.com", "secret"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewStatement() throws Exception {
        String json = "{\"month\":\"2025-05\",\"pin\":\"123456\"}";
        com.example.banking.dto.StatementEntry entry = new com.example.banking.dto.StatementEntry(
                "15/12/2023", "10:30", "A0", "ATS", new java.math.BigDecimal("100"),
                new java.math.BigDecimal("200"), "Deposit");
        java.util.List<com.example.banking.dto.StatementEntry> list = java.util.Collections.singletonList(entry);
        when(accountService.getStatement(eq("1234567"), eq("test@example.com"), eq("123456"), any()))
            .thenReturn(list);
        when(accountSecurity.isOwner("1234567", "test@example.com")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("test@example.com"))
            .thenReturn(User.withUsername("test@example.com")
                    .password(passwordEncoder.encode("secret"))
                    .roles("CUSTOMER")
                    .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/statement")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("test@example.com", "secret"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("A0"));
    }

    @Test
    void tellerCannotViewStatement() throws Exception {
        String json = "{\"month\":\"2025-05\",\"pin\":\"123456\"}";
        when(userDetailsService.loadUserByUsername("teller@example.com"))
            .thenReturn(User.withUsername("teller@example.com")
                    .password(passwordEncoder.encode("secret"))
                    .roles("TELLER")
                    .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/statement")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("teller@example.com", "secret"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }
}