package com.chump.rental.kafka.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@ToString
public class TelemetryEvent {

    private int scooterId;
    private int battery;
    private Point location;
}
