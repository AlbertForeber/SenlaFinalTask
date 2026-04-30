package com.chump.rental.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "trip_points")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TripPoint {

    @EmbeddedId
    private TripPointId id;

    @Column(name = "location", nullable = false, columnDefinition = "geography(Point, 4326)")
    private Point location;
}
