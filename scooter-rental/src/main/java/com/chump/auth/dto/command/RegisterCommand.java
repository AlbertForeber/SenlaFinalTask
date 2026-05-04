package com.chump.auth.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
public class RegisterCommand {

    private String username;
    private String password;
    private LocalDate dateOfBirth;
    private String fullName;
    private BigDecimal balance;
    private String deviceName;
    private String ipAddress;
}