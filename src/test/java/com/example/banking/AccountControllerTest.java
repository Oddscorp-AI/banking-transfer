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
import com.example.banking.dto.DepositRequest;
import com.example.banking.model.Account;
import com.example.banking.service.AccountService;

import static org.mockito.Mockito.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    void createAccount() throws Exception {
        String json = "{\"citizenId\":\"987654321\",\"thaiName\":\"Thai\",\"englishName\":\"English\",\"initialDeposit\":100.0}";

        Account account = new Account();
        account.setAccountNumber("1234567");
        when(accountService.createAccount(any(AccountRequest.class))).thenReturn(account);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567"));
    }

    @Test
    void depositMoney() throws Exception {
        String json = "{\"amount\":50.0}";

        Account account = new Account();
        account.setAccountNumber("1234567");
        account.setBalance(new java.math.BigDecimal("150.0"));
        when(accountService.deposit(eq("1234567"), any())).thenReturn(account);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/1234567/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.0));
    }
}
