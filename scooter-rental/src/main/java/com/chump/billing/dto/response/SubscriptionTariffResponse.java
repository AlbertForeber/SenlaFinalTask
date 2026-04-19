package com.chump.billing.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SubscriptionTariffResponse {

    private Integer id;
    private String name;
    private BigDecimal basePrice;
    private Integer durationDays;
}
