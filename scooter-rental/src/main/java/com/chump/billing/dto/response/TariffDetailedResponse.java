package com.chump.billing.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class TariffDetailedResponse {

    private Integer id;
    private String name;
    private BigDecimal basePrice;
    private String billingIntervalMinutes;
}
