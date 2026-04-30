package com.chump.rental.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.Instant;

@Embeddable
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TripPointId {

    @Column(name = "trip_id")
    private Integer tripId;

    @Column(name = "created_at")
    private Instant createdAt;
}
