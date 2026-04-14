package com.chump.common.dto.param;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeoSearchParams {

    private float latitude;
    private float longitude;
    private float radius;
}
