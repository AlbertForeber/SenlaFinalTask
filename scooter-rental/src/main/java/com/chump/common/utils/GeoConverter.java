package com.chump.common.utils;

import com.chump.rental.dto.entry.WaypointEntry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.Instant;

public class GeoConverter {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public static String waypointToString(WaypointEntry point) {
        return String.format("%f,%f,%d",
                point.getLocation().getX(),
                point.getLocation().getY(),
                point.getSendAt().toEpochMilli());
    }

    public static WaypointEntry stringToWaypoint(int scooterId, String string) {
        String[] info = string.split(",");
        double longitude = Double.parseDouble(info[0]);
        double latitude = Double.parseDouble(info[1]);
        long sendAt = Long.parseLong(info[2]);

        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        Instant instant = Instant.ofEpochMilli(sendAt);

        return WaypointEntry.builder()
                .scooterId(scooterId)
                .location(point)
                .sendAt(instant)
                .build();
    }
}
