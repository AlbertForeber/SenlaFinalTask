package com.chump.rental.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class RentalSpotWithScootersResponse {

    private Integer id;
    private String name;
    private Integer parentId;
    private List<ScooterResponse> scootersInSpot;
}
