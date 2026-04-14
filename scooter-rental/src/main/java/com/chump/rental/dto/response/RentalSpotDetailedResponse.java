package com.chump.rental.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Polygon;

@Getter
@Setter
@ToString
public class RentalSpotDetailedResponse {

    private Integer id;
    private Integer parentId;
    private String name;
    private Polygon area;
    private Boolean isZone;
}
