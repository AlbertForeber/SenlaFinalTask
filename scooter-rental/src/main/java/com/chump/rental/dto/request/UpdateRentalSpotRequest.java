package com.chump.rental.dto.request;

import com.chump.common.validation.Trimmed;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Polygon;

@Getter
@Setter
public class UpdateRentalSpotRequest {

    private Integer parentId;

    @Trimmed(message = "Field 'name' must not contain trailing spaces")
    @Size(max = 100, message = "Name must be less than 100 characters long")
    private String name;
    private Polygon area;
    private Boolean isParking;
}
