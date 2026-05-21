package com.chump.billing.dto.request;

import com.chump.common.validation.Trimmed;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateTariffRequest {

    @Trimmed(message = "Field 'name' must not contain trailing spaces")
    @NotBlank(message = "Field 'name' must not be empty")
    @Size(max = 256, message = "Tariff name must be less than 256")
    private String name;

    @NotNull(message = "Field 'basePrice' must not be empty")
    @Digits(integer = 5, fraction = 2, message = "5 digits total with 2 digits in the fractional part are allowed")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;

    @NotNull(message = "Field 'interval' must not be empty")
    @Positive(message = "Billing interval in minutes must be positive value")
    private Integer interval;
}
