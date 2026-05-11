package com.chump.billing.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubscriptionTariffRequest {

    @Positive(message = "Subscription duration in days must be positive value")
    private Integer durationDays;
}
