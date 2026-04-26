package com.chump.rental.dto.entry;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TelemetryEntry {

    private int scooterId;
    private int battery;
    private double latitude;
    private double longitude;
}
