package com.chump.emulator.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@Builder
public class WaypointEvent {

    private int scooterId;
    private Point location;
}
