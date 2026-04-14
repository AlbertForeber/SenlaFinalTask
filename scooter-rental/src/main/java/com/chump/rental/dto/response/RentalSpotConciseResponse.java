package com.chump.rental.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Polygon;

@Getter
@Setter
@ToString
public class RentalSpotConciseResponse {

    private Integer id;
    private String name;
    private Polygon area;
}
