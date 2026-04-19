package com.chump.common.dto.param;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeoSearchParams {

    private float latitude;
    private float longitude;
    private float radius;
}
