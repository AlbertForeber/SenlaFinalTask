package com.chump.rental.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

@Getter
@Setter
@ToString
public class RentalSpotHierarchyResponse {

    private Integer id;
    private List<RentalSpotHierarchyResponse> children;
    private String name;
    private Polygon area;
}
