package com.chump.rental.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentScooterRequest {

    @NotNull(message = "Field 'tariffId' must not be empty")
    @Positive(message = "Tariff ID must not be a positive number")
    private Integer tariffId;
}