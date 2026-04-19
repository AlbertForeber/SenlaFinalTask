package com.chump.billing.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SubscribedResponse {

    private TariffConciseResponse tariff;
    private LocalDate nextBillingDate;
    private BigDecimal instantPay;
}