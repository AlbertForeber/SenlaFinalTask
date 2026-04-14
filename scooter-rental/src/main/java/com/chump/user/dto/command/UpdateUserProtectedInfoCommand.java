package com.chump.user.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class UpdateUserProtectedInfoCommand {

    private BigDecimal balance;
    private BigDecimal discount;
}
