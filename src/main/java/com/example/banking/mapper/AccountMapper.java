package com.example.banking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.banking.dto.AccountRequest;
import com.example.banking.model.Account;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = java.math.BigDecimal.class)
public interface AccountMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "balance", expression = "java(request.initialDeposit() != null ? request.initialDeposit() : BigDecimal.ZERO)")
    Account toEntity(AccountRequest request);
}
