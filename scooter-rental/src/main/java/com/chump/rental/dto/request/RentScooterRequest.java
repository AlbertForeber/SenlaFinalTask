package com.chump.rental.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentScooterRequest {

    @PositiveOrZero(message = "Tariff ID must not be a negative number")
    private int tariffId;
}