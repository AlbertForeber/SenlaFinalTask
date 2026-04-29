package com.chump.rental.dto.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@AllArgsConstructor
public class WaypointEntry {

    private Point location;
}
