package com.chump.billing.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CurrentSubscriptionResponse {

    private SubscriptionTariffResponse tariff;
    private LocalDate nextBillingDate;
}