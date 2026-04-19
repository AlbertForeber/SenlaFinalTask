package com.chump.rental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Polygon;

@Getter
@Setter
public class CreateRentalSpotRequest {

    private Integer parentId;

    @NotBlank(message = "Field 'name' must not be empty")
    @Size(max = 100, message = "Name must be less than 100 characters long")
    private String name;

    @NotNull(message = "Field 'area' must not be empty")
    private Polygon area;

    @NotNull(message = "Field 'isParking' must not be empty")
    private Boolean isParking;
}
