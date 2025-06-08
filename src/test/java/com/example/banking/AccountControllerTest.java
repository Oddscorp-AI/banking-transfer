package com.example.banking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.banking.controller.AccountController;
import com.example.banking.dto.AccountRequest;
import com.example.banking.dto.StatementRequest;
import com.example.banking.model.Account;
import com.example.banking.dto.StatementEntry;
import com.example.banking.service.AccountService;
import com.example.banking.config.AccountSecurity;
import com.example.banking.security.JwtService;

import static org.mockito.Mockito.*;

@WebMvcTest(AccountController.class)
@Import(com.example.banking.config.SecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;


    @MockBean
    private AccountSecurity accountSecurity;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private com.example.banking.repository.UserRepository userRepository;


    @Test
    void createAccount() throws Exception {
        String json = "{\"citizenId\":\"987654321\",\"thaiName\":\"Thai\",\"englishName\":\"English\",\"initialDeposit\":100.0}";

        Account account = new Account();
        account.setAccountNumber("1234567");
        when(accountService.createAccount(any(AccountRequest.class))).thenReturn(account);
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.claims().setSubject("teller@example.com");
        claims.put("role", "TELLER");
        when(jwtService.parse("token")).thenReturn(claims);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567"));
    }

    @Test
    void customerCannotCreateAccount() throws Exception {
        String json = "{\"citizenId\":\"987654321\",\"thaiName\":\"Thai\",\"englishName\":\"English\",\"initialDeposit\":100.0}";

        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.claims().setSubject("customer@example.com");
        claims.put("role", "CUSTOMER");
        when(jwtService.parse("token")).thenReturn(claims);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts")
                .header("Authorization", "Bearer token")
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
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.claims().setSubject("teller@example.com");
        claims.put("role", "TELLER");
        when(jwtService.parse("token")).thenReturn(claims);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/deposit")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.0));
    }

    @Test
    void customerCannotDeposit() throws Exception {
        String json = "{\"amount\":50.0}";

        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.claims().setSubject("customer@example.com");
        claims.put("role", "CUSTOMER");
        when(jwtService.parse("token")).thenReturn(claims);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/deposit")
                .header("Authorization", "Bearer token")
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
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.claims().setSubject("test@example.com");
        claims.put("role", "CUSTOMER");
        when(jwtService.parse("token")).thenReturn(claims);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/1234567")
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567"));
    }

    @Test
    void tellerCannotViewAccount() throws Exception {
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.claims().setSubject("teller@example.com");
        claims.put("role", "TELLER");
        when(jwtService.parse("token")).thenReturn(claims);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/1234567")
                .header("Authorization", "Bearer token"))
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
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.claims().setSubject("test@example.com");
        claims.put("role", "CUSTOMER");
        when(jwtService.parse("token")).thenReturn(claims);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/transfer")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567"));
    }

    @Test
    void tellerCannotTransfer() throws Exception {
        String json = "{\"toAccount\":\"7654321\",\"amount\":50.0,\"pin\":\"123456\"}";

        io.jsonwebtoken.Claims claims2 = io.jsonwebtoken.Jwts.claims().setSubject("teller@example.com");
        claims2.put("role", "TELLER");
        when(jwtService.parse("token")).thenReturn(claims2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/transfer")
                .header("Authorization", "Bearer token")
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
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.claims().setSubject("test@example.com");
        claims.put("role", "CUSTOMER");
        when(jwtService.parse("token")).thenReturn(claims);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/statement")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("A0"));
    }

    @Test
    void tellerCannotViewStatement() throws Exception {
        String json = "{\"month\":\"2025-05\",\"pin\":\"123456\"}";
        io.jsonwebtoken.Claims claims2 = io.jsonwebtoken.Jwts.claims().setSubject("teller@example.com");
        claims2.put("role", "TELLER");
        when(jwtService.parse("token")).thenReturn(claims2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/statement")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }
}