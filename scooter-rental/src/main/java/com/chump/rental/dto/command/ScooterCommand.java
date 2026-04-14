package com.chump.rental.dto.command;

import com.chump.rental.model.status.ScooterStatus;
import lombok.Builder;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

@Builder
@Getter
public class ScooterCommand {

    private String serialNumber;
    private Integer modelId;
    private Integer battery;
    private Point location;
    private ScooterStatus status;
}