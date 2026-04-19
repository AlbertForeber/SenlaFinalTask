package com.chump.rental.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateScooterInfoRequest {

    private String serialNumber;

    @Positive(message = "Model ID must be positive number")
    private Integer modelId;
}
