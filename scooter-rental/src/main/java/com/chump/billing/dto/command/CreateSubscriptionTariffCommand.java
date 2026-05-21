package com.chump.billing.dto.command;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateSubscriptionTariffCommand {

    private String name;
    private BigDecimal basePrice;
    private Integer durationDays;
}
