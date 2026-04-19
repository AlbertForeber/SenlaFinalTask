package com.chump.billing.dto.command;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString // TODO убрать
public class CreateSubscriptionTariffCommand {

    private String name;
    private BigDecimal basePrice;
    private Integer durationDays;
}
