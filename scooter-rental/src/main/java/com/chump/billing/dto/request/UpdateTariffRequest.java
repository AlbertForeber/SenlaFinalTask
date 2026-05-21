package com.chump.billing.dto.request;

import com.chump.common.validation.Trimmed;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateTariffRequest {

    @Trimmed(message = "Field 'name' must not contain trailing spaces")
    @Size(max = 256, message = "Tariff name must be less than 256")
    private String name;

    @Digits(integer = 5, fraction = 2, message = "5 digits total with 2 digits in the fractional part are allowed")
    private BigDecimal basePrice;

    @Positive(message = "Billing interval in minutes must be positive value")
    private Integer interval;
}