package com.chump.rental.dto.entry;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Getter
@Setter
@Builder
public class WaypointEntry {

    private int scooterId;
    private Point location;
    private Instant sendAt;
}
