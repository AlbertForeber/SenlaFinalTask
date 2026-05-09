package com.chump.rental.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
public class TripRefundResponse {

    int userId;
    BigDecimal newBalance;
    BigDecimal refunded;
}
