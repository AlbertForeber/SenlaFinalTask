package com.chump.rental.dto.response;

import com.chump.rental.model.status.TripStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@ToString
public class TripDetailedResponse {

    private Integer id;
    private TripStatus status;
    private Integer scooterId;
    private Integer userId;
    private Instant startedAt;
    private Float distance;
    private Integer durationSeconds;
    private BigDecimal totalPrice;

    private List<TripPointResponse> tripPoints;
}
