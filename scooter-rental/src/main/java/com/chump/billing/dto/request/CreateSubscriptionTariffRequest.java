package com.chump.billing.dto.request;

import com.chump.common.validation.Trimmed;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateSubscriptionTariffRequest {

    @NotBlank(message = "Field 'name' must not be empty")
    @Trimmed(message = "Field 'name' must not contain trailing spaces")
    @Size(max = 256, message = "Tariff name must be less than 256")
    private String name;

    @NotNull(message = "Field 'basePrice' must not be empty")
    @Digits(integer = 5, fraction = 2, message = "5 digits total with 2 digits in the fractional part are allowed")
    private BigDecimal basePrice;

    @NotNull(message = "Field 'durationDays' must not be empty")
    @Positive(message = "Subscription duration in days must be positive value")
    private Integer durationDays;
}
