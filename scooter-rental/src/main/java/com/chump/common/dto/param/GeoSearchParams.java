package com.chump.common.dto.param;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class GeoSearchParams {

    private float latitude;
    private float longitude;
    private float radius;
}
