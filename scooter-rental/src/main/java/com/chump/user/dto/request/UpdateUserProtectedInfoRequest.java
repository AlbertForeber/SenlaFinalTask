package com.chump.user.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateUserProtectedInfoRequest {

    @Digits(integer = 8, fraction = 2, message = "8 digits total with 2 digits in the fractional part are allowed")
    @PositiveOrZero(message = "Balance must not be negative value")
    private BigDecimal balance;

    @Digits(integer = 1, fraction = 2, message = "3 digits total with 2 digits in the fractional part are allowed")
    @DecimalMax(value = "1", message = "Discount must not be higher than 1 (100%)")
    @PositiveOrZero(message = "Discount must not be negative value")
    private BigDecimal discount;
}
