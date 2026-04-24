package com.chump.rental.kafka.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@ToString
public class WaypointEvent {

    private int scooterId;
    private Point location;
}
