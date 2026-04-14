package com.chump.rental.dto.response;

import com.chump.rental.model.status.ScooterStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@ToString
public class ScooterResponse {

    private Integer id;
    private String serialNumber;
    private ScooterModelResponse model;
    private Integer battery;
    private Point location;
    private ScooterStatus status;
}

