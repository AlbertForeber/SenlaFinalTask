package com.chump.emulator.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@Builder
public class TelemetryEvent {

    private int scooterId;
    private int battery;
    private Point location;
}
