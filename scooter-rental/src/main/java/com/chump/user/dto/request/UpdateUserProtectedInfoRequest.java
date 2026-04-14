package com.chump.user.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateUserProtectedInfoRequest {

    @DecimalMin(value = "BigDecimal.ZERO", message = "Balance must not be negative value")
    private BigDecimal balance;

    @DecimalMin(value = "BigDecimal.ZERO", message = "Discount must not be negative value")
    private BigDecimal discount;
}
