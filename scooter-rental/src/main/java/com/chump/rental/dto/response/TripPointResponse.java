package com.chump.rental.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Getter
@Setter
@ToString
public class TripPointResponse {

    private Integer tripId;
    private Instant createdAt;
    private Point location;
}
