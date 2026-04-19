package com.chump.rental.dto.command;

import lombok.Builder;
import lombok.Getter;
import org.locationtech.jts.geom.Polygon;

@Builder
@Getter
public class RentalSpotCommand {

    private Integer parentId;
    private String name;
    private Polygon area;
    private Boolean isParking;
}