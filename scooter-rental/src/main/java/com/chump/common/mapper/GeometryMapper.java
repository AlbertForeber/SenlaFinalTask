package com.chump.common.mapper;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class GeometryMapper {

    @Named("wktToPoint")
    public Point wktToPoint(String wkt) {
        try {
            return (Point) new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid WKT point", e);
        }
    }

    @Named("wktToLineString")
    public LineString wktToLineString(String wkt) {
        try {
            return (LineString) new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid WKT line string", e);
        }
    }

    @Named("wktToPolygon")
    public Polygon wktToPolygon(String wkt) {
        try {
            return (Polygon) new WKTReader().read(wkt);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid WKT polygon", e);
        }
    }
}
