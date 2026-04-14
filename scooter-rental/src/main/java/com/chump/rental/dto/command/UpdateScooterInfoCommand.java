package com.chump.rental.dto.command;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UpdateScooterInfoCommand {

    private String serialNumber;
    private Integer modelId;
}