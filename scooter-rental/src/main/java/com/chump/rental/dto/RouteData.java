package com.chump.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;

@Getter
@Setter
@AllArgsConstructor
public class RouteData {

    private LineString route;
    private double distance;
}
