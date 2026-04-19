package com.chump.billing.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubscriptionTariffRequest {

    @PositiveOrZero(message = "Subscription duration in days must be positive value")
    private Integer durationDays;
}
