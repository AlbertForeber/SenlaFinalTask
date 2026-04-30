package com.chump.rental.dto.response;

import com.chump.rental.model.status.TripStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.LineString;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString
public class TripDetailedResponse {

    private Integer id;
    private TripStatus status;
    private Integer scooterId;
    private Integer userId;
    private Instant startedAt;
    private Double distance;
    private Integer durationSeconds;
    private BigDecimal totalPrice;
    private LineString route;
}
