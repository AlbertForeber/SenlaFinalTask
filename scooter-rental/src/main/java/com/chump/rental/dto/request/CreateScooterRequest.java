package com.chump.rental.dto.request;

import com.chump.common.validation.Trimmed;
import com.chump.rental.model.status.ScooterStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
public class CreateScooterRequest {

    @Trimmed(message = "Field 'serialNumber' must not contain trailing spaces")
    @NotBlank(message = "Field 'serialNumber' must not be empty")
    private String serialNumber;

    @NotNull(message = "Field 'modelId' must not be empty")
    @Positive(message = "Model ID must be positive number")
    private Integer modelId;

    @NotNull(message = "Field 'battery' must not be empty")
    @PositiveOrZero(message = "Battery must not be negative value")
    @Max(value = 100, message = "Battery must not be higher than 100")
    private Integer battery;

    @NotNull(message = "Field 'location' must not be empty")
    private Point location;

    @NotNull(message = "Field 'status' must not be empty")
    private ScooterStatus status;
}
