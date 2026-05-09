package com.chump.rental.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundTripRequest {

    @PositiveOrZero(message = "Field 'forLastSeconds' must not be negative")
    private int forLastSeconds;
}