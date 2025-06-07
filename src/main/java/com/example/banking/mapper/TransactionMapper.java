package com.example.banking.mapper;

import java.time.format.DateTimeFormatter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.banking.dto.StatementEntry;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionChannel;
import com.example.banking.model.TransactionType;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d/M/yyyy");
    DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Mapping(target = "date", expression = "java(tx.getTimestamp().toLocalDate().format(DATE_FMT))")
    @Mapping(target = "time", expression = "java(tx.getTimestamp().toLocalTime().format(TIME_FMT))")
    @Mapping(target = "code", expression = "java(mapCode(tx.getType()))")
    @Mapping(target = "channel", expression = "java(mapChannel(tx.getChannel()))")
    @Mapping(target = "debitCredit", expression = "java(tx.getType() == TransactionType.TRANSFER_OUT ? tx.getAmount().negate() : tx.getAmount())")
    StatementEntry toDto(Transaction tx);

    default String mapCode(TransactionType type) {
        return switch (type) {
            case DEPOSIT -> "A0";
            case TRANSFER_OUT -> "A1";
            case TRANSFER_IN -> "A3";
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    default String mapChannel(TransactionChannel channel) {
        return switch (channel) {
            case TELLER -> "OTC";
            case ONLINE -> "ATS";
            default -> throw new IllegalStateException("Unexpected value: " + channel);
        };
    }
}
