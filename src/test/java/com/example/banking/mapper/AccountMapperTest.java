package com.example.banking.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.example.banking.dto.AccountRequest;

class AccountMapperTest {
    private final AccountMapper mapper = Mappers.getMapper(AccountMapper.class);

    @Test
    void mapsInitialDeposit() {
        AccountRequest request = new AccountRequest("123", "Thai", "Eng", new BigDecimal("100"));
        var account = mapper.toEntity(request);
        assertEquals(new BigDecimal("100"), account.getBalance());
        assertEquals("123", account.getCitizenId());
    }

    @Test
    void mapsZeroWhenDepositNull() {
        AccountRequest request = new AccountRequest("123", "Thai", "Eng", null);
        var account = mapper.toEntity(request);
        assertEquals(BigDecimal.ZERO, account.getBalance());
    }
}
